/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.mgt.store.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.mgt.bean.Attribute;
import org.wso2.carbon.identity.mgt.bean.Domain;
import org.wso2.carbon.identity.mgt.bean.Group;
import org.wso2.carbon.identity.mgt.bean.User;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.claim.MetaClaim;
import org.wso2.carbon.identity.mgt.claim.MetaClaimMapping;
import org.wso2.carbon.identity.mgt.context.AuthenticationContext;
import org.wso2.carbon.identity.mgt.domain.DomainManager;
import org.wso2.carbon.identity.mgt.exception.AuthenticationFailure;
import org.wso2.carbon.identity.mgt.exception.CredentialStoreException;
import org.wso2.carbon.identity.mgt.exception.DomainException;
import org.wso2.carbon.identity.mgt.exception.GroupNotFoundException;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreClientException;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreConnectorException;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreServerException;
import org.wso2.carbon.identity.mgt.exception.UniqueIdResolverException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtDataHolder;
import org.wso2.carbon.identity.mgt.model.GroupModel;
import org.wso2.carbon.identity.mgt.model.UserModel;
import org.wso2.carbon.identity.mgt.store.IdentityStore;
import org.wso2.carbon.identity.mgt.store.connector.CredentialStoreConnector;
import org.wso2.carbon.identity.mgt.user.ConnectedGroup;
import org.wso2.carbon.identity.mgt.user.UniqueUser;
import org.wso2.carbon.identity.mgt.user.UserPartition;
import org.wso2.carbon.identity.mgt.util.IdentityUserMgtUtil;
import org.wso2.carbon.kernel.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.stream.Collectors;
import javax.security.auth.callback.Callback;

/**
 * Represents a virtual identity store to abstract the underlying stores.
 *
 * @since 1.0.0
 */
public class IdentityStoreImpl implements IdentityStore {

    private static final Logger log = LoggerFactory.getLogger(IdentityStoreImpl.class);

    private DomainManager domainManager;

    @Override
    public void init(DomainManager domainManager) throws IdentityStoreException {

        this.domainManager = domainManager;

        if (log.isDebugEnabled()) {
            log.debug("Identity store successfully initialized.");
        }
    }

    @Override
    public User getUser(Claim claim) throws IdentityStoreException, UserNotFoundException {

        //TODO Claim wrapper will handle the validation
//        if (claim == null || StringUtils.isNullOrEmpty(claim.getValue())) {
//            throw new IdentityStoreClientException("Invalid claim.");
//        }

        Domain domain;
        try {
            domain = domainManager.getPrimaryDomain();
        } catch (DomainException e) {
            throw new IdentityStoreServerException("Error while retrieving the primary domain.", e);
        }

        return doGetUser(claim, domain);
    }

    @Override
    public User getUser(Claim claim, String domainName) throws IdentityStoreException, UserNotFoundException {

        if (StringUtils.isNullOrEmpty(domainName)) {
            return getUser(claim);
        }

        //TODO Claim wrapper will handle the validation
//        if (claim == null || StringUtils.isNullOrEmpty(claim.getValue())) {
//            throw new IdentityStoreClientException("Invalid claim.");
//        }

        Domain domain;
        try {
            domain = domainManager.getDomainFromDomainName(domainName);
        } catch (DomainException e) {
            throw new IdentityStoreServerException(String.format("Error while retrieving domain from the domain name " +
                    "- %s", domainName), e);
        }

        return doGetUser(claim, domain);
    }

    @Override
    public User addUser(UserModel userModel) throws IdentityStoreException {

        if (userModel == null || userModel.getClaims().isEmpty()) {
            throw new IdentityStoreClientException("Invalid user or claim list is empty.");
        }

        Domain domain;
        try {
            domain = domainManager.getPrimaryDomain();
        } catch (DomainException e) {
            throw new IdentityStoreServerException("Error while retrieving the primary domain.", e);
        }

        return doAddUser(userModel, domain);
    }

    @Override
    public User addUser(UserModel userModel, String domainName) throws IdentityStoreException {

        if (userModel == null || userModel.getClaims() == null || userModel.getClaims().isEmpty()) {
            throw new IdentityStoreClientException("Invalid user or claim list is empty.");
        }

        if (StringUtils.isNullOrEmpty(domainName)) {
            return addUser(userModel);
        }

        Domain domain;
        try {
            domain = domainManager.getDomainFromDomainName(domainName);
        } catch (DomainException e) {
            throw new IdentityStoreServerException(String.format("Error while retrieving domain from the domain name " +
                    "- %s", domainName), e);
        }

        return doAddUser(userModel, domain);
    }

    @Override
    public AuthenticationContext authenticate(Claim claim, Callback credential, String domainName)
            throws AuthenticationFailure {

        if (claim == null || StringUtils.isNullOrEmpty(claim.getValue()) || credential == null) {
            throw new AuthenticationFailure("Invalid credentials.");
        }

        //TODO claim wrapper will handle the dialect conversion
//        try {
//            claim = claimManager.convertToDefaultClaimDialect(claim);
//        } catch (ClaimManagerException e) {
//            log.error("Failed to convert to the default claim dialect.", e);
//            throw new AuthenticationFailure("Provided claim is invalid.");
//        }

        if (!StringUtils.isNullOrEmpty(domainName)) {

            Domain domain;
            try {
                domain = domainManager.getDomainFromDomainName(domainName);
            } catch (DomainException e) {
                log.error(String.format("Error while retrieving domain from the domain name - %s", domainName), e);
                throw new AuthenticationFailure("Domain name is invalid.");
            }
            return doAuthenticate(claim, credential, domain);
        }

        SortedSet<Domain> domains;
        try {
            domains = domainManager.getSortedDomains();
        } catch (DomainException e) {
            log.error("Failed to retrieve the domains.", e);
            throw new AuthenticationFailure("Server error occurred.");
        }

        AuthenticationContext context = null;
        for (Domain domain : domains) {
            if (domain.isClaimSupported(claim.getClaimURI())) {
                try {
                    context = doAuthenticate(claim, credential, domain);
                    break;
                } catch (AuthenticationFailure e) {

                }
            }
        }

        if (context == null) {
            throw new AuthenticationFailure("Invalid credentials.");
        }
        return context;
    }

    private User doGetUser(Claim claim, Domain domain) throws IdentityStoreException {

        //TODO claim wrapper will handle the dialect conversion
//        try {
//            claim = claimManager.convertToDefaultClaimDialect(claim);
//        } catch (ClaimManagerException e) {
//            throw new IdentityStoreServerException("Failed to convert to the default claim dialect.", e);
//        }

        MetaClaimMapping metaClaimMapping;
        try {
            metaClaimMapping = domain.getMetaClaimMapping(claim.getClaimURI());
        } catch (DomainException e) {
            throw new IdentityStoreServerException("Failed to retrieve the meta claim mapping for the claim URI.");
        }

        if (!metaClaimMapping.isUnique()) {
            throw new IdentityStoreClientException("Provided claim is not unique.");
        }

        String connectorUserId;
        try {
            connectorUserId = domain.getIdentityStoreConnectorFromId(metaClaimMapping.getIdentityStoreConnectorId())
                    .getConnectorUserId(metaClaimMapping.getAttributeName(), claim.getValue());
        } catch (UserNotFoundException e) {
            throw new IdentityStoreServerException("Invalid claim value. No user mapped to the provided claim.", e);
        }

        UniqueUser uniqueUser;
        try {
            uniqueUser = domain.getUniqueIdResolver().getUniqueUser(connectorUserId, metaClaimMapping
                    .getIdentityStoreConnectorId());
        } catch (UniqueIdResolverException e) {
            throw new IdentityStoreServerException("Failed retrieve user unique id.", e);
        }

        //TODO - handle if uuid is null - connecting existing stores

        return new User.UserBuilder()
                .setUserId(uniqueUser.getUniqueUserId())
                .setDomain(domain)
                .setIdentityStore(this)
                .setAuthorizationStore(IdentityMgtDataHolder.getInstance().getAuthorizationStore())
                .build();
    }

    private User doAddUser(UserModel userModel, Domain domain) throws IdentityStoreException {

        //TODO claim wrapper will handle the dialect conversion
//        try {
//            userModel.setClaims(claimManager.convertToDefaultClaimDialect(userModel.getClaims()));
//        } catch (ClaimManagerException e) {
//            throw new IdentityStoreServerException("Failed to convert to the default claim dialect.", e);
//        }

        List<MetaClaimMapping> metaClaimMappings;
        try {
            metaClaimMappings = domain.getMetaClaimMappings();
        } catch (DomainException e) {
            throw new IdentityStoreServerException("Failed to retrieve meta claim mappings.");
        }

        Map<String, List<Attribute>> connectorIdToAttributesMap = getConnectorIdToAttributesMap(userModel.getClaims(),
                metaClaimMappings);

        List<UserPartition> userPartitions = new ArrayList<>();
        for (Map.Entry<String, List<Attribute>> entry : connectorIdToAttributesMap.entrySet()) {

            String connectorUserId;
            try {
                connectorUserId = domain.getIdentityStoreConnectorFromId(entry.getKey()).addUser(entry.getValue());
            } catch (IdentityStoreConnectorException e) {
                // Recover from the inconsistent state in the connectors
                if (connectorIdToAttributesMap.size() > 0) {
                    removeAddedUsersInAFailure(domain, userPartitions);
                }
                throw new IdentityStoreServerException("Identity store connector failed to add user attributes.", e);
            }

            userPartitions.add(new UserPartition(entry.getKey(), connectorUserId, true));
        }

        if (!userModel.getCredentials().isEmpty()) {
            Map<String, List<Callback>> connectorIdToCredentialsMap = getConnectorIdToCredentialsMap(userModel
                    .getCredentials(), domain.getCredentialStoreConnectors());

            for (Map.Entry<String, List<Callback>> entry : connectorIdToCredentialsMap.entrySet()) {

                String connectorUserId;
                try {
                    connectorUserId = domain.getCredentialStoreConnectorFromId(entry.getKey()).addCredential(
                            entry.getValue().toArray(new Callback[entry.getValue().size()]));
                } catch (CredentialStoreException e) {
                    // Recover from the inconsistent state in the connectors
                    if (connectorIdToAttributesMap.size() > 0) {
                        removeAddedUsersInAFailure(domain, userPartitions);
                    }
                    throw new IdentityStoreServerException("Credential store connector failed to add user attributes.",
                            e);
                }

                userPartitions.add(new UserPartition(entry.getKey(), connectorUserId, false));
            }
        }

        String userUniqueId = IdentityUserMgtUtil.generateUUID();
        try {
            domain.getUniqueIdResolver().addUser(new UniqueUser(userUniqueId, userPartitions), domain.getDomainName());
        } catch (UniqueIdResolverException e) {
            // Recover from the inconsistent state in the connectors
            removeAddedUsersInAFailure(domain, userPartitions);

            throw new IdentityStoreServerException("Error occurred while persisting user unique id.", e);
        }

        return new User.UserBuilder()
                .setUserId(userUniqueId)
                .setDomain(domain)
                .setIdentityStore(this)
                .setAuthorizationStore(IdentityMgtDataHolder.getInstance().getAuthorizationStore())
                .build();
    }

    private AuthenticationContext doAuthenticate(Claim claim, Callback credential, Domain domain)
            throws AuthenticationFailure {

        MetaClaimMapping metaClaimMapping;
        try {
            metaClaimMapping = domain.getMetaClaimMapping(claim.getClaimURI());
        } catch (DomainException e) {
            throw new AuthenticationFailure("Failed to retrieve the meta claim mapping for the claim URI.", e);
        }

        if (!metaClaimMapping.isUnique()) {
            throw new AuthenticationFailure("Provided claim is not unique.");
        }

        String connectorUserId;
        try {
            connectorUserId = domain.getIdentityStoreConnectorFromId(metaClaimMapping.getIdentityStoreConnectorId())
                    .getConnectorUserId(metaClaimMapping.getAttributeName(), claim.getValue());
        } catch (UserNotFoundException | IdentityStoreException e) {
            throw new AuthenticationFailure("Invalid claim value. No user mapped to the provided claim.", e);
        }

        UniqueUser uniqueUser;
        try {
            uniqueUser = domain.getUniqueIdResolver().getUniqueUser(connectorUserId, metaClaimMapping
                    .getIdentityStoreConnectorId());
        } catch (UniqueIdResolverException e) {
            throw new AuthenticationFailure("Failed retrieve unique user info.", e);
        }

        for (UserPartition userPartition : uniqueUser.getUserPartitions()) {
            if (!userPartition.isIdentityStore()) {
                CredentialStoreConnector connector = domain.getCredentialStoreConnectorFromId(userPartition
                        .getConnectorId());
                if (connector.canHandle(new Callback[]{credential})) {
                    try {
                        connector.authenticate(new Callback[]{credential});

                        return new AuthenticationContext(new User.UserBuilder()
                                .setUserId(uniqueUser.getUniqueUserId())
                                .setIdentityStore(this)
                                .setAuthorizationStore(IdentityMgtDataHolder.getInstance().getCarbonRealmService()
                                        .getAuthorizationStore())
                                .setDomain(domain)
                                .build());
                    } catch (CredentialStoreException e) {
                        throw new AuthenticationFailure("Failed to authenticate from the provided credential.", e);
                    }
                }
            }
        }

        throw new AuthenticationFailure("Failed to authenticate user.");
    }

    @Override
    public Group getGroup(Claim claim, String domainName) throws IdentityStoreException, GroupNotFoundException {

        if (claim == null || StringUtils.isNullOrEmpty(claim.getDialectURI()) || StringUtils.isNullOrEmpty(claim
                .getClaimURI()) || StringUtils.isNullOrEmpty(claim.getValue())) {
            throw new IdentityStoreClientException("Invalid claim.");
        }

        Domain domain;
        if (StringUtils.isNullOrEmpty(domainName)) {
            try {
                domain = domainManager.getPrimaryDomain();
            } catch (DomainException e) {
                throw new IdentityStoreServerException("Failed to retrieve the primary domain.");
            }
        } else {
            try {
                domain = domainManager.getDomainFromDomainName(domainName);
            } catch (DomainException e) {
                throw new IdentityStoreServerException(String.format("Failed to retrieve the domain - %s", domainName));
            }
        }

        Map<String, List<MetaClaimMapping>> metaClaimMappings = domain.getConnectorIdToMetaClaimMappings();
        if (metaClaimMappings.isEmpty()) {
            throw new IdentityStoreServerException("Invalid domain configuration found. No meta claim mappings.");
        }

        String connectorId = null;
        String attributeName = null;
        outer:
        for (Map.Entry<String, List<MetaClaimMapping>> entry : metaClaimMappings.entrySet()) {

            if (entry.getValue() != null) {
                for (MetaClaimMapping metaClaimMapping : entry.getValue()) {
                    if (claim.getClaimURI().equals(metaClaimMapping.getMetaClaim().getClaimURI())
                            && claim.getDialectURI().equals(metaClaimMapping.getMetaClaim().getDialectURI())) {
                        connectorId = entry.getKey();
                        attributeName = metaClaimMapping.getAttributeName();
                        break outer;
                    }
                }
            }
        }

        if (StringUtils.isNullOrEmpty(connectorId) || StringUtils.isNullOrEmpty(attributeName)) {
            throw new IdentityStoreClientException(String.format("Invalid claim. Claim URI - %s, Claim Dialect - %s",
                    claim.getClaimURI(), claim.getDialectURI()));
        }

        //TODO
//        List<String> connectorUniqueAttributes = domain.getIdentityStoreConnectorFromId(connectorId)
//                .getIdentityStoreConfig().getUniqueAttributes();
//
//        if (connectorUniqueAttributes == null) {
//            throw new IdentityStoreServerException("Invalid connector configuration. No unique attributes.");
//        }
//
//        if (!connectorUniqueAttributes.contains(attributeName)) {
//            throw new IdentityStoreClientException("Provided claim is not unique.");
//        }

        String connectorGroupId;
        try {
            connectorGroupId = domain.getIdentityStoreConnectorFromId(connectorId).getConnectorGroupId(attributeName,
                    claim.getValue());
        } catch (GroupNotFoundException e) {
            throw new IdentityStoreServerException("Invalid claim value. No group mapped to the provided claim.", e);
        }

        String groupUniqueId;
        try {
            groupUniqueId = domain.getUniqueIdResolver().getUniqueGroupId(connectorGroupId, connectorId);
        } catch (UniqueIdResolverException e) {
            throw new IdentityStoreServerException("Failed retrieve group unique id.");
        }

        return new Group.GroupBuilder()
                .setGroupId(groupUniqueId)
                .setDomain(domain)
                .setIdentityStore(this)
                .setAuthorizationStore(IdentityMgtDataHolder.getInstance().getAuthorizationStore())
                .build();
    }

    public List<Group> getGroup(Claim claim) throws IdentityStoreException, GroupNotFoundException {

        if (claim == null || StringUtils.isNullOrEmpty(claim.getDialectURI()) || StringUtils.isNullOrEmpty(claim
                .getClaimURI()) || StringUtils.isNullOrEmpty(claim.getValue())) {
            throw new IdentityStoreClientException("Invalid claim.");
        }

        SortedSet<Domain> domains;
        try {
            domains = domainManager.getSortedDomains();
        } catch (DomainException e) {
            throw new IdentityStoreServerException("Failed to retrieve domains.", e);
        }

        Map<Domain, MetaClaimMapping> selectedMetaClaimMappingMap = new HashMap<>();
        for (Domain domain : domains) {
            Map<String, List<MetaClaimMapping>> metaClaimMappings = domain.getConnectorIdToMetaClaimMappings();
            if (metaClaimMappings.isEmpty()) {
                throw new IdentityStoreServerException("Invalid domain configuration found. No meta claim mappings.");
            }

            outer:
            for (Map.Entry<String, List<MetaClaimMapping>> entry : metaClaimMappings.entrySet()) {

                if (entry.getValue() != null) {
                    for (MetaClaimMapping metaClaimMapping : entry.getValue()) {
                        if (claim.getClaimURI().equals(metaClaimMapping.getMetaClaim().getClaimURI())
                                && claim.getDialectURI().equals(metaClaimMapping.getMetaClaim().getDialectURI())) {
                            selectedMetaClaimMappingMap.put(domain, metaClaimMapping);
                            break outer;
                        }
                    }
                }
            }
        }

        if (selectedMetaClaimMappingMap.isEmpty()) {
            throw new IdentityStoreClientException("Invalid claim.");
        }

        Map<Domain, String> groupUniqueIds = new HashMap<>();
        for (Map.Entry<Domain, MetaClaimMapping> entry : selectedMetaClaimMappingMap.entrySet()) {

            //TODO
//            List<String> uniqueAttributes = entry.getKey().getIdentityStoreConnectorFromId(entry.getValue()
//                    .getIdentityStoreConnectorId()).getIdentityStoreConfig().getUniqueAttributes();
//            if (uniqueAttributes == null || uniqueAttributes.isEmpty() || !uniqueAttributes.contains(entry.getValue()
//                    .getAttributeName())) {
//                continue;
//            }

            String connectorGroupId = entry.getKey().getIdentityStoreConnectorFromId(entry.getValue()
                    .getIdentityStoreConnectorId()).getConnectorGroupId(entry.getValue().getAttributeName(), claim
                    .getValue());

            String groupUniqueId;
            try {
                groupUniqueId = entry.getKey().getUniqueIdResolver().getUniqueUser(connectorGroupId, entry.getValue()
                        .getIdentityStoreConnectorId()).getUniqueUserId();
            } catch (UniqueIdResolverException e) {
                throw new IdentityStoreServerException("Failed retrieve group unique id.");
            }
            groupUniqueIds.put(entry.getKey(), groupUniqueId);
        }

        if (groupUniqueIds.isEmpty()) {
            return Collections.emptyList();
        }

        return groupUniqueIds.entrySet().stream()
                .map(entry -> new Group.GroupBuilder()
                        .setGroupId(entry.getValue())
                        .setDomain(entry.getKey())
                        .setIdentityStore(this)
                        .setAuthorizationStore(IdentityMgtDataHolder.getInstance().getCarbonRealmService()
                                .getAuthorizationStore())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<Group> listGroups(int offset, int length) throws IdentityStoreException {
        return null;
    }

    @Override
    public List<Group> listGroups(int offset, int length, String domain) throws IdentityStoreException {
        return null;
    }

    @Override
    public List<Group> listGroups(Claim claim, int offset, int length) throws IdentityStoreException {
        return null;
    }

    @Override
    public List<Group> listGroups(Claim claim, int offset, int length, String domain) throws IdentityStoreException {
        return null;
    }

    @Override
    public List<Group> listGroups(MetaClaim metaClaim, String filterPattern, int offset, int length)
            throws IdentityStoreException {
        return null;
    }

    @Override
    public List<Group> listGroups(MetaClaim metaClaim, String filterPattern, int offset, int length, String domain)
            throws IdentityStoreException {
        return null;
    }

    @Override
    public User getUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException {

//        if (StringUtils.isNullOrEmpty(uniqueUserId)) {
//            throw new IdentityStoreClientException("Invalid unique user id. Provided id is null or empty.");
//        }
//
//        SortedSet<Domain> domains;
//        try {
//            domains = domainManager.getSortedDomains();
//        } catch (DomainException e) {
//            throw new IdentityStoreServerException("Failed to retrieve domains.", e);
//        }
//
//        Map<String, String> connectorUserIdsMap;
//        try {
//            connectorUserIdsMap = domain.getConnectorUserIds(uniqueUserId);
//        } catch (UniqueIdResolverException e) {
//            throw new IdentityStoreServerException(String.format("Failed to retrieve connected user id map for the " +
//                    "unique user id - %s.", uniqueUserId), e);
//        }
//
//        if (connectorUserIdsMap.isEmpty()) {
//            throw new IdentityStoreClientException(String.format("Invalid unique group id - %s", uniqueUserId));
//        }
//
//        String connectorId = connectorUserIdsMap.keySet().iterator().next();
//        Optional<Domain> groupDomain = domains.stream()
//                .filter(domain -> domain.getIdentityStoreConnectorFromId(connectorId) != null)
//                .findAny();
//
//        if (!groupDomain.isPresent()) {
//            throw new IdentityStoreServerException(String.format("Invalid domain configuration. Failed to retrieve " +
//                    "domain for user - %s", uniqueUserId));
//        }
//
//        return new User.UserBuilder()
//                .setUserId(uniqueUserId)
//                .setDomain(groupDomain.get())
//                .setIdentityStore(realmService.getIdentityStore())
//                .setAuthorizationStore(realmService.getAuthorizationStore())
//                .build();
        return null;
    }

    @Override
    public User getUser(String uniqueUserId, String domainName) throws IdentityStoreException, UserNotFoundException {

//        if (StringUtils.isNullOrEmpty(uniqueUserId)) {
//            throw new IdentityStoreClientException("Invalid unique user id. Provided id is null or empty.");
//        }
//
//        if (StringUtils.isNullOrEmpty(domainName)) {
//            return getUser(uniqueUserId);
//        }
//
//        Domain domain;
//        try {
//            domain = domainManager.getDomainFromDomainName(domainName);
//        } catch (DomainException e) {
//            throw new IdentityStoreServerException("Failed to retrieve domain.", e);
//        }
//
//        Map<String, String> connectorUserIdsMap;
//        try {
//            connectorUserIdsMap = uniqueIdResolver.getConnectorUserIds(uniqueUserId);
//        } catch (UniqueIdResolverException e) {
//            throw new IdentityStoreServerException(String.format("Failed to retrieve connected user id map for the " +
//                    "unique user id - %s.", uniqueUserId), e);
//        }
//
//        if (connectorUserIdsMap.isEmpty()) {
//            throw new IdentityStoreClientException(String.format("Invalid unique user id - %s", uniqueUserId));
//        }
//
//        if (domain.getIdentityStoreConnectorFromId(connectorUserIdsMap.keySet().iterator().next()) == null) {
//            throw new IdentityStoreClientException("Invalid domain.");
//        }
//
//        return new User.UserBuilder()
//                .setUserId(uniqueUserId)
//                .setDomain(domain)
//                .setIdentityStore(realmService.getIdentityStore())
//                .setAuthorizationStore(realmService.getAuthorizationStore())
//                .build();
        return null;
    }


    @Override
    public List<User> listUsers(int offset, int length) throws IdentityStoreException {
        return null;
    }

    @Override
    public List<User> listUsers(int offset, int length, String domain) throws IdentityStoreException {
        return null;
    }

    @Override
    public List<User> listUsers(Claim claim, int offset, int length) throws IdentityStoreException {

//        List<User> users = new ArrayList<>();
//
//        String claimURI = claim.getClaimURI();
//        String claimValue = claim.getValue();
//
//        int currentOffset = 0;
//        int currentCount = 0;
//
//        for (Domain domain : domainManager.getSortedDomains()) {
//
//            Map<String, List<MetaClaimMapping>> metaClaimMappings = domain.getConnectorIdToMetaClaimMappings();
//
//
//            for (IdentityStoreConnector identityStoreConnector : domain.getIdentityStoreConnectors()) {
//
//                String identityStoreConnectorId = identityStoreConnector.getIdentityStoreConfig().getConnectorId();
//
//                for (MetaClaimMapping metaClaimMapping :
//                        metaClaimMappings.get(identityStoreConnector.getIdentityStoreConnectorId())) {
//
//                    // Required number of users have been retrieved
//                    if (currentCount >= length) {
//                        break;
//                    }
//
//                    if (metaClaimMapping.getMetaClaim().getClaimURI().equals(claimURI)) {
//
//                        List<User.UserBuilder> userBuilderList =
//                                identityStoreConnector.getUserBuilderList(metaClaimMapping.getAttributeName(),
//                                        claimValue, offset, length - currentCount);
//
//                        for (User.UserBuilder userBuilder : userBuilderList) {
//
//                            if (currentOffset < offset) {
//                                currentOffset++;
//                                continue;
//                                // Skip all before the offset
//                            }
//
//                            try {
//                                userBuilder.setUserId(userManager.getUniqueUser(userBuilder.getUserId(),
//                                        identityStoreConnectorId));
//
//                                userBuilder.setIdentityStore(this);
//                                userBuilder.setAuthorizationStore(realmService.getAuthorizationStore());
//                                userBuilder.setDomain(domain);
//
//                                users.add(userBuilder.build());
//
//                                currentCount++;
//                            } catch (UniqueIdResolverException e) {
//                                // not throwing since looping through all connectors
//                                throw new IdentityStoreException("Error retrieving unique user Id for user " +
//                                        userBuilder.getUserId(), e);
//                            }
//                        }
//                    }
//                }
//
//            }
//        }

        return null;
    }

    @Override
    public List<User> listUsers(Claim claim, int offset, int length, String domain) throws IdentityStoreException {
        return null;
    }

    @Override
    public List<User> listUsers(MetaClaim metaClaim, String filterPattern, int offset, int length)
            throws IdentityStoreException {
        return null;
    }

    @Override
    public List<User> listUsers(MetaClaim metaClaim, String filterPattern, int offset, int length, String domain)
            throws IdentityStoreException {
        return null;
    }

    @Override
    public Group getGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException {
//
//        if (StringUtils.isNullOrEmpty(uniqueGroupId)) {
//            throw new IdentityStoreClientException("Invalid unique group id. Provided id is null or empty.");
//        }
//
//        SortedSet<Domain> domains;
//        try {
//            domains = domainManager.getSortedDomains();
//        } catch (DomainException e) {
//            throw new IdentityStoreServerException("Failed to retrieve domains.", e);
//        }
//
//        Map<String, String> connectorGroupIdsMap;
//        try {
//            connectorGroupIdsMap = uniqueIdResolver.getConnectorGroupIds(uniqueGroupId);
//        } catch (UniqueIdResolverException e) {
//            throw new IdentityStoreServerException(String.format("Failed to retrieve connected user id map for the " +
//                    "unique gruop id - %s.", uniqueGroupId), e);
//        }
//
//        if (connectorGroupIdsMap.isEmpty()) {
//            throw new IdentityStoreClientException(String.format("Invalid unique group id - %s", uniqueGroupId));
//        }
//
//        String connectorId = connectorGroupIdsMap.keySet().iterator().next();
//        Optional<Domain> groupDomain = domains.stream()
//                .filter(domain -> domain.getIdentityStoreConnectorFromId(connectorId) != null)
//                .findAny();
//
//        if (!groupDomain.isPresent()) {
//            throw new IdentityStoreServerException(String.format("Invalid domain configuration. Failed to retrieve " +
//                    "domain for group - %s", uniqueGroupId));
//        }
//
//        return new Group.GroupBuilder()
//                .setGroupId(uniqueGroupId)
//                .setDomain(groupDomain.get())
//                .setIdentityStore(realmService.getIdentityStore())
//                .setAuthorizationStore(realmService.getAuthorizationStore())
//                .build();
        return null;
    }

    @Override
    public Group getGroup(String uniqueGroupId, String domainName) throws IdentityStoreException,
            GroupNotFoundException {

//        if (StringUtils.isNullOrEmpty(uniqueGroupId)) {
//            throw new IdentityStoreClientException("Invalid unique group id. Provided id is null or empty.");
//        }
//
//        if (StringUtils.isNullOrEmpty(domainName)) {
//            return getGroup(uniqueGroupId);
//        }
//
//        Domain domain;
//        try {
//            domain = domainManager.getDomainFromDomainName(domainName);
//        } catch (DomainException e) {
//            throw new IdentityStoreServerException("Failed to retrieve domain.", e);
//        }
//
//        Map<String, String> connectorGroupIdsMap;
//        try {
//            connectorGroupIdsMap = uniqueIdResolver.getConnectorGroupIds(uniqueGroupId);
//        } catch (UniqueIdResolverException e) {
//            throw new IdentityStoreServerException(String.format("Failed to retrieve connected group id map for the
// " +
//                    "unique group id - %s.", uniqueGroupId), e);
//        }
//
//        if (connectorGroupIdsMap.isEmpty()) {
//            throw new IdentityStoreClientException(String.format("Invalid unique group id - %s", uniqueGroupId));
//        }
//
//        if (domain.getIdentityStoreConnectorFromId(connectorGroupIdsMap.keySet().iterator().next()) == null) {
//            throw new IdentityStoreClientException("Invalid domain.");
//        }
//
//        return new Group.GroupBuilder()
//                .setGroupId(uniqueGroupId)
//                .setDomain(domain)
//                .setIdentityStore(realmService.getIdentityStore())
//                .setAuthorizationStore(realmService.getAuthorizationStore())
//                .build();
        return null;
    }

    @Override
    public List<Group> getGroupsOfUser(String userId) throws IdentityStoreException {

//        List<Group> groupList = new ArrayList<>();
//
//        try {
//            User user = getUser(userId);
//
//
//            Domain domain = user.getDomain();
//
//            for (IdentityStoreConnector identityStoreConnector : domain.getIdentityStoreConnectors()) {
//
//                String identityStoreConnectorId = identityStoreConnector.getIdentityStoreConfig().getConnectorId();
//
//                try {
//
//                    String connectorUserId =
//                            uniqueIdResolver.getConnectorUserId(userId, identityStoreConnectorId);
//
//                    for (Group.GroupBuilder groupBuilder :
//                            identityStoreConnector.getGroupBuildersOfUser(connectorUserId)) {
//
//                        Group group = groupBuilder
//                                .setDomain(domain)
//                                .setGroupId(uniqueIdResolver.getUniqueUser(groupBuilder.getGroupId(),
//                                        identityStoreConnectorId).getUniqueUserId())
//                                .build();
//
//                        groupList.add(group);
//                    }
//
//                } catch (UniqueIdResolverException e) {
//                    // not throwing since looping through all connectors
//                    throw new IdentityStoreException("Error resolving globally unique Id", e);
//                }
//            }
//
//            return groupList;
//        } catch (UserNotFoundException e) {
//            throw new IdentityStoreException("User with Id " + userId + " was not found to retrieve groups", e);
//        }
        return null;
    }

    @Override
    public List<User> getUsersOfGroup(String groupID) throws IdentityStoreException {
        // TODO: implement
        return null;
    }

    @Override
    public List<Group> getGroupsOfUser(String userId, String domain) throws IdentityStoreException {
        return null;
    }

    @Override
    public List<User> getUsersOfGroup(String groupId, String domain) throws IdentityStoreException {
        return null;
    }

    @Override
    public boolean isUserInGroup(String userId, String groupId) throws IdentityStoreException {

//        try {
//            User user = getUser(userId);
//
//            for (IdentityStoreConnector identityStoreConnector : user.getDomain().getIdentityStoreConnectors
// ()) {
//
//                String identityStoreConnectorId = identityStoreConnector.getIdentityStoreConnectorId();
//
//                try {
//
//                    String connectorUserId =
//                            userManager.getConnectorUserId(userId, identityStoreConnectorId);
//
//                    if (identityStoreConnector.isUserInGroup(connectorUserId, groupId)) {
//                        return true;
//                    }
//                } catch (UniqueIdResolverException e) {
//                    // not throwing since looping through all connectors
//                    if (log.isDebugEnabled()) {
//                        log.debug("User " + userId + " is not mapped to connector " + identityStoreConnectorId);
//                    }
//                }
//            }
//        } catch (UserNotFoundException e) {
//            throw new IdentityStoreException("User for userId " + userId + " was not found to validate groups", e);
//        }
// //TODO Discuss - where we keep group? in connector or user manager
        return false;
    }

    @Override
    public boolean isUserInGroup(String userId, String groupId, String domain) throws IdentityStoreException {
        return false;
    }

    @Override
    public List<Claim> getClaims(User user) throws IdentityStoreException {
        List<Claim> claims = new ArrayList<>();
//        Domain domain = user.getDomain();
//
//        Map<String, List<MetaClaimMapping>> claimMappings = domain.getConnectorIdToMetaClaimMappings();
//
//        for (IdentityStoreConnector identityStoreConnector : domain.getIdentityStoreConnectors()) {
//
//            String identityStoreConnectorId = identityStoreConnector.getIdentityStoreConfig().getConnectorId();
//            List<MetaClaimMapping> metaClaimMappings = claimMappings.get(identityStoreConnectorId);
//
//            // Create <AttributeName, MetaClaim> map
//            Map<String, MetaClaim> attributeMapping = metaClaimMappings.stream()
//                    .collect(Collectors.toMap(MetaClaimMapping::getAttributeName, MetaClaimMapping::getMetaClaim));
//
//            try {
//                String connectorUserId = uniqueIdResolver.getConnectorUserId(user.getUserId(),
//                        identityStoreConnectorId);
//
//                List<Attribute> attributeValues = identityStoreConnector.getUserAttributeValues(connectorUserId,
//                        new ArrayList<>(attributeMapping.keySet()));
//                claims.addAll(buildClaims(attributeValues, attributeMapping));
//            } catch (IdentityStoreException | UniqueIdResolverException e) {
//                throw new IdentityStoreException("Error retrieving claims for user : " + user.getUserId(), e);
//            }
//        }

        return claims;
    }

    @Override
    public List<Claim> getClaims(User user, List<String> claimURIs) throws IdentityStoreException {
        List<Claim> claims = new ArrayList<>();
//        Domain domain = user.getDomain();
//
//        Map<String, List<MetaClaimMapping>> claimMappings = domain.getConnectorIdToMetaClaimMappings();
//
//        for (IdentityStoreConnector identityStoreConnector : domain.getIdentityStoreConnectors()) {
//
//            String identityStoreConnectorId = identityStoreConnector.getIdentityStoreConfig().getConnectorId();
//
//            List<MetaClaimMapping> metaClaimMappings = claimMappings.get(identityStoreConnectorId);
//
//            // Create <AttributeName, MetaClaim> map
//            Map<String, MetaClaim> attributeMapping = metaClaimMappings.stream().
//                    filter(metaClaimMapping -> claimURIs.contains(metaClaimMapping.getMetaClaim().getClaimURI()))
//                    .collect(Collectors.toMap(MetaClaimMapping::getAttributeName, MetaClaimMapping::getMetaClaim));
//
//            try {
//                String connectorUserId = uniqueIdResolver.getConnectorUserId(user.getUserId(),
//                        identityStoreConnectorId);
//
//                List<Attribute> attributeValues = identityStoreConnector.getUserAttributeValues(connectorUserId,
//                        new ArrayList<>(attributeMapping.keySet()));
//                claims.addAll(buildClaims(attributeValues, attributeMapping));
//            } catch (IdentityStoreException | UniqueIdResolverException e) {
//                throw new IdentityStoreException("Error retrieving claims for user : " + user.getUserId(), e);
//            }
//        }
//
//        if (claims.size() < claimURIs.size()) {
//            log.warn("Some of the requested claims for the user " + user.getUserId() + " could not be found");
//        }

        return claims;
    }


    @Override
    public List<User> addUsers(List<UserModel> userModels) throws IdentityStoreException {

        if (userModels == null || userModels.isEmpty()) {
            throw new IdentityStoreClientException("Invalid user list. User list is empty.");
        }

        Domain domain;
        try {
            domain = domainManager.getPrimaryDomain();
        } catch (DomainException e) {
            throw new IdentityStoreServerException("Error while retrieving the primary domain.", e);
        }

        return doAddUsers(userModels, domain);
    }

    @Override
    public List<User> addUsers(List<UserModel> userModels, String domainName) throws IdentityStoreException {

        if (StringUtils.isNullOrEmpty(domainName)) {
            return addUsers(userModels);
        }

        if (userModels == null || userModels.isEmpty()) {
            throw new IdentityStoreClientException("Invalid user list. User list is null or empty.");
        }

        Domain domain;
        try {
            domain = domainManager.getDomainFromDomainName(domainName);
        } catch (DomainException e) {
            throw new IdentityStoreServerException(String.format("Error while retrieving domain from the domain name " +
                    "- %s", domainName), e);
        }

        return doAddUsers(userModels, domain);
    }


    @Override
    public void updateUserClaims(String uniqueUserId, List<Claim> userClaims) throws IdentityStoreException {

//        if (StringUtils.isNullOrEmpty(uniqueUserId)) {
//            throw new IdentityStoreClientException("Invalid user unique id.");
//        }
//
//        String domainName;
//        try {
//            domainName = uniqueIdResolver.getDomainNameFromUserUniqueId(uniqueUserId);
//        } catch (UniqueIdResolverException e) {
//            throw new IdentityStoreClientException("Invalid user UUID. Failed to retrieve domain name.");
//        }
//
//        Domain domain;
//        try {
//            domain = domainManager.getDomainFromDomainName(domainName);
//        } catch (DomainException e) {
//            throw new IdentityStoreServerException(String.format("Error while retrieving domain from the domain
// name " +
//                    "- %s", domainName), e);
//        }
//
//        doUpdateUserClaims(uniqueUserId, userClaims, domain);
    }

    @Override
    public void updateUserClaims(String uniqueUserId, List<Claim> userClaimsToUpdate, List<Claim> userClaimsToRemove)
            throws IdentityStoreException {

//        if (StringUtils.isNullOrEmpty(uniqueUserId)) {
//            throw new IdentityStoreClientException("Invalid user unique id.");
//        }
//
//        String domainName;
//        try {
//            domainName = uniqueIdResolver.getDomainNameFromUserUniqueId(uniqueUserId);
//        } catch (UniqueIdResolverException e) {
//            throw new IdentityStoreClientException("Invalid user unique id. Failed to retrieve domain name.");
//        }
//
//        if ((userClaimsToUpdate == null || userClaimsToUpdate.isEmpty()) && (userClaimsToRemove == null ||
//                userClaimsToRemove.isEmpty())) {
//            return;
//        }
//
//        Domain domain;
//        try {
//            domain = domainManager.getDomainFromDomainName(domainName);
//        } catch (DomainException e) {
//            throw new IdentityStoreServerException(String.format("Error while retrieving domain from the domain
// name " +
//                    "- %s", domainName), e);
//        }
//
//        doUpdateUserClaims(uniqueUserId, userClaimsToUpdate, userClaimsToRemove, domain);
    }

    @Override
    public void deleteUser(String uniqueUserId) throws IdentityStoreException {

//        if (StringUtils.isNullOrEmpty(uniqueUserId)) {
//            throw new IdentityStoreClientException("Invalid user unique id.");
//        }
//
//        String domainName;
//        try {
//            domainName = uniqueIdResolver.getDomainNameFromUserUniqueId(uniqueUserId);
//        } catch (UniqueIdResolverException e) {
//            throw new IdentityStoreClientException("Invalid user unique id. Failed to retrieve domain name.");
//        }
//
//        Domain domain;
//        try {
//            domain = domainManager.getDomainFromDomainName(domainName);
//        } catch (DomainException e) {
//            throw new IdentityStoreServerException(String.format("Error while retrieving domain from the domain
// name " +
//                    "- %s", domainName), e);
//        }
//
//        Map<String, String> connectorUserIds;
//        try {
//            connectorUserIds = uniqueIdResolver.getConnectorUserIds(uniqueUserId);
//        } catch (UniqueIdResolverException e) {
//            throw new IdentityStoreServerException(String.format("Failed to retrieve connector id to user connector
// " +
//                    "id map for unique user id - %s", uniqueUserId), e);
//        }
//
//        for (Map.Entry<String, String> entry : connectorUserIds.entrySet()) {
//            domain.getIdentityStoreConnectorFromId(entry.getKey()).deleteUser(entry.getValue());
//        }
//
//        try {
//            uniqueIdResolver.deleteUser(uniqueUserId);
//            //TODO audit log
//        } catch (UniqueIdResolverException e) {
//            throw new IdentityStoreServerException("Failed to delete user - " + uniqueUserId, e);
//        }
    }

    @Override
    public void updateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIds) throws IdentityStoreException {

//        if (StringUtils.isNullOrEmpty(uniqueUserId)) {
//            throw new IdentityStoreClientException("Invalid user unique id.");
//        }
//
//        try {
//            uniqueIdResolver.updateGroupsOfUser(uniqueUserId, uniqueGroupIds);
//        } catch (UniqueIdResolverException e) {
//            throw new IdentityStoreServerException("Failed to update groups of user - " + uniqueUserId, e);
//        }
    }

    @Override
    public void updateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIdsToAdd, List<String>
            uniqueGroupIdsToRemove) throws IdentityStoreException {

//        if (StringUtils.isNullOrEmpty(uniqueUserId)) {
//            throw new IdentityStoreClientException("Invalid user unique id.");
//        }
//
//        try {
//            uniqueIdResolver.updateGroupsOfUser(uniqueUserId, uniqueGroupIdsToAdd, uniqueGroupIdsToRemove);
//        } catch (UniqueIdResolverException e) {
//            throw new IdentityStoreServerException("Failed to update groups of user - " + uniqueUserId, e);
//        }
    }

    @Override
    public Group addGroup(GroupModel groupModel) throws IdentityStoreException {

        if (groupModel == null || groupModel.getGroupClaims() == null || groupModel.getGroupClaims().isEmpty()) {
            throw new IdentityStoreClientException("Invalid group or claim list is empty.");
        }

        Domain domain;
        try {
            domain = domainManager.getPrimaryDomain();
        } catch (DomainException e) {
            throw new IdentityStoreServerException("Error while retrieving primary domain.", e);
        }

        return doAddGroup(groupModel, domain);
    }

    @Override
    public Group addGroup(GroupModel groupModel, String domainName) throws IdentityStoreException {

        if (groupModel == null || groupModel.getGroupClaims() == null || groupModel.getGroupClaims().isEmpty()) {
            throw new IdentityStoreClientException("Invalid group or claim list is empty.");
        }

        if (StringUtils.isNullOrEmpty(domainName)) {
            return addGroup(groupModel);
        }

        Domain domain;
        try {
            domain = domainManager.getDomainFromDomainName(domainName);
        } catch (DomainException e) {
            throw new IdentityStoreServerException(String.format("Error while retrieving domain from the domain name " +
                    "- %s", domainName), e);
        }

        return doAddGroup(groupModel, domain);
    }

    @Override
    public List<Group> addGroups(List<GroupModel> groupModels) throws IdentityStoreException {

        if (groupModels == null || groupModels.isEmpty()) {
            throw new IdentityStoreClientException("Invalid group list. Group list is null or empty.");
        }

        Domain domain;
        try {
            domain = domainManager.getPrimaryDomain();
        } catch (DomainException e) {
            throw new IdentityStoreServerException("Error while retrieving primary domain.", e);
        }

        return doAddGroups(groupModels, domain);
    }

    @Override
    public List<Group> addGroups(List<GroupModel> groupModels, String domainName) throws IdentityStoreException {

        if (StringUtils.isNullOrEmpty(domainName)) {
            return addGroups(groupModels);
        }

        if (groupModels == null || groupModels.isEmpty()) {
            throw new IdentityStoreClientException("Invalid group list. Group list is null or empty.");
        }

        Domain domain;
        try {
            domain = domainManager.getDomainFromDomainName(domainName);
        } catch (DomainException e) {
            throw new IdentityStoreServerException(String.format("Error while retrieving domain from the domain name " +
                    "- %s", domainName), e);
        }

        return doAddGroups(groupModels, domain);
    }

    @Override
    public void updateGroupClaims(String uniqueGroupId, List<Claim> groupClaims) throws IdentityStoreException {
//
//        if (StringUtils.isNullOrEmpty(uniqueGroupId)) {
//            throw new IdentityStoreClientException("Invalid group unique id.");
//        }
//
//        String domainName;
//        try {
//            domainName = uniqueIdResolver.getDomainNameFromGroupUniqueId(uniqueGroupId);
//        } catch (UniqueIdResolverException e) {
//            throw new IdentityStoreClientException("Invalid group unique id. Failed to retrieve domain name.");
//        }
//
//        Domain domain;
//        try {
//            domain = domainManager.getDomainFromDomainName(domainName);
//        } catch (DomainException e) {
//            throw new IdentityStoreServerException(String.format("Error while retrieving domain from the domain
// name " +
//                    "- %s", domainName), e);
//        }
//
//        doUpdateGroupClaims(uniqueGroupId, groupClaims, domain);
    }

    @Override
    public void updateGroupClaims(String uniqueGroupId, List<Claim> groupClaimsToUpdate,
                                  List<Claim> groupClaimsToRemove) throws IdentityStoreException {

//        if (StringUtils.isNullOrEmpty(uniqueGroupId)) {
//            throw new IdentityStoreClientException("Invalid group unique id.");
//        }
//
//        String domainName;
//        try {
//            domainName = uniqueIdResolver.getDomainNameFromGroupUniqueId(uniqueGroupId);
//        } catch (UniqueIdResolverException e) {
//            throw new IdentityStoreClientException("Invalid group unique id. Failed to retrieve domain name.");
//        }
//
//        if ((groupClaimsToUpdate == null || groupClaimsToUpdate.isEmpty()) && (groupClaimsToRemove == null ||
//                groupClaimsToRemove.isEmpty())) {
//            return;
//        }
//
//        Domain domain;
//        try {
//            domain = domainManager.getDomainFromDomainName(domainName);
//        } catch (DomainException e) {
//            throw new IdentityStoreServerException(String.format("Error while retrieving domain from the domain
// name " +
//                    "- %s", domainName), e);
//        }
//
//        doUpdateGroupClaims(uniqueGroupId, groupClaimsToUpdate, groupClaimsToRemove, domain);
    }

    @Override
    public void deleteGroup(String uniqueGroupId) throws IdentityStoreException {

//        if (StringUtils.isNullOrEmpty(uniqueGroupId)) {
//            throw new IdentityStoreClientException("Invalid group unique id.");
//        }
//
//        String domainName;
//        try {
//            domainName = uniqueIdResolver.getDomainNameFromUserUniqueId(uniqueGroupId);
//        } catch (UniqueIdResolverException e) {
//            throw new IdentityStoreClientException("Invalid group unique id. Failed to retrieve domain name.");
//        }
//
//        Domain domain;
//        try {
//            domain = domainManager.getDomainFromDomainName(domainName);
//        } catch (DomainException e) {
//            throw new IdentityStoreServerException(String.format("Error while retrieving domain from the domain
// name " +
//                    "- %s", domainName), e);
//        }
//
//        Map<String, String> connectorGroupIds;
//        try {
//            connectorGroupIds = uniqueIdResolver.getConnectorGroupIds(uniqueGroupId);
//        } catch (UniqueIdResolverException e) {
//            throw new IdentityStoreServerException(String.format("Failed to retrieve connector id to group
// connector " +
//                    "id map for unique group id - %s", uniqueGroupId), e);
//        }
//
//        for (Map.Entry<String, String> entry : connectorGroupIds.entrySet()) {
//            domain.getIdentityStoreConnectorFromId(entry.getKey()).deleteGroup(entry.getValue());
//        }
//
//        try {
//            uniqueIdResolver.deleteGroup(uniqueGroupId);
//        } catch (UniqueIdResolverException e) {
//            throw new IdentityStoreServerException(String.format("Failed to delete group - %s", uniqueGroupId), e);
//        }
    }

    @Override
    public void updateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIds) throws IdentityStoreException {


//        if (StringUtils.isNullOrEmpty(uniqueGroupId)) {
//            throw new IdentityStoreClientException("Invalid group unique id.");
//        }
//
//        try {
//            uniqueIdResolver.updateGroupsOfUser(uniqueGroupId, uniqueUserIds);
//        } catch (UniqueIdResolverException e) {
//            throw new IdentityStoreServerException(String.format("Failed to update users of group - %s",
//                    uniqueGroupId), e);
//        }
    }

    @Override
    public void updateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIdsToAdd,
                                   List<String> uniqueUserIdsToRemove) throws IdentityStoreException {

//        if (StringUtils.isNullOrEmpty(uniqueGroupId)) {
//            throw new IdentityStoreClientException("Invalid group unique id.");
//        }
//
//        try {
//            uniqueIdResolver.updateUsersOfGroup(uniqueGroupId, uniqueUserIdsToAdd, uniqueUserIdsToRemove);
//        } catch (UniqueIdResolverException e) {
//            throw new IdentityStoreServerException("Failed to update groups of user - " + uniqueGroupId, e);
//        }
    }

    private List<User> doAddUsers(List<UserModel> userModels, Domain domain) throws IdentityStoreException {

//        for (UserModel userModel : userModels) {
//            try {
//                userModel.setClaims(claimManager.convertToDefaultClaimDialect(userModel.getClaims()));
//            } catch (ClaimManagerException e) {
//                throw new IdentityStoreServerException("Failed to convert to the default claim dialect.", e);
//            }
//        }

//        Map<String, UserModel> userUniqueIdToUserModelMap = userModels.stream().collect(Collectors.toMap
//                (userModel -> IdentityUserMgtUtil.generateUUID(), userModel -> userModel));

        List<MetaClaimMapping> metaClaimMappings;
        try {
            metaClaimMappings = domain.getMetaClaimMappings();
        } catch (DomainException e) {
            throw new IdentityStoreServerException("Failed to retrieve meta claim mappings.");
        }

        List<Map<String, List<Attribute>>> connectorIdToAttributesMaps = userModels.stream()
                .filter(Objects::nonNull)
                .map(userModel -> getConnectorIdToAttributesMap(userModel.getClaims(), metaClaimMappings))
                .collect(Collectors.toList());

        Map<String, Map<String, List<Attribute>>> connectorViseUserMap = getConnectorViseAttributesMap
                (connectorIdToAttributesMaps);

        Map<String, List<UserPartition>> connectedUsersList = new HashMap<>();

        for (Map.Entry<String, Map<String, List<Attribute>>> entry : connectorViseUserMap.entrySet()) {

            Map<String, String> uniqueIds = domain.getIdentityStoreConnectorFromId(entry.getKey())
                    .addUsers(entry.getValue());

            if (uniqueIds != null) {
                uniqueIds.entrySet().stream()
                        .forEach(t -> {
                            List<UserPartition> userPartitions = connectedUsersList.get(t.getKey());
                            if (userPartitions == null) {
                                userPartitions = new ArrayList<>();
                                connectedUsersList.put(t.getKey(), userPartitions);
                            }
                            userPartitions.add(new UserPartition(entry.getKey(), t.getValue(), true));
                        });
            }
            // TODO handle any failure
        }

        try {
            domain.getUniqueIdResolver().addUsers(connectedUsersList);
        } catch (UniqueIdResolverException e) {
            // TODO handle any failure
            throw new IdentityStoreServerException("Error occurred while persisting user unique ids.", e);
        }

        return connectedUsersList.entrySet().stream()
                .map(entry -> new User.UserBuilder()
                        .setUserId(entry.getKey())
                        .setDomain(domain)
                        .setIdentityStore(this)
                        .setAuthorizationStore(IdentityMgtDataHolder.getInstance().getCarbonRealmService()
                                .getAuthorizationStore())
                        .build())
                .collect(Collectors.toList());
    }

//    private void doUpdateUserClaims(String userUniqueId, List<Claim> userClaims, Domain domain)
//            throws IdentityStoreException {
//
//        Map<String, String> connectorUserIds;
//        try {
//            connectorUserIds = domain.getUniqueIdResolver().getConnectorUserIds(userUniqueId);
//        } catch (UniqueIdResolverException e) {
//            throw new IdentityStoreServerException(String.format("Failed to retrieve connector id to user connector
// " +
//                    "id map for the user unique id - %s ", userUniqueId), e);
//        }
//
//        Map<String, String> updatedUniqueIds = new HashMap<>();
//
//        if (userClaims == null || userClaims.isEmpty()) {
//            connectorUserIds.entrySet().stream()
//                    .forEach(rethrowConsumer(entry -> {
//                        (domain.getIdentityStoreConnectorFromId(entry.getKey()))
//                                .updateUserAttributes(entry.getValue(), new ArrayList<>());
//                    }));
//            //TODO: do we need to delete user unique id? credential store still may have references
//        } else {
//            Map<String, List<MetaClaimMapping>> metaClaimMappings = domain.getConnectorIdToMetaClaimMappings();
//            if (metaClaimMappings.isEmpty()) {
//                throw new IdentityStoreServerException("Invalid domain configuration found. No meta claim mappings.");
//            }
//
//            Map<String, List<Attribute>> connectorAttributeMap = getConnectorAttributesMap(userClaims,
//                    metaClaimMappings);
//
//            for (Map.Entry<String, String> entry : connectorUserIds.entrySet()) {
//                String uniqueId = domain.getIdentityStoreConnectorFromId(entry.getKey()).updateUserAttributes
//                        (connectorUserIds.get(entry.getKey()), connectorAttributeMap.get(entry.getKey()));
//                updatedUniqueIds.put(entry.getKey(), uniqueId);
//            }
//        }
//
//        if (!connectorUserIds.equals(updatedUniqueIds)) {
//            try {
//                domain.getUniqueIdResolver().updateUser(userUniqueId, updatedUniqueIds);
//            } catch (UniqueIdResolverException e) {
//                throw new IdentityStoreServerException("Failed to update user connector ids.", e);
//            }
//        }
//    }

//    private void doUpdateUserClaims(String userUniqueId, List<Claim> userClaimsToUpdate, List<Claim>
// userClaimsToRemove,
//                                    Domain domain) throws IdentityStoreException {
//
//        Map<String, String> connectorUserIds;
//        try {
//            connectorUserIds = domain.getUniqueIdResolver().getConnectorUserIds(userUniqueId);
//        } catch (UniqueIdResolverException e) {
//            throw new IdentityStoreServerException(String.format("Failed to retrieve connector id to user connector
// " +
//                    "id map for the user unique id - %s ", userUniqueId), e);
//        }
//
//        Map<String, List<MetaClaimMapping>> metaClaimMappings = domain.getConnectorIdToMetaClaimMappings();
//        if (metaClaimMappings.isEmpty()) {
//            throw new IdentityStoreServerException("Invalid identity store configuration found.");
//        }
//
//        Map<String, List<Attribute>> connectorAttributeMapToUpdate = getConnectorAttributesMap(userClaimsToUpdate,
//                metaClaimMappings);
//
//        Map<String, List<Attribute>> connectorAttributeMapToRemove = getConnectorAttributesMap(userClaimsToRemove,
//                metaClaimMappings);
//
//        Map<String, String> updatedConnectorUserIds = new HashMap<>();
//
//        for (Map.Entry<String, String> entry : connectorUserIds.entrySet()) {
//            String uniqueId = domain.getIdentityStoreConnectorFromId(entry.getKey())
//                    .updateUserAttributes(connectorUserIds.get(entry.getKey()), connectorAttributeMapToUpdate
//                            .get(entry.getKey()), connectorAttributeMapToRemove.get(entry.getKey()));
//            updatedConnectorUserIds.put(entry.getKey(), uniqueId);
//        }
//
//
//        if (!connectorUserIds.equals(updatedConnectorUserIds)) {
//            try {
//                domain.getUniqueIdResolver().updateUser(userUniqueId, updatedConnectorUserIds);
//            } catch (UniqueIdResolverException e) {
//                throw new IdentityStoreServerException("Failed to update user connected ids.", e);
//            }
//        }
//    }

    private Group doAddGroup(GroupModel groupModel, Domain domain) throws IdentityStoreException {

        Map<String, List<MetaClaimMapping>> metaClaimMappings = domain.getConnectorIdToMetaClaimMappings();
        if (metaClaimMappings.isEmpty()) {
            throw new IdentityStoreServerException("Invalid domain configuration found. No meta claim mappings.");
        }

        Map<String, List<Attribute>> connectorAttributeMap = getConnectorAttributesMap(groupModel.getGroupClaims(),
                metaClaimMappings);

        //TODO check group is present

        List<ConnectedGroup> connectedGroups = new ArrayList<>();
        for (Map.Entry<String, List<Attribute>> entry : connectorAttributeMap.entrySet()) {
            String uniqueId = domain.getIdentityStoreConnectorFromId(entry.getKey()).addGroup(entry.getValue());
            connectedGroups.add(new ConnectedGroup(entry.getKey(), uniqueId));
            // TODO handle any failure
        }

        String groupUniqueId = IdentityUserMgtUtil.generateUUID();
        try {
            domain.getUniqueIdResolver().addGroup(groupUniqueId, connectedGroups);
        } catch (UniqueIdResolverException e) {
            // TODO handle any failure
            throw new IdentityStoreServerException("Error occurred while persisting group unique group id.", e);
        }

        return new Group.GroupBuilder()
                .setGroupId(groupUniqueId)
                .setDomain(domain)
                .setIdentityStore(this)
                .setAuthorizationStore(IdentityMgtDataHolder.getInstance().getCarbonRealmService()
                        .getAuthorizationStore())
                .build();
    }

    private List<Group> doAddGroups(List<GroupModel> groupModels, Domain domain) throws IdentityStoreException {

        Map<String, List<MetaClaimMapping>> metaClaimMappings = domain.getConnectorIdToMetaClaimMappings();
        if (metaClaimMappings.isEmpty()) {
            throw new IdentityStoreServerException("Invalid domain configuration found. No meta claim mappings.");
        }

        List<Map<String, List<Attribute>>> connectorAttributesMaps = groupModels.stream()
                .filter(Objects::nonNull)
                .filter(userModel -> userModel.getGroupClaims() != null)
                .map(groupModel -> getConnectorAttributesMap(groupModel.getGroupClaims(), metaClaimMappings))
                .collect(Collectors.toList());

        Map<String, Map<String, List<Attribute>>> connectorViseGroupMap = getConnectorViseAttributesMap
                (connectorAttributesMaps);

        Map<String, List<ConnectedGroup>> connectedGroupsMaps = new HashMap<>();

        for (Map.Entry<String, Map<String, List<Attribute>>> entry : connectorViseGroupMap.entrySet()) {

            Map<String, String> uniqueIds = domain.getIdentityStoreConnectorFromId(entry.getKey()).
                    addGroups(entry.getValue());

            if (uniqueIds != null) {
                uniqueIds.entrySet().stream()
                        .forEach(t -> {
                            List<ConnectedGroup> connectedGroups = connectedGroupsMaps.get(t.getKey());
                            if (connectedGroups == null) {
                                connectedGroups = new ArrayList<>();
                                connectedGroupsMaps.put(t.getKey(), connectedGroups);
                            }
                            connectedGroups.add(new ConnectedGroup(entry.getKey(), t.getValue()));
                        });
            }
            // TODO handle any failure
        }

        try {
            domain.getUniqueIdResolver().addGroups(connectedGroupsMaps);
        } catch (UniqueIdResolverException e) {
            // TODO handle any failure
            throw new IdentityStoreServerException("Error occurred while persisting group unique ids.", e);
        }

        return connectedGroupsMaps.entrySet().stream()
                .map(entry -> new Group.GroupBuilder()
                        .setGroupId(entry.getKey())
                        .setDomain(domain)
                        .setIdentityStore(this)
                        .setAuthorizationStore(IdentityMgtDataHolder.getInstance().getCarbonRealmService()
                                .getAuthorizationStore())
                        .build())
                .collect(Collectors.toList());
    }

//    private void doUpdateGroupClaims(String uniqueGroupId, List<Claim> groupClaims, Domain domain)
//            throws IdentityStoreException {
//
//        Map<String, String> connectorGroupIds;
//        try {
//            connectorGroupIds = domain.getUniqueIdResolver().getConnectorGroupIds(uniqueGroupId);
//        } catch (UniqueIdResolverException e) {
//            throw new IdentityStoreServerException(String.format("Failed to retrieve connector id to group
// connector " +
//                    "id map for unique group id - %s", uniqueGroupId), e);
//        }
//
//        Map<String, String> updatedUniqueIds = new HashMap<>();
//
//        if (groupClaims == null || groupClaims.isEmpty()) {
//            connectorGroupIds.entrySet().stream()
//                    .forEach(rethrowConsumer(entry -> {
//                        domain.getIdentityStoreConnectorFromId(entry.getKey()).updateGroupAttributes(entry.getValue(),
//                                new ArrayList<>());
//                    }));
//            //TODO: do we need to delete group unique id? credential store still may have references
//        } else {
//
//            Map<String, List<MetaClaimMapping>> metaClaimMappings = domain.getConnectorIdToMetaClaimMappings();
//            if (metaClaimMappings.isEmpty()) {
//                throw new IdentityStoreServerException("Invalid domain configuration found. No meta claim mappings.");
//            }
//
//            Map<String, List<Attribute>> connectorAttributeMap = getConnectorAttributesMap(groupClaims,
//                    metaClaimMappings);
//
//            for (Map.Entry<String, String> entry : connectorGroupIds.entrySet()) {
//                String uniqueConnectorId = domain.getIdentityStoreConnectorFromId(entry.getKey())
// .updateGroupAttributes
//                        (connectorGroupIds.get(entry.getKey()), connectorAttributeMap.get(entry.getKey()));
//                updatedUniqueIds.put(entry.getKey(), uniqueConnectorId);
//            }
//        }
//
//        if (!connectorGroupIds.equals(updatedUniqueIds)) {
//            try {
//                domain.getUniqueIdResolver().updateGroup(uniqueGroupId, updatedUniqueIds);
//            } catch (UniqueIdResolverException e) {
//                throw new IdentityStoreServerException("Failed to update group connected ids.", e);
//            }
//        }
//    }

//    private void doUpdateGroupClaims(String uniqueGroupId, List<Claim> groupClaimsToUpdate,
//                                     List<Claim> groupClaimsToRemove, Domain domain) throws IdentityStoreException {
//
//        Map<String, String> connectorGroupIds;
//        try {
//            connectorGroupIds = domain.getUniqueIdResolver().getConnectorGroupIds(uniqueGroupId);
//        } catch (UniqueIdResolverException e) {
//            throw new IdentityStoreServerException("Failed to retrieve connector id to group connector id map for " +
//                    "for unique group id - %s" + uniqueGroupId, e);
//        }
//
//        Map<String, List<MetaClaimMapping>> metaClaimMappings = domain.getConnectorIdToMetaClaimMappings();
//        if (metaClaimMappings.isEmpty()) {
//            throw new IdentityStoreServerException("Invalid domain configuration found. No meta claim mappings.");
//        }
//
//        Map<String, List<Attribute>> connectorAttributeMapToUpdate = getConnectorAttributesMap(groupClaimsToUpdate,
//                metaClaimMappings);
//
//        Map<String, List<Attribute>> connectorAttributeMapToRemove = getConnectorAttributesMap(groupClaimsToRemove,
//                metaClaimMappings);
//
//        Map<String, String> updatedUniqueIds = new HashMap<>();
//
//        for (Map.Entry<String, String> entry : connectorGroupIds.entrySet()) {
//            String uniqueId = domain.getIdentityStoreConnectorFromId(entry.getKey()).updateGroupAttributes
//                    (connectorGroupIds.get(entry.getKey()), connectorAttributeMapToUpdate.get(entry.getKey()),
//                            connectorAttributeMapToRemove.get(entry.getKey()));
//            updatedUniqueIds.put(entry.getKey(), uniqueId);
//        }
//
//
//        if (!connectorGroupIds.equals(updatedUniqueIds)) {
//            try {
//                domain.getUniqueIdResolver().updateGroup(uniqueGroupId, updatedUniqueIds);
//            } catch (UniqueIdResolverException e) {
//                throw new IdentityStoreServerException("Failed to update group connected ids.", e);
//            }
//        }
//    }


    private Map<String, List<Attribute>> getConnectorIdToAttributesMap(List<Claim> claims,
                                                                       List<MetaClaimMapping> metaClaimMappings) {

        Map<String, List<Attribute>> connectorIdToAttributesMap = new HashMap<>();

        claims.stream()
                .forEach(claim -> {
                            Optional<MetaClaimMapping> optional = metaClaimMappings.stream()
                                    .filter(metaClaimMapping -> metaClaimMapping.getMetaClaim().getClaimURI()
                                            .equals(claim.getClaimURI()))
                                    .findFirst();

                            if (optional.isPresent()) {
                                MetaClaimMapping metaClaimMapping = optional.get();
                                List<Attribute> attributes = connectorIdToAttributesMap.get(metaClaimMapping
                                        .getIdentityStoreConnectorId());
                                if (attributes == null) {
                                    attributes = new ArrayList<>();
                                    connectorIdToAttributesMap.put(metaClaimMapping.getIdentityStoreConnectorId(),
                                            attributes);
                                }
                                attributes.add(new Attribute(metaClaimMapping.getAttributeName(), claim.getValue()));
                            }
                        }
                );

        return connectorIdToAttributesMap;
    }

    private Map<String, List<Callback>> getConnectorIdToCredentialsMap(
            List<Callback> credentials, List<CredentialStoreConnector> credentialStoreConnectors) {

        Map<String, List<Callback>> connectorIdToCredentialsMap = new HashMap<>();

        credentials.stream()
                .filter(Objects::nonNull)
                .forEach(callback -> {
                    Optional<CredentialStoreConnector> optional = credentialStoreConnectors.stream()
                            .filter(connector -> connector.canHandle(new Callback[]{callback}))
                            .findAny();

                    if (optional.isPresent()) {
                        CredentialStoreConnector connector = optional.get();
                        List<Callback> callbacks = connectorIdToCredentialsMap.get(connector
                                .getCredentialStoreConnectorId());
                        if (callbacks == null) {
                            callbacks = new ArrayList<>();
                            connectorIdToCredentialsMap.put(connector.getCredentialStoreConnectorId(), callbacks);
                        }
                        callbacks.add(callback);
                    }
                });

        return connectorIdToCredentialsMap;
    }

    private Map<String, List<Attribute>> getConnectorAttributesMap(List<Claim> claims, Map<String,
            List<MetaClaimMapping>> metaClaimMappings) {

        Map<String, List<Attribute>> connectorAttributeMap = new HashMap<>();
        claims.stream()
                .filter(Objects::nonNull)
                .forEach(claim -> {
                    metaClaimMappings.entrySet().stream()
                            .filter(entry -> entry.getValue() != null)
                            .forEach(entry -> {
                                Optional<MetaClaimMapping> optional = entry.getValue().stream()
                                        .filter(metaClaimMapping -> claim.getClaimURI().equals(metaClaimMapping
                                                .getMetaClaim().getClaimURI()) && claim.getDialectURI().equals
                                                (metaClaimMapping.getMetaClaim().getDialectURI()))
                                        .findFirst();
                                if (optional.isPresent()) {
                                    Attribute attribute = new Attribute();
                                    MetaClaimMapping metaClaimMapping = optional.get();
                                    attribute.setAttributeName(metaClaimMapping.getAttributeName());
                                    attribute.setAttributeValue(claim.getValue());
                                    List<Attribute> attributes = connectorAttributeMap.get(entry.getKey());
                                    if (attributes == null) {
                                        attributes = new ArrayList<>();
                                        connectorAttributeMap.put(entry.getKey(), attributes);
                                    }
                                    attributes.add(attribute);
                                }
                            });
                });
        return connectorAttributeMap;
    }

    private Map<String, Map<String, List<Attribute>>> getConnectorViseAttributesMap(
            List<Map<String, List<Attribute>>> connectorAttributesMaps) {
        Map<String, Map<String, List<Attribute>>> uuidConnectorAttributeMap = new HashMap<>();
        connectorAttributesMaps.stream()
                .forEach(map -> {
                    String uuid = IdentityUserMgtUtil.generateUUID();
                    map.entrySet().stream()
                            .forEach(entry -> {
                                Map<String, List<Attribute>> uuidAttributesMap = uuidConnectorAttributeMap.get(entry
                                        .getKey());
                                if (uuidAttributesMap == null) {
                                    uuidAttributesMap = new HashMap<>();
                                    uuidConnectorAttributeMap.put(entry.getKey(), uuidAttributesMap);
                                }
                                uuidAttributesMap.put(uuid, entry.getValue());
                            });
                });
        return uuidConnectorAttributeMap;
    }

//    private List<Claim> buildClaims(List<Attribute> attributes, Map<String, MetaClaim> attributeMapping) {
//
//        return attributes.stream().map(attribute -> {
//            MetaClaim metaClaim = attributeMapping.get(attribute.getAttributeName());
//            Claim claim = new Claim();
//            claim.setClaimURI(metaClaim.getClaimURI());
//            claim.setDialectURI(metaClaim.getDialectURI());
//            claim.setValue(attribute.getAttributeValue());
//            return claim;
//        }).collect(Collectors.toList());
//    }

    private void removeAddedUsersInAFailure(Domain domain, List<UserPartition> userPartitions) {

        for (UserPartition userPartition : userPartitions) {
            try {
                domain.getIdentityStoreConnectorFromId(userPartition.getConnectorId())
                        .removeAddedUsersInAFailure(Collections.singletonList(userPartition
                                .getConnectorUserId()));
            } catch (IdentityStoreConnectorException e) {
                log.error("Error occurred while removing invalid connector user ids. " + String.join(" , ",
                        userPartitions.stream().map(UserPartition::toString).collect(Collectors.toList())
                ), e);
            }
        }
    }
}

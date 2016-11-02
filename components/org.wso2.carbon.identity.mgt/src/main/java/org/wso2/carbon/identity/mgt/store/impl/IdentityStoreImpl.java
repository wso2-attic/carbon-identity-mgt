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
import org.wso2.carbon.identity.mgt.domain.DomainManager;
import org.wso2.carbon.identity.mgt.exception.DomainException;
import org.wso2.carbon.identity.mgt.exception.GroupNotFoundException;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreClientException;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreServerException;
import org.wso2.carbon.identity.mgt.exception.UserManagerException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.mgt.internal.CarbonSecurityDataHolder;
import org.wso2.carbon.identity.mgt.model.GroupModel;
import org.wso2.carbon.identity.mgt.model.UserModel;
import org.wso2.carbon.identity.mgt.service.RealmService;
import org.wso2.carbon.identity.mgt.store.IdentityStore;
import org.wso2.carbon.identity.mgt.store.connector.IdentityStoreConnector;
import org.wso2.carbon.identity.mgt.user.ConnectedGroup;
import org.wso2.carbon.identity.mgt.user.ConnectedUser;
import org.wso2.carbon.identity.mgt.user.UserManager;
import org.wso2.carbon.identity.mgt.util.IdentityMgtConstants;
import org.wso2.carbon.identity.mgt.util.IdentityUserMgtUtil;
import org.wso2.carbon.kernel.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.wso2.carbon.kernel.utils.LambdaExceptionUtils.rethrowConsumer;

/**
 * Represents a virtual identity store to abstract the underlying stores.
 *
 * @since 1.0.0
 */
public class IdentityStoreImpl implements IdentityStore {

    private static final Logger log = LoggerFactory.getLogger(IdentityStoreImpl.class);

    private DomainManager domainManager;

    private RealmService realmService;

    private UserManager userManager;

    @Override
    public void init(DomainManager domainManager) throws IdentityStoreException {

        this.domainManager = domainManager;
        this.realmService = CarbonSecurityDataHolder.getInstance().getCarbonRealmService();
        this.userManager = CarbonSecurityDataHolder.getInstance().getUserManager();

        if (log.isDebugEnabled()) {
            log.debug("Identity store successfully initialized.");
        }
    }


    @Override
    public Group getGroup(Claim claim, String domain) throws IdentityStoreException, GroupNotFoundException {
        return null;
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
    public User getUser(String userId) throws IdentityStoreException, UserNotFoundException {

        for (Domain domain : domainManager.getSortedDomains()) {


            for (IdentityStoreConnector identityStoreConnector : domain.getSortedIdentityStoreConnectors()) {

                String identityStoreConnectorId = identityStoreConnector.getIdentityStoreConnectorId();

                try {
                    String connectorUserId =
                            userManager.getConnectorUserId(userId, identityStoreConnectorId);

                    for (String attribute : identityStoreConnector.getIdentityStoreConfig().getUniqueAttributes()) {

                        try {
                            User.UserBuilder userBuilder =
                                    identityStoreConnector.getUserBuilder(attribute, connectorUserId);

                            return userBuilder.setUserId(userId)
                                    .setDomain(domain)
                                    .setIdentityStore(this)
                                    .setAuthorizationStore(realmService.getAuthorizationStore())
                                    .build();

                        } catch (UserNotFoundException e) { // not throwing since looping through all unique attributes
                            log.debug("A user with attribute " + attribute + " : " + connectorUserId +
                                    " was not found in connector " + identityStoreConnectorId);
                        }

                    }


                } catch (UserManagerException e) { // not throwing since looping through all connectors
                    if (log.isDebugEnabled()) {
                        log.debug("Couldn't find connect specific user Id for " + userId + " in connector " +
                                identityStoreConnectorId);
                    }
                }

            }

        }

        throw new UserNotFoundException("User with unique user Id " + userId + " was not found");

    }

    @Override
    public User getUser(String userId, String domain) throws IdentityStoreException, UserNotFoundException {
        return null;
    }

    @Override
    public User getUser(Claim claim) throws IdentityStoreException, UserNotFoundException {
        String claimValue = claim.getValue();

        String[] domainSplit = claimValue.split(IdentityMgtConstants.URL_SPLITTER, 2);


        // If domain is provided in claim value, retrieve user from the specific domain
        if (domainSplit.length == 2) {
            return getUser(claim, domainSplit[0]);
        }

        String claimURI = claim.getClaimURI();

        for (Domain domain : domainManager.getSortedDomains()) {

            if (domain.isClaimAvailable(claimURI)) {

                Map<String, List<MetaClaimMapping>> claimMappings = domain.getClaimMappings();

                for (IdentityStoreConnector identityStoreConnector : domain.getSortedIdentityStoreConnectors()) {

                    String identityStoreConnectorId = identityStoreConnector.getIdentityStoreConfig().getConnectorId();

                    List<String> uniqueAttributes =
                            identityStoreConnector.getIdentityStoreConfig().getUniqueAttributes();

                    for (MetaClaimMapping claimMapping : claimMappings.get(identityStoreConnectorId)) {

                        // Skip if the claim does not match or not a unique attribute
                        if (!claimMapping.getMetaClaim().getClaimURI().equals(claimURI) ||
                                !uniqueAttributes.contains(claimMapping.getAttributeName())) {
                            continue;
                        }

                        try {
                            User.UserBuilder userBuilder = identityStoreConnector
                                    .getUserBuilder(claimMapping.getAttributeName(), claimValue);

                            return userBuilder
                                    .setUserId(userManager.getUniqueUserId(userBuilder.getUserId(),
                                            identityStoreConnectorId))
                                    .setDomain(domain)
                                    .setIdentityStore(this)
                                    .setAuthorizationStore(realmService.getAuthorizationStore())
                                    .build();

                        } catch (UserNotFoundException e) {
                            // not throwing since looping through all unique attributes
                            if (log.isDebugEnabled()) {
                                log.debug("User for claim " + claimURI + " with value " + claimValue +
                                        " was not found in connector : " + identityStoreConnectorId);
                            }
                        } catch (UserManagerException e) {
                            throw new IdentityStoreException("Error retrieving a unique user id for user", e);
                        }

                    }
                }
            }
        }

        throw new UserNotFoundException("User for claim " + claimURI + " with value " + claimValue + " was not found");
    }

    @Override
    public User getUser(Claim claim, String domainName) throws IdentityStoreException, UserNotFoundException {

        String claimURI = claim.getClaimURI();
        String claimValue = claim.getValue();


        try {
            Domain domain = domainManager.getDomainFromDomainName(domainName);

            if (domain.isClaimAvailable(claimURI)) {

                String[] domainSplit = claimValue.split(IdentityMgtConstants.URL_SPLITTER, 2);

                String domainUnawareClaimValue;
                // If domain is provided in claim value, retrieve user from the specific domain
                if (domainSplit.length == 2) {
                    domainUnawareClaimValue = domainSplit[1];
                } else {
                    domainUnawareClaimValue = claimValue;
                }

                Map<String, List<MetaClaimMapping>> metaClaimMappings = domain.getClaimMappings();

                for (IdentityStoreConnector identityStoreConnector : domain.getSortedIdentityStoreConnectors()) {

                    String identityStoreConnectorId = identityStoreConnector.getIdentityStoreConfig().getConnectorId();


                    List<String> uniqueAttributes =
                            identityStoreConnector.getIdentityStoreConfig().getUniqueAttributes();

                    for (MetaClaimMapping claimMapping : metaClaimMappings.get(identityStoreConnectorId)) {

                        // Skip if the claim does not match or not a unique attribute
                        if (!claimMapping.getMetaClaim().getClaimURI().equals(claimURI) ||
                                !uniqueAttributes.contains(claimMapping.getAttributeName())) {
                            continue;
                        }

                        try {
                            User.UserBuilder userBuilder = identityStoreConnector
                                    .getUserBuilder(claimMapping.getAttributeName(), domainUnawareClaimValue);

                            return userBuilder
                                    .setUserId(userManager.getUniqueUserId(
                                            userBuilder.getUserId(), identityStoreConnectorId))
                                    .setDomain(domain)
                                    .setIdentityStore(this)
                                    .setAuthorizationStore(realmService.getAuthorizationStore())
                                    .build();

                        } catch (UserNotFoundException e) {
                            // not throwing since looping through all connectors
                            if (log.isDebugEnabled()) {
                                log.debug("User for claim " + claimURI + " with value " + claimValue +
                                        " was not found in connector : " + identityStoreConnectorId);
                            }
                        }

                    }
                }
            }
        } catch (DomainException e) {
            throw new IdentityStoreException("Invalid domain information is provided to retrieve user", e);
        } catch (UserManagerException e) {
            throw new IdentityStoreException("Error retrieving a unique user id for user", e);
        }

        throw new UserNotFoundException("User for claim " + claimURI + " with value " + claimValue + " was not found");
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

        List<User> users = new ArrayList<>();

        String claimURI = claim.getClaimURI();
        String claimValue = claim.getValue();

        int currentOffset = 0;
        int currentCount = 0;

        for (Domain domain : domainManager.getSortedDomains()) {

            Map<String, List<MetaClaimMapping>> metaClaimMappings = domain.getClaimMappings();


            for (IdentityStoreConnector identityStoreConnector : domain.getSortedIdentityStoreConnectors()) {

                String identityStoreConnectorId = identityStoreConnector.getIdentityStoreConfig().getConnectorId();

                for (MetaClaimMapping metaClaimMapping :
                        metaClaimMappings.get(identityStoreConnector.getIdentityStoreConnectorId())) {

                    // Required number of users have been retrieved
                    if (currentCount >= length) {
                        break;
                    }

                    if (metaClaimMapping.getMetaClaim().getClaimURI().equals(claimURI)) {

                        List<User.UserBuilder> userBuilderList =
                                identityStoreConnector.getUserBuilderList(metaClaimMapping.getAttributeName(),
                                        claimValue, offset, length - currentCount);

                        for (User.UserBuilder userBuilder : userBuilderList) {

                            if (currentOffset < offset) {
                                currentOffset++;
                                continue;
                                // Skip all before the offset
                            }

                            try {
                                userBuilder.setUserId(userManager.getUniqueUserId(userBuilder.getUserId(),
                                        identityStoreConnectorId));

                                userBuilder.setIdentityStore(this);
                                userBuilder.setAuthorizationStore(realmService.getAuthorizationStore());
                                userBuilder.setDomain(domain);

                                users.add(userBuilder.build());

                                currentCount++;
                            } catch (UserManagerException e) {
                                // not throwing since looping through all connectors
                                throw new IdentityStoreException("Error retrieving unique user Id for user " +
                                        userBuilder.getUserId(), e);
                            }
                        }
                    }
                }

            }
        }

        return users;
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
    public Group getGroup(String groupName) throws IdentityStoreException, GroupNotFoundException {

        Claim claim = new Claim();
        claim.setDialectURI(""); // TODO: Set the dialect URI for the primary attribute.
        claim.setClaimURI(""); // TODO: Set the URI for the primary attribute.
        claim.setValue(groupName);

        return getGroup(claim);
    }

    @Override
    public Group getGroup(String groupId, String domain) throws IdentityStoreException, GroupNotFoundException {
        return null;
    }

    @Override
    public Group getGroup(Claim claim) throws IdentityStoreException, GroupNotFoundException {

        String claimURI = claim.getClaimURI();
        String claimValue = claim.getValue();


        for (Domain domain : domainManager.getSortedDomains()) {

            Map<String, List<MetaClaimMapping>> claimMappings = domain.getClaimMappings();

            for (IdentityStoreConnector identityStoreConnector : domain.getSortedIdentityStoreConnectors()) {

                String identityStoreConnectorId = identityStoreConnector.getIdentityStoreConfig().getConnectorId();

                for (MetaClaimMapping metaClaimMapping :
                        claimMappings.get(identityStoreConnector.getIdentityStoreConnectorId())) {

                    if (metaClaimMapping.getMetaClaim().getClaimURI().equals(claimURI)) {

                        try {
                            Group.GroupBuilder groupBuilder = identityStoreConnector
                                    .getGroupBuilder(metaClaimMapping.getAttributeName(), claimValue);

                            groupBuilder.setDomain(domain);

                            return groupBuilder.build();

                        } catch (GroupNotFoundException e) {
                            // not throwing since looping through all connectors
                            if (log.isDebugEnabled()) {
                                log.debug("Group " + claimValue + " not found in identity store connector" +
                                        identityStoreConnectorId);
                            }
                        }
                    }
                }
            }
        }

        throw new GroupNotFoundException(String.format("Group not found for claim %s : %s", claimURI, claimValue));
    }

    @Override
    public List<Group> getGroupsOfUser(String userId) throws IdentityStoreException {

        List<Group> groupList = new ArrayList<>();

        try {
            User user = getUser(userId);


            Domain domain = user.getDomain();

            for (IdentityStoreConnector identityStoreConnector : domain.getSortedIdentityStoreConnectors()) {

                String identityStoreConnectorId = identityStoreConnector.getIdentityStoreConfig().getConnectorId();

                try {

                    String connectorUserId =
                            userManager.getConnectorUserId(userId, identityStoreConnectorId);

                    for (Group.GroupBuilder groupBuilder :
                            identityStoreConnector.getGroupBuildersOfUser(connectorUserId)) {

                        Group group = groupBuilder
                                .setDomain(domain)
                                .setGroupId(userManager.getUniqueUserId(groupBuilder.getGroupId(),
                                        identityStoreConnectorId))
                                .build();

                        groupList.add(group);
                    }

                } catch (UserManagerException e) {
                    // not throwing since looping through all connectors
                    throw new IdentityStoreException("Error resolving globally unique Id", e);
                }
            }

            return groupList;
        } catch (UserNotFoundException e) {
            throw new IdentityStoreException("User with Id " + userId + " was not found to retrieve groups", e);
        }
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

        try {
            User user = getUser(userId);

            for (IdentityStoreConnector identityStoreConnector : user.getDomain().getSortedIdentityStoreConnectors()) {

                String identityStoreConnectorId = identityStoreConnector.getIdentityStoreConnectorId();

                try {

                    String connectorUserId =
                            userManager.getConnectorUserId(userId, identityStoreConnectorId);

                    if (identityStoreConnector.isUserInGroup(connectorUserId, groupId)) {
                        return true;
                    }
                } catch (UserManagerException e) {
                    // not throwing since looping through all connectors
                    if (log.isDebugEnabled()) {
                        log.debug("User " + userId + " is not mapped to connector " + identityStoreConnectorId);
                    }
                }
            }
        } catch (UserNotFoundException e) {
            throw new IdentityStoreException("User for userId " + userId + " was not found to validate groups", e);
        }

        return false;
    }

    @Override
    public boolean isUserInGroup(String userId, String groupId, String domain) throws IdentityStoreException {
        return false;
    }

    @Override
    public List<Claim> getClaims(User user) throws IdentityStoreException {
        List<Claim> claims = new ArrayList<>();
        Domain domain = user.getDomain();

        Map<String, List<MetaClaimMapping>> claimMappings = domain.getClaimMappings();

        for (IdentityStoreConnector identityStoreConnector : domain.getSortedIdentityStoreConnectors()) {

            String identityStoreConnectorId = identityStoreConnector.getIdentityStoreConfig().getConnectorId();
            List<MetaClaimMapping> metaClaimMappings = claimMappings.get(identityStoreConnectorId);

            // Create <AttributeName, MetaClaim> map
            Map<String, MetaClaim> attributeMapping = metaClaimMappings.stream()
                    .collect(Collectors.toMap(MetaClaimMapping::getAttributeName, MetaClaimMapping::getMetaClaim));

            try {
                String connectorUserId = userManager.getConnectorUserId(user.getUserId(), identityStoreConnectorId);

                List<Attribute> attributeValues = identityStoreConnector.getUserAttributeValues(connectorUserId,
                        new ArrayList<>(attributeMapping.keySet()));
                claims.addAll(buildClaims(attributeValues, attributeMapping));
            } catch (IdentityStoreException | UserManagerException e) {
                throw new IdentityStoreException("Error retrieving claims for user : " + user.getUserId(), e);
            }
        }

        return claims;
    }

    @Override
    public List<Claim> getClaims(User user, List<String> claimURIs) throws IdentityStoreException {
        List<Claim> claims = new ArrayList<>();
        Domain domain = user.getDomain();

        Map<String, List<MetaClaimMapping>> claimMappings = domain.getClaimMappings();

        for (IdentityStoreConnector identityStoreConnector : domain.getSortedIdentityStoreConnectors()) {

            String identityStoreConnectorId = identityStoreConnector.getIdentityStoreConfig().getConnectorId();

            List<MetaClaimMapping> metaClaimMappings = claimMappings.get(identityStoreConnectorId);

            // Create <AttributeName, MetaClaim> map
            Map<String, MetaClaim> attributeMapping = metaClaimMappings.stream().
                    filter(metaClaimMapping -> claimURIs.contains(metaClaimMapping.getMetaClaim().getClaimURI()))
                    .collect(Collectors.toMap(MetaClaimMapping::getAttributeName, MetaClaimMapping::getMetaClaim));

            try {
                String connectorUserId = userManager.getConnectorUserId(user.getUserId(), identityStoreConnectorId);

                List<Attribute> attributeValues = identityStoreConnector.getUserAttributeValues(connectorUserId,
                        new ArrayList<>(attributeMapping.keySet()));
                claims.addAll(buildClaims(attributeValues, attributeMapping));
            } catch (IdentityStoreException | UserManagerException e) {
                throw new IdentityStoreException("Error retrieving claims for user : " + user.getUserId(), e);
            }
        }

        if (claims.size() < claimURIs.size()) {
            log.warn("Some of the requested claims for the user " + user.getUserId() + " could not be found");
        }

        return claims;
    }


    @Override
    public User addUser(UserModel userModel) throws IdentityStoreException {

        if (userModel == null || userModel.getUserClaims() == null || userModel.getUserClaims().isEmpty()) {
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

        if (userModel == null || userModel.getUserClaims() == null || userModel.getUserClaims().isEmpty()) {
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

        if (StringUtils.isNullOrEmpty(uniqueUserId)) {
            throw new IdentityStoreClientException("Invalid user unique id.");
        }

        String domainName;
        try {
            domainName = userManager.getDomainNameFromUserUniqueId(uniqueUserId);
        } catch (UserManagerException e) {
            throw new IdentityStoreClientException("Invalid user UUID. Failed to retrieve domain name.");
        }

        Domain domain;
        try {
            domain = domainManager.getDomainFromDomainName(domainName);
        } catch (DomainException e) {
            throw new IdentityStoreServerException(String.format("Error while retrieving domain from the domain name " +
                    "- %s", domainName), e);
        }

        doUpdateUserClaims(uniqueUserId, userClaims, domain);
    }

    @Override
    public void updateUserClaims(String uniqueUserId, List<Claim> userClaimsToUpdate, List<Claim> userClaimsToRemove)
            throws IdentityStoreException {

        if (StringUtils.isNullOrEmpty(uniqueUserId)) {
            throw new IdentityStoreClientException("Invalid user unique id.");
        }

        String domainName;
        try {
            domainName = userManager.getDomainNameFromUserUniqueId(uniqueUserId);
        } catch (UserManagerException e) {
            throw new IdentityStoreClientException("Invalid user unique id. Failed to retrieve domain name.");
        }

        if ((userClaimsToUpdate == null || userClaimsToUpdate.isEmpty()) && (userClaimsToRemove == null ||
                userClaimsToRemove.isEmpty())) {
            return;
        }

        Domain domain;
        try {
            domain = domainManager.getDomainFromDomainName(domainName);
        } catch (DomainException e) {
            throw new IdentityStoreServerException(String.format("Error while retrieving domain from the domain name " +
                    "- %s", domainName), e);
        }

        doUpdateUserClaims(uniqueUserId, userClaimsToUpdate, userClaimsToRemove, domain);
    }

    @Override
    public void deleteUser(String uniqueUserId) throws IdentityStoreException {

        if (StringUtils.isNullOrEmpty(uniqueUserId)) {
            throw new IdentityStoreClientException("Invalid user unique id.");
        }

        String domainName;
        try {
            domainName = userManager.getDomainNameFromUserUniqueId(uniqueUserId);
        } catch (UserManagerException e) {
            throw new IdentityStoreClientException("Invalid user unique id. Failed to retrieve domain name.");
        }

        Domain domain;
        try {
            domain = domainManager.getDomainFromDomainName(domainName);
        } catch (DomainException e) {
            throw new IdentityStoreServerException(String.format("Error while retrieving domain from the domain name " +
                    "- %s", domainName), e);
        }

        Map<String, String> connectorUserIds;
        try {
            connectorUserIds = userManager.getConnectorUserIds(uniqueUserId);
        } catch (UserManagerException e) {
            throw new IdentityStoreServerException(String.format("Failed to retrieve connector id to user connector " +
                    "id map for unique user id - %s", uniqueUserId), e);
        }

        for (Map.Entry<String, String> entry : connectorUserIds.entrySet()) {
            domain.getIdentityStoreConnectorFromId(entry.getKey()).deleteUser(entry.getValue());
        }

        try {
            userManager.deleteUser(uniqueUserId);
            //TODO audit log
        } catch (UserManagerException e) {
            throw new IdentityStoreServerException("Failed to delete user - " + uniqueUserId, e);
        }
    }

    @Override
    public void updateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIds) throws IdentityStoreException {

        if (StringUtils.isNullOrEmpty(uniqueUserId)) {
            throw new IdentityStoreClientException("Invalid user unique id.");
        }

        try {
            userManager.updateGroupsOfUser(uniqueUserId, uniqueGroupIds);
        } catch (UserManagerException e) {
            throw new IdentityStoreServerException("Failed to update groups of user - " + uniqueUserId, e);
        }
    }

    @Override
    public void updateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIdsToAdd, List<String>
            uniqueGroupIdsToRemove) throws IdentityStoreException {

        if (StringUtils.isNullOrEmpty(uniqueUserId)) {
            throw new IdentityStoreClientException("Invalid user unique id.");
        }

        try {
            userManager.updateGroupsOfUser(uniqueUserId, uniqueGroupIdsToAdd, uniqueGroupIdsToRemove);
        } catch (UserManagerException e) {
            throw new IdentityStoreServerException("Failed to update groups of user - " + uniqueUserId, e);
        }
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

        if (StringUtils.isNullOrEmpty(uniqueGroupId)) {
            throw new IdentityStoreClientException("Invalid group unique id.");
        }

        String domainName;
        try {
            domainName = userManager.getDomainNameFromGroupUniqueId(uniqueGroupId);
        } catch (UserManagerException e) {
            throw new IdentityStoreClientException("Invalid group unique id. Failed to retrieve domain name.");
        }

        Domain domain;
        try {
            domain = domainManager.getDomainFromDomainName(domainName);
        } catch (DomainException e) {
            throw new IdentityStoreServerException(String.format("Error while retrieving domain from the domain name " +
                    "- %s", domainName), e);
        }

        doUpdateGroupClaims(uniqueGroupId, groupClaims, domain);
    }

    @Override
    public void updateGroupClaims(String uniqueGroupId, List<Claim> groupClaimsToUpdate,
                                  List<Claim> groupClaimsToRemove) throws IdentityStoreException {

        if (StringUtils.isNullOrEmpty(uniqueGroupId)) {
            throw new IdentityStoreClientException("Invalid group unique id.");
        }

        String domainName;
        try {
            domainName = userManager.getDomainNameFromGroupUniqueId(uniqueGroupId);
        } catch (UserManagerException e) {
            throw new IdentityStoreClientException("Invalid group unique id. Failed to retrieve domain name.");
        }

        if ((groupClaimsToUpdate == null || groupClaimsToUpdate.isEmpty()) && (groupClaimsToRemove == null ||
                groupClaimsToRemove.isEmpty())) {
            return;
        }

        Domain domain;
        try {
            domain = domainManager.getDomainFromDomainName(domainName);
        } catch (DomainException e) {
            throw new IdentityStoreServerException(String.format("Error while retrieving domain from the domain name " +
                    "- %s", domainName), e);
        }

        doUpdateGroupClaims(uniqueGroupId, groupClaimsToUpdate, groupClaimsToRemove, domain);
    }

    @Override
    public void deleteGroup(String uniqueGroupId) throws IdentityStoreException {

        if (StringUtils.isNullOrEmpty(uniqueGroupId)) {
            throw new IdentityStoreClientException("Invalid group unique id.");
        }

        String domainName;
        try {
            domainName = userManager.getDomainNameFromUserUniqueId(uniqueGroupId);
        } catch (UserManagerException e) {
            throw new IdentityStoreClientException("Invalid group unique id. Failed to retrieve domain name.");
        }

        Domain domain;
        try {
            domain = domainManager.getDomainFromDomainName(domainName);
        } catch (DomainException e) {
            throw new IdentityStoreServerException(String.format("Error while retrieving domain from the domain name " +
                    "- %s", domainName), e);
        }

        Map<String, String> connectorGroupIds;
        try {
            connectorGroupIds = userManager.getConnectorGroupIds(uniqueGroupId);
        } catch (UserManagerException e) {
            throw new IdentityStoreServerException(String.format("Failed to retrieve connector id to group connector " +
                    "id map for unique group id - %s", uniqueGroupId), e);
        }

        for (Map.Entry<String, String> entry : connectorGroupIds.entrySet()) {
            domain.getIdentityStoreConnectorFromId(entry.getKey()).deleteGroup(entry.getValue());
        }

        try {
            userManager.deleteGroup(uniqueGroupId);
        } catch (UserManagerException e) {
            throw new IdentityStoreServerException(String.format("Failed to delete group - %s", uniqueGroupId), e);
        }
    }

    @Override
    public void updateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIds) throws IdentityStoreException {


        if (StringUtils.isNullOrEmpty(uniqueGroupId)) {
            throw new IdentityStoreClientException("Invalid group unique id.");
        }

        try {
            userManager.updateGroupsOfUser(uniqueGroupId, uniqueUserIds);
        } catch (UserManagerException e) {
            throw new IdentityStoreServerException(String.format("Failed to update users of group - %s",
                    uniqueGroupId), e);
        }
    }

    @Override
    public void updateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIdsToAdd,
                                   List<String> uniqueUserIdsToRemove) throws IdentityStoreException {

        if (StringUtils.isNullOrEmpty(uniqueGroupId)) {
            throw new IdentityStoreClientException("Invalid group unique id.");
        }

        try {
            userManager.updateUsersOfGroup(uniqueGroupId, uniqueUserIdsToAdd, uniqueUserIdsToRemove);
        } catch (UserManagerException e) {
            throw new IdentityStoreServerException("Failed to update groups of user - " + uniqueGroupId, e);
        }
    }

    private User doAddUser(UserModel userModel, Domain domain) throws IdentityStoreException {

        Map<String, List<MetaClaimMapping>> metaClaimMappings = domain.getClaimMappings();
        if (metaClaimMappings.isEmpty()) {
            throw new IdentityStoreServerException("Invalid domain configuration found. No meta claim mappings.");
        }

        Map<String, List<Attribute>> connectorAttributeMap = getConnectorAttributesMap(userModel.getUserClaims(),
                metaClaimMappings);

        //TODO check user is present

        List<ConnectedUser> connectedUsers = new ArrayList<>();
        for (Map.Entry<String, List<Attribute>> entry : connectorAttributeMap.entrySet()) {
            String connectorUserId = domain.getIdentityStoreConnectorFromId(entry.getKey()).addUser(entry.getValue());
            connectedUsers.add(new ConnectedUser(entry.getKey(), connectorUserId));
            // TODO handle any failure
        }

        String userUniqueId = IdentityUserMgtUtil.generateUUID();
        try {
            userManager.addUser(userUniqueId, connectedUsers);
        } catch (UserManagerException e) {
            // TODO handle any failure
            throw new IdentityStoreServerException("Error occurred while persisting user unique id.", e);
        }

        return new User.UserBuilder()
                .setUserId(userUniqueId)
                .setDomain(domain)
                .setIdentityStore(realmService.getIdentityStore())
                .setAuthorizationStore(realmService.getAuthorizationStore())
                .build();
    }

    private List<User> doAddUsers(List<UserModel> userModels, Domain domain) throws IdentityStoreException {

        Map<String, List<MetaClaimMapping>> metaClaimMappings = domain.getClaimMappings();
        if (metaClaimMappings.isEmpty()) {
            throw new IdentityStoreServerException("Invalid domain configuration found. No meta claim mappings.");
        }

        List<Map<String, List<Attribute>>> connectorAttributesMaps = userModels.stream()
                .filter(Objects::nonNull)
                .filter(userModel -> userModel.getUserClaims() != null)
                .map(userModel -> getConnectorAttributesMap(userModel.getUserClaims(), metaClaimMappings))
                .collect(Collectors.toList());

        Map<String, Map<String, List<Attribute>>> connectorViseUserMap = getConnectorViseAttributesMap
                (connectorAttributesMaps);

        Map<String, List<ConnectedUser>> connectedUsersList = new HashMap<>();

        for (Map.Entry<String, Map<String, List<Attribute>>> entry : connectorViseUserMap.entrySet()) {

            Map<String, String> uniqueIds = domain.getIdentityStoreConnectorFromId(entry.getKey())
                    .addUsers(entry.getValue());

            if (uniqueIds != null) {
                uniqueIds.entrySet().stream()
                        .forEach(t -> {
                            List<ConnectedUser> connectedUsers = connectedUsersList.get(t.getKey());
                            if (connectedUsers == null) {
                                connectedUsers = new ArrayList<>();
                                connectedUsersList.put(t.getKey(), connectedUsers);
                            }
                            connectedUsers.add(new ConnectedUser(entry.getKey(), t.getValue()));
                        });
            }
            // TODO handle any failure
        }

        try {
            userManager.addUsers(connectedUsersList);
        } catch (UserManagerException e) {
            // TODO handle any failure
            throw new IdentityStoreServerException("Error occurred while persisting user unique ids.", e);
        }

        return connectedUsersList.entrySet().stream()
                .map(entry -> new User.UserBuilder()
                        .setUserId(entry.getKey())
                        .setDomain(domain)
                        .setIdentityStore(realmService.getIdentityStore())
                        .setAuthorizationStore(realmService.getAuthorizationStore())
                        .build())
                .collect(Collectors.toList());
    }

    private void doUpdateUserClaims(String userUniqueId, List<Claim> userClaims, Domain domain)
            throws IdentityStoreException {

        Map<String, String> connectorUserIds;
        try {
            connectorUserIds = userManager.getConnectorUserIds(userUniqueId);
        } catch (UserManagerException e) {
            throw new IdentityStoreServerException(String.format("Failed to retrieve connector id to user connector " +
                    "id map for the user unique id - %s ", userUniqueId), e);
        }

        Map<String, String> updatedUniqueIds = new HashMap<>();

        if (userClaims == null || userClaims.isEmpty()) {
            connectorUserIds.entrySet().stream()
                    .forEach(rethrowConsumer(entry -> {
                        (domain.getIdentityStoreConnectorFromId(entry.getKey()))
                                .updateUserAttributes(entry.getValue(), new ArrayList<>());
                    }));
            //TODO: do we need to delete user unique id? credential store still may have references
        } else {
            Map<String, List<MetaClaimMapping>> metaClaimMappings = domain.getClaimMappings();
            if (metaClaimMappings.isEmpty()) {
                throw new IdentityStoreServerException("Invalid domain configuration found. No meta claim mappings.");
            }

            Map<String, List<Attribute>> connectorAttributeMap = getConnectorAttributesMap(userClaims,
                    metaClaimMappings);

            for (Map.Entry<String, String> entry : connectorUserIds.entrySet()) {
                String uniqueId = domain.getIdentityStoreConnectorFromId(entry.getKey()).updateUserAttributes
                        (connectorUserIds.get(entry.getKey()), connectorAttributeMap.get(entry.getKey()));
                updatedUniqueIds.put(entry.getKey(), uniqueId);
            }
        }

        if (!connectorUserIds.equals(updatedUniqueIds)) {
            try {
                userManager.updateUser(userUniqueId, updatedUniqueIds);
            } catch (UserManagerException e) {
                throw new IdentityStoreServerException("Failed to update user connector ids.", e);
            }
        }
    }

    private void doUpdateUserClaims(String userUniqueId, List<Claim> userClaimsToUpdate, List<Claim> userClaimsToRemove,
                                    Domain domain) throws IdentityStoreException {

        Map<String, String> connectorUserIds;
        try {
            connectorUserIds = userManager.getConnectorUserIds(userUniqueId);
        } catch (UserManagerException e) {
            throw new IdentityStoreServerException(String.format("Failed to retrieve connector id to user connector " +
                    "id map for the user unique id - %s ", userUniqueId), e);
        }

        Map<String, List<MetaClaimMapping>> metaClaimMappings = domain.getClaimMappings();
        if (metaClaimMappings.isEmpty()) {
            throw new IdentityStoreServerException("Invalid identity store configuration found.");
        }

        Map<String, List<Attribute>> connectorAttributeMapToUpdate = getConnectorAttributesMap(userClaimsToUpdate,
                metaClaimMappings);

        Map<String, List<Attribute>> connectorAttributeMapToRemove = getConnectorAttributesMap(userClaimsToRemove,
                metaClaimMappings);

        Map<String, String> updatedConnectorUserIds = new HashMap<>();

        for (Map.Entry<String, String> entry : connectorUserIds.entrySet()) {
            String uniqueId = domain.getIdentityStoreConnectorFromId(entry.getKey())
                    .updateUserAttributes(connectorUserIds.get(entry.getKey()), connectorAttributeMapToUpdate
                            .get(entry.getKey()), connectorAttributeMapToRemove.get(entry.getKey()));
            updatedConnectorUserIds.put(entry.getKey(), uniqueId);
        }


        if (!connectorUserIds.equals(updatedConnectorUserIds)) {
            try {
                userManager.updateUser(userUniqueId, updatedConnectorUserIds);
            } catch (UserManagerException e) {
                throw new IdentityStoreServerException("Failed to update user connected ids.", e);
            }
        }
    }

    private Group doAddGroup(GroupModel groupModel, Domain domain) throws IdentityStoreException {

        Map<String, List<MetaClaimMapping>> metaClaimMappings = domain.getClaimMappings();
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
            userManager.addGroup(groupUniqueId, connectedGroups);
        } catch (UserManagerException e) {
            // TODO handle any failure
            throw new IdentityStoreServerException("Error occurred while persisting group unique group id.", e);
        }

        return new Group.GroupBuilder()
                .setGroupId(groupUniqueId)
                .setDomain(domain)
                .build();
    }

    private List<Group> doAddGroups(List<GroupModel> groupModels, Domain domain) throws IdentityStoreException {

        Map<String, List<MetaClaimMapping>> metaClaimMappings = domain.getClaimMappings();
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
            userManager.addGroups(connectedGroupsMaps);
        } catch (UserManagerException e) {
            // TODO handle any failure
            throw new IdentityStoreServerException("Error occurred while persisting group unique ids.", e);
        }

        return connectedGroupsMaps.entrySet().stream()
                .map(entry -> new Group.GroupBuilder()
                        .setGroupId(entry.getKey())
                        .setDomain(domain)
                        .build())
                .collect(Collectors.toList());
    }

    private void doUpdateGroupClaims(String uniqueGroupId, List<Claim> groupClaims, Domain domain)
            throws IdentityStoreException {

        Map<String, String> connectorGroupIds;
        try {
            connectorGroupIds = userManager.getConnectorGroupIds(uniqueGroupId);
        } catch (UserManagerException e) {
            throw new IdentityStoreServerException(String.format("Failed to retrieve connector id to group connector " +
                    "id map for unique group id - %s", uniqueGroupId), e);
        }

        Map<String, String> updatedUniqueIds = new HashMap<>();

        if (groupClaims == null || groupClaims.isEmpty()) {
            connectorGroupIds.entrySet().stream()
                    .forEach(rethrowConsumer(entry -> {
                        domain.getIdentityStoreConnectorFromId(entry.getKey()).updateGroupAttributes(entry.getValue(),
                                new ArrayList<>());
                    }));
            //TODO: do we need to delete group unique id? credential store still may have references
        } else {

            Map<String, List<MetaClaimMapping>> metaClaimMappings = domain.getClaimMappings();
            if (metaClaimMappings.isEmpty()) {
                throw new IdentityStoreServerException("Invalid domain configuration found. No meta claim mappings.");
            }

            Map<String, List<Attribute>> connectorAttributeMap = getConnectorAttributesMap(groupClaims,
                    metaClaimMappings);

            for (Map.Entry<String, String> entry : connectorGroupIds.entrySet()) {
                String uniqueConnectorId = domain.getIdentityStoreConnectorFromId(entry.getKey()).updateGroupAttributes
                        (connectorGroupIds.get(entry.getKey()), connectorAttributeMap.get(entry.getKey()));
                updatedUniqueIds.put(entry.getKey(), uniqueConnectorId);
            }
        }

        if (!connectorGroupIds.equals(updatedUniqueIds)) {
            try {
                userManager.updateGroup(uniqueGroupId, updatedUniqueIds);
            } catch (UserManagerException e) {
                throw new IdentityStoreServerException("Failed to update group connected ids.", e);
            }
        }
    }

    private void doUpdateGroupClaims(String uniqueGroupId, List<Claim> groupClaimsToUpdate,
                                     List<Claim> groupClaimsToRemove, Domain domain) throws IdentityStoreException {

        Map<String, String> connectorGroupIds;
        try {
            connectorGroupIds = userManager.getConnectorGroupIds(uniqueGroupId);
        } catch (UserManagerException e) {
            throw new IdentityStoreServerException("Failed to retrieve connector id to group connector id map for " +
                    "for unique group id - %s" + uniqueGroupId, e);
        }

        Map<String, List<MetaClaimMapping>> metaClaimMappings = domain.getClaimMappings();
        if (metaClaimMappings.isEmpty()) {
            throw new IdentityStoreServerException("Invalid domain configuration found. No meta claim mappings.");
        }

        Map<String, List<Attribute>> connectorAttributeMapToUpdate = getConnectorAttributesMap(groupClaimsToUpdate,
                metaClaimMappings);

        Map<String, List<Attribute>> connectorAttributeMapToRemove = getConnectorAttributesMap(groupClaimsToRemove,
                metaClaimMappings);

        Map<String, String> updatedUniqueIds = new HashMap<>();

        for (Map.Entry<String, String> entry : connectorGroupIds.entrySet()) {
            String uniqueId = domain.getIdentityStoreConnectorFromId(entry.getKey()).updateGroupAttributes
                    (connectorGroupIds.get(entry.getKey()), connectorAttributeMapToUpdate.get(entry.getKey()),
                            connectorAttributeMapToRemove.get(entry.getKey()));
            updatedUniqueIds.put(entry.getKey(), uniqueId);
        }


        if (!connectorGroupIds.equals(updatedUniqueIds)) {
            try {
                userManager.updateGroup(uniqueGroupId, updatedUniqueIds);
            } catch (UserManagerException e) {
                throw new IdentityStoreServerException("Failed to update group connected ids.", e);
            }
        }
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

    private List<Claim> buildClaims(List<Attribute> attributes, Map<String, MetaClaim> attributeMapping) {

        return attributes.stream().map(attribute -> {
            MetaClaim metaClaim = attributeMapping.get(attribute.getAttributeName());
            Claim claim = new Claim();
            claim.setClaimURI(metaClaim.getClaimURI());
            claim.setDialectURI(metaClaim.getDialectURI());
            claim.setValue(attribute.getAttributeValue());
            return claim;
        }).collect(Collectors.toList());
    }
}


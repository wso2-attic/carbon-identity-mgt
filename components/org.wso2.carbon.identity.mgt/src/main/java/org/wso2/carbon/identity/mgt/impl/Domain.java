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

package org.wso2.carbon.identity.mgt.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.mgt.bean.GroupBean;
import org.wso2.carbon.identity.mgt.bean.UserBean;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.claim.MetaClaim;
import org.wso2.carbon.identity.mgt.claim.MetaClaimMapping;
import org.wso2.carbon.identity.mgt.connector.Attribute;
import org.wso2.carbon.identity.mgt.connector.CredentialStoreConnector;
import org.wso2.carbon.identity.mgt.connector.IdentityStoreConnector;
import org.wso2.carbon.identity.mgt.exception.AuthenticationFailure;
import org.wso2.carbon.identity.mgt.exception.CredentialStoreConnectorException;
import org.wso2.carbon.identity.mgt.exception.DomainClientException;
import org.wso2.carbon.identity.mgt.exception.DomainException;
import org.wso2.carbon.identity.mgt.exception.GroupNotFoundException;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreConnectorException;
import org.wso2.carbon.identity.mgt.exception.UniqueIdResolverException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.mgt.impl.util.IdentityUserMgtUtil;
import org.wso2.carbon.identity.mgt.resolver.DomainGroup;
import org.wso2.carbon.identity.mgt.resolver.DomainUser;
import org.wso2.carbon.identity.mgt.resolver.GroupPartition;
import org.wso2.carbon.identity.mgt.resolver.UniqueIdResolver;
import org.wso2.carbon.identity.mgt.resolver.UserPartition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.security.auth.callback.Callback;

import static org.wso2.carbon.kernel.utils.StringUtils.isNullOrEmpty;

/**
 * Represents a domain.
 */
public class Domain {

    private static final Logger log = LoggerFactory.getLogger(Domain.class);

    /**
     * Mapping between IdentityStoreConnector ID and IdentityStoreConnector
     */
    private Map<String, IdentityStoreConnector> identityStoreConnectorsMap = new HashMap<>();

    /**
     * Mapping between CredentialStoreConnector ID and CredentialStoreConnector
     */
    private Map<String, CredentialStoreConnector> credentialStoreConnectorsMap = new HashMap<>();

    /**
     * Set of IdentityStoreConnectors for this domain sorted by their priority.
     */
    private List<IdentityStoreConnector> identityStoreConnectors = new ArrayList<>();

    /**
     * Set of CredentialStoreConnectors for this domain sorted by their priority.
     */
    private List<CredentialStoreConnector> credentialStoreConnectors = new ArrayList<>();

    private List<MetaClaimMapping> metaClaimMappings = new ArrayList<>();

    private Map<String, MetaClaimMapping> claimUriToMetaClaimMappings = new HashMap<>();

    /**
     * Id of the domain.
     */
    private int id;

    /**
     * Name of the domain.
     */
    private String name;

    /**
     * Priority of the domain.
     * Highest priority for domain is 1
     * Domain priority value should be greater than 0
     */
    private int order;

    private UniqueIdResolver uniqueIdResolver;

    public Domain(int id, String name, int order, UniqueIdResolver uniqueIdResolver) throws
            DomainException {

        if (order < 1) {
            throw new DomainException("Domain priority value should be greater than 0");
        }

        this.id = id;
        this.name = name;
        this.order = order;
        this.uniqueIdResolver = uniqueIdResolver;
    }

    public int getId() {

        return id;
    }

    /**
     * Get the domain name.
     *
     * @return String - domain name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the priority of the domain.
     *
     * @return integer - domain priority
     */
    public int getOrder() {

        return order;
    }

    /**
     * Add an identity store connector to the map.
     *
     * @param identityStoreConnector Identity Store connector.
     * @throws DomainException domain exception.
     */
    public void addIdentityStoreConnector(IdentityStoreConnector identityStoreConnector) throws DomainException {

        identityStoreConnectorsMap.put(identityStoreConnector.getIdentityStoreConfig().getConnectorId(),
                identityStoreConnector);
        identityStoreConnectors.add(identityStoreConnector);
    }

    /**
     * Add an credential store connector to the map.
     *
     * @param credentialStoreConnector Credential Store connector
     * @throws DomainException domain exception
     */
    public void addCredentialStoreConnector(CredentialStoreConnector credentialStoreConnector) throws DomainException {

        credentialStoreConnectorsMap.put(credentialStoreConnector.getCredentialStoreConfig().getConnectorId(),
                credentialStoreConnector);
        credentialStoreConnectors.add(credentialStoreConnector);
    }

    /**
     * Checks weather a certain claim URI exists in the domain claim mappings.
     *
     * @param claimURI Claim
     * @return is claim belong to domain
     */
    public boolean isClaimSupported(String claimURI) {

        return claimUriToMetaClaimMappings.keySet().contains(claimURI);
    }

    /**
     * Is user exists in the domain.
     *
     * @param domainUserId domain user id.
     * @return user existence.
     * @throws DomainException Domain Exception.
     */
    public boolean isUserExists(String domainUserId) throws DomainException {

        try {
            return this.uniqueIdResolver.isUserExists(domainUserId, this.id);
        } catch (UniqueIdResolverException e) {
            throw new DomainException("Failed to check existence of user.", e);
        }
    }

    public String getDomainUserId(Claim claim) throws DomainException, UserNotFoundException {

        MetaClaimMapping metaClaimMapping = claimUriToMetaClaimMappings.get(claim.getClaimUri());

        if (!metaClaimMapping.isUnique()) {
            throw new DomainClientException("Provided claim is not unique.");
        }

        IdentityStoreConnector identityStoreConnector = identityStoreConnectorsMap
                .get(metaClaimMapping.getIdentityStoreConnectorId());

        String connectorUserId;
        try {
            connectorUserId = identityStoreConnector.getConnectorUserId(metaClaimMapping.getAttributeName(),
                    claim.getValue());
        } catch (IdentityStoreConnectorException e) {
            throw new DomainException("Failed to get connector user id", e);
        }

        if (isNullOrEmpty(connectorUserId)) {
            throw new UserNotFoundException("Invalid claim value.");
        }

        DomainUser domainUser;
        try {
            domainUser = uniqueIdResolver.getUserFromConnectorUserId(connectorUserId, metaClaimMapping
                    .getIdentityStoreConnectorId(), this.id);
        } catch (UniqueIdResolverException e) {
            throw new DomainException("Failed to retrieve the domain user id.", e);
        }

        if (domainUser == null || isNullOrEmpty(domainUser.getDomainUserId())) {
            throw new DomainException("Failed to retrieve the domain user id.");
        }

        return domainUser.getDomainUserId();
    }

    public List<String> listDomainUsers(int offset, int length) throws DomainException {

        List<DomainUser> domainUsers;
        try {
            domainUsers = this.uniqueIdResolver.listDomainUsers(offset, length, this.id);
        } catch (UniqueIdResolverException e) {
            throw new DomainException("Failed to retrieve partitions of users.", e);
        }

        if (domainUsers == null || domainUsers.isEmpty()) {
            return Collections.emptyList();
        }

        return domainUsers.stream()
                .filter(Objects::nonNull)
                .filter(uniqueUser -> !isNullOrEmpty(uniqueUser.getDomainUserId()))
                .map(DomainUser::getDomainUserId)
                .collect(Collectors.toList());
    }

    public List<String> listDomainUsers(Claim claim, int offset, int length) throws DomainException {

        MetaClaimMapping metaClaimMapping = claimUriToMetaClaimMappings.get(claim.getClaimUri());

        IdentityStoreConnector identityStoreConnector = identityStoreConnectorsMap
                .get(metaClaimMapping.getIdentityStoreConnectorId());

        List<String> connectorUserIds;
        try {
            connectorUserIds = identityStoreConnector.listConnectorUserIds(metaClaimMapping.getAttributeName(),
                    claim.getValue(), offset, length);
        } catch (IdentityStoreConnectorException e) {
            throw new DomainException("Failed to list connector user ids", e);
        }

        if (connectorUserIds == null || connectorUserIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<DomainUser> domainUsers;
        try {
            domainUsers = uniqueIdResolver.getUsers(connectorUserIds, metaClaimMapping
                    .getIdentityStoreConnectorId(), this.id);
        } catch (UniqueIdResolverException e) {
            throw new DomainException("Failed to retrieve the unique user ids.", e);
        }

        if (domainUsers == null || domainUsers.isEmpty()) {
            throw new DomainException("Failed to retrieve the unique user ids.");
        }

        return domainUsers.stream()
                .filter(Objects::nonNull)
                .filter(domainUser -> !isNullOrEmpty(domainUser.getDomainUserId()))
                .map(DomainUser::getDomainUserId)
                .collect(Collectors.toList());
    }

    /**
     *  List a set of users which satisfy multiple claims.
     *
     * @param claims list of claims.
     * @param offset
     * @param length
     * @return
     * @throws DomainException
     */
    public List<String> listDomainUsers(List<Claim> claims, int offset, int length) throws DomainException {

        if (claims.isEmpty()) {
            return null;
        }
        // Map of connector ID and its list of attributes.
        Map<String, List<Attribute>> connectorIdToAttributeMap = getAttributesMap(claims);
        Map<String, List<String>> connectorToDomainUserIdMap = new HashMap<>();
        List<String> intersect = new ArrayList<>();
        IdentityStoreConnector identityStoreConnector;

        if (!connectorIdToAttributeMap.isEmpty()) {
            for (Map.Entry<String, List<Attribute>> entry : connectorIdToAttributeMap.entrySet()) {
                List<String> connectorUserIds = null;
                try {
                    identityStoreConnector = identityStoreConnectorsMap.get(entry.getKey());
                    connectorUserIds = identityStoreConnector.getUsers(entry.getValue(), offset, length);

                } catch (IdentityStoreConnectorException e) {
                    throw new DomainException("Failed to list connector user ids.", e);
                }

                List<DomainUser> domainUsers;
                List<String> domainUserIds = new ArrayList<>();
                try {
                    domainUsers = uniqueIdResolver.getUsers(connectorUserIds, entry.getKey(), this.id);
                    for (DomainUser domainuser : domainUsers) {
                        domainUserIds.add(domainuser.getDomainUserId());
                    }
                    connectorToDomainUserIdMap.put(entry.getKey(), domainUserIds);
                } catch (UniqueIdResolverException e) {
                    throw new DomainException("Failed to retrieve the unique user ids.", e);
                }
            }
        }

        // Select the intersection of UserIds
        if (!connectorToDomainUserIdMap.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : connectorToDomainUserIdMap.entrySet()) {
                if (entry.getValue() == null || entry.getValue().isEmpty()) {
                    intersect.clear();
                    break;
                } else {
                    if (intersect.isEmpty()) {
                        intersect.addAll(entry.getValue());
                    } else {
                        List<String> temp = entry.getValue().stream()
                                .filter(intersect::contains)
                                .collect(Collectors.toList());
                        intersect.clear();
                        intersect.addAll(temp);
                    }
                }
            }
        }
        return intersect;
    }

    public List<String> listDomainUsers(MetaClaim metaClaim, String filterPattern, int offset, int length)
            throws DomainException {

        MetaClaimMapping metaClaimMapping = claimUriToMetaClaimMappings.get(metaClaim.getClaimUri());

        IdentityStoreConnector identityStoreConnector = identityStoreConnectorsMap
                .get(metaClaimMapping.getIdentityStoreConnectorId());

        List<String> connectorUserIds;
        try {
            connectorUserIds = identityStoreConnector.listConnectorUserIdsByPattern(metaClaimMapping.getAttributeName(),
                    filterPattern, offset, length);
        } catch (IdentityStoreConnectorException e) {
            throw new DomainException("Failed to list connector user ids by pattern", e);
        }

        if (connectorUserIds == null || connectorUserIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<DomainUser> domainUsers;
        try {
            domainUsers = uniqueIdResolver.getUsers(connectorUserIds, metaClaimMapping
                    .getIdentityStoreConnectorId(), this.id);
        } catch (UniqueIdResolverException e) {
            throw new DomainException("Failed to retrieve the unique user ids.", e);
        }

        if (domainUsers == null || domainUsers.isEmpty()) {
            throw new DomainException("Failed to retrieve the unique user ids.");
        }

        return domainUsers.stream()
                .filter(Objects::nonNull)
                .filter(uniqueUser -> !isNullOrEmpty(uniqueUser.getDomainUserId()))
                .map(DomainUser::getDomainUserId)
                .collect(Collectors.toList());
    }

    /**
     * Is group exists in the domain.
     *
     * @param domainGroupId domain group id.
     * @return group existence.
     * @throws DomainException Domain Exception.
     */
    public boolean isGroupExists(String domainGroupId) throws DomainException {

        try {
            return this.uniqueIdResolver.isGroupExists(domainGroupId, this.id);
        } catch (UniqueIdResolverException e) {
            throw new DomainException("Failed to check existence of group.", e);
        }
    }

    public String getDomainGroupId(Claim claim) throws DomainException, GroupNotFoundException {

        MetaClaimMapping metaClaimMapping = claimUriToMetaClaimMappings.get(claim.getClaimUri());

        if (!metaClaimMapping.isUnique()) {
            throw new DomainClientException("Provided claim is not unique.");
        }

        IdentityStoreConnector identityStoreConnector = identityStoreConnectorsMap
                .get(metaClaimMapping.getIdentityStoreConnectorId());

        String connectorGroupId;
        try {
            connectorGroupId = identityStoreConnector.getConnectorGroupId(metaClaimMapping.getAttributeName(),
                    claim.getValue());
        } catch (GroupNotFoundException e) {
            throw new GroupNotFoundException("Failed to get connector group id for claim %s." + claim.getClaimUri(), e);
        } catch (IdentityStoreConnectorException e) {
            throw new DomainException("An error occurred while searching the group.", e);
        }

        if (isNullOrEmpty(connectorGroupId)) {
            throw new GroupNotFoundException("Invalid claim value.");
        }

        DomainGroup domainGroup;
        try {
            domainGroup = uniqueIdResolver.getGroupFromConnectorGroupId(connectorGroupId, metaClaimMapping
                    .getIdentityStoreConnectorId(), this.id);
        } catch (GroupNotFoundException e) {
            throw new GroupNotFoundException("Failed to retrieve the domain group id.", e);
        } catch (UniqueIdResolverException e) {
            throw new DomainException("An error occurred while searching the group.", e);
        }

        if (domainGroup == null || isNullOrEmpty(domainGroup.getDomainGroupId())) {
            throw new GroupNotFoundException("Failed to retrieve the domain group id.");
        }

        return domainGroup.getDomainGroupId();
    }

    public List<String> listDomainGroups(int offset, int length) throws DomainException {

        List<DomainGroup> domainGroups;
        try {
            domainGroups = this.uniqueIdResolver.listGroups(offset, length, this.id);
        } catch (UniqueIdResolverException e) {
            throw new DomainException("Failed to retrieve partitions of groups.", e);
        }

        if (domainGroups == null || domainGroups.isEmpty()) {
            return Collections.emptyList();
        }

        return domainGroups.stream()
                .filter(Objects::nonNull)
                .filter(domainGroup -> !isNullOrEmpty(domainGroup.getDomainGroupId()))
                .map(DomainGroup::getDomainGroupId)
                .collect(Collectors.toList());
    }

    public List<String> listDomainGroups(Claim claim, int offset, int length) throws DomainException {

        MetaClaimMapping metaClaimMapping = claimUriToMetaClaimMappings.get(claim.getClaimUri());

        if (!metaClaimMapping.isUnique()) {
            throw new DomainClientException("Provided claim is not unique.");
        }

        IdentityStoreConnector identityStoreConnector = identityStoreConnectorsMap
                .get(metaClaimMapping.getIdentityStoreConnectorId());

        List<String> connectorGroupIds;
        try {
            connectorGroupIds = identityStoreConnector.listConnectorGroupIds(metaClaimMapping.getAttributeName(),
                    claim.getValue(), offset, length);
        } catch (IdentityStoreConnectorException e) {
            throw new DomainException("Failed to list connector group ids", e);
        }

        if (connectorGroupIds == null || connectorGroupIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<DomainGroup> domainGroups;
        try {
            domainGroups = uniqueIdResolver.getGroups(connectorGroupIds, metaClaimMapping
                    .getIdentityStoreConnectorId(), this.id);
        } catch (UniqueIdResolverException e) {
            throw new DomainException("Failed to retrieve the unique group ids.", e);
        }

        if (domainGroups == null || domainGroups.isEmpty()) {
            throw new DomainException("Failed to retrieve the unique group ids.");
        }

        return domainGroups.stream()
                .filter(Objects::nonNull)
                .filter(uniqueUser -> !isNullOrEmpty(uniqueUser.getDomainGroupId()))
                .map(DomainGroup::getDomainGroupId)
                .collect(Collectors.toList());
    }

    public List<String> listDomainGroups(MetaClaim metaClaim, String filterPattern, int offset, int length)
            throws DomainException {

        MetaClaimMapping metaClaimMapping = claimUriToMetaClaimMappings.get(metaClaim.getClaimUri());

        if (!metaClaimMapping.isUnique()) {
            throw new DomainException("Provided claim is not unique.");
        }

        IdentityStoreConnector identityStoreConnector = identityStoreConnectorsMap
                .get(metaClaimMapping.getIdentityStoreConnectorId());

        List<String> connectorGroupIds;
        try {
            connectorGroupIds = identityStoreConnector.listConnectorGroupIdsByPattern(
                    metaClaimMapping.getAttributeName(), filterPattern, offset, length);
        } catch (IdentityStoreConnectorException e) {
            throw new DomainException("Failed to list connector group ids by pattern", e);
        }

        if (connectorGroupIds == null || connectorGroupIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<DomainGroup> domainGroups;
        try {
            domainGroups = uniqueIdResolver.getGroups(connectorGroupIds, metaClaimMapping
                    .getIdentityStoreConnectorId(), this.id);
        } catch (UniqueIdResolverException e) {
            throw new DomainException("Failed to retrieve the unique group ids.", e);
        }

        if (domainGroups == null || domainGroups.isEmpty()) {
            throw new DomainException("Failed to retrieve the unique group ids.");
        }

        return domainGroups.stream()
                .filter(Objects::nonNull)
                .filter(uniqueUser -> !isNullOrEmpty(uniqueUser.getDomainGroupId()))
                .map(DomainGroup::getDomainGroupId)
                .collect(Collectors.toList());
    }

    public List<String> getGroupsOfUser(String domainUserId) throws DomainException {

        List<DomainGroup> domainGroups;
        try {
            domainGroups = uniqueIdResolver.getGroupsOfUser(domainUserId, this.id);
        } catch (UniqueIdResolverException e) {
            throw new DomainException(String.format("Failed to retrieve the unique group ids for user id - %s.",
                    domainUserId), e);
        }

        if (domainGroups == null || domainGroups.isEmpty()) {
            return Collections.emptyList();
        }

        return domainGroups.stream()
                .filter(Objects::nonNull)
                .filter(uniqueUser -> !isNullOrEmpty(uniqueUser.getDomainGroupId()))
                .map(DomainGroup::getDomainGroupId)
                .collect(Collectors.toList());
    }

    public List<String> getUsersOfGroup(String domainGroupId) throws DomainException {

        List<DomainUser> domainUsers;
        try {
            domainUsers = uniqueIdResolver.getUsersOfGroup(domainGroupId, this.id);
        } catch (UniqueIdResolverException e) {
            throw new DomainException(String.format("Failed to retrieve the unique user ids for group id - %s.",
                    domainGroupId), e);
        }

        if (domainUsers == null || domainUsers.isEmpty()) {
            return Collections.emptyList();
        }

        return domainUsers.stream()
                .filter(Objects::nonNull)
                .filter(uniqueUser -> !isNullOrEmpty(uniqueUser.getDomainUserId()))
                .map(DomainUser::getDomainUserId)
                .collect(Collectors.toList());
    }

    public boolean isUserInGroup(String domainUserId, String domainGroupId) throws DomainException {

        try {
            return uniqueIdResolver.isUserInGroup(domainUserId, domainGroupId, this.id);
        } catch (UniqueIdResolverException e) {
            throw new DomainException("Failed to check whether user exists in group.", e);
        }
    }

    public List<Claim> getClaimsOfUser(String domainUserId) throws DomainException, UserNotFoundException {

        DomainUser domainUser;
        try {
            domainUser = uniqueIdResolver.getUser(domainUserId, this.id);
        } catch (UniqueIdResolverException e) {
            throw new DomainException(String.format("Failed to retrieve unique user - %s.", domainUserId), e);
        }

        if (domainUser == null) {
            throw new UserNotFoundException("Invalid unique user id.");
        }

        if (domainUser.getUserPartitions() == null || domainUser.getUserPartitions().isEmpty()) {
            return Collections.emptyList();
        }

        List<UserPartition> userPartitions = domainUser.getUserPartitions().stream()
                .filter(UserPartition::isIdentityStore)
                .collect(Collectors.toList());

        if (userPartitions.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, List<Attribute>> attributesMap = new HashMap<>();
        for (UserPartition userPartition : userPartitions) {
            IdentityStoreConnector identityStoreConnector = identityStoreConnectorsMap.get(userPartition
                    .getConnectorId());
            try {
                List<Attribute> attributes = identityStoreConnector.getUserAttributeValues
                        (userPartition.getConnectorUserId());
                attributesMap.put(userPartition.getConnectorId(), attributes);
            } catch (IdentityStoreConnectorException e) {
                throw new DomainException("Failed to get user attribute values", e);
            }
        }

        if (attributesMap.isEmpty()) {
            return Collections.emptyList();
        }

        return buildClaims(attributesMap);
    }

    public List<Claim> getClaimsOfUser(String domainUserId, List<MetaClaim> metaClaims)
            throws DomainException, UserNotFoundException {

        DomainUser domainUser;
        try {
            domainUser = uniqueIdResolver.getUser(domainUserId, this.id);
        } catch (UniqueIdResolverException e) {
            throw new DomainException(String.format("Failed to retrieve unique user - %s.", domainUserId), e);
        }

        if (domainUser == null) {
            throw new UserNotFoundException("Invalid unique user id.");
        }

        if (domainUser.getUserPartitions() == null || domainUser.getUserPartitions().isEmpty()) {
            return Collections.emptyList();
        }

        List<UserPartition> userPartitions = domainUser.getUserPartitions().stream()
                .filter(UserPartition::isIdentityStore)
                .collect(Collectors.toList());

        if (userPartitions.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, List<String>> attributeNamesMap = getConnectorIdToAttributeNameMap(metaClaims);

        Map<String, List<Attribute>> attributesMap = new HashMap<>();
        for (UserPartition userPartition : userPartitions) {
            List<String> attributeNames = attributeNamesMap.get(userPartition.getConnectorId());
            IdentityStoreConnector identityStoreConnector = identityStoreConnectorsMap.get(userPartition
                    .getConnectorId());
            if (attributeNames != null) {
                try {
                    List<Attribute> attributes = identityStoreConnector.getUserAttributeValues(userPartition
                            .getConnectorUserId(), attributeNames);
                    attributesMap.put(userPartition.getConnectorId(), attributes);
                } catch (IdentityStoreConnectorException e) {
                    throw new DomainException("Failed to get user attribute values.", e);
                }
            }
        }

        return buildClaims(attributesMap);
    }

    public List<Claim> getClaimsOfGroup(String domainGroupId) throws DomainException, GroupNotFoundException {

        DomainGroup domainGroup;
        try {
            domainGroup = uniqueIdResolver.getGroup(domainGroupId, this.id);
        } catch (UniqueIdResolverException e) {
            throw new DomainException(String.format("Failed to retrieve unique group - %s.",
                    domainGroupId), e);
        }

        if (domainGroup == null) {
            throw new GroupNotFoundException("Invalid unique group id.");
        }

        if (domainGroup.getGroupPartitions() == null || domainGroup.getGroupPartitions().isEmpty()) {
            return Collections.emptyList();
        }

        List<GroupPartition> groupPartitions = domainGroup.getGroupPartitions();

        if (groupPartitions.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, List<Attribute>> attributesMap = new HashMap<>();
        for (GroupPartition groupPartition : groupPartitions) {
            IdentityStoreConnector identityStoreConnector = identityStoreConnectorsMap.get(groupPartition
                    .getConnectorId());
            try {
                List<Attribute> attributes = identityStoreConnector.getGroupAttributeValues
                        (groupPartition.getConnectorGroupId());
                attributesMap.put(groupPartition.getConnectorId(), attributes);
            } catch (IdentityStoreConnectorException e) {
                throw new DomainException("Failed to get group attribute values", e);
            }
        }

        if (attributesMap.isEmpty()) {
            return Collections.emptyList();
        }

        return buildClaims(attributesMap);
    }

    public List<Claim> getClaimsOfGroup(String domainGroupId, List<MetaClaim> metaClaims)
            throws DomainException, GroupNotFoundException {

        DomainGroup domainGroup;
        try {
            domainGroup = uniqueIdResolver.getGroup(domainGroupId, this.id);
        } catch (UniqueIdResolverException e) {
            throw new DomainException(String.format("Failed to retrieve unique group - %s.",
                    domainGroupId), e);
        }

        if (domainGroup == null) {
            throw new GroupNotFoundException("Invalid unique group id.");
        }

        if (domainGroup.getGroupPartitions() == null || domainGroup.getGroupPartitions().isEmpty()) {
            return Collections.emptyList();
        }

        List<GroupPartition> groupPartitions = domainGroup.getGroupPartitions();

        if (groupPartitions.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, List<String>> attributeNamesMap = getConnectorIdToAttributeNameMap(metaClaims);

        Map<String, List<Attribute>> attributesMap = new HashMap<>();
        for (GroupPartition groupPartition : groupPartitions) {
            List<String> attributeNames = attributeNamesMap.get(groupPartition.getConnectorId());
            IdentityStoreConnector identityStoreConnector = identityStoreConnectorsMap.get(groupPartition
                    .getConnectorId());
            if (attributeNames != null) {
                try {
                    List<Attribute> attributes = identityStoreConnector.getGroupAttributeValues(groupPartition
                            .getConnectorGroupId(), attributeNames);
                    attributesMap.put(groupPartition.getConnectorId(), attributes);
                } catch (IdentityStoreConnectorException e) {
                    throw new DomainException("Failed to get group attribute values.", e);
                }
            }
        }

        return buildClaims(attributesMap);
    }

    public String addUser(UserBean userBean) throws DomainException {

        List<UserPartition> userPartitions = new ArrayList<>();

        if (!userBean.getClaims().isEmpty()) {

            Map<String, List<Attribute>> attributesMap = getAttributesMap(userBean.getClaims());

            for (Map.Entry<String, List<Attribute>> entry : attributesMap.entrySet()) {
                IdentityStoreConnector identityStoreConnector = identityStoreConnectorsMap.get(entry.getKey());
                String connectorUserId;
                try {
                    connectorUserId = identityStoreConnector.addUser(entry.getValue());
                } catch (IdentityStoreConnectorException e) {
                    // Recover from the inconsistent state in the connectors
                    if (userPartitions.size() > 0) {
                        removeAddedUsersInAFailure(userPartitions);
                    }
                    throw new DomainException("Identity store connector failed to add user attributes.", e);
                }

                userPartitions.add(new UserPartition(entry.getKey(), connectorUserId, true));
            }
        }

        if (!userBean.getCredentials().isEmpty()) {
            Map<String, List<Callback>> credentialsMap = getCredentialMap(userBean.getCredentials());
            for (Map.Entry<String, List<Callback>> entry : credentialsMap.entrySet()) {
                CredentialStoreConnector credentialStoreConnector = credentialStoreConnectorsMap.get(entry.getKey());
                String connectorUserId;
                try {
                    connectorUserId = credentialStoreConnector.addCredential(entry.getValue());
                } catch (CredentialStoreConnectorException e) {
                    // Recover from the inconsistent state in the connectors
                    if (userPartitions.size() > 0) {
                        removeAddedUsersInAFailure(userPartitions);
                    }
                    throw new DomainException("Credential store connector failed to add user attributes" +
                            ".", e);
                }

                userPartitions.add(new UserPartition(entry.getKey(), connectorUserId, false));
            }
        }

        String userUniqueId = IdentityUserMgtUtil.generateUUID();
        try {
            String receivedUserUniqueId = uniqueIdResolver.addUser(new DomainUser(userUniqueId, userPartitions),
                    this.id);

            if (isNullOrEmpty(receivedUserUniqueId)) {
                return receivedUserUniqueId;
            }
        } catch (UniqueIdResolverException e) {
            // Recover from the inconsistent state in the connectors
            removeAddedUsersInAFailure(userPartitions);
            throw new DomainException("Error occurred while persisting user unique id.", e);
        }

        return userUniqueId;
    }

    public List<String> addUsers(List<UserBean> userBeen) throws DomainException {

        // Assign unique user id for each user bean
        Map<String, UserBean> userModelMap = userBeen.stream()
                .filter(Objects::nonNull)
                .filter(userModel -> !userModel.getClaims().isEmpty() || !userModel.getCredentials().isEmpty())
                .collect(Collectors.toMap(userModel -> IdentityUserMgtUtil.generateUUID(), userModel -> userModel));

        Map<String, Map<String, List<Attribute>>> attributesMap = getAttributesMapOfUsers(userModelMap);

        Map<String, Map<String, List<Callback>>> credentialMap = getCredentialsMapOfUsers(userModelMap);

        Map<String, List<UserPartition>> userPartitionsMapOfUsers = new HashMap<>();
        if (!attributesMap.isEmpty()) {
            for (Map.Entry<String, Map<String, List<Attribute>>> entry : attributesMap.entrySet()) {

                IdentityStoreConnector identityStoreConnector = identityStoreConnectorsMap.get(entry.getKey());
                Map<String, String> uniqueUserIds;
                try {
                    uniqueUserIds = identityStoreConnector.addUsers(entry.getValue());
                } catch (IdentityStoreConnectorException e) {
                    if (!userPartitionsMapOfUsers.isEmpty()) {
                        userPartitionsMapOfUsers.entrySet().stream()
                                .forEach(partitionEntry -> removeAddedUsersInAFailure(partitionEntry.getValue()));
                    }
                    throw new DomainException("Failed to add users.", e);
                }

                if (uniqueUserIds != null) {
                    uniqueUserIds.entrySet().stream()
                            .forEach(uniqueUserId -> {
                                List<UserPartition> userPartitions = userPartitionsMapOfUsers.get(uniqueUserId
                                        .getKey());
                                if (userPartitions == null) {
                                    userPartitions = new ArrayList<>();
                                    userPartitionsMapOfUsers.put(uniqueUserId.getKey(), userPartitions);
                                }
                                userPartitions.add(new UserPartition(entry.getKey(), uniqueUserId.getValue(), true));
                            });
                }
            }
        }

        if (!credentialMap.isEmpty()) {
            for (Map.Entry<String, Map<String, List<Callback>>> entry : credentialMap
                    .entrySet()) {

                CredentialStoreConnector credentialStoreConnector = credentialStoreConnectorsMap.get(entry.getKey());
                Map<String, String> uniqueUserIds;
                try {
                    uniqueUserIds = credentialStoreConnector.addCredentials(entry.getValue());
                } catch (CredentialStoreConnectorException e) {
                    if (!userPartitionsMapOfUsers.isEmpty()) {
                        userPartitionsMapOfUsers.entrySet().stream()
                                .forEach(partitionEntry -> removeAddedUsersInAFailure(partitionEntry.getValue()));
                    }
                    throw new DomainException("Failed to add users.", e);
                }

                if (uniqueUserIds != null) {
                    uniqueUserIds.entrySet().stream()
                            .forEach(uniqueUserId -> {
                                List<UserPartition> userPartitions = userPartitionsMapOfUsers.get(uniqueUserId
                                        .getKey());
                                if (userPartitions == null) {
                                    userPartitions = new ArrayList<>();
                                    userPartitionsMapOfUsers.put(uniqueUserId.getKey(), userPartitions);
                                }
                                userPartitions.add(new UserPartition(entry.getKey(), uniqueUserId.getValue(), false));
                            });
                }
            }
        }

        if (userPartitionsMapOfUsers.isEmpty()) {
            return Collections.emptyList();
        }

        List<DomainUser> domainUsers = userPartitionsMapOfUsers.entrySet().stream()
                .map(entry -> new DomainUser(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        try {
            List<String> domainUserIds = uniqueIdResolver.addUsers(domainUsers, this.id);
            if (domainUserIds != null && !domainUserIds.isEmpty()) {
                return domainUserIds;
            }
        } catch (UniqueIdResolverException e) {
            if (!userPartitionsMapOfUsers.isEmpty()) {
                userPartitionsMapOfUsers.entrySet().stream()
                        .forEach(partitionEntry -> removeAddedUsersInAFailure(partitionEntry.getValue()));
            }
            throw new DomainException("Error occurred while persisting user unique ids.", e);
        }

        return domainUsers.stream()
                .map(DomainUser::getDomainUserId)
                .collect(Collectors.toList());
    }

    public void updateUserClaims(String domainUserId, List<Claim> claims) throws DomainException,
            UserNotFoundException {

        DomainUser domainUser;
        try {
            domainUser = uniqueIdResolver.getUser(domainUserId, this.id);
        } catch (UniqueIdResolverException e) {
            throw new DomainException(String.format("Failed to retrieve unique user - %s.", domainUserId), e);
        }

        if (domainUser == null) {
            throw new UserNotFoundException("Invalid unique user id.");
        }

        Map<String, String> connectorUserIdMap = new HashMap<>();

        if (!domainUser.getUserPartitions().isEmpty()) {
            connectorUserIdMap.putAll(domainUser.getUserPartitions().stream()
                    .filter(UserPartition::isIdentityStore)
                    .collect(Collectors.toMap(UserPartition::getConnectorId, UserPartition::getConnectorUserId)));
        }

        Map<String, String> updatedConnectorUserIdMap = new HashMap<>();

        if ((claims == null || claims.isEmpty()) && !connectorUserIdMap.isEmpty()) {
            for (Map.Entry<String, String> entry : connectorUserIdMap.entrySet()) {
                IdentityStoreConnector identityStoreConnector = identityStoreConnectorsMap.get(entry.getKey());
                String updatedConnectorUserId;
                try {
                    updatedConnectorUserId = identityStoreConnector.updateUserAttributes(entry.getValue(),
                            new ArrayList<>());
                } catch (IdentityStoreConnectorException e) {
                    throw new DomainException("Failed to update attributes of user.", e);
                }
                updatedConnectorUserIdMap.put(entry.getKey(), updatedConnectorUserId);
            }
        } else {

            Map<String, List<Attribute>> attributesMap = getAttributesMap(claims);

            Map<String, String> tempConnectorUserIdMap = attributesMap.keySet().stream()
                    .collect(Collectors.toMap(connectorId -> connectorId, connectorId -> ""));

            tempConnectorUserIdMap.putAll(connectorUserIdMap);

            for (Map.Entry<String, String> entry : tempConnectorUserIdMap.entrySet()) {

                IdentityStoreConnector identityStoreConnector = identityStoreConnectorsMap.get(entry.getKey());
                String updatedConnectorUserId;
                if (isNullOrEmpty(entry.getValue())) {
                    try {
                        updatedConnectorUserId = identityStoreConnector.addUser(attributesMap.get(entry.getKey()));
                    } catch (IdentityStoreConnectorException e) {
                        throw new DomainException("Identity store connector failed to add user " +
                                "attributes.", e);
                    }
                } else {
                    try {
                        updatedConnectorUserId = identityStoreConnector.updateUserAttributes(entry.getValue(),
                                attributesMap.get(entry.getKey()));
                    } catch (IdentityStoreConnectorException e) {
                        throw new DomainException("Failed to update user attributes.", e);
                    }
                }
                updatedConnectorUserIdMap.put(entry.getKey(), updatedConnectorUserId);
            }
        }

        if (!connectorUserIdMap.equals(updatedConnectorUserIdMap)) {
            try {
                uniqueIdResolver.updateUser(domainUserId, updatedConnectorUserIdMap, this.id);
            } catch (UniqueIdResolverException e) {
                throw new DomainException("Failed to update user connector ids.", e);
            }
        }
    }

    public void updateUserClaims(String domainUserId, List<Claim> claimsToUpdate, List<Claim> claimsToRemove)
            throws DomainException, UserNotFoundException {

        DomainUser domainUser;
        try {
            domainUser = uniqueIdResolver.getUser(domainUserId, this.id);
        } catch (UniqueIdResolverException e) {
            throw new DomainException(String.format("Failed to retrieve unique user - %s.", domainUserId), e);
        }

        if (domainUser == null) {
            throw new UserNotFoundException("Invalid unique user id.");
        }

        Map<String, String> connectorUserIdMap = new HashMap<>();

        if (!domainUser.getUserPartitions().isEmpty()) {
            connectorUserIdMap.putAll(domainUser.getUserPartitions().stream()
                    .filter(UserPartition::isIdentityStore)
                    .collect(Collectors.toMap(UserPartition::getConnectorId, UserPartition::getConnectorUserId)));
        }

        Map<String, List<Attribute>> attributesMapToUpdate = getAttributesMap(claimsToUpdate);
        Map<String, List<Attribute>> attributeMapToRemove = getAttributesMap(claimsToRemove);

        Set<String> connectorIds = new HashSet<>();

        if (!attributesMapToUpdate.isEmpty()) {
            connectorIds.addAll(attributesMapToUpdate.keySet());
        }

        if (!attributeMapToRemove.isEmpty()) {
            connectorIds.addAll(attributeMapToRemove.keySet());
        }

        Map<String, String> updatedConnectorUserIds = new HashMap<>();

        for (String connectorId : connectorIds) {
            IdentityStoreConnector identityStoreConnector = identityStoreConnectorsMap.get(connectorId);
            String updatedConnectorUserId;
            if (isNullOrEmpty(connectorUserIdMap.get(connectorId))) {
                if (attributesMapToUpdate.get(connectorId) != null) {
                    try {
                        updatedConnectorUserId = identityStoreConnector.addUser(attributesMapToUpdate.get(connectorId));
                    } catch (IdentityStoreConnectorException e) {
                        throw new DomainException("Identity store connector failed to add user " +
                                "attributes.", e);
                    }
                    updatedConnectorUserIds.put(connectorId, updatedConnectorUserId);
                }
            } else {
                try {
                    updatedConnectorUserId = identityStoreConnector.updateUserAttributes(
                            connectorUserIdMap.get(connectorId),
                            attributesMapToUpdate.get(connectorId) != null ? attributesMapToUpdate.get(connectorId) :
                                    Collections.emptyList(),
                            attributeMapToRemove.get(connectorId) != null ? attributeMapToRemove.get(connectorId) :
                                    Collections.emptyList());
                } catch (IdentityStoreConnectorException e) {
                    throw new DomainException("Failed to update user attributes", e);
                }
                updatedConnectorUserIds.put(connectorId, updatedConnectorUserId);
            }
        }

        if (!connectorUserIdMap.equals(updatedConnectorUserIds)) {
            try {
                uniqueIdResolver.updateUser(domainUserId, updatedConnectorUserIds, this.id);
            } catch (UniqueIdResolverException e) {
                throw new DomainException("Failed to update user connector ids.", e);
            }
        }
    }

    public void updateUserCredentials(String domainUserId, List<Callback> callbacks) throws DomainException,
            UserNotFoundException {

        DomainUser domainUser;
        try {
            domainUser = uniqueIdResolver.getUser(domainUserId, this.id);
        } catch (UniqueIdResolverException e) {
            throw new DomainException(String.format("Failed to retrieve unique user - %s.", domainUserId), e);
        }

        if (domainUser == null) {
            throw new UserNotFoundException("Invalid unique user id.");
        }

        Map<String, String> connectorUserIdMap = new HashMap<>();

        if (!domainUser.getUserPartitions().isEmpty()) {
            connectorUserIdMap.putAll(domainUser.getUserPartitions().stream()
                    .filter(userPartition -> !userPartition.isIdentityStore())
                    .collect(Collectors.toMap(UserPartition::getConnectorId, UserPartition::getConnectorUserId)));
        }

        Map<String, String> updatedConnectorUserIdMap = new HashMap<>();

        if ((callbacks == null || callbacks.isEmpty()) && !connectorUserIdMap.isEmpty()) {
            for (Map.Entry<String, String> entry : connectorUserIdMap.entrySet()) {
                CredentialStoreConnector credentialStoreConnector = credentialStoreConnectorsMap.get(entry.getKey());
                String updatedConnectorUserId;
                try {
                    updatedConnectorUserId = credentialStoreConnector.updateCredentials(entry.getValue(),
                            new ArrayList<>());
                } catch (CredentialStoreConnectorException e) {
                    throw new DomainException("Failed to update credentials of user.", e);
                }
                updatedConnectorUserIdMap.put(entry.getKey(), updatedConnectorUserId);
            }
        } else {

            Map<String, List<Callback>> credentialMap = getCredentialMap(callbacks);

            Map<String, String> tempConnectorUserIdMap = credentialMap.keySet().stream()
                    .collect(Collectors.toMap(connectorId -> connectorId, connectorId -> ""));

            tempConnectorUserIdMap.putAll(connectorUserIdMap);

            for (Map.Entry<String, String> entry : tempConnectorUserIdMap.entrySet()) {

                CredentialStoreConnector credentialStoreConnector = credentialStoreConnectorsMap.get(entry.getKey());
                String updatedConnectorUserId;
                if (isNullOrEmpty(entry.getValue())) {
                    try {
                        updatedConnectorUserId = credentialStoreConnector.addCredential(credentialMap.get(entry
                                .getKey()));
                    } catch (CredentialStoreConnectorException e) {
                        throw new DomainException("Credential store connector failed to add user " +
                                "credentials.", e);
                    }
                } else {
                    try {
                        updatedConnectorUserId = credentialStoreConnector.updateCredentials(entry.getValue(),
                                credentialMap.get(entry.getKey()));
                    } catch (CredentialStoreConnectorException e) {
                        throw new DomainException("Failed to update user credentials.", e);
                    }
                }
                updatedConnectorUserIdMap.put(entry.getKey(), updatedConnectorUserId);
            }
        }

        if (!connectorUserIdMap.equals(updatedConnectorUserIdMap)) {
            try {
                uniqueIdResolver.updateUser(domainUserId, updatedConnectorUserIdMap, this.id);
            } catch (UniqueIdResolverException e) {
                throw new DomainException("Failed to update user connector ids.", e);
            }
        }
    }

    public void updateUserCredentials(String domainUserId, List<Callback> credentialsToUpdate,
                                      List<Callback> credentialsToRemove) throws DomainException,
            UserNotFoundException {

        DomainUser domainUser;
        try {
            domainUser = uniqueIdResolver.getUser(domainUserId, this.id);
        } catch (UniqueIdResolverException e) {
            throw new DomainException(String.format("Failed to retrieve unique user - %s.", domainUserId), e);
        }

        if (domainUser == null) {
            throw new UserNotFoundException("Invalid unique user id.");
        }

        Map<String, String> connectorUserIdMap = new HashMap<>();

        if (!domainUser.getUserPartitions().isEmpty()) {
            connectorUserIdMap.putAll(domainUser.getUserPartitions().stream()
                    .filter(userPartition -> !userPartition.isIdentityStore())
                    .collect(Collectors.toMap(UserPartition::getConnectorId, UserPartition::getConnectorUserId)));
        }

        Map<String, List<Callback>> credentialMapToUpdate = getCredentialMap(credentialsToUpdate);
        Map<String, List<Callback>> credentialMapToRemove = getCredentialMap(credentialsToRemove);

        Set<String> connectorIds = new HashSet<>();

        if (!credentialMapToUpdate.isEmpty()) {
            connectorIds.addAll(credentialMapToUpdate.keySet());
        }

        if (!credentialMapToRemove.isEmpty()) {
            connectorIds.addAll(credentialMapToRemove.keySet());
        }

        Map<String, String> updatedConnectorUserIds = new HashMap<>();

        for (String connectorId : connectorIds) {
            CredentialStoreConnector credentialStoreConnector = credentialStoreConnectorsMap.get(connectorId);
            String updatedConnectorUserId;
            if (isNullOrEmpty(connectorUserIdMap.get(connectorId))) {
                if (credentialMapToUpdate.get(connectorId) != null) {
                    try {
                        updatedConnectorUserId = credentialStoreConnector.addCredential(credentialMapToUpdate
                                .get(connectorId));
                    } catch (CredentialStoreConnectorException e) {
                        throw new DomainException("Credential store connector failed to add user " +
                                "credentials.", e);
                    }
                    updatedConnectorUserIds.put(connectorId, updatedConnectorUserId);
                }
            } else {
                try {
                    updatedConnectorUserId = credentialStoreConnector.updateCredentials(
                            connectorUserIdMap.get(connectorId),
                            credentialMapToUpdate.get(connectorId),
                            credentialMapToRemove.get(connectorId));
                } catch (CredentialStoreConnectorException e) {
                    throw new DomainException("Failed to update user credentials", e);
                }
                updatedConnectorUserIds.put(connectorId, updatedConnectorUserId);
            }
        }

        if (!connectorUserIdMap.equals(updatedConnectorUserIds)) {
            try {
                uniqueIdResolver.updateUser(domainUserId, updatedConnectorUserIds, this.id);
            } catch (UniqueIdResolverException e) {
                throw new DomainException("Failed to update user connector ids.", e);
            }
        }
    }

    public void deleteUser(String domainUserId) throws DomainException, UserNotFoundException {

        DomainUser domainUser;
        try {
            domainUser = uniqueIdResolver.getUser(domainUserId, this.id);
        } catch (UniqueIdResolverException e) {
            throw new DomainException(String.format("Failed to retrieve unique user - %s.", domainUserId), e);
        }

        if (domainUser == null) {
            throw new UserNotFoundException("Invalid unique user id.");
        }

        List<UserPartition> userPartitions = domainUser.getUserPartitions();

        if (!userPartitions.isEmpty()) {
            for (UserPartition userPartition : userPartitions) {
                if (userPartition.isIdentityStore()) {
                    IdentityStoreConnector identityStoreConnector = identityStoreConnectorsMap
                            .get(userPartition.getConnectorId());
                    try {
                        identityStoreConnector.deleteUser(userPartition.getConnectorUserId());
                    } catch (IdentityStoreConnectorException e) {
                        throw new DomainException("Failed to delete user", e);
                    }
                } else {
                    CredentialStoreConnector credentialStoreConnector = credentialStoreConnectorsMap
                            .get(userPartition.getConnectorId());
                    try {
                        credentialStoreConnector.deleteCredential(userPartition.getConnectorUserId());
                    } catch (CredentialStoreConnectorException e) {
                        throw new DomainException(String.format("Failed to delete credential entry in " +
                                "connector - %s with id - %s", userPartition.getConnectorId(), userPartition
                                .getConnectorUserId()));
                    }
                }
            }
        }

        try {
            uniqueIdResolver.deleteUser(domainUserId, this.id);
        } catch (UniqueIdResolverException e) {
            throw new DomainException(String.format("Failed to delete unique user id - %s.", domainUserId));
        }
    }

    public void updateGroupsOfUser(String domainUserId, List<String> domainGroupIds) throws DomainException {

        try {
            uniqueIdResolver.updateGroupsOfUser(domainUserId, domainGroupIds, this.id);
        } catch (UniqueIdResolverException e) {
            throw new DomainException("Failed update groups of user", e);
        }
    }

    public void updateGroupsOfUser(String domainUserId, List<String> domainGroupIdsToUpdate,
                                   List<String> domainGroupIdsToRemove) throws DomainException {

        try {
            uniqueIdResolver.updateGroupsOfUser(domainUserId, domainGroupIdsToUpdate, domainGroupIdsToRemove, this.id);
        } catch (UniqueIdResolverException e) {
            throw new DomainException("Failed update groups of user", e);
        }
    }

    public String addGroup(GroupBean groupBean) throws DomainException {

        Map<String, List<Attribute>> attributesMap = getAttributesMap(groupBean.getClaims());

        List<GroupPartition> groupPartitions = new ArrayList<>();

        for (Map.Entry<String, List<Attribute>> entry : attributesMap.entrySet()) {
            IdentityStoreConnector identityStoreConnector = identityStoreConnectorsMap.get(entry.getKey());
            String connectorGroupId;
            try {
                connectorGroupId = identityStoreConnector.addGroup(entry.getValue());
            } catch (IdentityStoreConnectorException e) {
                // Recover from the inconsistent state in the connectors
                if (groupPartitions.size() > 0) {
                    removeAddedGroupsInAFailure(groupPartitions);
                }
                throw new DomainException("Identity store connector failed to add user attributes.", e);
            }

            groupPartitions.add(new GroupPartition(entry.getKey(), connectorGroupId));
        }


        String groupUniqueId = IdentityUserMgtUtil.generateUUID();
        try {
            String receivedGroupUniqueId = uniqueIdResolver.addGroup(new DomainGroup(groupUniqueId, groupPartitions),
                    this.id);
            if (!isNullOrEmpty(receivedGroupUniqueId)) {
                return receivedGroupUniqueId;
            }
        } catch (UniqueIdResolverException e) {
            // Recover from the inconsistent state in the connectors
            removeAddedGroupsInAFailure(groupPartitions);
            throw new DomainException("Error occurred while persisting user unique id.", e);
        }

        return groupUniqueId;
    }

    public List<String> addGroups(List<GroupBean> groupBeen) throws DomainException {

        Map<String, GroupBean> groupModelMap = groupBeen.stream()
                .filter(Objects::nonNull)
                .filter(groupModel -> !groupModel.getClaims().isEmpty())
                .collect(Collectors.toMap(groupModel -> IdentityUserMgtUtil.generateUUID(), groupModel -> groupModel));

        Map<String, Map<String, List<Attribute>>> attributesMap = getAttributesMapOfGroups(groupModelMap);

        Map<String, List<GroupPartition>> groupPartitionMapOfGroups = new HashMap<>();
        if (!attributesMap.isEmpty()) {
            for (Map.Entry<String, Map<String, List<Attribute>>> entry : attributesMap
                    .entrySet()) {

                IdentityStoreConnector identityStoreConnector = identityStoreConnectorsMap.get(entry.getKey());
                Map<String, String> uniqueGroupIds;
                try {
                    uniqueGroupIds = identityStoreConnector.addGroups(entry.getValue());
                } catch (IdentityStoreConnectorException e) {
                    if (!groupPartitionMapOfGroups.isEmpty()) {
                        groupPartitionMapOfGroups.entrySet().stream()
                                .forEach(partitionEntry -> removeAddedGroupsInAFailure(partitionEntry.getValue()));
                    }
                    throw new DomainException("Failed to add groups.", e);
                }
                if (uniqueGroupIds != null) {
                    uniqueGroupIds.entrySet().stream()
                            .forEach(uniqueGroupId -> {
                                List<GroupPartition> groupPartitions = groupPartitionMapOfGroups
                                        .get(uniqueGroupId.getKey());
                                if (groupPartitions == null) {
                                    groupPartitions = new ArrayList<>();
                                    groupPartitionMapOfGroups.put(uniqueGroupId.getKey(), groupPartitions);
                                }
                                groupPartitions.add(new GroupPartition(entry.getKey(), uniqueGroupId.getValue()));
                            });
                }
            }
        }

        if (groupPartitionMapOfGroups.isEmpty()) {
            return Collections.emptyList();
        }

        List<DomainGroup> domainGroups = groupPartitionMapOfGroups.entrySet().stream()
                .map(entry -> new DomainGroup(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        try {

            List<String> domainGroupIds = uniqueIdResolver.addGroups(domainGroups, this.id);
            if (domainGroupIds != null && !domainGroupIds.isEmpty()) {
                return domainGroupIds;
            }
        } catch (UniqueIdResolverException e) {
            if (!groupPartitionMapOfGroups.isEmpty()) {
                groupPartitionMapOfGroups.entrySet().stream()
                        .forEach(partitionEntry -> removeAddedGroupsInAFailure(partitionEntry.getValue()));
            }
            throw new DomainException("Error occurred while persisting group unique ids.", e);
        }

        return domainGroups.stream()
                .map(DomainGroup::getDomainGroupId)
                .collect(Collectors.toList());
    }

    public void updateGroupClaims(String domainGroupId, List<Claim> claims) throws DomainException,
            GroupNotFoundException {

        DomainGroup domainGroup;
        try {
            domainGroup = uniqueIdResolver.getGroup(domainGroupId, this.id);
        } catch (UniqueIdResolverException e) {
            throw new DomainException(String.format("Failed to retrieve unique group - %s.",
                    domainGroupId), e);
        }

        if (domainGroup == null) {
            throw new GroupNotFoundException("Invalid unique group id.");
        }

        Map<String, String> connectorGroupIdMap = new HashMap<>();

        if (!domainGroup.getGroupPartitions().isEmpty()) {
            connectorGroupIdMap.putAll(domainGroup.getGroupPartitions().stream()
                    .collect(Collectors.toMap(GroupPartition::getConnectorId, GroupPartition::getConnectorGroupId)));
        }

        Map<String, String> updatedConnectorGroupIdMap = new HashMap<>();

        if ((claims == null || claims.isEmpty()) && !connectorGroupIdMap.isEmpty()) {
            for (Map.Entry<String, String> entry : connectorGroupIdMap.entrySet()) {
                IdentityStoreConnector identityStoreConnector = identityStoreConnectorsMap.get(entry.getKey());
                String updatedConnectorGroupId;
                try {
                    updatedConnectorGroupId = identityStoreConnector.updateGroupAttributes(entry.getValue(),
                            new ArrayList<>());
                } catch (IdentityStoreConnectorException e) {
                    throw new DomainException("Failed to update group attributes.", e);
                }
                updatedConnectorGroupIdMap.put(entry.getKey(), updatedConnectorGroupId);
            }
        } else {

            Map<String, List<Attribute>> attributesMap = getAttributesMap(claims);

            Map<String, String> tempConnectorIdMap = attributesMap.keySet().stream()
                    .collect(Collectors.toMap(connectorId -> connectorId, connectorId -> ""));

            tempConnectorIdMap.putAll(connectorGroupIdMap);

            for (Map.Entry<String, String> entry : tempConnectorIdMap.entrySet()) {
                IdentityStoreConnector identityStoreConnector = identityStoreConnectorsMap.get(entry.getKey());
                String updatedConnectorGroupId;
                if (isNullOrEmpty(entry.getValue())) {
                    try {
                        updatedConnectorGroupId = identityStoreConnector.addGroup(attributesMap.get(entry.getKey()));
                    } catch (IdentityStoreConnectorException e) {
                        throw new DomainException("Identity store connector failed to add group attributes.", e);
                    }
                } else {
                    try {
                        updatedConnectorGroupId = identityStoreConnector.updateGroupAttributes(entry.getValue(),
                                attributesMap.get(entry.getKey()));
                    } catch (IdentityStoreConnectorException e) {
                        throw new DomainException("Failed to update group attributes.", e);
                    }
                }
                updatedConnectorGroupIdMap.put(entry.getKey(), updatedConnectorGroupId);
            }
        }

        if (!connectorGroupIdMap.equals(updatedConnectorGroupIdMap)) {
            try {
                uniqueIdResolver.updateGroup(domainGroupId, updatedConnectorGroupIdMap, this.id);
            } catch (UniqueIdResolverException e) {
                throw new DomainException("Failed to update group connector ids.", e);
            }
        }
    }

    public void updateGroupClaims(String domainGroupId, List<Claim> claimsToUpdate, List<Claim> claimsToRemove)
            throws DomainException, GroupNotFoundException {

        DomainGroup domainGroup;
        try {
            domainGroup = uniqueIdResolver.getGroup(domainGroupId, this.id);
        } catch (UniqueIdResolverException e) {
            throw new DomainException(String.format("Failed to retrieve unique group - %s.",
                    domainGroupId), e);
        }

        if (domainGroup == null) {
            throw new GroupNotFoundException("Invalid unique group id.");
        }

        Map<String, String> connectorGroupIdMap = new HashMap<>();

        if (!domainGroup.getGroupPartitions().isEmpty()) {
            connectorGroupIdMap.putAll(domainGroup.getGroupPartitions().stream()
                    .collect(Collectors.toMap(GroupPartition::getConnectorId, GroupPartition::getConnectorGroupId)));
        }

        Map<String, List<Attribute>> attributesMapToUpdate = getAttributesMap(claimsToUpdate);
        Map<String, List<Attribute>> attributesMapToRemove = getAttributesMap(claimsToRemove);

        Set<String> connectorIds = new HashSet<>();

        if (!attributesMapToUpdate.isEmpty()) {
            connectorIds.addAll(attributesMapToUpdate.keySet());
        }

        if (!attributesMapToRemove.isEmpty()) {
            connectorIds.addAll(attributesMapToRemove.keySet());
        }

        Map<String, String> updatedConnectorGroupIdMap = new HashMap<>();

        for (String connectorId : connectorIds) {
            IdentityStoreConnector identityStoreConnector = identityStoreConnectorsMap.get(connectorId);
            String updatedConnectorGroupId;
            if (isNullOrEmpty(connectorGroupIdMap.get(connectorId))) {
                if (attributesMapToUpdate.get(connectorId) != null) {
                    try {
                        updatedConnectorGroupId = identityStoreConnector.addGroup(attributesMapToUpdate
                                .get(connectorId));
                    } catch (IdentityStoreConnectorException e) {
                        throw new DomainException("Identity store connector failed to add group attributes.", e);
                    }
                    updatedConnectorGroupIdMap.put(connectorId, updatedConnectorGroupId);
                }
            } else {
                try {
                    updatedConnectorGroupId = identityStoreConnector.updateGroupAttributes(
                            connectorGroupIdMap.get(connectorId),
                            attributesMapToUpdate.get(connectorId),
                            attributesMapToRemove.get(connectorId));
                } catch (IdentityStoreConnectorException e) {
                    throw new DomainException("Failed to update group attributes.", e);
                }
                updatedConnectorGroupIdMap.put(connectorId, updatedConnectorGroupId);
            }

        }

        if (!connectorGroupIdMap.equals(updatedConnectorGroupIdMap)) {
            try {
                uniqueIdResolver.updateGroup(domainGroupId, updatedConnectorGroupIdMap, this.id);
            } catch (UniqueIdResolverException e) {
                throw new DomainException("Failed to update group connector ids.", e);
            }
        }
    }

    public void deleteGroup(String domainGroupId) throws DomainException, GroupNotFoundException {

        DomainGroup domainGroup;
        try {
            domainGroup = uniqueIdResolver.getGroup(domainGroupId, this.id);
        } catch (UniqueIdResolverException e) {
            throw new DomainException(String.format("Failed to retrieve unique group - %s.", domainGroupId), e);
        }

        if (domainGroup == null) {
            throw new GroupNotFoundException("Invalid unique group id.");
        }

        List<GroupPartition> groupPartitions = domainGroup.getGroupPartitions();

        if (!groupPartitions.isEmpty()) {
            for (GroupPartition groupPartition : groupPartitions) {
                IdentityStoreConnector identityStoreConnector = identityStoreConnectorsMap.get(groupPartition
                        .getConnectorId());
                try {
                    identityStoreConnector.deleteGroup(groupPartition.getConnectorGroupId());
                } catch (IdentityStoreConnectorException e) {
                    throw new DomainException(String.format("Failed to delete user entry in connector - %s with id - " +
                            "%s", groupPartition.getConnectorId(), groupPartition.getConnectorGroupId()));
                }
            }
        }

        try {
            uniqueIdResolver.deleteGroup(domainGroupId, this.id);
        } catch (UniqueIdResolverException e) {
            throw new DomainException(String.format("Failed to delete unique user id - %s.", domainGroupId));
        }
    }

    public void updateUsersOfGroup(String domainGroupId, List<String> domainUserIds) throws DomainException {

        try {
            uniqueIdResolver.updateUsersOfGroup(domainGroupId, domainUserIds, this.id);
        } catch (UniqueIdResolverException e) {
            throw new DomainException("Failed update users of group.", e);
        }
    }

    public void updateUsersOfGroup(String domainGroupId, List<String> domainUserIdsToUpdate,
                                   List<String> domainUserIdsToRemove) throws DomainException {

        try {
            uniqueIdResolver.updateGroupsOfUser(domainGroupId, domainUserIdsToUpdate, domainUserIdsToRemove, this.id);
        } catch (UniqueIdResolverException e) {
            throw new DomainException("Failed update users of group.", e);
        }
    }

    public String authenticate(Claim claim, Callback[] credentials) throws AuthenticationFailure {

        MetaClaimMapping metaClaimMapping = claimUriToMetaClaimMappings.get(claim.getClaimUri());

        if (!metaClaimMapping.isUnique()) {
            throw new AuthenticationFailure("Provided claim is not unique.");
        }

        IdentityStoreConnector identityStoreConnector = identityStoreConnectorsMap.get(metaClaimMapping
                .getIdentityStoreConnectorId());
        String connectorUserId;
        try {
            connectorUserId = identityStoreConnector.getConnectorUserId(metaClaimMapping.getAttributeName(),
                    claim.getValue());
        } catch (UserNotFoundException | IdentityStoreConnectorException e) {
            throw new AuthenticationFailure("Invalid claim value. No user mapped to the provided claim.", e);
        }

        DomainUser domainUser;
        try {
            domainUser = uniqueIdResolver.getUserFromConnectorUserId(connectorUserId, metaClaimMapping
                    .getIdentityStoreConnectorId(), this.id);
        } catch (UniqueIdResolverException e) {
            throw new AuthenticationFailure("Failed retrieve unique user info.", e);
        }

        for (UserPartition userPartition : domainUser.getUserPartitions()) {
            if (!userPartition.isIdentityStore()) {
                CredentialStoreConnector connector = credentialStoreConnectorsMap.get(userPartition.getConnectorId());
                if (connector.canHandle(credentials)) {
                    try {
                        connector.authenticate(userPartition.getConnectorUserId(), credentials);
                        return domainUser.getDomainUserId();
                    } catch (CredentialStoreConnectorException e) {
                        throw new AuthenticationFailure("Failed to authenticate from the provided credential.", e);
                    }
                }
            }
        }

        throw new AuthenticationFailure("Failed to authenticate user.");
    }

    public MetaClaimMapping getMetaClaimMapping(String claimURI) throws DomainException {

        if (claimUriToMetaClaimMappings.isEmpty()) {
            throw new DomainException("Invalid domain configuration found. No meta claim mappings.");
        }

        return claimUriToMetaClaimMappings.get(claimURI);
    }

    public List<MetaClaimMapping> getMetaClaimMappings() throws DomainException {

        if (metaClaimMappings.isEmpty()) {
            throw new DomainException("Invalid domain configuration found. No meta claim mappings.");
        }

        return metaClaimMappings;
    }

    public void setMetaClaimMappings(List<MetaClaimMapping> metaClaimMappings) {

        if (metaClaimMappings == null || metaClaimMappings.isEmpty()) {
            this.metaClaimMappings.clear();
            this.claimUriToMetaClaimMappings.clear();
            return;
        }

        this.metaClaimMappings = metaClaimMappings;
        this.claimUriToMetaClaimMappings = this.metaClaimMappings.stream()
                .collect(Collectors.toMap(metaClaimMapping -> metaClaimMapping.getMetaClaim().getClaimUri(),
                        metaClaimMapping -> metaClaimMapping));
    }

    private List<Claim> buildClaims(Map<String, List<Attribute>> connectorIdToAttributesMap) {

        List<Claim> claims = new ArrayList<>();

        if (!connectorIdToAttributesMap.isEmpty()) {
            connectorIdToAttributesMap.entrySet().stream()
                    .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                    .forEach(entry -> {
                                entry.getValue().stream()
                                        .forEach(attribute -> {
                                                    Optional<MetaClaim> optional = metaClaimMappings.stream()
                                                            .filter(metaClaimMapping -> metaClaimMapping
                                                                    .getAttributeName().equals(attribute
                                                                            .getAttributeName()))
                                                            .map(MetaClaimMapping::getMetaClaim)
                                                            .findAny();

                                                    if (optional.isPresent()) {
                                                        MetaClaim metaClaim = optional.get();
                                                        claims.add(new Claim(metaClaim.getDialectUri(), metaClaim
                                                                .getClaimUri(), attribute.getAttributeValue()));
                                                    }
                                                }
                                        );
                            }
                    );
        }

        return claims;
    }

    private Map<String, List<String>> getConnectorIdToAttributeNameMap(List<MetaClaim> metaClaims) {

        Map<String, List<String>> connectorIdToAttributeNameMap = new HashMap<>();

        if (!metaClaims.isEmpty()) {
            metaClaims.stream()
                    .filter(Objects::nonNull)
                    .filter(metaClaim -> !isNullOrEmpty(metaClaim.getClaimUri()))
                    .forEach(metaClaim -> {
                                Optional<MetaClaimMapping> optional = metaClaimMappings.stream()
                                        .filter(metaClaimMapping -> metaClaimMapping.getMetaClaim().getClaimUri()
                                                .equals(metaClaim.getClaimUri()))
                                        .findFirst();
                                if (optional.isPresent()) {
                                    MetaClaimMapping metaClaimMapping = optional.get();

                                    List<String> attributeNames = connectorIdToAttributeNameMap.get(metaClaimMapping
                                            .getIdentityStoreConnectorId());
                                    if (attributeNames == null) {
                                        attributeNames = new ArrayList<String>();
                                        connectorIdToAttributeNameMap.put(metaClaimMapping
                                                .getIdentityStoreConnectorId(), attributeNames);
                                    }
                                    attributeNames.add(metaClaimMapping.getAttributeName());
                                }
                            }
                    );
        }
        return connectorIdToAttributeNameMap;
    }

    private Map<String, List<Attribute>> getAttributesMap(List<Claim> claims) {

        Map<String, List<Attribute>> connectorIdToAttributesMap = new HashMap<>();

        if (claims != null && !claims.isEmpty()) {
            claims.stream()
                    .forEach(claim -> {
                                Optional<MetaClaimMapping> optional = metaClaimMappings.stream()
                                        .filter(metaClaimMapping -> metaClaimMapping.getMetaClaim().getClaimUri()
                                                .equals(claim.getClaimUri()))
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
                                    attributes.add(new Attribute(metaClaimMapping.getAttributeName(),
                                            claim.getValue()));
                                }
                            }
                    );
        }

        return connectorIdToAttributesMap;
    }

    private void removeAddedUsersInAFailure(List<UserPartition> userPartitions) {

        for (UserPartition userPartition : userPartitions) {
            try {
                identityStoreConnectorsMap.get(userPartition.getConnectorId())
                        .removeAddedUsersInAFailure(Collections.singletonList(userPartition
                                .getConnectorUserId()));
            } catch (IdentityStoreConnectorException e) {
                log.error("Error occurred while removing invalid connector user ids. " + String.join(" , ",
                        userPartitions.stream().map(UserPartition::toString).collect(Collectors.toList())
                ), e);
            }
        }
    }

    private Map<String, List<Callback>> getCredentialMap(List<Callback> credentials) {

        Map<String, List<Callback>> connectorIdToCredentialsMap = new HashMap<>();

        if (!credentials.isEmpty()) {
            credentials.stream()
                    .filter(Objects::nonNull)
                    .forEach(callback -> {
                        Optional<CredentialStoreConnector> optional = credentialStoreConnectors.stream()
                                .filter(connector -> connector.canStore(new Callback[]{callback}))
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
        }

        return connectorIdToCredentialsMap;
    }

    private Map<String, Map<String, List<Attribute>>> getAttributesMapOfUsers(Map<String, UserBean> userModelMap) {

        Map<String, Map<String, List<Attribute>>> attributesMap = new HashMap<>();

        if (!userModelMap.entrySet().isEmpty()) {
            userModelMap.entrySet().stream()
                    .forEach(userModelEntry -> {
                        Map<String, List<Attribute>> connectorIdToAttributesMap = getAttributesMap
                                (userModelEntry.getValue().getClaims());

                        if (!connectorIdToAttributesMap.isEmpty()) {
                            connectorIdToAttributesMap.entrySet().stream()
                                    .forEach(attributeEntry -> {
                                                Map<String, List<Attribute>> uniqueUserIdToAttributesMap =
                                                        attributesMap.get(attributeEntry
                                                                .getKey());
                                                if (uniqueUserIdToAttributesMap == null) {
                                                    uniqueUserIdToAttributesMap = new HashMap<>();
                                                    attributesMap.put(attributeEntry.getKey(),
                                                            uniqueUserIdToAttributesMap);
                                                }
                                                uniqueUserIdToAttributesMap.put(userModelEntry.getKey(), attributeEntry
                                                        .getValue());
                                            }
                                    );
                        }
                    });
        }

        return attributesMap;
    }

    private Map<String, Map<String, List<Callback>>> getCredentialsMapOfUsers(Map<String, UserBean> userModelMap) {

        Map<String, Map<String, List<Callback>>> credentialMap = new HashMap<>();

        if (!userModelMap.entrySet().isEmpty()) {
            userModelMap.entrySet().stream()
                    .forEach(userModelEntry -> {
                        Map<String, List<Callback>> connectorIdToCredentialsMap = getCredentialMap
                                (userModelEntry.getValue().getCredentials());

                        if (!connectorIdToCredentialsMap.isEmpty()) {
                            connectorIdToCredentialsMap.entrySet().stream()
                                    .forEach(credentialEntry -> {
                                                Map<String, List<Callback>> uniqueUserIdToCredentialMap =
                                                        credentialMap.get(credentialEntry
                                                                .getKey());
                                                if (uniqueUserIdToCredentialMap == null) {
                                                    uniqueUserIdToCredentialMap = new HashMap<>();
                                                    credentialMap.put(credentialEntry.getKey(),
                                                            uniqueUserIdToCredentialMap);
                                                }
                                                uniqueUserIdToCredentialMap.put(userModelEntry.getKey(),
                                                        credentialEntry.getValue());
                                            }
                                    );
                        }
                    });
        }

        return credentialMap;
    }

    private void removeAddedGroupsInAFailure(List<GroupPartition> groupPartitions) {

        for (GroupPartition groupPartition : groupPartitions) {
            try {
                identityStoreConnectorsMap.get(groupPartition.getConnectorId())
                        .removeAddedGroupsInAFailure(Collections.singletonList(groupPartition.getConnectorGroupId()));
            } catch (IdentityStoreConnectorException e) {
                log.error("Error occurred while removing invalid connector user ids. " + String.join(" , ",
                        groupPartitions.stream().map(GroupPartition::toString).collect(Collectors.toList())
                ), e);
            }
        }
    }

    private Map<String, Map<String, List<Attribute>>> getAttributesMapOfGroups(Map<String, GroupBean> groupModelMap) {

        Map<String, Map<String, List<Attribute>>> attributesMap = new HashMap<>();

        if (!groupModelMap.entrySet().isEmpty()) {
            groupModelMap.entrySet().stream()
                    .forEach(groupModelEntry -> {
                        Map<String, List<Attribute>> connectorIdToAttributesMap = getAttributesMap
                                (groupModelEntry.getValue().getClaims());

                        if (!connectorIdToAttributesMap.isEmpty()) {
                            connectorIdToAttributesMap.entrySet().stream()
                                    .forEach(attributeEntry -> {
                                                Map<String, List<Attribute>> uniqueUserIdToAttributesMap =
                                                        attributesMap.get(attributeEntry
                                                                .getKey());
                                                if (uniqueUserIdToAttributesMap == null) {
                                                    uniqueUserIdToAttributesMap = new HashMap<>();
                                                    attributesMap.put(attributeEntry.getKey(),
                                                            uniqueUserIdToAttributesMap);
                                                }
                                                uniqueUserIdToAttributesMap.put(groupModelEntry.getKey(),
                                                        attributeEntry.getValue());
                                            }
                                    );
                        }
                    });
        }

        return attributesMap;
    }
}

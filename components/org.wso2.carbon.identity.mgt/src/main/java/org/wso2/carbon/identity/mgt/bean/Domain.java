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

package org.wso2.carbon.identity.mgt.bean;

import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.claim.MetaClaim;
import org.wso2.carbon.identity.mgt.claim.MetaClaimMapping;
import org.wso2.carbon.identity.mgt.exception.DomainClientException;
import org.wso2.carbon.identity.mgt.exception.DomainException;
import org.wso2.carbon.identity.mgt.exception.GroupNotFoundException;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreConnectorException;
import org.wso2.carbon.identity.mgt.exception.UniqueIdResolverException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.mgt.store.connector.CredentialStoreConnector;
import org.wso2.carbon.identity.mgt.store.connector.IdentityStoreConnector;
import org.wso2.carbon.identity.mgt.user.DomainGroup;
import org.wso2.carbon.identity.mgt.user.DomainUser;
import org.wso2.carbon.identity.mgt.user.GroupPartition;
import org.wso2.carbon.identity.mgt.user.UniqueIdResolver;
import org.wso2.carbon.identity.mgt.user.UserPartition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.wso2.carbon.kernel.utils.StringUtils.isNullOrEmpty;

/**
 * Represents a domain.
 */
public class Domain {

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

    /**
     * Mapping between IdentityStoreConnector ID and MetaClaimMapping
     */
    private Map<String, List<MetaClaimMapping>> connectorIdToMetaClaimMappings = new HashMap<>();

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
     * Get IdentityStoreConnector from identity store connector id.
     *
     * @param identityStoreConnectorId identity store connector id.
     * @return IdentityStoreConnector instance.
     */
    public IdentityStoreConnector getIdentityStoreConnectorFromId(String identityStoreConnectorId) {

        return identityStoreConnectorsMap.get(identityStoreConnectorId);
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
     * Get CredentialStoreConnector from credential store connector id.
     *
     * @param credentialStoreConnectorId String - CredentialStoreConnector ID
     * @return credentialStoreConnector
     */
    public CredentialStoreConnector getCredentialStoreConnectorFromId(String credentialStoreConnectorId) {

        return credentialStoreConnectorsMap.get(credentialStoreConnectorId);
    }

    /**
     * Checks weather a certain claim URI exists in the domain claim mappings.
     *
     * @param claimURI Claim
     * @return is claim belong to domain
     */
    public boolean isClaimSupported(String claimURI) {

        return connectorIdToMetaClaimMappings.values().stream()
                .anyMatch(list -> list.stream().filter(metaClaimMapping ->
                        claimURI.equals(metaClaimMapping.getMetaClaim().getClaimUri()))
                        .findFirst().isPresent());
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
            return this.uniqueIdResolver.isUserExists(domainUserId);
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
            domainUser = uniqueIdResolver.getDomainUserFromConnectorUserId(connectorUserId, metaClaimMapping
                    .getIdentityStoreConnectorId());
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
            domainUsers = this.uniqueIdResolver.listDomainUsers(offset, length);
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

        if (!metaClaimMapping.isUnique()) {
            throw new DomainClientException("Provided claim is not unique.");
        }

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
            domainUsers = uniqueIdResolver.getDomainUsers(connectorUserIds, metaClaimMapping
                    .getIdentityStoreConnectorId());
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

    public List<String> listDomainUsers(MetaClaim metaClaim, String filterPattern, int offset, int length)
            throws DomainException {

        MetaClaimMapping metaClaimMapping = claimUriToMetaClaimMappings.get(metaClaim.getClaimUri());

        if (!metaClaimMapping.isUnique()) {
            throw new DomainException("Provided claim is not unique.");
        }

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
            domainUsers = uniqueIdResolver.getDomainUsers(connectorUserIds, metaClaimMapping
                    .getIdentityStoreConnectorId());
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
            return this.uniqueIdResolver.isGroupExists(domainGroupId);
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
        } catch (IdentityStoreConnectorException e) {
            throw new DomainException("Failed to get connector group id", e);
        }

        if (isNullOrEmpty(connectorGroupId)) {
            throw new GroupNotFoundException("Invalid claim value.");
        }

        DomainGroup domainGroup;
        try {
            domainGroup = uniqueIdResolver.getDomainGroupFromConnectorGroupId(connectorGroupId, metaClaimMapping
                    .getIdentityStoreConnectorId());
        } catch (UniqueIdResolverException e) {
            throw new DomainException("Failed to retrieve the domain group id.", e);
        }

        if (domainGroup == null || isNullOrEmpty(domainGroup.getDomainGroupId())) {
            throw new DomainException("Failed to retrieve the domain group id.");
        }

        return domainGroup.getDomainGroupId();
    }

    public List<String> listDomainGroups(int offset, int length) throws DomainException {

        List<DomainGroup> domainGroups;
        try {
            domainGroups = this.uniqueIdResolver.listDomainGroups(offset, length);
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
            domainGroups = uniqueIdResolver.getDomainGroups(connectorGroupIds, metaClaimMapping
                    .getIdentityStoreConnectorId());
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
            domainGroups = uniqueIdResolver.getDomainGroups(connectorGroupIds, metaClaimMapping
                    .getIdentityStoreConnectorId());
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
            domainGroups = uniqueIdResolver.getDomainGroupsOfUser(domainUserId);
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
            domainUsers = uniqueIdResolver.getDomainUsersOfGroup(domainGroupId);
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
            return uniqueIdResolver.isUserInGroup(domainUserId, domainGroupId);
        } catch (UniqueIdResolverException e) {
            throw new DomainException("Failed to check whether user exists in group.", e);
        }
    }

    public List<Claim> getClaimsOfUser(String domainUserId) throws DomainException, UserNotFoundException {

        DomainUser domainUser;
        try {
            domainUser = uniqueIdResolver.getDomainUser(domainUserId);
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
            domainUser = uniqueIdResolver.getDomainUser(domainUserId);
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

        Map<String, List<String>> attributeNamesMap = getConnectorIdToAttributeNameMap(metaClaimMappings, metaClaims);

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
            domainGroup = uniqueIdResolver.getUniqueGroup(domainGroupId);
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
            domainGroup = uniqueIdResolver.getUniqueGroup(domainGroupId);
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

        Map<String, List<String>> attributeNamesMap = getConnectorIdToAttributeNameMap(metaClaimMappings, metaClaims);

        Map<String, List<Attribute>> attributesMap = new HashMap<>();
        for (GroupPartition groupPartition : groupPartitions) {
            List<String> attributeNames = attributeNamesMap.get(groupPartition.getConnectorId());
            IdentityStoreConnector identityStoreConnector = identityStoreConnectorsMap.get(groupPartition
                    .getConnectorId());
            if (attributeNames != null) {
                try {
                    List<Attribute> attributes = identityStoreConnector.getUserAttributeValues(groupPartition
                            .getConnectorGroupId(), attributeNames);
                    attributesMap.put(groupPartition.getConnectorId(), attributes);
                } catch (IdentityStoreConnectorException e) {
                    throw new DomainException("Failed to get group attribute values.", e);
                }
            }
        }

        return buildClaims(attributesMap);
    }

    /**
     * Get claim mappings for an identity store id.
     *
     * @return Map of connector Id to List of MetaClaimMapping
     */
    public Map<String, List<MetaClaimMapping>> getConnectorIdToMetaClaimMappings() {

        return connectorIdToMetaClaimMappings;
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

    /**
     * Set claim mappings for an identity store id.
     *
     * @param connectorIdToMetaClaimMappings Map&lt;String, List&lt;MetaClaimMapping&gt;&gt; claim mappings
     */
    public void setConnectorIdToMetaClaimMappings(Map<String, List<MetaClaimMapping>> connectorIdToMetaClaimMappings) {

        this.connectorIdToMetaClaimMappings = connectorIdToMetaClaimMappings;
    }

    /**
     * Get set of IdentityStoreConnectors for this domain sorted by their priority.
     *
     * @return Sorted IdentityStoreConnectors set
     */
    public List<IdentityStoreConnector> getIdentityStoreConnectors() {

        if (identityStoreConnectors == null) {
            return Collections.emptyList();
        }
        return identityStoreConnectors;
    }

    /**
     * Get set of CredentialStoreConnectors for this domain sorted by their priority.
     *
     * @return Sorted CredentialStoreConnectors set
     */
    public List<CredentialStoreConnector> getCredentialStoreConnectors() {

        if (credentialStoreConnectors == null) {
            return Collections.emptyList();
        }
        return credentialStoreConnectors;
    }

    public UniqueIdResolver getUniqueIdResolver() {
        return uniqueIdResolver;
    }

    public void setUniqueIdResolver(UniqueIdResolver uniqueIdResolver) {
        this.uniqueIdResolver = uniqueIdResolver;
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

    private Map<String, List<String>> getConnectorIdToAttributeNameMap(List<MetaClaimMapping> metaClaimMappings,
                                                                       List<MetaClaim> metaClaims) {

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
}

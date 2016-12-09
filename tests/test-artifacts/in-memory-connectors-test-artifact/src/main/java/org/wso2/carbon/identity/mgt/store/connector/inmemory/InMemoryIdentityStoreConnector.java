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

package org.wso2.carbon.identity.mgt.store.connector.inmemory;

import org.wso2.carbon.identity.mgt.bean.Attribute;
import org.wso2.carbon.identity.mgt.config.IdentityStoreConnectorConfig;
import org.wso2.carbon.identity.mgt.exception.GroupNotFoundException;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreConnectorException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.mgt.store.connector.IdentityStoreConnector;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * InMemory Identity Store Connector.
 */
public class InMemoryIdentityStoreConnector implements IdentityStoreConnector {

    private IdentityStoreConnectorConfig identityStoreConnectorConfig;

    private Map<String, List<Attribute>> userStoreMap = new HashMap<>();

    private Map<String, List<Attribute>> groupStoreMap = new HashMap<>();

    @Override
    public void init(IdentityStoreConnectorConfig identityStoreConnectorConfig) throws IdentityStoreConnectorException {

        this.identityStoreConnectorConfig = identityStoreConnectorConfig;
    }

    @Override
    public String getIdentityStoreConnectorId() {
        return null;
    }

    @Override
    public String getConnectorUserId(String attributeName, String attributeValue) throws UserNotFoundException,
            IdentityStoreConnectorException {

        if (userStoreMap.size() == 0) {
            throw new UserNotFoundException("User not found.");
        }

        Optional<Map.Entry<String, List<Attribute>>> mapEntry = userStoreMap.entrySet().stream()
                .filter(entry -> {
                    if (entry.getValue() != null) {
                        Optional<Attribute> optional = entry.getValue().stream()
                                .filter(attribute -> attribute.getAttributeName().equals(attributeName) && attribute
                                        .getAttributeValue().equals(attributeValue))
                                .findAny();
                        return optional.isPresent();
                    }
                    return false;
                }).findAny();
        if (mapEntry.isPresent()) {
            return mapEntry.get().getKey();
        }
        throw new UserNotFoundException("User not found.");
    }

    @Override
    public List<String> listConnectorUserIds(String attributeName, String attributeValue, int offset, int length)
            throws IdentityStoreConnectorException {

        return userStoreMap.entrySet().stream()
                .filter(entry ->
                        entry.getValue().stream()
                                .filter(attribute -> attribute.getAttributeName().equals(attributeName) && attribute
                                        .getAttributeValue().equals(attributeValue))
                                .findAny().isPresent()
                )
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> listConnectorUserIdsByPattern(String attributeName, String filterPattern, int offset, int
            length) throws IdentityStoreConnectorException {

        return userStoreMap.entrySet().stream()
                .filter(entry ->
                        entry.getValue().stream()
                                .filter(attribute -> attribute.getAttributeName().equals(attributeName) && attribute
                                        .getAttributeValue().toLowerCase().matches(filterPattern.toLowerCase()))
                                .findAny().isPresent()
                )
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public int getUserCount() throws IdentityStoreConnectorException {
        return 0;
    }

    @Override
    public List<Attribute> getUserAttributeValues(String userID) throws IdentityStoreConnectorException {

        return userStoreMap.get(userID);
    }

    @Override
    public List<Attribute> getUserAttributeValues(String userID, List<String> attributeNames) throws
            IdentityStoreConnectorException {

        if (attributeNames.isEmpty() || userStoreMap.get(userID) == null) {
            return Collections.emptyList();
        }

        return userStoreMap.get(userID).stream()
                .filter(attribute ->
                        attributeNames.stream()
                                .filter(Objects::nonNull)
                                .filter(attributeName -> attribute.getAttributeName().equals(attributeName))
                                .findAny()
                                .isPresent()
                ).collect(Collectors.toList());
    }

    @Override
    public int getGroupCount() throws IdentityStoreConnectorException {
        return 0;
    }

    @Override
    public String getConnectorGroupId(String attributeName, String attributeValue) throws GroupNotFoundException,
            IdentityStoreConnectorException {

        if (groupStoreMap.size() == 0) {
            throw new GroupNotFoundException("Group not found.");
        }

        Optional<Map.Entry<String, List<Attribute>>> mapEntry = groupStoreMap.entrySet().stream()
                .filter(entry -> {
                    if (entry.getValue() != null) {
                        Optional<Attribute> optional = entry.getValue().stream()
                                .filter(attribute -> attribute.getAttributeName().equals(attributeName) && attribute
                                        .getAttributeValue().equals(attributeValue))
                                .findAny();
                        return optional.isPresent();
                    }
                    return false;
                }).findAny();
        if (mapEntry.isPresent()) {
            return mapEntry.get().getKey();
        }
        throw new GroupNotFoundException("Group not found.");
    }

    @Override
    public List<String> listConnectorGroupIds(String attributeName, String attributeValue, int offset, int length)
            throws IdentityStoreConnectorException {

        return groupStoreMap.entrySet().stream()
                .filter(entry ->
                        entry.getValue().stream()
                                .filter(attribute -> attribute.getAttributeName().equals(attributeName) && attribute
                                        .getAttributeValue().equals(attributeValue))
                                .findAny().isPresent()
                )
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> listConnectorGroupIdsByPattern(String attributeName, String filterPattern, int offset, int
            length) throws IdentityStoreConnectorException {

        return groupStoreMap.entrySet().stream()
                .filter(entry ->
                        entry.getValue().stream()
                                .filter(attribute -> attribute.getAttributeName().equals(attributeName) && attribute
                                        .getAttributeValue().toLowerCase().matches(filterPattern.toLowerCase()))
                                .findAny().isPresent()
                )
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public List<Attribute> getGroupAttributeValues(String groupId) throws IdentityStoreConnectorException {

        return groupStoreMap.get(groupId);
    }

    @Override
    public List<Attribute> getGroupAttributeValues(String groupId, List<String> attributeNames) throws
            IdentityStoreConnectorException {

        if (attributeNames.isEmpty() || groupStoreMap.get(groupId) == null) {
            return Collections.emptyList();
        }

        return groupStoreMap.get(groupId).stream()
                .filter(attribute ->
                        attributeNames.stream()
                                .filter(Objects::nonNull)
                                .filter(attributeName -> attribute.getAttributeName().equals(attributeName))
                                .findAny()
                                .isPresent()
                ).collect(Collectors.toList());
    }

    @Override
    public boolean isUserInGroup(String userId, String groupId) throws IdentityStoreConnectorException {
        return false;
    }

    @Override
    public boolean isReadOnly() throws IdentityStoreConnectorException {
        return false;
    }

    @Override
    public IdentityStoreConnectorConfig getIdentityStoreConfig() {
        return identityStoreConnectorConfig;
    }

    @Override
    public String addUser(List<Attribute> attributes) throws IdentityStoreConnectorException {

        String userId = UUID.randomUUID().toString();
        userStoreMap.put(userId, attributes);
        return userId;
    }

    @Override
    public Map<String, String> addUsers(Map<String, List<Attribute>> attributes) throws
            IdentityStoreConnectorException {
        Map<String, String> userIds = new HashMap<>();
        for (Map.Entry<String, List<Attribute>> entry : attributes.entrySet()) {
            String userId = UUID.randomUUID().toString();
            userStoreMap.put(userId, entry.getValue());
            userIds.put(entry.getKey(), userId);
        }

        return userIds;
    }

    @Override
    public String updateUserAttributes(String userIdentifier, List<Attribute> attributes) throws
            IdentityStoreConnectorException {

        userStoreMap.put(userIdentifier, attributes);
        return userIdentifier;
    }

    @Override
    public String updateUserAttributes(String userIdentifier, List<Attribute> attributesToAdd, List<Attribute>
            attributesToRemove) throws IdentityStoreConnectorException {

        List<Attribute> attributes = userStoreMap.get(userIdentifier);
        attributes = getAttributes(attributesToAdd, attributesToRemove, attributes);
        userStoreMap.put(userIdentifier, attributes);
        return userIdentifier;
    }

    @Override
    public void deleteUser(String userIdentifier) throws IdentityStoreConnectorException {

    }

    @Override
    public void updateGroupsOfUser(String userIdentifier, List<String> groupIdentifiers) throws
            IdentityStoreConnectorException {

    }

    @Override
    public void updateGroupsOfUser(String userIdentifier, List<String> groupIdentifiersToAdd, List<String>
            groupIdentifiersToRemove) throws IdentityStoreConnectorException {

    }

    @Override
    public String addGroup(List<Attribute> attributes) throws IdentityStoreConnectorException {

        String groupId = UUID.randomUUID().toString();
        groupStoreMap.put(groupId, attributes);
        return groupId;
    }

    @Override
    public Map<String, String> addGroups(Map<String, List<Attribute>> attributes) throws
            IdentityStoreConnectorException {

        Map<String, String> groupIds = new HashMap<>();
        for (Map.Entry<String, List<Attribute>> entry : attributes.entrySet()) {
            String groupId = UUID.randomUUID().toString();
            groupStoreMap.put(groupId, entry.getValue());
            groupIds.put(entry.getKey(), groupId);
        }

        return groupIds;
    }

    @Override
    public String updateGroupAttributes(String groupIdentifier, List<Attribute> attributes) throws
            IdentityStoreConnectorException {

        groupStoreMap.put(groupIdentifier, attributes);
        return groupIdentifier;
    }

    @Override
    public String updateGroupAttributes(String groupIdentifier, List<Attribute> attributesToAdd, List<Attribute>
            attributesToRemove) throws IdentityStoreConnectorException {

        List<Attribute> attributes = groupStoreMap.get(groupIdentifier);
        attributes = getAttributes(attributesToAdd, attributesToRemove, attributes);
        userStoreMap.put(groupIdentifier, attributes);
        return groupIdentifier;
    }

    private List<Attribute> getAttributes(List<Attribute> attributesToAdd, List<Attribute> attributesToRemove,
                                          List<Attribute> attributes) {
        Map<String, Attribute> attributeMap = new HashMap<>();
        if (!attributes.isEmpty()) {
            attributeMap = attributes.stream()
                    .collect(Collectors.toMap(Attribute::getAttributeName, attribute -> attribute));
        }
        if (!attributesToAdd.isEmpty()) {
            attributeMap.putAll(attributesToAdd.stream()
                    .collect(Collectors.toMap(Attribute::getAttributeName, attribute -> attribute)));
        }
        if (!attributesToRemove.isEmpty()) {
            for (Attribute attribute : attributesToRemove) {
                attributeMap.remove(attribute.getAttributeName());
            }
        }
        attributes = attributeMap.values().stream().collect(Collectors.toList());
        return attributes;
    }

    @Override
    public void deleteGroup(String groupIdentifier) throws IdentityStoreConnectorException {

    }

    @Override
    public void updateUsersOfGroup(String groupIdentifier, List<String> userIdentifiers) throws
            IdentityStoreConnectorException {

    }

    @Override
    public void updateUsersOfGroup(String groupIdentifier, List<String> userIdentifiersToAdd, List<String>
            userIdentifiersToRemove) throws IdentityStoreConnectorException {

    }

    @Override
    public void removeAddedUsersInAFailure(List<String> connectorUserIds) throws IdentityStoreConnectorException {

    }

    @Override
    public void removeAddedGroupsInAFailure(List<String> connectorGroupIds) throws IdentityStoreConnectorException {

    }
}

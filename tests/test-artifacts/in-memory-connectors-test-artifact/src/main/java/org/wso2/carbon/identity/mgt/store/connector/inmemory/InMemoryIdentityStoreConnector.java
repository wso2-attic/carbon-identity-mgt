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
import org.wso2.carbon.identity.mgt.bean.Group;
import org.wso2.carbon.identity.mgt.bean.User;
import org.wso2.carbon.identity.mgt.config.IdentityStoreConnectorConfig;
import org.wso2.carbon.identity.mgt.exception.GroupNotFoundException;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreConnectorException;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.mgt.store.connector.IdentityStoreConnector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * InMemory Identity Store Connector.
 */
public class InMemoryIdentityStoreConnector implements IdentityStoreConnector {

    private IdentityStoreConnectorConfig identityStoreConnectorConfig;

    private Map<String, List<Attribute>> userStoreMap = new HashMap<>();

    @Override
    public void init(IdentityStoreConnectorConfig identityStoreConnectorConfig) throws IdentityStoreException {

        this.identityStoreConnectorConfig = identityStoreConnectorConfig;
    }

    @Override
    public String getIdentityStoreConnectorId() {
        return null;
    }

    @Override
    public String getConnectorUserId(String attributeName, String attributeValue) throws UserNotFoundException,
            IdentityStoreException {

        if (userStoreMap.size() == 0) {
            return null;
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
            throws IdentityStoreException {
        return null;
    }

    @Override
    public List<String> listConnectorUserIdsByPattern(String attributeName, String filterPattern, int offset, int
            length) throws IdentityStoreException {
        return null;
    }

    @Override
    public int getUserCount() throws IdentityStoreException {
        return 0;
    }

    @Override
    public List<User.UserBuilder> getUserBuilderList(String attributeName, String filterPattern, int offset, int
            length) throws IdentityStoreException {
        return null;
    }

    @Override
    public List<User.UserBuilder> getAllUserBuilderList(String attributeName, String filterPattern) throws
            IdentityStoreException {
        return null;
    }

    @Override
    public List<Attribute> getUserAttributeValues(String userID) throws IdentityStoreException {
        return null;
    }

    @Override
    public List<Attribute> getUserAttributeValues(String userID, List<String> attributeNames) throws
            IdentityStoreException {
        return null;
    }

    @Override
    public Group.GroupBuilder getGroupBuilder(String attributeName, String attributeValue) throws
            GroupNotFoundException, IdentityStoreException {
        return null;
    }

    @Override
    public int getGroupCount() throws IdentityStoreException {
        return 0;
    }

    @Override
    public String getConnectorGroupId(String attributeName, String attributeValue) throws GroupNotFoundException,
            IdentityStoreException {
        return null;
    }

    @Override
    public List<String> listConnectorGroupIds(String attributeName, String attributeValue, int offset, int length)
            throws IdentityStoreException {
        return null;
    }

    @Override
    public List<String> listConnectorGroupIdsByPattern(String attributeName, String filterPattern, int offset, int
            length) throws IdentityStoreException {
        return null;
    }

    @Override
    public List<Group.GroupBuilder> getGroupBuilderList(String filterPattern, int offset, int length) throws
            IdentityStoreException {
        return null;
    }

    @Override
    public List<Attribute> getGroupAttributeValues(String groupId) throws IdentityStoreException {
        return null;
    }

    @Override
    public List<Attribute> getGroupAttributeValues(String groupId, List<String> attributeNames) throws
            IdentityStoreException {
        return null;
    }

    @Override
    public List<Group.GroupBuilder> getGroupBuildersOfUser(String userID) throws IdentityStoreException {
        return null;
    }

    @Override
    public List<User.UserBuilder> getUserBuildersOfGroup(String groupID) throws IdentityStoreException {
        return null;
    }

    @Override
    public boolean isUserInGroup(String userId, String groupId) throws IdentityStoreException {
        return false;
    }

    @Override
    public boolean isReadOnly() throws IdentityStoreException {
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
    public Map<String, String> addUsers(Map<String, List<Attribute>> attributes) throws IdentityStoreException {
        return null;
    }

    @Override
    public String updateUserAttributes(String userIdentifier, List<Attribute> attributes) throws
            IdentityStoreException {
        return null;
    }

    @Override
    public String updateUserAttributes(String userIdentifier, List<Attribute> attributesToAdd, List<Attribute>
            attributesToRemove) throws IdentityStoreException {
        return null;
    }

    @Override
    public void deleteUser(String userIdentifier) throws IdentityStoreException {

    }

    @Override
    public void updateGroupsOfUser(String userIdentifier, List<String> groupIdentifiers) throws IdentityStoreException {

    }

    @Override
    public void updateGroupsOfUser(String userIdentifier, List<String> groupIdentifiersToAdd, List<String>
            groupIdentifiersToRemove) throws IdentityStoreException {

    }

    @Override
    public String addGroup(List<Attribute> attributes) throws IdentityStoreConnectorException {
        return null;
    }

    @Override
    public Map<String, String> addGroups(Map<String, List<Attribute>> attributes) throws IdentityStoreException {
        return null;
    }

    @Override
    public String updateGroupAttributes(String groupIdentifier, List<Attribute> attributes) throws
            IdentityStoreException {
        return null;
    }

    @Override
    public String updateGroupAttributes(String groupIdentifier, List<Attribute> attributesToAdd, List<Attribute>
            attributesToRemove) throws IdentityStoreException {
        return null;
    }

    @Override
    public void deleteGroup(String groupIdentifier) throws IdentityStoreConnectorException {

    }

    @Override
    public void updateUsersOfGroup(String groupIdentifier, List<String> userIdentifiers) throws IdentityStoreException {

    }

    @Override
    public void updateUsersOfGroup(String groupIdentifier, List<String> userIdentifiersToAdd, List<String>
            userIdentifiersToRemove) throws IdentityStoreException {

    }

    @Override
    public void removeAddedUsersInAFailure(List<String> connectorUserIds) throws IdentityStoreConnectorException {

    }

    @Override
    public void removeAddedGroupsInAFailure(List<String> connectorGroupIds) throws IdentityStoreConnectorException {

    }
}

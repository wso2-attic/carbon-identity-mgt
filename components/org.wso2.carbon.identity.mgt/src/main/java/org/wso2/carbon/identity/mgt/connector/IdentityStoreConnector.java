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

package org.wso2.carbon.identity.mgt.connector;

import org.wso2.carbon.identity.mgt.connector.config.IdentityStoreConnectorConfig;
import org.wso2.carbon.identity.mgt.exception.GroupNotFoundException;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreConnectorException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;

import java.util.List;
import java.util.Map;

/**
 * User store.
 */
public interface IdentityStoreConnector {

    /**
     * Initialize identity store by passing identity store configurations read from files.
     *
     * @param identityStoreConnectorConfig IdentityStoreConnectorConfig for this connector.
     * @throws IdentityStoreConnectorException Identity Store Connector Exception.
     */
    void init(IdentityStoreConnectorConfig identityStoreConnectorConfig) throws IdentityStoreConnectorException;

    /**
     * Get identity store connector Id.
     *
     * @return returns the unique Id for the connector.
     */
    String getIdentityStoreConnectorId();

    /**
     * Get connector user id from unique attribute..
     *
     * @param attributeName  Name of the attribute.
     * @param attributeValue Value of the attribute.
     * @return Connector user id.
     * @throws UserNotFoundException           User not found exception.
     * @throws IdentityStoreConnectorException Identity Store Connector Exception.
     */
    String getConnectorUserId(String attributeName, String attributeValue) throws UserNotFoundException,
            IdentityStoreConnectorException;

    /**
     * List connector user ids from a attribute for a given range.
     *
     * @param attributeName  Name of the attribute.
     * @param attributeValue Value of the attribute.
     * @param offset         Start position.
     * @param length         Number of users to retrieve.
     * @return List of connector user ids.
     * @throws IdentityStoreConnectorException Identity Store Connector Exception.
     */
    List<String> listConnectorUserIds(String attributeName, String attributeValue, int offset, int length) throws
            IdentityStoreConnectorException;

    /**
     * List connector user ids from a attribute pattern for a given range.
     *
     * @param attributeName Name of the attribute.
     * @param filterPattern Pattern of the attribute.
     * @param offset        Start position.
     * @param length        Number of users to retrieve.
     * @return List of connector user ids.
     * @throws IdentityStoreConnectorException Identity Store Connector Exception.
     */
    List<String> listConnectorUserIdsByPattern(String attributeName, String filterPattern, int offset, int length)
            throws IdentityStoreConnectorException;

    /**
     * Get the count of the users available in the identity store.
     *
     * @return Number of users.
     * @throws IdentityStoreConnectorException Identity Store Connector Exception.
     */
    int getUserCount() throws IdentityStoreConnectorException;

    /**
     * Retrieve attributes of the user with the given ID.
     *
     * @param userID ID of the user whose claims are requested
     * @return Attribute map of the user with given ID
     * @throws IdentityStoreConnectorException Identity Store Connector Exception.
     */
    List<Attribute> getUserAttributeValues(String userID) throws IdentityStoreConnectorException;

    /**
     * Get user attributes for given attribute names.
     *
     * @param userID         Unique id of the user.
     * @param attributeNames User attribute names.
     * @return Map of user attributes.
     * @throws IdentityStoreConnectorException Identity Store Connector Exception.
     */
    List<Attribute> getUserAttributeValues(String userID, List<String> attributeNames) throws
            IdentityStoreConnectorException;

    /**
     * Get the count of the groups available in the identity store.
     *
     * @return Number of groups.
     * @throws IdentityStoreConnectorException Identity Store Connector Exception.
     */
    int getGroupCount() throws IdentityStoreConnectorException;

    /**
     * Get connector group id from unique attribute..
     *
     * @param attributeName  Name of the attribute.
     * @param attributeValue Value of the attribute.
     * @return Connector group id.
     * @throws GroupNotFoundException          Group not found exception.
     * @throws IdentityStoreConnectorException Identity Store Connector Exception.
     */
    String getConnectorGroupId(String attributeName, String attributeValue) throws GroupNotFoundException,
            IdentityStoreConnectorException;

    /**
     * List connector group ids from a attribute for a given range.
     *
     * @param attributeName  Name of the attribute.
     * @param attributeValue Value of the attribute.
     * @param offset         Start position.
     * @param length         Number of groups to retrieve.
     * @return List of connector group ids.
     * @throws IdentityStoreConnectorException Identity Store Connector Exception.
     */
    List<String> listConnectorGroupIds(String attributeName, String attributeValue, int offset, int length) throws
            IdentityStoreConnectorException;

    /**
     * List connector group ids from a attribute pattern for a given range.
     *
     * @param attributeName Name of the attribute.
     * @param filterPattern Pattern of the attribute.
     * @param offset        Start position.
     * @param length        Number of groups to retrieve.
     * @return List of connector group ids.
     * @throws IdentityStoreConnectorException Identity Store Connector Exception.
     */
    List<String> listConnectorGroupIdsByPattern(String attributeName, String filterPattern, int offset, int length)
            throws IdentityStoreConnectorException;

    /**
     * Get all of the attributes that belongs to this group.
     *
     * @param groupId Id of the group.
     * @return Map of attributes.
     * @throws IdentityStoreConnectorException IdentityStore Exception
     */
    List<Attribute> getGroupAttributeValues(String groupId) throws IdentityStoreConnectorException;

    /**
     * Get attribute values for the given names in the group.
     *
     * @param groupId        Id of the group.
     * @param attributeNames List of attribute names.
     * @return Map of attributes.
     * @throws IdentityStoreConnectorException IdentityStore Exception
     */
    List<Attribute> getGroupAttributeValues(String groupId, List<String> attributeNames)
            throws IdentityStoreConnectorException;

    /**
     * Checks whether the user is in the group.
     *
     * @param userId  Id of the user.
     * @param groupId Id of the group.
     * @return true if user is in the group.
     * @throws IdentityStoreConnectorException Identity Store Connector Exception.
     */
    boolean isUserInGroup(String userId, String groupId) throws IdentityStoreConnectorException;

    /**
     * To check whether a user store is read only.
     *
     * @return True if the user store is read only, unless returns false
     * @throws IdentityStoreConnectorException Identity Store Connector Exception.
     */
    boolean isReadOnly() throws IdentityStoreConnectorException;

    /**
     * Returns IdentityStoreConnectorConfig which consists of user store configurations.
     *
     * @return IdentityStoreConnectorConfig which consists of user store configurations
     */
    IdentityStoreConnectorConfig getIdentityStoreConfig();

    /**
     * Get a list of users which matches a given list of attributes.
     *
     * @param attributes Attributes of the user.
     * @return List of connector unique ids of the users.
     * @throws IdentityStoreConnectorException Identity store connector exception.
     */
    List<String> getUsers(List<Attribute> attributes, int offset, int length) throws IdentityStoreConnectorException;

    /**
     * Adds a new user.
     *
     * @param attributes Attributes of the user.
     * @return connector unique id of the user.
     * @throws IdentityStoreConnectorException Identity store connector exception.
     */
    String addUser(List<Attribute> attributes) throws IdentityStoreConnectorException;

    /**
     * Adds new users.
     *
     * @param attributes Attributes of the users.
     * @return Map of global unique id of the user and connector user id.
     * @throws IdentityStoreConnectorException Identity Store Connector Exception.
     */
    Map<String, String> addUsers(Map<String, List<Attribute>> attributes) throws IdentityStoreConnectorException;

    /**
     * Update all attributes of a user.
     *
     * @param userIdentifier User identifier.
     * @param attributes     Attribute values to update.
     * @return connector unique id of user.
     * @throws IdentityStoreConnectorException Identity Store Connector Exception.
     */
    String updateUserAttributes(String userIdentifier, List<Attribute> attributes) throws
            IdentityStoreConnectorException;

    /**
     * Update selected attributes of a user.
     *
     * @param userIdentifier     User identifier.
     * @param attributesToAdd    Attribute values to add.
     * @param attributesToRemove Attribute values to remove.
     * @return connector unique id of user.
     * @throws IdentityStoreConnectorException Identity Store Connector Exception.
     */
    String updateUserAttributes(String userIdentifier, List<Attribute> attributesToAdd,
                                List<Attribute> attributesToRemove) throws IdentityStoreConnectorException;

    /**
     * Delete a user.
     *
     * @param userIdentifier User identifier.
     * @throws IdentityStoreConnectorException Identity Store Connector Exception.
     */
    void deleteUser(String userIdentifier) throws IdentityStoreConnectorException;

    /**
     * Update group list of user.
     *
     * @param userIdentifier   User identifier.
     * @param groupIdentifiers Group identifiers.
     * @throws IdentityStoreConnectorException Identity Store Connector Exception.
     */
    void updateGroupsOfUser(String userIdentifier, List<String> groupIdentifiers) throws
            IdentityStoreConnectorException;

    /**
     * Update selected group list of user.
     *
     * @param userIdentifier           User identifier.
     * @param groupIdentifiersToAdd    Group identifier list to update.
     * @param groupIdentifiersToRemove Group identifier list to remove.
     * @throws IdentityStoreConnectorException Identity Store Connector Exception.
     */
    void updateGroupsOfUser(String userIdentifier, List<String> groupIdentifiersToAdd,
                            List<String> groupIdentifiersToRemove) throws IdentityStoreConnectorException;

    /**
     * Adds a new group.
     *
     * @param attributes Attributes of the group.
     * @return connector unique id of group.
     * @throws IdentityStoreConnectorException Identity store connector exception.
     */
    String addGroup(List<Attribute> attributes) throws IdentityStoreConnectorException;

    /**
     * Adds new groups.
     *
     * @param attributes Attributes of the groups.
     * @return Map with global unique id of the group with connector unique id.
     * @throws IdentityStoreConnectorException Identity Store Connector Exception.
     */
    Map<String, String> addGroups(Map<String, List<Attribute>> attributes) throws IdentityStoreConnectorException;

    /**
     * Update all attributes of a group.
     *
     * @param groupIdentifier Group identifier.
     * @param attributes      Attribute values to update.
     * @return connector unique id of group.
     * @throws IdentityStoreConnectorException Identity Store Connector Exception.
     */
    String updateGroupAttributes(String groupIdentifier, List<Attribute> attributes) throws
            IdentityStoreConnectorException;

    /**
     * Update selected attributes of a group.
     *
     * @param groupIdentifier    Group identifier.
     * @param attributesToAdd    Attribute values to update.
     * @param attributesToRemove Attribute values to remove.
     * @return connector unique id of group.
     * @throws IdentityStoreConnectorException Identity Store Connector Exception.
     */
    String updateGroupAttributes(String groupIdentifier, List<Attribute> attributesToAdd,
                                 List<Attribute> attributesToRemove) throws IdentityStoreConnectorException;

    /**
     * Delete a group.
     *
     * @param groupIdentifier Group identifier.
     * @throws IdentityStoreConnectorException Identity Store Connector Exception.
     */
    void deleteGroup(String groupIdentifier) throws IdentityStoreConnectorException;

    /**
     * Update user list of a group.
     *
     * @param groupIdentifier Group identifier.
     * @param userIdentifiers User identifier list.
     * @throws IdentityStoreConnectorException Identity Store Connector Exception.
     */
    void updateUsersOfGroup(String groupIdentifier, List<String> userIdentifiers) throws
            IdentityStoreConnectorException;

    /**
     * Update selected user list of a group.
     *
     * @param groupIdentifier         Group identifier.
     * @param userIdentifiersToAdd    User identifier list to add.
     * @param userIdentifiersToRemove User identifier list to remove.
     * @throws IdentityStoreConnectorException Identity Store Connector Exception.
     */
    void updateUsersOfGroup(String groupIdentifier, List<String> userIdentifiersToAdd,
                            List<String> userIdentifiersToRemove) throws IdentityStoreConnectorException;

    /**
     * Remove added users in a failure
     *
     * @param connectorUserIds list of users to remove from connector in a failure situation.
     * @throws IdentityStoreConnectorException Identity store connector exception.
     */
    void removeAddedUsersInAFailure(List<String> connectorUserIds) throws IdentityStoreConnectorException;

    /**
     * Remove added groups in a failure
     *
     * @param connectorGroupIds list of groups to remove from connector in a failure situation.
     * @throws IdentityStoreConnectorException Identity store connector exception.
     */
    void removeAddedGroupsInAFailure(List<String> connectorGroupIds) throws IdentityStoreConnectorException;
}

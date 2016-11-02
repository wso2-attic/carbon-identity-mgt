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

package org.wso2.carbon.identity.mgt.user;

import org.wso2.carbon.identity.mgt.exception.UserManagerException;

import java.util.List;
import java.util.Map;

/**
 * UserManager interface.
 *
 * The implementation of this interface is responsible for handling the globally unique user Id.
 */
public interface UserManager {

    /**
     * Get global unique Id for a connector specific user Id.
     *
     * @param connectorUserId The connector specific user Id.
     * @param connectorId     The connector Id.
     * @return Globally unique user Id.
     * @throws UserManagerException User Manager Exception.
     */
    String getUniqueUserId(String connectorUserId, String connectorId) throws UserManagerException;

    /**
     * Get connector specific user Id.
     *
     * @param uniqueUserId The globally unique user Id
     * @param connectorId  The connector Id
     * @return Connector specific user Id
     * @throws UserManagerException User Manager Exception.
     */
    String getConnectorUserId(String uniqueUserId, String connectorId) throws UserManagerException;

    /**
     * Add user.
     *
     * @param uniqueUserId   Globally unique user Id.
     * @param connectedUsers Connected user list from different connectors.
     * @throws UserManagerException User Manager Exception.
     */
    void addUser(String uniqueUserId, List<ConnectedUser> connectedUsers) throws UserManagerException;

    /**
     * Add users.
     *
     * @param connectedUsersMap Globally unique user id against connected user list from different connectors map.
     * @throws UserManagerException User Manager Exception.
     */
    void addUsers(Map<String, List<ConnectedUser>> connectedUsersMap) throws UserManagerException;

    /**
     * Update user.
     *
     * @param uniqueUserId Globally unique user Id.
     * @param connectorUserIdMap Connector user id map.
     * @throws UserManagerException User Manager Exception.
     */
    void updateUser(String uniqueUserId, Map<String, String> connectorUserIdMap) throws UserManagerException;

    /**
     * Delete user.
     *
     * @param uniqueUserId Globally unique user Id.
     * @throws UserManagerException User Manager Exception.
     */
    void deleteUser(String uniqueUserId) throws UserManagerException;

    /**
     * Get connector user ids.
     *
     * @param userUniqueId Globally unique user Id.
     * @return connector user id against connector id map.
     * @throws UserManagerException User Manager Exception.
     */
    Map<String, String> getConnectorUserIds(String userUniqueId) throws UserManagerException;

    /**
     * Get domain name from user unique id.
     *
     * @param uniqueUserId Globally unique user Id.
     * @return Domain name.
     * @throws UserManagerException User Manager Exception.
     */
    String getDomainNameFromUserUniqueId(String uniqueUserId) throws UserManagerException;

    /**
     * Get domain name from group unique id.
     *
     * @param uniqueUserId Globally unique user Id.
     * @return Domain name.
     * @throws UserManagerException User Manager Exception.
     */
    String getDomainNameFromGroupUniqueId(String uniqueUserId) throws UserManagerException;

    /**
     * Get connector group ids.
     *
     * @param uniqueGroupId Globally unique group Id.
     * @return connector group id against connector id map.
     * @throws UserManagerException User Manager Exception.
     */
    Map<String, String> getConnectorGroupIds(String uniqueGroupId) throws UserManagerException;

    /**
     * Add group.
     *
     * @param uniqueGroupId Globally unique group Id.
     * @param connectedGroups Connected group list from different connectors.
     * @throws UserManagerException User Manager Exception.
     */
    void addGroup(String uniqueGroupId, List<ConnectedGroup> connectedGroups) throws UserManagerException;

    /**
     * Add groups.
     *
     * @param connectedGroupsMap Globally unique group id against connected group list from different connectors map.
     * @throws UserManagerException
     */
    void addGroups(Map<String, List<ConnectedGroup>> connectedGroupsMap) throws UserManagerException;

    /**
     * Update group.
     *
     * @param uniqueGroupId Globally unique group Id.
     * @param connectorGroupIdMap Connector group id map.
     * @throws UserManagerException User Manager Exception.
     */
    void updateGroup(String uniqueGroupId, Map<String, String> connectorGroupIdMap) throws UserManagerException;

    /**
     * Delete group.
     *
     * @param uniqueGroupId  Globally unique group Id.
     * @throws UserManagerException User Manager Exception.
     */
    void deleteGroup(String uniqueGroupId) throws UserManagerException;

    /**
     * Update groups of user.
     *
     * @param uniqueUserId Globally unique user Id.
     * @param uniqueGroupIds Globally unique group Ids.
     * @throws UserManagerException User Manager Exception.
     */
    void updateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIds) throws UserManagerException;

    /**
     * Update groups of user.
     *
     * @param uniqueUserId Globally unique user Id.
     * @param uniqueGroupIdsToUpdate Globally unique group ids to update.
     * @param uniqueGroupIdsToRemove Globally unique group ids to remove.
     * @throws UserManagerException User Manager Exception.
     */
    void updateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIdsToUpdate,
                            List<String> uniqueGroupIdsToRemove) throws UserManagerException;

    /**
     * Update users of group.
     *
     * @param uniqueGroupId Globally unique group Id.
     * @param uniqueUserIds Globally unique user Ids.
     * @throws UserManagerException User Manager Exception.
     */
    void updateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIds) throws UserManagerException;

    /**
     * Update users of group,
     *
     * @param uniqueGroupId Globally unique group Id.
     * @param uniqueUserIdsToUpdate Globally unique user ids to update.
     * @param uniqueUserIdsToRemove Globally unique user ids to remove.
     * @throws UserManagerException User Manager Exception.
     */
    void updateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIdsToUpdate,
                            List<String> uniqueUserIdsToRemove) throws UserManagerException;
}

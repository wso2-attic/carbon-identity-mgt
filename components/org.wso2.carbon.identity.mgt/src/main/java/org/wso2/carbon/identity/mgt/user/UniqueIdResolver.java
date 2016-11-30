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

import org.wso2.carbon.identity.mgt.config.UniqueIdResolverConfig;
import org.wso2.carbon.identity.mgt.exception.GroupNotFoundException;
import org.wso2.carbon.identity.mgt.exception.UniqueIdResolverException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;

import java.util.List;
import java.util.Map;

/**
 * UniqueIdResolver interface.
 * <p>
 * The implementation of this interface is responsible for handling the globally unique user Id.
 */
public interface UniqueIdResolver {


    void init(UniqueIdResolverConfig uniqueIdResolverConfig) throws UniqueIdResolverException;

    /**
     * Get unique user for a unique user Id.
     *
     * @param uniqueUserId Globally unique user Id.
     * @return Unique user.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    UniqueUser getUniqueUser(String uniqueUserId) throws UniqueIdResolverException, UserNotFoundException;

    /**
     * Get global unique Id for a connector specific user Id.
     *
     * @param connectorUserId The connector specific user Id.
     * @param connectorId     The connector Id.
     * @return Globally unique user Id.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    UniqueUser getUniqueUserFromConnectorUserId(String connectorUserId, String connectorId) throws
            UniqueIdResolverException;

    /**
     * Get global unique Ids for a connector specific user Ids.
     *
     * @param connectorUserIds The connector specific user Ids.
     * @param connectorId      The connector Id.
     * @return Globally unique user Ids.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    List<UniqueUser> getUniqueUsers(List<String> connectorUserIds, String connectorId) throws UniqueIdResolverException;

    /**
     * Check whether user exists or not.
     *
     * @param uniqueUserId Globally unique user Id.
     * @return existence of user.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    boolean isUserExists(String uniqueUserId) throws UniqueIdResolverException;

    /**
     * List a set of users selected from the given range.
     *
     * @param offset Start position
     * @param length Number of users to retrieve
     * @return list of unique users within given range
     * @throws UniqueIdResolverException Unique Id Resolver Exception
     */
    List<UniqueUser> listUsers(int offset, int length) throws UniqueIdResolverException;

    /**
     * Get unique group for a unique group Id.
     *
     * @param uniqueGroupId Globally unique group Id.
     * @return Unique group.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    UniqueGroup getUniqueGroup(String uniqueGroupId) throws UniqueIdResolverException, GroupNotFoundException;

    /**
     * Get global unique Id for a connector specific group Id.
     *
     * @param connectorGroupId The connector specific group Id.
     * @param connectorId      The connector Id.
     * @return Globally unique group Id.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    UniqueGroup getUniqueGroupFromConnectorGroupId(String connectorGroupId, String connectorId) throws
            UniqueIdResolverException;

    /**
     * Check whether group exists or not.
     *
     * @param uniqueGroupId Globally unique group Id.
     * @return existence of group.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    boolean isGroupExists(String uniqueGroupId) throws UniqueIdResolverException;

    /**
     * List a set of groups selected from the given range.
     *
     * @param offset Start position
     * @param length Number of groups to retrieve
     * @return list of unique groups within given range
     * @throws UniqueIdResolverException Unique Id Resolver Exception
     */
    List<UniqueGroup> listGroups(int offset, int length) throws UniqueIdResolverException;

    /**
     * Get global unique Ids for a connector specific group Ids.
     *
     * @param connectorGroupIds The connector specific group Ids.
     * @param connectorId      The connector Id.
     * @return Globally unique group Ids.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    List<UniqueGroup> getUniqueGroups(List<String> connectorGroupIds, String connectorId) throws
            UniqueIdResolverException;

    /**
     * Get groups of user by user unique id.
     *
     * @param uniqueUserId Globally unique user Id.
     * @return list of groups.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    List<UniqueGroup> getGroupsOfUser(String uniqueUserId) throws UniqueIdResolverException;

    /**
     * Get users of group by group unique id.
     *
     * @param uniqueGroupId Globally unique group Id.
     * @return list of users.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    List<UniqueUser> getUsersOfGroup(String uniqueGroupId) throws UniqueIdResolverException;

    /**
     * Check whether user belong to a specific group.
     * @param uniqueUserId Globally unique user Id.
     * @param uniqueGroupId Globally unique group Id.
     * @return existence of the user in the group.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    boolean isUserInGroup(String uniqueUserId, String uniqueGroupId) throws UniqueIdResolverException;

    /**
     * Add user.
     *
     * @param uniqueUser Globally unique user.
     * @param domainName Domain name.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    void addUser(UniqueUser uniqueUser, String domainName) throws UniqueIdResolverException;

    /**
     * Add users.
     *
     * @param uniqueUsers Globally unique users.
     * @param domainName Domain name.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    void addUsers(List<UniqueUser> uniqueUsers, String domainName) throws UniqueIdResolverException;

    /**
     * Update user.
     *
     * @param uniqueUserId       Globally unique user Id.
     * @param connectorUserIdMap Connector user id map.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    void updateUser(String uniqueUserId, Map<String, String> connectorUserIdMap) throws UniqueIdResolverException;

    /**
     * Delete user.
     *
     * @param uniqueUserId Globally unique user Id.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    void deleteUser(String uniqueUserId) throws UniqueIdResolverException;

    /**
     * Add group.
     *
     * @param uniqueGroup Globally unique group.
     * @param domainName Domain name.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    void addGroup(UniqueGroup uniqueGroup, String domainName) throws UniqueIdResolverException;

    /**
     * Add groups.
     *
     * @param uniqueGroups Globally unique groups.
     * @param domainName Domain name.
     * @throws UniqueIdResolverException Unique Id Resolver Exception
     */
    void addGroups(List<UniqueGroup> uniqueGroups, String domainName) throws UniqueIdResolverException;

    /**
     * Update group.
     *
     * @param uniqueGroupId       Globally unique group Id.
     * @param connectorGroupIdMap Connector group id map.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    void updateGroup(String uniqueGroupId, Map<String, String> connectorGroupIdMap) throws UniqueIdResolverException;

    /**
     * Delete group.
     *
     * @param uniqueGroupId Globally unique group Id.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    void deleteGroup(String uniqueGroupId) throws UniqueIdResolverException;

    /**
     * Update groups of user.
     *
     * @param uniqueUserId   Globally unique user Id.
     * @param uniqueGroupIds Globally unique group Ids.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    void updateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIds) throws UniqueIdResolverException;

    /**
     * Update groups of user.
     *
     * @param uniqueUserId           Globally unique user Id.
     * @param uniqueGroupIdsToUpdate Globally unique group ids to update.
     * @param uniqueGroupIdsToRemove Globally unique group ids to remove.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    void updateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIdsToUpdate,
                            List<String> uniqueGroupIdsToRemove) throws UniqueIdResolverException;

    /**
     * Update users of group.
     *
     * @param uniqueGroupId Globally unique group Id.
     * @param uniqueUserIds Globally unique user Ids.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    void updateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIds) throws UniqueIdResolverException;

    /**
     * Update users of group,
     *
     * @param uniqueGroupId         Globally unique group Id.
     * @param uniqueUserIdsToUpdate Globally unique user ids to update.
     * @param uniqueUserIdsToRemove Globally unique user ids to remove.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    void updateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIdsToUpdate,
                            List<String> uniqueUserIdsToRemove) throws UniqueIdResolverException;
}

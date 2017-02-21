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

package org.wso2.carbon.identity.mgt.resolver;

import org.wso2.carbon.identity.mgt.exception.GroupNotFoundException;
import org.wso2.carbon.identity.mgt.exception.UniqueIdResolverException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;

import java.util.List;
import java.util.Map;

/**
 * UniqueIdResolver interface.
 * <p>
 * The implementation of this interface is responsible for handling the globally unique user Id.
 *
 *
 * TODO - what this do, who have access, when this get invoked
 */
public interface UniqueIdResolver {


    void init(UniqueIdResolverConfig uniqueIdResolverConfig) throws UniqueIdResolverException;

    /**
     * Get unique user for a unique user Id.
     *
     * @param domainUserId Globally unique user Id.
     * @return Unique user.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     * @throws UserNotFoundException User Not Found Exception
     */
    DomainUser getUser(String domainUserId, int domainId) throws UniqueIdResolverException, UserNotFoundException;

    /**
     * Get global unique Id for a connector specific user Id.
     *
     * @param connectorUserId The connector specific user Id.
     * @param connectorId     The connector Id.
     * @return Globally unique user Id.
     * @throws UserNotFoundException User Not Found Exception
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    DomainUser getUserFromConnectorUserId(String connectorUserId, String connectorId, int domainId) throws
            UserNotFoundException, UniqueIdResolverException;

    /**
     * Get global unique Ids for a connector specific user Ids.
     *
     * @param connectorUserIds The connector specific user Ids.
     * @param connectorId      The connector Id.
     * @return Globally unique user Ids.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    List<DomainUser> getUsers(List<String> connectorUserIds, String connectorId, int domainId) throws
            UniqueIdResolverException;

    /**
     * Check whether user exists or not.
     *
     * @param domainUserId Globally unique user Id.
     * @return existence of user.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    boolean isUserExists(String domainUserId, int domainId) throws UniqueIdResolverException;

    /**
     * List a set of users selected from the given range.
     *
     * @param offset Start position
     * @param length Number of users to retrieve
     * @return list of unique users within given range
     * @throws UniqueIdResolverException Unique Id Resolver Exception
     */
    List<DomainUser> listDomainUsers(int offset, int length, int domainId) throws UniqueIdResolverException;

    /**
     * Get unique group for a unique group Id.
     *
     * @param domainGroupId Globally unique group Id.
     * @return Unique group.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    DomainGroup getGroup(String domainGroupId, int domainId) throws UniqueIdResolverException,
            GroupNotFoundException;

    /**
     * Get global unique Id for a connector specific group Id.
     *
     * @param connectorGroupId The connector specific group Id.
     * @param connectorId      The connector Id.
     * @return Globally unique group Id.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     * @throws GroupNotFoundException Group Not Found Exception
     */
    DomainGroup getGroupFromConnectorGroupId(String connectorGroupId, String connectorId, int domainId) throws
            UniqueIdResolverException, GroupNotFoundException;

    /**
     * Check whether group exists or not.
     *
     * @param uniqueGroupId Globally unique group Id.
     * @return existence of group.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    boolean isGroupExists(String uniqueGroupId, int domainId) throws UniqueIdResolverException;

    /**
     * List a set of groups selected from the given range.
     *
     * @param offset Start position
     * @param length Number of groups to retrieve
     * @return list of unique groups within given range
     * @throws UniqueIdResolverException Unique Id Resolver Exception
     */
    List<DomainGroup> listGroups(int offset, int length, int domainId) throws UniqueIdResolverException;

    /**
     * Get global unique Ids for a connector specific group Ids.
     *
     * @param connectorGroupIds The connector specific group Ids.
     * @param connectorId      The connector Id.
     * @return Globally unique group Ids.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    List<DomainGroup> getGroups(List<String> connectorGroupIds, String connectorId, int domainId) throws
            UniqueIdResolverException;

    /**
     * Get groups of user by user unique id.
     *
     * @param domainUserId Globally unique user Id.
     * @return list of groups.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    List<DomainGroup> getGroupsOfUser(String domainUserId, int domainId) throws UniqueIdResolverException;

    /**
     * Get users of group by group unique id.
     *
     * @param domainGroupId Globally unique group Id.
     * @return list of users.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    List<DomainUser> getUsersOfGroup(String domainGroupId, int domainId) throws UniqueIdResolverException;

    /**
     * Check whether user belong to a specific group.
     * @param domainUserId Globally unique user Id.
     * @param domainGroupId Globally unique group Id.
     * @return existence of the user in the group.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    boolean isUserInGroup(String domainUserId, String domainGroupId, int domainId) throws UniqueIdResolverException;

    /**
     * Add user.
     *
     * @param domainUser Globally unique user.
     * @param domainId Domain identifier.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    String addUser(DomainUser domainUser, int domainId) throws UniqueIdResolverException;

    /**
     * Add users.
     *
     * @param domainUsers Globally unique users.
     * @param domainId Domain identifier.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    List<String> addUsers(List<DomainUser> domainUsers, int domainId) throws
            UniqueIdResolverException;

    /**
     * Update user.
     *
     * @param domainUserId       Globally unique user Id.
     * @param connectorUserIdMap Connector user id map.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    void updateUser(String domainUserId, Map<String, String> connectorUserIdMap, int domainId, String state) throws
            UniqueIdResolverException;

    /**
     * Delete user.
     *
     * @param domainUserId Globally unique user Id.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    void deleteUser(String domainUserId, int domainId) throws UniqueIdResolverException;

    /**
     * Add group.
     *
     * @param domainGroup Globally unique group.
     * @param domainId Domain identifier.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    String addGroup(DomainGroup domainGroup, int domainId) throws UniqueIdResolverException;

    /**
     * Add groups.
     *
     * @param domainGroups Globally unique groups.
     * @param domainId Domain identifier.
     * @throws UniqueIdResolverException Unique Id Resolver Exception
     */
    List<String> addGroups(List<DomainGroup> domainGroups, int domainId) throws
            UniqueIdResolverException;

    /**
     * Update group.
     *
     * @param domainGroupId       Globally unique group Id.
     * @param connectorGroupIdMap Connector group id map.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    void updateGroup(String domainGroupId, Map<String, String> connectorGroupIdMap, int domainId) throws
            UniqueIdResolverException;

    /**
     * Delete group.
     *
     * @param domainGroupId Globally unique group Id.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    void deleteGroup(String domainGroupId, int domainId) throws UniqueIdResolverException;

    /**
     * Update groups of user.
     *
     * @param domainUserId   Globally unique user Id.
     * @param domainGroupIds Globally unique group Ids.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    void updateGroupsOfUser(String domainUserId, List<String> domainGroupIds, int domainId) throws
            UniqueIdResolverException;

    /**
     * Update groups of user.
     *
     * @param domainUserId           Globally unique user Id.
     * @param domainGroupIdsToUpdate Globally unique group ids to update.
     * @param domainGroupIdsToRemove Globally unique group ids to remove.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    void updateGroupsOfUser(String domainUserId, List<String> domainGroupIdsToUpdate,
                            List<String> domainGroupIdsToRemove, int domainId) throws UniqueIdResolverException;

    /**
     * Update users of group.
     *
     * @param domainGroupId Globally unique group Id.
     * @param domainUserIds Globally unique user Ids.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    void updateUsersOfGroup(String domainGroupId, List<String> domainUserIds, int domainId) throws
            UniqueIdResolverException;

    /**
     * Update users of group,
     *
     * @param doaminGroupId         Globally unique group Id.
     * @param domainUserIdsToUpdate Globally unique user ids to update.
     * @param domainUserIdsToRemove Globally unique user ids to remove.
     * @throws UniqueIdResolverException Unique Id Resolver Exception.
     */
    void updateUsersOfGroup(String doaminGroupId, List<String> domainUserIdsToUpdate,
                            List<String> domainUserIdsToRemove, int domainId) throws UniqueIdResolverException;

    /**
     * Set User state
     * @param domainUserId : Domain user Id
     * @param targetState ; Target state
     * @param domainId : Domain Id
     * @throws UniqueIdResolverException
     */
    void setUserState(String domainUserId, String targetState, int domainId) throws UniqueIdResolverException;

}

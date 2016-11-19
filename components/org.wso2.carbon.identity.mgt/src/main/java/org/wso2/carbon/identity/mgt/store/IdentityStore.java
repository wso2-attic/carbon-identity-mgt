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

package org.wso2.carbon.identity.mgt.store;

import org.wso2.carbon.identity.mgt.bean.Group;
import org.wso2.carbon.identity.mgt.bean.User;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.claim.MetaClaim;
import org.wso2.carbon.identity.mgt.context.AuthenticationContext;
import org.wso2.carbon.identity.mgt.domain.DomainManager;
import org.wso2.carbon.identity.mgt.exception.AuthenticationFailure;
import org.wso2.carbon.identity.mgt.exception.GroupNotFoundException;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.mgt.model.GroupModel;
import org.wso2.carbon.identity.mgt.model.UserModel;

import java.util.List;
import javax.security.auth.callback.Callback;

/**
 * Represents a virtual identity store to abstract the underlying stores.
 *
 * @since 1.0.0
 */

public interface IdentityStore {

    /**
     * Initialize IdentityStore with {@link DomainManager} instance.
     *
     * @param domainManager Active {@link DomainManager} intance
     * @throws IdentityStoreException IdentityStore Exception
     */
    void init(DomainManager domainManager)
            throws IdentityStoreException;

    /**
     * Retrieve a user by global unique Id.
     *
     * @param uniqueUserId Global Unique Id
     * @return User object
     * @throws IdentityStoreException IdentityStore Exception
     * @throws UserNotFoundException when trying to get user with incorrect unique user id
     */
    User getUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException;

    /**
     * Retrieve a user by global unique Id.
     *
     * @param uniqueUserId The globally unique user Id
     * @param domain The domain the user is in
     * @return User
     * @throws IdentityStoreException IdentityStore Exception
     * @throws UserNotFoundException when trying to get user with incorrect unique user id
     */
    User getUser(String uniqueUserId, String domain) throws IdentityStoreException, UserNotFoundException;

    /**
     * Retrieve a user by claim.
     *
     * @param claim Populated claim
     * @return User object
     * @throws IdentityStoreException IdentityStore Exception
     * @throws UserNotFoundException when trying to get user with incorrect unique user id
     */
    User getUser(Claim claim) throws IdentityStoreException, UserNotFoundException;

    /**
     * Retrieve a user by claim from a specific domain.
     *
     * @param claim      Populated claim
     * @param domainName Domain name to retrieve user from
     * @return User object
     * @throws IdentityStoreException IdentityStore Exception
     * @throws UserNotFoundException when trying to get user with incorrect unique user id
     */
    User getUser(Claim claim, String domainName) throws IdentityStoreException, UserNotFoundException;

    /**
     * List a set of users selected from the given range.
     *
     * @param offset Start position
     * @param length Number of users to retrieve
     * @return A list of users within given range
     * @throws IdentityStoreException IdentityStore Exception
     */
    List<User> listUsers(int offset, int length) throws IdentityStoreException;

    /**
     * List a set of users selected from a specific domain for a given range
     *
     * @param offset     Start position
     * @param length     Number of users to retrieve
     * @param domainName The domain name to retrieve users from
     * @return A list of users within given range selected from the given domain
     * @throws IdentityStoreException IdentityStore Exception
     */
    List<User> listUsers(int offset, int length, String domainName) throws IdentityStoreException;

    /**
     * List a set of users that matches a given claim.
     *
     * @param claim  Populated claim
     * @param offset Start position
     * @param length Number of users to retrieve
     * @return List of users
     * @throws IdentityStoreException IdentityStore Exception
     */
    List<User> listUsers(Claim claim, int offset, int length) throws IdentityStoreException;

    /**
     * List a set of users that matches a given claim in a specified range.
     *
     * @param claim  Populated claim
     * @param offset Start position
     * @param length Number of Users to retrieve
     * @param domain The domain to retrieve users from
     * @return List of users
     * @throws IdentityStoreException IdentityStore Exception
     */
    List<User> listUsers(Claim claim, int offset, int length, String domain) throws IdentityStoreException;

    /**
     * List a set of users that matches a given claim in a specific domain.
     *
     * @param metaClaim Metaclaim
     * @param filterPattern filter pattern to search user
     * @param offset start index of the user
     * @param length number of users to retrieve
     * @return List of users
     * @throws IdentityStoreException IdentityStore Exception
     */
    List<User> listUsers(MetaClaim metaClaim, String filterPattern, int offset, int length)
            throws IdentityStoreException;

    /**
     * List a set of users that matches a given claim in a specified range in a specific domain.
     *
     * @param metaClaim Metaclaim
     * @param filterPattern filter pattern to search user
     * @param offset start index of the user
     * @param length number of users to retrieve
     * @param domain domain of the user
     * @return List of users
     * @throws IdentityStoreException IdentityStore Exception
     */
    List<User> listUsers(MetaClaim metaClaim, String filterPattern, int offset, int length, String domain)
            throws IdentityStoreException;

    /**
     * Retrieve group from group Id.
     *
     * @param uniqueGroupId The Id of the group
     * @return Group
     * @throws IdentityStoreException IdentityStore Exception
     * @throws GroupNotFoundException when group is not found
     */
    Group getGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException;

    /**
     * Get group from group Id from a specific domain.
     *
     * @param uniqueGroupId The Id of the group
     * @param domain  The domain to retrieve group from
     * @return Group
     * @throws IdentityStoreException IdentityStore Exception
     * @throws GroupNotFoundException when group is not found
     */
    Group getGroup(String uniqueGroupId, String domain) throws IdentityStoreException, GroupNotFoundException;

    /**
     * Get group that matches a claim.
     *
     * @param claim Populated claim
     * @return Group
     * @throws IdentityStoreException IdentityStore Exception
     * @throws GroupNotFoundException when group is not found
     */
    List<Group> getGroup(Claim claim) throws IdentityStoreException, GroupNotFoundException;

    /**
     * Get group that matches a claim from a specific domain.
     *
     * @param claim  Populated claim
     * @param domain The domain to retrieve groups from
     * @return Group
     * @throws IdentityStoreException IdentityStore Exception
     * @throws GroupNotFoundException when group is not found
     */
    Group getGroup(Claim claim, String domain) throws IdentityStoreException, GroupNotFoundException;

    /**
     * List groups from a given range.
     *
     * @param offset Start position
     * @param length Number of groups to retrieve
     * @return List of groups within given range
     * @throws IdentityStoreException IdentityStore Exception
     */
    List<Group> listGroups(int offset, int length) throws IdentityStoreException;

    /**
     * List groups from a given range for a given domain.
     *
     * @param offset Start position
     * @param length Number of groups to retrieve
     * @param domain The domain to retrieve groups from
     * @return List of groups within given range in the given domain
     * @throws IdentityStoreException IdentityStore Exception
     */
    List<Group> listGroups(int offset, int length, String domain) throws IdentityStoreException;

    /**
     * List groups that matches a given claim in a given range.
     *
     * @param claim  Populated claim
     * @param offset Start position
     * @param length Number of groups to retrieve
     * @return List of groups that matches the given claim in the given range
     * @throws IdentityStoreException IdentityStore Exception
     */
    List<Group> listGroups(Claim claim, int offset, int length) throws IdentityStoreException;

    /**
     * List groups that matches a given claim in a given range for a specific domain.
     *
     * @param claim  Populated claim
     * @param offset Start position
     * @param length Number of groups to retrieve
     * @param domain The domain to retrieve groups from
     * @return List of groups that matches the given claim in the given range in the given domain
     * @throws IdentityStoreException IdentityStore Exception
     */
    List<Group> listGroups(Claim claim, int offset, int length, String domain) throws IdentityStoreException;

    /**
     * List groups that matches a given claim in a given range.
     *
     * @param metaClaim Metaclaim
     * @param filterPattern filter pattern to search
     * @param offset start index of the group
     * @param length number of users to retrieve
     * @return List of groups
     * @throws IdentityStoreException IdentityStore Exception
     */
    List<Group> listGroups(MetaClaim metaClaim, String filterPattern, int offset, int length)
            throws IdentityStoreException;

    /**
     * List groups that matches a given claim in a given range for a specific domain.
     *
     * @param metaClaim Metaclaim
     * @param filterPattern filter pattern to search
     * @param offset start index of the group
     * @param length number of users to retrieve
     * @param domain domain of group
     * @return List of groups
     * @throws IdentityStoreException IdentityStore Exception
     */
    List<Group> listGroups(MetaClaim metaClaim, String filterPattern, int offset, int length, String domain)
            throws IdentityStoreException;

    /**
     * Get list of groups a user belongs to.
     *
     * @param uniqueUserId The Id of the user
     * @return List of groups the user is in
     * @throws IdentityStoreException IdentityStore Exception
     */
    List<Group> getGroupsOfUser(String uniqueUserId) throws IdentityStoreException;

    /**
     * Get list of users in a given group.
     *
     * @param uniqueGroupId The group to find users of
     * @return List of users contained in the group
     * @throws IdentityStoreException IdentityStore Exception
     */
    List<User> getUsersOfGroup(String uniqueGroupId) throws IdentityStoreException;

    /**
     * Get list of groups a user belongs to in a specific domain.
     *
     * @param uniqueUserId The Id of the user
     * @param domain The domain the users belongs to
     * @return List of groups the user is in
     * @throws IdentityStoreException IdentityStore Exception
     */
    List<Group> getGroupsOfUser(String uniqueUserId, String domain) throws IdentityStoreException;

    /**
     * Get list of users in a given group for a specific domain.
     *
     * @param uniqueGroupId The group to find users of
     * @param domain  The domain the user belongs to
     * @return List of users contained in the group
     * @throws IdentityStoreException IdentityStore Exception
     */
    List<User> getUsersOfGroup(String uniqueGroupId, String domain) throws IdentityStoreException;

    /**
     * Check if a user belongs to a given group.
     *
     * @param uniqueUserId  The user Id
     * @param uniqueGroupId The group Id
     * @return True if user belongs to the given group
     * @throws IdentityStoreException IdentityStore Exception
     */
    boolean isUserInGroup(String uniqueUserId, String uniqueGroupId) throws IdentityStoreException;

    /**
     * Check if a user belongs to a given group in a specific domain.
     *
     * @param uniqueUserId  The user Id
     * @param uniqueGroupId The group Id
     * @param domain  The domain the user and the group belongs to
     * @return True if user belongs to the given group
     * @throws IdentityStoreException IdentityStore Exception
     */
    boolean isUserInGroup(String uniqueUserId, String uniqueGroupId, String domain) throws IdentityStoreException;

    //TODO : these should go under User
    /**
     * Get all claims of a user.
     *
     * @param user The user to retrieve claims for
     * @return List of claims
     * @throws IdentityStoreException IdentityStoreE xception
     */
    List<Claim> getClaims(User user) throws IdentityStoreException;

    /**
     * Get all claims of a user for given URIs.
     *
     * @param user      The user to retrieve claims for
     * @param claimURIs List of claimURIs to retrieve claims for
     * @return List of claims
     * @throws IdentityStoreException IdentityStore Exception
     */
    List<Claim> getClaims(User user, List<String> claimURIs) throws IdentityStoreException;


    /**
     * Add new user to the default domain.
     *
     * @param user User model.
     * @return Created user.
     * @throws IdentityStoreException Identity store exception.
     */
    User addUser(UserModel user) throws IdentityStoreException;

    /**
     * Add new user to a specific domain.
     *
     * @param user   User model.
     * @param domain User domain.
     * @return Created user.
     * @throws IdentityStoreException Identity store exception.
     */
    User addUser(UserModel user, String domain) throws IdentityStoreException;

    /**
     * Add new users to the default domain.
     *
     * @param users User models.
     * @return Created users.
     * @throws IdentityStoreException Identity store exception.
     */
    List<User> addUsers(List<UserModel> users) throws IdentityStoreException;

    /**
     * Add new users to a specific domain.
     *
     * @param users  User models.
     * @param domain User domain.
     * @return Created users.
     * @throws IdentityStoreException Identity store exception.
     */
    List<User> addUsers(List<UserModel> users, String domain) throws IdentityStoreException;

    /**
     * Update user claims by user id.
     *
     * @param uniqueUserId     User unique id.
     * @param userClaims User claims.
     * @throws IdentityStoreException Identity store exception.
     */
    void updateUserClaims(String uniqueUserId, List<Claim> userClaims) throws IdentityStoreException;

    /**
     * Update selected user claims by user id.
     *
     * @param uniqueUserId             User unique id.
     * @param userClaimsToAdd    user claims to update.
     * @param userClaimsToRemove user claims to remove.
     * @throws IdentityStoreException Identity store exception.
     */
    void updateUserClaims(String uniqueUserId, List<Claim> userClaimsToAdd, List<Claim> userClaimsToRemove) throws
            IdentityStoreException;

    /**
     * Delete a user by user id.
     *
     * @param uniqueUserId User unique id.
     * @throws IdentityStoreException Identity store exception.
     */
    void deleteUser(String uniqueUserId) throws IdentityStoreException;

    /**
     * Update groups of a user by user id.
     *
     * @param uniqueUserId   User unique id.
     * @param uniqueGroupIds Group unique id list.
     * @throws IdentityStoreException Identity store exception.
     */
    void updateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIds) throws IdentityStoreException;

    /**
     * Update selected groups of a user by user id.
     *
     * @param uniqueUserId           User unique id.
     * @param uniqueGroupIdsToAdd    Group ids to add.
     * @param uniqueGroupIdsToRemove Group ids to remove.
     * @throws IdentityStoreException Identity store exception.
     */
    void updateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIdsToAdd, List<String> uniqueGroupIdsToRemove)
            throws IdentityStoreException;

    /**
     * Add new group to the default domain.
     *
     * @param groupModel Group model.
     * @return Created group.
     * @throws IdentityStoreException Identity store exception.
     */
    Group addGroup(GroupModel groupModel) throws IdentityStoreException;

    /**
     * Add new group to the specific domain.
     *
     * @param groupModel Group model.
     * @param domain     Group damian.
     * @return Created group.
     * @throws IdentityStoreException Identity store exception.
     */
    Group addGroup(GroupModel groupModel, String domain) throws IdentityStoreException;

    /**
     * Add new groups to the default domain.
     *
     * @param groups Group models.
     * @return Created groups.
     * @throws IdentityStoreException Identity store exception.
     */
    List<Group> addGroups(List<GroupModel> groups) throws IdentityStoreException;

    /**
     * Add new groups to the specific domain.
     *
     * @param groups Group models.
     * @param domain Group domain.
     * @return Created groups.
     * @throws IdentityStoreException Identity store exception.
     */
    List<Group> addGroups(List<GroupModel> groups, String domain) throws IdentityStoreException;

    /**
     * Update group claims by group id.
     *
     * @param uniqueGroupId     Group unique id.
     * @param groupClaims Group claims.
     * @throws IdentityStoreException Identity store exception.
     */
    void updateGroupClaims(String uniqueGroupId, List<Claim> groupClaims) throws IdentityStoreException;

    /**
     * Update selected group claims by group id.
     *
     * @param uniqueGroupId             Group unique id.
     * @param groupClaimsToAdd    Group ids to add.
     * @param groupClaimsToRemove Group ids to remove.
     * @throws IdentityStoreException Identity store exception.
     */
    void updateGroupClaims(String uniqueGroupId, List<Claim> groupClaimsToAdd, List<Claim> groupClaimsToRemove) throws
            IdentityStoreException;

    /**
     * Deleate a group by group id.
     *
     * @param uniqueGroupId Group unique id.
     * @throws IdentityStoreException Identity store exception.
     */
    void deleteGroup(String uniqueGroupId) throws IdentityStoreException;

    /**
     * Update users of a group by group id.
     *
     * @param uniqueGroupId Group unique id.
     * @param uniqueUserIds User unique id list.
     * @throws IdentityStoreException Identity store exception.
     */
    void updateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIds) throws IdentityStoreException;

    /**
     * Update selected users of a group by group id.
     *
     * @param uniqueGroupId         Group unique id.
     * @param uniqueUserIdsToAdd    User unique id list to add.
     * @param uniqueUserIdsToRemove User unique id list to remove.
     * @throws IdentityStoreException Identity store exception.
     */
    void updateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIdsToAdd, List<String>
            uniqueUserIdsToRemove) throws IdentityStoreException;

    /**
     * Authenticate the user.
     *
     * @param claim Unique claim.
     * @param credential Credential.
     * @param domainName Domain name.
     * @return Authentication context.
     * @throws AuthenticationFailure Authentication failure.
     */
    AuthenticationContext authenticate(Claim claim, Callback credential, String domainName)
            throws AuthenticationFailure;

}

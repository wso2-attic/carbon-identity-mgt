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

import org.wso2.carbon.identity.mgt.bean.Domain;
import org.wso2.carbon.identity.mgt.bean.Group;
import org.wso2.carbon.identity.mgt.bean.User;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.claim.MetaClaim;
import org.wso2.carbon.identity.mgt.context.AuthenticationContext;
import org.wso2.carbon.identity.mgt.exception.AuthenticationFailure;
import org.wso2.carbon.identity.mgt.exception.GroupNotFoundException;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.mgt.model.GroupModel;
import org.wso2.carbon.identity.mgt.model.UserModel;

import java.util.List;
import java.util.Set;
import javax.security.auth.callback.Callback;

/**
 * Represents a virtual identity store to abstract the underlying stores.
 *
 * @since 1.0.0
 */

public interface IdentityStore {

    /**
     * Initialize IdentityStore with {@link Domain} list.
     *
     * @param domains domains
     * @throws IdentityStoreException IdentityStore Exception
     */
    void init(List<Domain> domains) throws IdentityStoreException;

    /**
     * Retrieve a user by global unique Id.
     *
     * @param uniqueUserId Global Unique Id
     * @return User object
     * @throws IdentityStoreException IdentityStore Exception
     * @throws UserNotFoundException  when trying to get user with incorrect unique user id
     */
    User getUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException;

    /**
     * Retrieve a user by global unique Id.
     *
     * @param uniqueUserId The globally unique user Id
     * @param domainName   The domain the user is in
     * @return User
     * @throws IdentityStoreException IdentityStore Exception
     * @throws UserNotFoundException  when trying to get user with incorrect unique user id
     */
    User getUser(String uniqueUserId, String domainName) throws IdentityStoreException, UserNotFoundException;

    /**
     * Retrieve a user by claim.
     *
     * @param claim Populated claim
     * @return User object
     * @throws IdentityStoreException IdentityStore Exception
     * @throws UserNotFoundException  when trying to get user with incorrect unique user id
     */
    User getUser(Claim claim) throws IdentityStoreException, UserNotFoundException;

    /**
     * Retrieve a user by claim from a specific domain.
     *
     * @param claim      Populated claim
     * @param domainName Domain name to retrieve user from
     * @return User object
     * @throws IdentityStoreException IdentityStore Exception
     * @throws UserNotFoundException  when trying to get user with incorrect unique user id
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
     * @param claim      Populated claim
     * @param offset     Start position
     * @param length     Number of Users to retrieve
     * @param domainName The domain to retrieve users from
     * @return List of users
     * @throws IdentityStoreException IdentityStore Exception
     */
    List<User> listUsers(Claim claim, int offset, int length, String domainName) throws IdentityStoreException;

    /**
     * List a set of users that matches a given claim in a specific domain.
     *
     * @param metaClaim     Meta claim
     * @param filterPattern filter pattern to search user
     * @param offset        start index of the user
     * @param length        number of users to retrieve
     * @return List of users
     * @throws IdentityStoreException IdentityStore Exception
     */
    List<User> listUsers(MetaClaim metaClaim, String filterPattern, int offset, int length)
            throws IdentityStoreException;

    /**
     * List a set of users that matches a given claim in a specified range in a specific domain.
     *
     * @param metaClaim     Meta claim
     * @param filterPattern filter pattern to search user
     * @param offset        start index of the user
     * @param length        number of users to retrieve
     * @param domainName    domain of the user
     * @return List of users
     * @throws IdentityStoreException IdentityStore Exception
     */
    List<User> listUsers(MetaClaim metaClaim, String filterPattern, int offset, int length, String domainName)
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
     * @param domainName    The domain to retrieve group from
     * @return Group
     * @throws IdentityStoreException IdentityStore Exception
     * @throws GroupNotFoundException when group is not found
     */
    Group getGroup(String uniqueGroupId, String domainName) throws IdentityStoreException, GroupNotFoundException;

    /**
     * Get group that matches a claim.
     *
     * @param claim Populated claim
     * @return Group
     * @throws IdentityStoreException IdentityStore Exception
     * @throws GroupNotFoundException when group is not found
     */
    Group getGroup(Claim claim) throws IdentityStoreException, GroupNotFoundException;

    /**
     * Get group that matches a claim from a specific domain.
     *
     * @param claim      Populated claim
     * @param domainName The domain to retrieve groups from
     * @return Group
     * @throws IdentityStoreException IdentityStore Exception
     * @throws GroupNotFoundException when group is not found
     */
    Group getGroup(Claim claim, String domainName) throws IdentityStoreException, GroupNotFoundException;

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
     * @param offset     Start position
     * @param length     Number of groups to retrieve
     * @param domainName The domain to retrieve groups from
     * @return List of groups within given range in the given domain
     * @throws IdentityStoreException IdentityStore Exception
     */
    List<Group> listGroups(int offset, int length, String domainName) throws IdentityStoreException;

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
     * @param claim      Populated claim
     * @param offset     Start position
     * @param length     Number of groups to retrieve
     * @param domainName The domain to retrieve groups from
     * @return List of groups that matches the given claim in the given range in the given domain
     * @throws IdentityStoreException IdentityStore Exception
     */
    List<Group> listGroups(Claim claim, int offset, int length, String domainName) throws IdentityStoreException;

    /**
     * List groups that matches a given claim in a given range.
     *
     * @param metaClaim     Meta claim
     * @param filterPattern filter pattern to search
     * @param offset        start index of the group
     * @param length        number of users to retrieve
     * @return List of groups
     * @throws IdentityStoreException IdentityStore Exception
     */
    List<Group> listGroups(MetaClaim metaClaim, String filterPattern, int offset, int length)
            throws IdentityStoreException;

    /**
     * List groups that matches a given claim in a given range for a specific domain.
     *
     * @param metaClaim     Meta claim
     * @param filterPattern filter pattern to search
     * @param offset        start index of the group
     * @param length        number of users to retrieve
     * @param domainName    domain of group
     * @return List of groups
     * @throws IdentityStoreException IdentityStore Exception
     */
    List<Group> listGroups(MetaClaim metaClaim, String filterPattern, int offset, int length, String domainName)
            throws IdentityStoreException;

    /**
     * Get list of groups a user belongs to.
     *
     * @param uniqueUserId The Id of the user
     * @return List of groups the user is in
     * @throws IdentityStoreException IdentityStore Exception
     * @throws UserNotFoundException  User Not Found Exception
     */
    List<Group> getGroupsOfUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException;

    /**
     * Get list of groups a user belongs to in a specific domain.
     *
     * @param uniqueUserId The Id of the user
     * @param domainName   The domain the users belongs to
     * @return List of groups the user is in
     * @throws IdentityStoreException IdentityStore Exception
     * @throws UserNotFoundException  User Not Found Exception
     */
    List<Group> getGroupsOfUser(String uniqueUserId, String domainName) throws IdentityStoreException,
            UserNotFoundException;

    /**
     * Get list of users in a given group.
     *
     * @param uniqueGroupId The group to find users of
     * @return List of users contained in the group
     * @throws IdentityStoreException IdentityStore Exception
     */
    List<User> getUsersOfGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException;

    /**
     * Get list of users in a given group for a specific domain.
     *
     * @param uniqueGroupId The group to find users of
     * @param domainName    The domain the user belongs to
     * @return List of users contained in the group
     * @throws IdentityStoreException IdentityStore Exception
     */
    List<User> getUsersOfGroup(String uniqueGroupId, String domainName) throws IdentityStoreException,
            GroupNotFoundException;

    /**
     * Check if a user belongs to a given group.
     *
     * @param uniqueUserId  The user Id
     * @param uniqueGroupId The group Id
     * @return True if user belongs to the given group
     * @throws IdentityStoreException IdentityStore Exception
     * @throws UserNotFoundException  User Not Found Exception
     */
    boolean isUserInGroup(String uniqueUserId, String uniqueGroupId) throws IdentityStoreException,
            UserNotFoundException;

    /**
     * Check if a user belongs to a given group in a specific domain.
     *
     * @param uniqueUserId  The user Id
     * @param uniqueGroupId The group Id
     * @param domainName    The domain the user and the group belongs to
     * @return True if user belongs to the given group
     * @throws IdentityStoreException IdentityStore Exception
     */
    boolean isUserInGroup(String uniqueUserId, String uniqueGroupId, String domainName) throws
            IdentityStoreException, UserNotFoundException;

    /**
     * Get all claims of a user.
     *
     * @param uniqueUserId The user Id.
     * @throws IdentityStoreException Identity Store Exception
     * @throws UserNotFoundException  User Not Found Exception
     */
    List<Claim> getClaimsOfUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException;

    /**
     * Get all claims of a user.
     *
     * @param uniqueUserId The user Id.
     * @return domainName domainName.
     * @throws IdentityStoreException Identity Store Exception
     * @throws UserNotFoundException  User Not Found Exception
     */
    List<Claim> getClaimsOfUser(String uniqueUserId, String domainName) throws IdentityStoreException,
            UserNotFoundException;

    /**
     * Get all claims of a user for given URIs.
     *
     * @param uniqueUserId The user to retrieve claims for
     * @param metaClaims   List of meta claims to retrieve claims for
     * @return List of claims
     * @throws IdentityStoreException IdentityStore Exception
     * @throws UserNotFoundException  User Not Found Exception
     */
    List<Claim> getClaimsOfUser(String uniqueUserId, List<MetaClaim> metaClaims) throws IdentityStoreException,
            UserNotFoundException;

    /**
     * Get all claims of a user for given URIs.
     *
     * @param uniqueUserId The user to retrieve claims for
     * @param metaClaims   List of meta claims to retrieve claims for
     * @param domainName   Domain name
     * @return List of claims
     * @throws IdentityStoreException IdentityStore Exception
     * @throws UserNotFoundException  User Not Found Exception
     */
    List<Claim> getClaimsOfUser(String uniqueUserId, List<MetaClaim> metaClaims, String domainName) throws
            IdentityStoreException, UserNotFoundException;

    /**
     * Get all claims of a group.
     *
     * @param uniqueGroupId The group Id.
     * @throws IdentityStoreException Identity Store Exception
     * @throws GroupNotFoundException  Group Not Found Exception
     */
    List<Claim> getClaimsOfGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException;

    /**
     * Get all claims of a group.
     *
     * @param uniqueGroupId The group Id.
     * @return domainName domainName.
     * @throws IdentityStoreException Identity Store Exception
     * @throws GroupNotFoundException  Group Not Found Exception
     */
    List<Claim> getClaimsOfGroup(String uniqueGroupId, String domainName) throws IdentityStoreException,
            GroupNotFoundException;

    /**
     * Get all claims of a group for given URIs.
     *
     * @param uniqueGroupId The group to retrieve claims for
     * @param metaClaims   List of meta claims to retrieve claims for
     * @return List of claims
     * @throws IdentityStoreException IdentityStore Exception
     * @throws GroupNotFoundException  Group Not Found Exception
     */
    List<Claim> getClaimsOfGroup(String uniqueGroupId, List<MetaClaim> metaClaims) throws IdentityStoreException,
            GroupNotFoundException;

    /**
     * Get all claims of a group for given URIs.
     *
     * @param uniqueGroupId The group to retrieve claims for
     * @param metaClaims   List of meta claims to retrieve claims for
     * @param domainName   Domain name
     * @return List of claims
     * @throws IdentityStoreException IdentityStore Exception
     * @throws GroupNotFoundException  Group Not Found Exception
     */
    List<Claim> getClaimsOfGroup(String uniqueGroupId, List<MetaClaim> metaClaims, String domainName) throws
            IdentityStoreException, GroupNotFoundException;

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
     * @param user       User model.
     * @param domainName User domain.
     * @return Created user.
     * @throws IdentityStoreException Identity store exception.
     */
    User addUser(UserModel user, String domainName) throws IdentityStoreException;

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
     * @param users      User models.
     * @param domainName User domain.
     * @return Created users.
     * @throws IdentityStoreException Identity store exception.
     */
    List<User> addUsers(List<UserModel> users, String domainName) throws IdentityStoreException;

    /**
     * Update user claims by user id.
     *
     * @param uniqueUserId User unique id.
     * @param claims       User claims.
     * @throws IdentityStoreException Identity store exception.
     * @throws UserNotFoundException  User Not Found Exception
     */
    void updateUserClaims(String uniqueUserId, List<Claim> claims) throws IdentityStoreException, UserNotFoundException;

    /**
     * Update user claims by user id.
     *
     * @param uniqueUserId User unique id.
     * @param claims       User claims.
     * @param domainName   domain name.
     * @throws IdentityStoreException Identity store exception.
     * @throws UserNotFoundException  User Not Found Exception
     */
    void updateUserClaims(String uniqueUserId, List<Claim> claims, String domainName) throws
            IdentityStoreException, UserNotFoundException;

    /**
     * Update selected user claims by user id.
     *
     * @param uniqueUserId   User unique id.
     * @param claimsToAdd    user claims to update.
     * @param claimsToRemove user claims to remove.
     * @throws IdentityStoreException Identity store exception.
     * @throws UserNotFoundException  User Not Found Exception
     */
    void updateUserClaims(String uniqueUserId, List<Claim> claimsToAdd, List<Claim> claimsToRemove) throws
            IdentityStoreException, UserNotFoundException;


    /**
     * Update selected user claims by user id.
     *
     * @param uniqueUserId   User unique id.
     * @param claimsToAdd    user claims to update.
     * @param claimsToRemove user claims to remove.
     * @param domainName     domain name.
     * @throws IdentityStoreException Identity store exception.
     * @throws UserNotFoundException  User Not Found Exception
     */
    void updateUserClaims(String uniqueUserId, List<Claim> claimsToAdd, List<Claim> claimsToRemove, String
            domainName) throws IdentityStoreException, UserNotFoundException;

    /**
     * Delete a user by user id.
     *
     * @param uniqueUserId User unique id.
     * @throws IdentityStoreException Identity store exception.
     * @throws UserNotFoundException  User Not Found Exception
     */
    void deleteUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException;

    /**
     * Delete a user by user id.
     *
     * @param uniqueUserId User unique id.
     * @param domainName   domain name.
     * @throws IdentityStoreException Identity store exception.
     * @throws UserNotFoundException  User Not Found Exception
     */
    void deleteUser(String uniqueUserId, String domainName) throws IdentityStoreException,
            UserNotFoundException;

    /**
     * Update groups of a user by user id.
     *
     * @param uniqueUserId   User unique id.
     * @param uniqueGroupIds Group unique id list.
     * @throws IdentityStoreException Identity store exception.
     */
    void updateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIds) throws IdentityStoreException;

    /**
     * Update groups of a user by user id.
     *
     * @param uniqueUserId   User unique id.
     * @param uniqueGroupIds Group unique id list.
     * @param domainName     domain name.
     * @throws IdentityStoreException Identity store exception.
     */
    void updateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIds, String domainName) throws
            IdentityStoreException;

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
     * Update selected groups of a user by user id.
     *
     * @param uniqueUserId           User unique id.
     * @param uniqueGroupIdsToAdd    Group ids to add.
     * @param uniqueGroupIdsToRemove Group ids to remove.
     * @param domainName             domain name.
     * @throws IdentityStoreException Identity store exception.
     */
    void updateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIdsToAdd, List<String>
            uniqueGroupIdsToRemove, String domainName) throws IdentityStoreException;

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
     * @param domainName Group damian.
     * @return Created group.
     * @throws IdentityStoreException Identity store exception.
     */
    Group addGroup(GroupModel groupModel, String domainName) throws IdentityStoreException;

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
     * @param groups     Group models.
     * @param domainName Group domain.
     * @return Created groups.
     * @throws IdentityStoreException Identity store exception.
     */
    List<Group> addGroups(List<GroupModel> groups, String domainName) throws IdentityStoreException;

    /**
     * Update group claims by group id.
     *
     * @param uniqueGroupId Group unique id.
     * @param claims        Group claims.
     * @throws IdentityStoreException Identity store exception.
     */
    void updateGroupClaims(String uniqueGroupId, List<Claim> claims) throws IdentityStoreException,
            GroupNotFoundException;

    /**
     * Update group claims by group id.
     *
     * @param uniqueGroupId Group unique id.
     * @param claims        Group claims.
     * @param domainName    Group domain.
     * @throws IdentityStoreException Identity store exception.
     */
    void updateGroupClaims(String uniqueGroupId, List<Claim> claims, String domainName) throws
            IdentityStoreException, GroupNotFoundException;

    /**
     * Update selected group claims by group id.
     *
     * @param uniqueGroupId  Group unique id.
     * @param claimsToAdd    Group ids to add.
     * @param claimsToRemove Group ids to remove.
     * @throws IdentityStoreException Identity store exception.
     */
    void updateGroupClaims(String uniqueGroupId, List<Claim> claimsToAdd, List<Claim> claimsToRemove) throws
            IdentityStoreException, GroupNotFoundException;

    /**
     * Update selected group claims by group id.
     *
     * @param uniqueGroupId  Group unique id.
     * @param claimsToAdd    Group ids to add.
     * @param claimsToRemove Group ids to remove.
     * @param domainName     Group domain.
     * @throws IdentityStoreException Identity store exception.
     */
    void updateGroupClaims(String uniqueGroupId, List<Claim> claimsToAdd, List<Claim> claimsToRemove,
                           String domainName) throws IdentityStoreException, GroupNotFoundException;

    /**
     * Delete a group by group id.
     *
     * @param uniqueGroupId Group unique id.
     * @throws IdentityStoreException Identity store exception.
     */
    void deleteGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException;

    /**
     * Delete a group by group id.
     *
     * @param uniqueGroupId Group unique id.
     * @param domainName    Group domain.
     * @throws IdentityStoreException Identity store exception.
     */
    void deleteGroup(String uniqueGroupId, String domainName) throws IdentityStoreException, GroupNotFoundException;

    /**
     * Update users of a group by group id.
     *
     * @param uniqueGroupId Group unique id.
     * @param uniqueUserIds User unique id list.
     * @throws IdentityStoreException Identity store exception.
     */
    void updateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIds) throws IdentityStoreException;

    /**
     * Update users of a group by group id.
     *
     * @param uniqueGroupId Group unique id.
     * @param uniqueUserIds User unique id list.
     * @param domainName    Group domain.
     * @throws IdentityStoreException Identity store exception.
     */
    void updateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIds, String domainName) throws
            IdentityStoreException;

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
     * Update selected users of a group by group id.
     *
     * @param uniqueGroupId         Group unique id.
     * @param uniqueUserIdsToAdd    User unique id list to add.
     * @param uniqueUserIdsToRemove User unique id list to remove.
     * @param domainName            Group domain.
     * @throws IdentityStoreException Identity store exception.
     */
    void updateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIdsToAdd, List<String>
            uniqueUserIdsToRemove, String domainName) throws IdentityStoreException;

    /**
     * Authenticate the user.
     *
     * @param claim      Unique claim.
     * @param credentials Credentials.
     * @param domainName Domain name.
     * @return Authentication context.
     * @throws AuthenticationFailure Authentication failure.
     */
    AuthenticationContext authenticate(Claim claim, Callback[] credentials, String domainName)
            throws AuthenticationFailure;

    /**
     * Get primary domain name.
     *
     * @return primary domain name.
     * @throws IdentityStoreException Identity store exception.
     */
    String getPrimaryDomainName() throws IdentityStoreException;

    /**
     * Get all domain names.
     *
     * @return domain names list.
     * @throws IdentityStoreException Identity store exception.
     */
    Set<String> getDomainNames() throws IdentityStoreException;
}

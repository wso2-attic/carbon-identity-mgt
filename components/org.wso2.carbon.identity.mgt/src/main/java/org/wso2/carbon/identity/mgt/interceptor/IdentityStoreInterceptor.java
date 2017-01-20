/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.mgt.interceptor;

import org.wso2.carbon.identity.mgt.AuthenticationContext;
import org.wso2.carbon.identity.mgt.Group;
import org.wso2.carbon.identity.mgt.User;
import org.wso2.carbon.identity.mgt.bean.GroupBean;
import org.wso2.carbon.identity.mgt.bean.UserBean;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.claim.MetaClaim;
import org.wso2.carbon.identity.mgt.exception.AuthenticationFailure;
import org.wso2.carbon.identity.mgt.exception.GroupNotFoundException;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;

import java.util.List;
import java.util.Set;
import javax.security.auth.callback.Callback;

/**
 * Listener interface for IdentityStore
 * @since 1.0.0
 */
public interface IdentityStoreInterceptor {

    /**
     * Get the execution order identifier for this interceptor.
     * The interceptor execution order will be from lowest to the highest.
     *
     * @return The execution order identifier integer value.
     */
    int getExecutionOrderId();

    /**
     * Get whether the interceptor is enabled or not.
     *
     * @return If interceptor is enables returns true, otherwise false.
     */
    boolean isEnabled();

    /**
     * Triggers prior to getting the user from unique user ID.
     *
     * @param uniqueUserId Unique user ID of the user.
     * @throws IdentityStoreException Identity Store Exception.
     * @throws UserNotFoundException User not found exception.
     */
    void doPreGetUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException;

    /**
     * Triggers post to getting the user from unique user ID.
     *
     * @param uniqueUserId Username of the Unique user ID.
     * @param user User result to be returned from getUser method.
     * @throws IdentityStoreException Identity Store Exception.
     * @throws UserNotFoundException User not found exception.
     */
    void doPostGetUser(String uniqueUserId, User user) throws IdentityStoreException, UserNotFoundException;

    /**
     * Triggers prior to getting the user from a claim.
     *
     * @param claim User claim.
     * @throws IdentityStoreException Identity Store Exception.
     * @throws UserNotFoundException User Not Found Exception.
     */
    void doPreGetUser(Claim claim) throws IdentityStoreException, UserNotFoundException;

    /**
     * Triggers post to getting the user from a claim.
     *
     * @param claim User claim.
     * @param user User result to be returned from getUser method.
     * @throws IdentityStoreException Identity Store Exception.
     * @throws UserNotFoundException User Not Found Exception.
     */
    void doPostGetUser(Claim claim, User user) throws IdentityStoreException, UserNotFoundException;

    /**
     * Triggers prior to getting the user from user Id.
     *
     * @param claim User claim.
     * @param domainName Domain name.
     * @throws IdentityStoreException Identity Store Exception.
     */
    void doPreGetUser(Claim claim, String domainName) throws IdentityStoreException;

    /**
     * Triggers post to getting the user from user Id.
     *
     * @param claim User claim.
     * @param domainName Domain name.
     * @param user User result to be returned from getUserFromId method.
     * @throws IdentityStoreException Identity Store Exception.
     */
    void doPostGetUser(Claim claim, String domainName, User user) throws IdentityStoreException;

    /**
     * Triggers prior to listing all users in User Store.
     *
     * @param offset Offset for list of users.
     * @param length Length from the offset.
     * @throws IdentityStoreException Identity Store Exception.
     */
    void doPreListUsers(int offset, int length) throws IdentityStoreException;

    /**
     * Triggers post to listing all users in User Store according to the filter pattern.
     *
     * @param offset Offset for list of users.
     * @param length Length from the offset.
     * @param users User list to be returned from listUsers method.
     * @throws IdentityStoreException Identity Store Exception.
     */
    void doPostListUsers(int offset, int length, List<User> users) throws IdentityStoreException;

    /**
     * Triggers prior to listing all users in User Store.
     *
     * @param offset Offset for list of users.
     * @param length Length from the offset.
     * @param domainName The domain name to retrieve users from.
     * @throws IdentityStoreException Identity Store Exception.
     */
    void doPreListUsers(int offset, int length, String domainName) throws IdentityStoreException;

    /**
     * Triggers post to listing all users in User Store according to the filter pattern.
     *
     * @param offset Offset for list of users.
     * @param length Length from the offset.
     * @param domainName The domain name to retrieve users from.
     * @param users User list to be returned from listUsers method.
     * @throws IdentityStoreException Identity Store Exception.
     */
    void doPostListUsers(int offset, int length, String domainName, List<User> users) throws IdentityStoreException;

    /**
     * Triggers prior to listing all users that matches a given claim.
     *
     * @param claim  Populated claim
     * @param offset Offset for list of users.
     * @param length Length from the offset.
     * @throws IdentityStoreException Identity Store Exception.
     */
    void doPreListUsers(Claim claim, int offset, int length) throws IdentityStoreException;

    /**
     * Triggers post to listing all users that matches a given claim.
     *
     * @param claim  Populated claim
     * @param offset Offset for list of users.
     * @param length Length from the offset.
     * @param users User list to be returned from listUsers method.
     * @throws IdentityStoreException Identity Store Exception.
     */
    void doPostListUsers(Claim claim, int offset, int length, List<User> users) throws IdentityStoreException;

    /**
     * Triggers prior to listing all users that matches a given claim.
     *
     * @param claim  Populated claim
     * @param offset Offset for list of users.
     * @param length Length from the offset.
     * @throws IdentityStoreException Identity Store Exception.
     */
    void doPreListUsers(Claim claim, int offset, int length, String domainName) throws IdentityStoreException;

    /**
     * Triggers post to listing all users that matches a given claim.
     *
     * @param claim  Populated claim
     * @param offset Offset for list of users.
     * @param length Length from the offset.
     * @param users User list to be returned from listUsers method.
     * @throws IdentityStoreException Identity Store Exception.
     */
    void doPostListUsers(Claim claim, int offset, int length, String domainName, List<User> users) throws
            IdentityStoreException;

    /**
     * Triggers prior to listing all users in User Store according to the filter pattern.
     *
     * @param filterPattern Filter patter to filter users.
     * @param offset Offset for list of users.
     * @param length Length from the offset.
     * @throws IdentityStoreException Identity Store Exception.
     */
    void doPreListUsers(MetaClaim metaClaim, String filterPattern, int offset, int length) throws
            IdentityStoreException;

    /**
     * Triggers post to listing all users in User Store according to the filter pattern.
     *
     * @param filterPattern Filter patter to filter users.
     * @param offset Offset for list of users.
     * @param length Length from the offset.
     * @param users User list to be returned from listUsers method.
     * @throws IdentityStoreException Identity Store Exception.
     */
    void doPostListUsers(MetaClaim metaClaim, String filterPattern, int offset, int length, List<User> users) throws
            IdentityStoreException;

    /**
     * Triggers prior to listing all users in User Store according to the filter pattern.
     *
     * @param filterPattern Filter patter to filter users.
     * @param offset Offset for list of users.
     * @param length Length from the offset.
     * @throws IdentityStoreException Identity Store Exception.
     */
    void doPreListUsers(MetaClaim metaClaim, String filterPattern, int offset, int length, String domainName) throws
            IdentityStoreException;

    /**
     * Triggers post to listing all users in User Store according to the filter pattern.
     *
     * @param filterPattern Filter patter to filter users.
     * @param offset Offset for list of users.
     * @param length Length from the offset.
     * @param users User list to be returned from listUsers method.
     * @throws IdentityStoreException Identity Store Exception.
     */
    void doPostListUsers(MetaClaim metaClaim, String filterPattern, int offset, int length, String domainName,
                         List<User> users) throws IdentityStoreException;

    /**
     * Triggers prior to getting the group from name.
     *
     * @param uniqueGroupId Name of the group.
     * @throws IdentityStoreException Identity Store Exception.
     * @throws GroupNotFoundException Group not found exception.
     */
    void doPreGetGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException;

    /**
     * Triggers post to getting the group from name.
     *
     * @param uniqueGroupId Name of the group.
     * @param group Group result to be returned from getGroup method.
     * @throws IdentityStoreException Identity Store Exception.
     * @throws GroupNotFoundException Group not found exception.
     */
    void doPostGetGroup(String uniqueGroupId, Group group) throws IdentityStoreException, GroupNotFoundException;

    /**
     * Triggers prior to getting the group from name.
     *
     * @param claim Name of the group.
     * @throws IdentityStoreException Identity Store Exception.
     * @throws GroupNotFoundException Group not found exception.
     */
    void doPreGetGroup(Claim claim) throws IdentityStoreException, GroupNotFoundException;

    /**
     * Triggers post to getting the group from name.
     *
     * @param claim Name of the group.
     * @param group Group result to be returned from getGroup method.
     * @throws IdentityStoreException Identity Store Exception.
     * @throws GroupNotFoundException Group not found exception.
     */
    void doPostGetGroup(Claim claim, Group group) throws IdentityStoreException, GroupNotFoundException;

    /**
     * Triggers prior to getting the group from name.
     *
     * @param claim Name of the group.
     * @throws IdentityStoreException Identity Store Exception.
     * @throws GroupNotFoundException Group not found exception.
     */
    void doPreGetGroup(Claim claim, String domainName) throws IdentityStoreException, GroupNotFoundException;

    /**
     * Triggers post to getting the group from name.
     *
     * @param claim Name of the group.
     * @param group Group result to be returned from getGroup method.
     * @throws IdentityStoreException Identity Store Exception.
     * @throws GroupNotFoundException Group not found exception.
     */
    void doPostGetGroup(Claim claim, String domainName, Group group) throws IdentityStoreException,
            GroupNotFoundException;

    /**
     * Triggers prior to listing groups according to the filter pattern.
     *
     * @param offset Offset for the group list.
     * @param length Length from the offset.
     * @throws IdentityStoreException Identity Store Exception.
     */
    void doPreListGroups(int offset, int length) throws IdentityStoreException;

    /**
     * Triggers post to listing groups according to the filter pattern.
     *
     * @param offset Offset for the group list.
     * @param length Length from the offset.
     * @param groups Group list to be returned from listGroups method.
     * @throws IdentityStoreException Identity Store Exception.
     */
    void doPostListGroups(int offset, int length, List<Group> groups)
            throws IdentityStoreException;

    /**
     * Triggers prior to listing groups according to the filter pattern.
     *
     * @param offset Offset for the group list.
     * @param length Length from the offset.
     * @throws IdentityStoreException Identity Store Exception.
     */
    void doPreListGroups(int offset, int length, String domainName) throws IdentityStoreException;

    /**
     * Triggers post to listing groups according to the filter pattern.
     *
     * @param offset Offset for the group list.
     * @param length Length from the offset.
     * @param groups Group list to be returned from listGroups method.
     * @throws IdentityStoreException Identity Store Exception.
     */
    void doPostListGroups(int offset, int length, String domainName, List<Group> groups)
            throws IdentityStoreException;

    /**
     * Triggers prior to listing groups according to the filter pattern.
     *
     * @param offset Offset for the group list.
     * @param length Length from the offset.
     * @throws IdentityStoreException Identity Store Exception.
     */
    void doPreListGroups(Claim claim, int offset, int length) throws IdentityStoreException;

    /**
     * Triggers post to listing groups according to the filter pattern.
     *
     * @param offset Offset for the group list.
     * @param length Length from the offset.
     * @param groups Group list to be returned from listGroups method.
     * @throws IdentityStoreException Identity Store Exception.
     */
    void doPostListGroups(Claim claim, int offset, int length, List<Group> groups)
            throws IdentityStoreException;

    /**
     * Triggers prior to listing groups according to the filter pattern.
     *
     * @param offset Offset for the group list.
     * @param length Length from the offset.
     * @throws IdentityStoreException Identity Store Exception.
     */
    void doPreListGroups(MetaClaim metaClaim, String filterPattern, int offset, int length) throws
            IdentityStoreException;

    /**
     * Triggers post to listing groups according to the filter pattern.
     *
     * @param offset Offset for the group list.
     * @param length Length from the offset.
     * @param groups Group list to be returned from listGroups method.
     * @throws IdentityStoreException Identity Store Exception.
     */
    void doPostListGroups(MetaClaim metaClaim, String filterPattern, int offset, int length, List<Group> groups)
            throws IdentityStoreException;

    /**
     * Triggers prior to listing groups according to the filter pattern.
     *
     * @param offset Offset for the group list.
     * @param length Length from the offset.
     * @throws IdentityStoreException Identity Store Exception.
     */
    void doPreListGroups(MetaClaim metaClaim, String filterPattern, int offset, int length, String domainName) throws
            IdentityStoreException;

    /**
     * Triggers post to listing groups according to the filter pattern.
     *
     * @param offset Offset for the group list.
     * @param length Length from the offset.
     * @param groups Group list to be returned from listGroups method.
     * @throws IdentityStoreException Identity Store Exception.
     */
    void doPostListGroups(MetaClaim metaClaim, String filterPattern, int offset, int length, String domainName,
                          List<Group> groups) throws IdentityStoreException;

    /**
     * Triggers prior to listing groups according to the filter pattern.
     *
     * @param offset Offset for the group list.
     * @param length Length from the offset.
     * @throws IdentityStoreException Identity Store Exception.
     */
    void doPreListGroups(Claim claim, int offset, int length, String domainName) throws IdentityStoreException;

    /**
     * Triggers post to listing groups according to the filter pattern.
     *
     * @param offset Offset for the group list.
     * @param length Length from the offset.
     * @param groups Group list to be returned from listGroups method.
     * @throws IdentityStoreException Identity Store Exception.
     */
    void doPostListGroups(Claim claim, int offset, int length, String domainName, List<Group> groups)
            throws IdentityStoreException;


    /**
     * Triggers prior to getting the groups assigned to the specified user.
     *
     * @param uniqueUserId Id of the user.
     * @throws IdentityStoreException Identity Store Exception.
     */
    void doPreGetGroupsOfUser(String uniqueUserId) throws IdentityStoreException;


    /**
     * Triggers post to getting the groups assigned to the specified user.
     *
     * @param uniqueUserId Id of the user.
     * @param groups Group list to be returned from getGroupsOfUser method.
     * @throws IdentityStoreException Identity Store Exception.
     */
    void doPostGetGroupsOfUser(String uniqueUserId, List<Group> groups) throws IdentityStoreException;

    /**
     * Triggers prior to getting the users assigned to the specified group.
     *
     * @param uniqueGroupId Id of the group.
     * @throws IdentityStoreException Identity Store Exception.
     */
    void doPreGetUsersOfGroup(String uniqueGroupId) throws IdentityStoreException;

    /**
     * Triggers post to getting the users assigned to the specified group.
     *
     * @param uniqueGroupId Id of the group.
     * @param users Users list to be returned from getUsersOfGroup method.
     * @throws IdentityStoreException Identity Store Exception.
     */
    void doPostGetUsersOfGroup(String uniqueGroupId, List<User> users) throws IdentityStoreException;

    /**
     * Triggers prior to checking whether the user is in the group.
     *
     * @param uniqueUserId Id of the user.
     * @throws IdentityStoreException Identity Store Exception.
     */
    void doPreIsUserInGroup(String uniqueUserId, String uniqueGroupId) throws IdentityStoreException;

    /**
     * Triggers post to checking whether the user is in the group.
     *
     * @param uniqueUserId Id of the user.
     * @param uniqueGroupId Id of the group.
     * @throws IdentityStoreException Identity Store Exception.
     */
    void doPostIsUserInGroup(String uniqueUserId, String uniqueGroupId, Boolean isUserInGroup) throws
            IdentityStoreException;

    void doPreGetClaimsOfUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException;

    void doPostGetClaimsOfUser(String uniqueUserId, List<Claim> claims) throws IdentityStoreException,
            UserNotFoundException;

    void doPreGetClaimsOfUser(String uniqueUserId, List<MetaClaim> metaClaims) throws IdentityStoreException,
            UserNotFoundException;

    void doPostGetClaimsOfUser(String uniqueUserId, List<MetaClaim> metaClaims, List<Claim> claims) throws
            IdentityStoreException, UserNotFoundException;

    void doPreGetClaimsOfGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException;

    void doPostGetClaimsOfGroup(String uniqueGroupId, List<Claim> claims) throws IdentityStoreException,
            GroupNotFoundException;

    void doPreGetClaimsOfGroup(String uniqueGroupId, List<MetaClaim> metaClaims) throws IdentityStoreException,
            GroupNotFoundException;

    void doPostGetClaimsOfGroup(String uniqueGroupId, List<MetaClaim> metaClaims, List<Claim> claims) throws
            IdentityStoreException, GroupNotFoundException;

    void doPreAddUser(UserBean user) throws IdentityStoreException;

    void doPostAddUser(UserBean userBean, User user) throws IdentityStoreException;

    void doPreAddUser(UserBean user, String domainName) throws IdentityStoreException;

    void doPostAddUser(UserBean userBean, String domainName, User user) throws IdentityStoreException;

    void doPreAddUsers(List<UserBean> users) throws IdentityStoreException;

    void doPostAddUsers(List<UserBean> userBeans, List<User> users) throws IdentityStoreException;

    void doPreAddUsers(List<UserBean> users, String domainName) throws IdentityStoreException;

    void doPostAddUsers(List<UserBean> userBeans, String domainName, List<User> users) throws IdentityStoreException;

    void doPreUpdateUserClaims(String uniqueUserId, List<Claim> claims) throws IdentityStoreException,
            UserNotFoundException;

    void doPostUpdateUserClaims(String uniqueUserId, List<Claim> claims) throws IdentityStoreException,
            UserNotFoundException;

    void doPreUpdateUserClaims(String uniqueUserId, List<Claim> claimsToAdd, List<Claim> claimsToRemove) throws
            IdentityStoreException, UserNotFoundException;

    void doPostUpdateUserClaims(String uniqueUserId, List<Claim> claimsToAdd, List<Claim> claimsToRemove) throws
            IdentityStoreException, UserNotFoundException;

    void doPreUpdateUserCredentials(String uniqueUserId, List<Callback> credentials) throws IdentityStoreException,
            UserNotFoundException;

    void doPostUpdateUserCredentials(String uniqueUserId, List<Callback> credentials) throws IdentityStoreException,
            UserNotFoundException;

    void doPreUpdateUserCredentials(String uniqueUserId, List<Callback> credentialsToAdd, List<Callback>
            credentialsToRemove) throws IdentityStoreException, UserNotFoundException;

    void doPostUpdateUserCredentials(String uniqueUserId, List<Callback> credentialsToAdd, List<Callback>
            credentialsToRemove) throws IdentityStoreException, UserNotFoundException;

    void doPreDeleteUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException;

    void doPostDeleteUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException;

    void doPreUpdateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIds) throws IdentityStoreException;

    void doPostUpdateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIds) throws IdentityStoreException;

    void doPreUpdateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIdsToAdd, List<String>
            uniqueGroupIdsToRemove) throws IdentityStoreException;

    void doPostUpdateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIdsToAdd, List<String>
            uniqueGroupIdsToRemove) throws IdentityStoreException;

    void doPreAddGroup(GroupBean groupBean) throws IdentityStoreException;

    void doPostAddGroup(GroupBean groupBean, Group group) throws IdentityStoreException;

    void doPreAddGroup(GroupBean groupBean, String domainName) throws IdentityStoreException;

    void doPostAddGroup(GroupBean groupBean, String domainName, Group group) throws IdentityStoreException;

    void doPreAddGroups(List<GroupBean> groupsBeans) throws IdentityStoreException;

    void doPostAddGroups(List<GroupBean> groupBeans, List<Group> groups) throws IdentityStoreException;

    void doPreAddGroups(List<GroupBean> groupBeans, String domainName) throws IdentityStoreException;

    void doPostAddGroups(List<GroupBean> groupsBeans, String domainName, List<Group> groups) throws
            IdentityStoreException;

    void doPreUpdateGroupClaims(String uniqueGroupId, List<Claim> claims) throws IdentityStoreException,
            GroupNotFoundException;

    void doPostUpdateGroupClaims(String uniqueGroupId, List<Claim> claims) throws IdentityStoreException,
            GroupNotFoundException;

    void doPreUpdateGroupClaims(String uniqueGroupId, List<Claim> claimsToAdd, List<Claim> claimsToRemove) throws
            IdentityStoreException, GroupNotFoundException;

    void doPostUpdateGroupClaims(String uniqueGroupId, List<Claim> claimsToAdd, List<Claim> claimsToRemove) throws
            IdentityStoreException, GroupNotFoundException;

    void doPreDeleteGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException;

    void doPostDeleteGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException;

    void doPreUpdateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIds) throws IdentityStoreException;

    void doPostUpdateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIds) throws IdentityStoreException;

    void doPreUpdateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIdsToAdd, List<String>
            uniqueUserIdsToRemove) throws IdentityStoreException;

    void doPostUpdateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIdsToAdd, List<String>
            uniqueUserIdsToRemove) throws IdentityStoreException;

    void doPreAuthenticate(Claim claim, Callback[] credentials, String domainName) throws AuthenticationFailure,
            IdentityStoreException;

    void doPostAuthenticate(Claim claim, Callback[] credentials, String domainName, AuthenticationContext
            authenticationContext) throws AuthenticationFailure, IdentityStoreException;

    void doPreGetPrimaryDomainName() throws IdentityStoreException;

    void doPostGetPrimaryDomainName(String primaryDomainName) throws IdentityStoreException;

    void doPreGetDomainNames() throws IdentityStoreException;

    void doPostGetDomainNames(Set<String> domainNames) throws IdentityStoreException;

}

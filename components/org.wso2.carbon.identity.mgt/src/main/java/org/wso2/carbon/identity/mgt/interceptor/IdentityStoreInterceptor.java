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
 * Interceptor interface for IdentityStore
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
     * Triggers prior to getting the user from unique user ID.
     *
     * @param uniqueUserId Unique user ID of the user.
     * @throws IdentityStoreException Identity store exception.
     * @throws UserNotFoundException User not found exception.
     */
    void doPreGetUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException;

    /**
     * Triggers post to getting the user from unique user ID.
     *
     * @param uniqueUserId User's Unique ID.
     * @param user User result to be returned from getUser method.
     * @throws IdentityStoreException Identity store exception.
     * @throws UserNotFoundException User not found exception.
     */
    void doPostGetUser(String uniqueUserId, User user) throws IdentityStoreException, UserNotFoundException;

    /**
     * Triggers prior to getting the user from a claim.
     *
     * @param claim User claim.
     * @throws IdentityStoreException Identity store exception.
     * @throws UserNotFoundException User Not Found Exception.
     */
    void doPreGetUser(Claim claim) throws IdentityStoreException, UserNotFoundException;

    /**
     * Triggers post to getting the user from a claim.
     *
     * @param claim User claim.
     * @param user User result to be returned from getUser method.
     * @throws IdentityStoreException Identity store exception.
     * @throws UserNotFoundException User Not Found Exception.
     */
    void doPostGetUser(Claim claim, User user) throws IdentityStoreException, UserNotFoundException;

    /**
     * Triggers prior to getting the user from a domain for a given claim.
     *
     * @param claim User claim.
     * @param domainName Domain name.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPreGetUser(Claim claim, String domainName) throws IdentityStoreException;

    /**
     * Triggers post to getting the user from a domain for a given claim.
     *
     * @param claim User claim.
     * @param domainName The domain name to retrieve user from..
     * @param user User result to be returned from getUser method.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPostGetUser(Claim claim, String domainName, User user) throws IdentityStoreException;

    /**
     * Triggers prior to listing users with an offset and length.
     *
     * @param offset Offset for list of users.
     * @param length Length from the offset.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPreListUsers(int offset, int length) throws IdentityStoreException;

    /**
     * Triggers post to listing users with an offset and length.
     *
     * @param offset Offset for list of users.
     * @param length Length from the offset.
     * @param users User list to be returned from listUsers method.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPostListUsers(int offset, int length, List<User> users) throws IdentityStoreException;

    /**
     * Triggers prior to listing all users in a domain with an offset and length.
     *
     * @param offset Offset for list of users.
     * @param length Length from the offset.
     * @param domainName The domain name to retrieve users from.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPreListUsers(int offset, int length, String domainName) throws IdentityStoreException;

    /**
     * Triggers post to listing all users in a domain with an offset and length.
     *
     * @param offset Offset for list of users.
     * @param length Length from the offset.
     * @param domainName The domain name to retrieve users from.
     * @param users User list to be returned from listUsers method.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPostListUsers(int offset, int length, String domainName, List<User> users) throws IdentityStoreException;

    /**
     * Triggers prior to listing all users that matches a given claim with an offset and length..
     *
     * @param claim  User claim
     * @param offset Offset for list of users.
     * @param length Length from the offset.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPreListUsers(Claim claim, int offset, int length) throws IdentityStoreException;

    /**
     * Triggers post to listing all users that matches a given claim with an offset and length.
     *
     * @param claim  User claim
     * @param offset Offset for list of users.
     * @param length Length from the offset.
     * @param users User list to be returned from listUsers method.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPostListUsers(Claim claim, int offset, int length, List<User> users) throws IdentityStoreException;

    /**
     * Triggers prior to listing users that matches a given claim with an offset and length.
     *
     * @param claim  User claim
     * @param offset Offset for list of users.
     * @param length Length from the offset.
     * @param domainName The domain name to retrieve users from.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPreListUsers(Claim claim, int offset, int length, String domainName) throws IdentityStoreException;

    /**
     * Triggers post to listing users that matches a given claim with an offset and length.
     *
     * @param claim  User claim
     * @param offset Offset for list of users.
     * @param length Length from the offset.
     * @param domainName The domain name to retrieve users from.
     * @param users User list to be returned from listUsers method.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPostListUsers(Claim claim, int offset, int length, String domainName, List<User> users) throws
            IdentityStoreException;

    /**
     * Triggers prior to listing users that matches a given filter to a meta claim with an offset and length.
     *
     * @param metaClaim User meta claim.
     * @param filterPattern Filter patter to filter users.
     * @param offset Offset for list of users.
     * @param length Length from the offset.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPreListUsers(MetaClaim metaClaim, String filterPattern, int offset, int length) throws
            IdentityStoreException;

    /**
     * Triggers post to listing users that matches a given filter to a meta claim with an offset and length.
     *
     * @param metaClaim User meta claim.
     * @param filterPattern Filter patter to filter users.
     * @param offset Offset for list of users.
     * @param length Length from the offset.
     * @param users User list to be returned from listUsers method.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPostListUsers(MetaClaim metaClaim, String filterPattern, int offset, int length, List<User> users) throws
            IdentityStoreException;

    /**
     * Triggers prior to listing users that matches a given filter to a meta claim with an offset and length in a
     * domain.
     *
     * @param metaClaim User meta claim.
     * @param filterPattern Filter patter to filter users.
     * @param offset Offset for list of users.
     * @param length Length from the offset.
     * @param domainName The domain name to retrieve users from.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPreListUsers(MetaClaim metaClaim, String filterPattern, int offset, int length, String domainName) throws
            IdentityStoreException;

    /**
     * Triggers post listing users that matches a given filter to a meta claim with an offset and length in a
     * domain.
     *
     * @param metaClaim User meta claim.
     * @param filterPattern Filter patter to filter users.
     * @param offset Offset for list of users.
     * @param length Length from the offset.
     * @param domainName The domain name to retrieve users from.
     * @param users User list to be returned from listUsers method.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPostListUsers(MetaClaim metaClaim, String filterPattern, int offset, int length, String domainName,
                         List<User> users) throws IdentityStoreException;

    /**
     * Triggers prior to getting the group from unique group ID.
     *
     * @param uniqueGroupId Name of the group.
     * @throws IdentityStoreException Identity store exception.
     * @throws GroupNotFoundException Group not found exception.
     */
    void doPreGetGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException;

    /**
     * Triggers post to getting the group from unique group ID.
     *
     * @param uniqueGroupId Name of the group.
     * @param group Group result to be returned from getGroup method.
     * @throws IdentityStoreException Identity store exception.
     * @throws GroupNotFoundException Group not found exception.
     */
    void doPostGetGroup(String uniqueGroupId, Group group) throws IdentityStoreException, GroupNotFoundException;

    /**
     * Triggers prior to getting the group from a claim.
     *
     * @param claim Group claim.
     * @throws IdentityStoreException Identity store exception.
     * @throws GroupNotFoundException Group not found exception.
     */
    void doPreGetGroup(Claim claim) throws IdentityStoreException, GroupNotFoundException;

    /**
     * Triggers post to getting the group from a claim.
     *
     * @param claim Group claim.
     * @param group Group result to be returned from getGroup method.
     * @throws IdentityStoreException Identity store exception.
     * @throws GroupNotFoundException Group not found exception.
     */
    void doPostGetGroup(Claim claim, Group group) throws IdentityStoreException, GroupNotFoundException;

    /**
     * Triggers prior to getting a group from a claim in a domain.
     *
     * @param claim Group claim.
     * @param domainName The domain name to retrieve group from.
     * @throws IdentityStoreException Identity store exception.
     * @throws GroupNotFoundException Group not found exception.
     */
    void doPreGetGroup(Claim claim, String domainName) throws IdentityStoreException, GroupNotFoundException;

    /**
     * Triggers post to getting a group from a claim in a domain.
     *
     * @param claim Group claim.
     * @param domainName The domain name to retrieve group from.
     * @param group Group result to be returned from getGroup method.
     * @throws IdentityStoreException Identity store exception.
     * @throws GroupNotFoundException Group not found exception.
     */
    void doPostGetGroup(Claim claim, String domainName, Group group) throws IdentityStoreException,
            GroupNotFoundException;

    /**
     * Triggers prior to listing groups with an offset and length.
     *
     * @param offset Offset for the group list.
     * @param length Length from the offset.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPreListGroups(int offset, int length) throws IdentityStoreException;

    /**
     * Triggers post to listing groups with an offset and length.
     *
     * @param offset Offset for the group list.
     * @param length Length from the offset.
     * @param groups Group list to be returned from listGroups method.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPostListGroups(int offset, int length, List<Group> groups)
            throws IdentityStoreException;

    /**
     * Triggers prior to listing groups with an offset and length in a domain.
     *
     * @param offset Offset for the group list.
     * @param length Length from the offset.
     * @param domainName The domain name to retrieve groups from.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPreListGroups(int offset, int length, String domainName) throws IdentityStoreException;

    /**
     * Triggers post to listing groups with an offset and length in a domain.
     *
     * @param offset Offset for the group list.
     * @param length Length from the offset.
     * @param domainName The domain name to retrieve groups from.
     * @param groups Group list to be returned from listGroups method.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPostListGroups(int offset, int length, String domainName, List<Group> groups)
            throws IdentityStoreException;

    /**
     * Triggers prior to listing groups that matches a claim with an offset and length.
     *
     * @param claim Group claim.
     * @param offset Offset for the group list.
     * @param length Length from the offset.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPreListGroups(Claim claim, int offset, int length) throws IdentityStoreException;

    /**
     * Triggers post to listing groups that matches a claim with an offset and length.
     *
     * @param claim Group claim.
     * @param offset Offset for the group list.
     * @param length Length from the offset.
     * @param groups Group list to be returned from listGroups method.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPostListGroups(Claim claim, int offset, int length, List<Group> groups)
            throws IdentityStoreException;

    /**
     * Triggers prior to listing groups that matches a given filter to a meta claim with an offset and length.
     *
     * @param metaClaim Group meta claim.
     * @param filterPattern Filter pattern to filter groups.
     * @param offset Offset for the group list. Offset for the group list.
     * @param length Length from the offset.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPreListGroups(MetaClaim metaClaim, String filterPattern, int offset, int length) throws
            IdentityStoreException;

    /**
     * Triggers post to listing groups that matches a given filter to a meta claim with an offset and length.
     *
     * @param metaClaim Group meta claim.
     * @param filterPattern Filter pattern to filter groups.
     * @param offset Offset for the group list. Offset for the group list.
     * @param length Length from the offset.
     * @param groups Group list to be returned from listGroups method.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPostListGroups(MetaClaim metaClaim, String filterPattern, int offset, int length, List<Group> groups)
            throws IdentityStoreException;

    /**
     * Triggers prior to listing groups that matches a given filter to a meta claim with an offset and length in a
     * domain.
     *
     * @param metaClaim Group meta claim
     * @param filterPattern Filter pattern to filter groups.
     * @param offset Offset for the group list.
     * @param length Length from the offset.
     * @param domainName The domain name to retrieve groups from.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPreListGroups(MetaClaim metaClaim, String filterPattern, int offset, int length, String domainName) throws
            IdentityStoreException;

    /**
     * Triggers post to listing groups that matches a given filter to a meta claim with an offset and length in a
     * domain.
     *
     * @param metaClaim Group meta claim
     * @param filterPattern Filter pattern to filter groups.
     * @param offset Offset for the group list.
     * @param length Length from the offset.
     * @param groups Group list to be returned from listGroups method.
     * @param domainName The domain name to retrieve groups from.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPostListGroups(MetaClaim metaClaim, String filterPattern, int offset, int length, String domainName,
                          List<Group> groups) throws IdentityStoreException;

    /**
     * Triggers prior to listing groups by a claim with an offset and length in a domain.
     *
     * @param claim Group claim.
     * @param offset Offset for the group list.
     * @param length Length from the offset.
     * @param domainName The domain name to retrieve groups from.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPreListGroups(Claim claim, int offset, int length, String domainName) throws IdentityStoreException;

    /**
     * Triggers post to listing groups by a claim with an offset and length in a domain.
     *
     * @param claim Group claim.
     * @param offset Offset for the group list.
     * @param length Length from the offset.
     * @param domainName The domain name to retrieve groups from.
     * @param groups Group list to be returned from listGroups method.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPostListGroups(Claim claim, int offset, int length, String domainName, List<Group> groups)
            throws IdentityStoreException;


    /**
     * Triggers prior to getting the groups assigned to a specified user.
     *
     * @param uniqueUserId Unique ID of the user.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPreGetGroupsOfUser(String uniqueUserId) throws IdentityStoreException;


    /**
     * Triggers post to getting the groups assigned to a specified user.
     *
     * @param uniqueUserId Unique ID of the user.
     * @param groups Group list to be returned from getGroupsOfUser method.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPostGetGroupsOfUser(String uniqueUserId, List<Group> groups) throws IdentityStoreException;

    /**
     * Triggers prior to getting the users assigned to a specified group.
     *
     * @param uniqueGroupId Unique ID of the group.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPreGetUsersOfGroup(String uniqueGroupId) throws IdentityStoreException;

    /**
     * Triggers post to getting the users assigned to a specified group.
     *
     * @param uniqueGroupId Unique ID of the group.
     * @param users Users list to be returned from getUsersOfGroup method.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPostGetUsersOfGroup(String uniqueGroupId, List<User> users) throws IdentityStoreException;

    /**
     * Triggers prior to checking whether the user is in a group.
     *
     * @param uniqueUserId Unique ID of the user.
     * @param uniqueGroupId Unique ID of the group.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPreIsUserInGroup(String uniqueUserId, String uniqueGroupId) throws IdentityStoreException;

    /**
     * Triggers post to checking whether the user is in a group.
     *
     * @param uniqueUserId Unique ID of the user.
     * @param uniqueGroupId Unique ID of the group.
     * @param isUserInGroup Result to be returned from isUserInGroup method.
     * @throws IdentityStoreException Identity store exception. Identity Store Exception.
     */
    void doPostIsUserInGroup(String uniqueUserId, String uniqueGroupId, Boolean isUserInGroup) throws
            IdentityStoreException;

    /**
     * Triggers prior to getting claims of a user by user ID.
     *
     * @param uniqueUserId Unique ID of the user.
     * @throws IdentityStoreException Identity store exception.
     * @throws UserNotFoundException User not found exception.
     */
    void doPreGetClaimsOfUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException;

    /**
     * Triggers post to getting claims of a user by user ID.
     *
     * @param uniqueUserId Unique ID of the user.
     * @param claims Claims to be returned from getClaimsOfUser method.
     * @throws IdentityStoreException Identity store exception.
     * @throws UserNotFoundException User not found exception.
     */
    void doPostGetClaimsOfUser(String uniqueUserId, List<Claim> claims) throws IdentityStoreException,
            UserNotFoundException;

    /**
     * Triggers prior to getting claims of a user by user ID and meta claims.
     *
     * @param uniqueUserId Unique ID of the user.
     * @param metaClaims List of meta claims.
     * @throws IdentityStoreException Identity store exception.
     * @throws UserNotFoundException User not found exception.
     */
    void doPreGetClaimsOfUser(String uniqueUserId, List<MetaClaim> metaClaims) throws IdentityStoreException,
            UserNotFoundException;

    /**
     * Triggers prior to getting claims of a user by user ID and meta claims.
     *
     * @param uniqueUserId Unique ID of the user.
     * @param metaClaims List of meta claims.
     * @param claims Claims to be returned from getClaimsOfUser method.
     * @throws IdentityStoreException Identity store exception.
     * @throws UserNotFoundException User not found exception.
     */
    void doPostGetClaimsOfUser(String uniqueUserId, List<MetaClaim> metaClaims, List<Claim> claims) throws
            IdentityStoreException, UserNotFoundException;

    /**
     * Triggers prior to getting claims of a group by group ID.
     *
     * @param uniqueGroupId Unique ID of the group.
     * @throws IdentityStoreException Identity store exception.
     * @throws GroupNotFoundException Group not found exception.
     */
    void doPreGetClaimsOfGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException;

    /**
     * Triggers post to getting claims of a group by group ID.
     *
     * @param uniqueGroupId Unique ID of the group.
     * @param claims Claims to be returned from getClaimsOfGroup method.
     * @throws IdentityStoreException Identity store exception.
     * @throws GroupNotFoundException Group not found exception.
     */
    void doPostGetClaimsOfGroup(String uniqueGroupId, List<Claim> claims) throws IdentityStoreException,
            GroupNotFoundException;

    /**
     * Triggers prior to getting claims of a group by group ID and meta claims.
     *
     * @param uniqueGroupId Unique ID of the group.
     * @param metaClaims List of meta claims.
     * @throws IdentityStoreException Identity store exception.
     * @throws GroupNotFoundException Group not found exception.
     */
    void doPreGetClaimsOfGroup(String uniqueGroupId, List<MetaClaim> metaClaims) throws IdentityStoreException,
            GroupNotFoundException;

    /**
     * Triggers post to getting claims of a group by group ID and meta claims
     *
     * @param uniqueGroupId Unique ID of the group.
     * @param metaClaims List of meta claims.
     * @param claims Claims to be returned from getClaimsOfGroup method.
     * @throws IdentityStoreException Identity store exception.
     * @throws GroupNotFoundException Group not found exception.
     */
    void doPostGetClaimsOfGroup(String uniqueGroupId, List<MetaClaim> metaClaims, List<Claim> claims) throws
            IdentityStoreException, GroupNotFoundException;

    /**
     * Triggers prior to adding user.
     *
     * @param userBean User bean.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPreAddUser(UserBean userBean) throws IdentityStoreException;

    /**
     * Triggers post to adding user.
     *
     * @param userBean User bean.
     * @param user User to be returned from addUser method.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPostAddUser(UserBean userBean, User user) throws IdentityStoreException;

    /**
     * Triggers prior to adding a user to a given domain.
     *
     * @param userBean User bean.
     * @param domainName  The domain name to add the user.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPreAddUser(UserBean userBean, String domainName) throws IdentityStoreException;

    /**
     * Triggers post to adding a user to a given domain.
     *
     * @param userBean User bean.
     * @param domainName  The domain name to add the user.
     * @param user User to be returned from addUser method.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPostAddUser(UserBean userBean, String domainName, User user) throws IdentityStoreException;

    /**
     * Triggers prior to adding a list of users.
     *
     * @param usersBeans List of user beans.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPreAddUsers(List<UserBean> usersBeans) throws IdentityStoreException;

    /**
     * Triggers post to adding a list of users.
     *
     * @param userBeans List of user beans.
     * @param users List of users to be returned from addUsers method.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPostAddUsers(List<UserBean> userBeans, List<User> users) throws IdentityStoreException;

    /**
     * Triggers prior to adding a list of users to a given domain.
     *
     * @param userBeans List of user beans.
     * @param domainName The domain name to add the users.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPreAddUsers(List<UserBean> userBeans, String domainName) throws IdentityStoreException;

    /**
     * Triggers post to adding a list of users to a given domain.
     *
     * @param userBeans List of user beans.
     * @param domainName The domain name to add the users.
     * @param users List of users to be returned from addUsers method.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPostAddUsers(List<UserBean> userBeans, String domainName, List<User> users) throws IdentityStoreException;

    /**
     * Triggers prior to updating claims of a user.
     *
     * @param uniqueUserId Unique ID of the user.
     * @param claims List of claims to be updated.
     * @throws IdentityStoreException Identity store exception.
     * @throws UserNotFoundException User not found exception.
     */
    void doPreUpdateUserClaims(String uniqueUserId, List<Claim> claims) throws IdentityStoreException,
            UserNotFoundException;

    /**
     * Triggers post to updating claims of a user.
     *
     * @param uniqueUserId Unique ID of the user.
     * @param claims List of claims to be updated.
     * @throws IdentityStoreException Identity store exception.
     * @throws UserNotFoundException User not found exception.
     */
    void doPostUpdateUserClaims(String uniqueUserId, List<Claim> claims) throws IdentityStoreException,
            UserNotFoundException;

    /**
     * Triggers prior to add/remove claims of a user.
     *
     * @param uniqueUserId Unique ID of the user.
     * @param claimsToAdd List of claims to add.
     * @param claimsToRemove List of claims to remove.
     * @throws IdentityStoreException Identity store exception.
     * @throws UserNotFoundException User not found exception.
     */
    void doPreUpdateUserClaims(String uniqueUserId, List<Claim> claimsToAdd, List<Claim> claimsToRemove) throws
            IdentityStoreException, UserNotFoundException;

    /**
     * Triggers post to add/remove claims of a user.
     *
     * @param uniqueUserId Unique ID of the user.
     * @param claimsToAdd List of claims to add.
     * @param claimsToRemove List of claims to remove.
     * @throws IdentityStoreException Identity store exception.
     * @throws UserNotFoundException User not found exception.
     */
    void doPostUpdateUserClaims(String uniqueUserId, List<Claim> claimsToAdd, List<Claim> claimsToRemove) throws
            IdentityStoreException, UserNotFoundException;

    /**
     * Triggers prior to updating the user credentials.
     *
     * @param uniqueUserId Unique ID of the user.
     * @param credentials Callbacks with credentials.
     * @throws IdentityStoreException Identity store exception.
     * @throws UserNotFoundException User not found exception.
     */
    void doPreUpdateUserCredentials(String uniqueUserId, List<Callback> credentials) throws IdentityStoreException,
            UserNotFoundException;

    /**
     * Triggers post to updating the user credentials.
     *
     * @param uniqueUserId Unique ID of the user.
     * @param credentials Callbacks with credentials.
     * @throws IdentityStoreException Identity store exception.
     * @throws UserNotFoundException User not found exception.
     */
    void doPostUpdateUserCredentials(String uniqueUserId, List<Callback> credentials) throws IdentityStoreException,
            UserNotFoundException;

    /**
     * Triggers prior to add/remove user credentials.
     *
     * @param uniqueUserId Unique ID of the user.
     * @param credentialsToAdd Callbacks with credentials to be added.
     * @param credentialsToRemove Callbacks with credentials to be removed.
     * @throws IdentityStoreException Identity store exception.
     * @throws UserNotFoundException User not found exception.
     */
    void doPreUpdateUserCredentials(String uniqueUserId, List<Callback> credentialsToAdd, List<Callback>
            credentialsToRemove) throws IdentityStoreException, UserNotFoundException;

    /**
     * Triggers post to add/remove user credentials.
     *
     * @param uniqueUserId Unique ID of the user.
     * @param credentialsToAdd Callbacks with credentials to be added.
     * @param credentialsToRemove Callbacks with credentials to be removed.
     * @throws IdentityStoreException Identity store exception.
     * @throws UserNotFoundException User not found exception.
     */
    void doPostUpdateUserCredentials(String uniqueUserId, List<Callback> credentialsToAdd, List<Callback>
            credentialsToRemove) throws IdentityStoreException, UserNotFoundException;

    /**
     * Triggers prior to deleting a user.
     *
     * @param uniqueUserId Unique ID of the user.
     * @throws IdentityStoreException Identity store exception.
     * @throws UserNotFoundException User not found exception.
     */
    void doPreDeleteUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException;

    /**
     * Triggers post to deleting a user.
     *
     * @param uniqueUserId Unique ID of the user.
     * @throws IdentityStoreException Identity store exception.
     * @throws UserNotFoundException User not found exception.
     */
    void doPostDeleteUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException;

    /**
     * Triggers prior to updating groups of a user.
     *
     * @param uniqueUserId Unique ID of the user.
     * @param uniqueGroupIds Unique ID list of groups.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPreUpdateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIds) throws IdentityStoreException;

    /**
     * Triggers post to updating groups of a user.
     *
     * @param uniqueUserId Unique ID of the user.
     * @param uniqueGroupIds Unique ID list of groups.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPostUpdateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIds) throws IdentityStoreException;

    /**
     * Triggers prior to add/remove groups of a user.
     *
     * @param uniqueUserId Unique ID of the user.
     * @param uniqueGroupIdsToAdd Unique ID list of groups to be added.
     * @param uniqueGroupIdsToRemove Unique ID list of groups to be removed.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPreUpdateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIdsToAdd, List<String>
            uniqueGroupIdsToRemove) throws IdentityStoreException;

    /**
     * Triggers post to add/remove groups of a user.
     *
     * @param uniqueUserId Unique ID of the user.
     * @param uniqueGroupIdsToAdd Unique ID list of groups to be added.
     * @param uniqueGroupIdsToRemove Unique ID list of groups to be removed.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPostUpdateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIdsToAdd, List<String>
            uniqueGroupIdsToRemove) throws IdentityStoreException;

    /**
     * Triggers prior to adding a group.
     *
     * @param groupBean Group bean.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPreAddGroup(GroupBean groupBean) throws IdentityStoreException;

    /**
     * Triggers post to adding a group.
     *
     * @param groupBean Group bean.
     * @param group Group to be returned from addGroup method.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPostAddGroup(GroupBean groupBean, Group group) throws IdentityStoreException;

    /**
     * Triggers prior to adding a group to a given domain.
     *
     * @param groupBean Group bean.
     * @param domainName The domain name to add the group.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPreAddGroup(GroupBean groupBean, String domainName) throws IdentityStoreException;

    /**
     * Triggers post to adding a group to a given domain.
     *
     * @param groupBean Group bean.
     * @param domainName The domain name to add the group.
     * @param group Group to be returned from addGroup method.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPostAddGroup(GroupBean groupBean, String domainName, Group group) throws IdentityStoreException;

    /**
     * Triggers prior to adding a list ot group.
     *
     * @param groupBeans List of group beans.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPreAddGroups(List<GroupBean> groupBeans) throws IdentityStoreException;

    /**
     * Triggers post to adding a list ot group.
     *
     * @param groupBeans List of group beans.
     * @param groups Lit of groups to be returned from addGroups method.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPostAddGroups(List<GroupBean> groupBeans, List<Group> groups) throws IdentityStoreException;

    /**
     * Triggers prior to adding a list of groups to a given domain.
     *
     * @param groupBeans List of group beans.
     * @param domainName The domain name to add the groups.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPreAddGroups(List<GroupBean> groupBeans, String domainName) throws IdentityStoreException;

    /**
     * Triggers post to adding a list of groups to a given domain.
     *
     * @param groupBeans List of group beans.
     * @param domainName The domain name to add the groups.
     * @param groups Lit of groups to be returned from addGroups method.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPostAddGroups(List<GroupBean> groupBeans, String domainName, List<Group> groups) throws
            IdentityStoreException;

    /**
     * Triggers prior to updating claims of a group.
     *
     * @param uniqueGroupId Unique ID of the group.
     * @param claims List of claims to be updated.
     * @throws IdentityStoreException Identity store exception.
     * @throws GroupNotFoundException Group not found exception.
     */
    void doPreUpdateGroupClaims(String uniqueGroupId, List<Claim> claims) throws IdentityStoreException,
            GroupNotFoundException;

    /**
     * Triggers post to updating claims of a group.
     *
     * @param uniqueGroupId Unique ID of the group.
     * @param claims List of claims to be updated.
     * @throws IdentityStoreException Identity store exception.
     * @throws GroupNotFoundException Group not found exception.
     */
    void doPostUpdateGroupClaims(String uniqueGroupId, List<Claim> claims) throws IdentityStoreException,
            GroupNotFoundException;

    /**
     * Triggers prior to add/remove claims of a group.
     *
     * @param uniqueGroupId Unique ID of the group.
     * @param claimsToAdd List of claims ot be added.
     * @param claimsToRemove List of claims to be removed.
     * @throws IdentityStoreException Identity store exception.
     * @throws GroupNotFoundException Group not found exception.
     */
    void doPreUpdateGroupClaims(String uniqueGroupId, List<Claim> claimsToAdd, List<Claim> claimsToRemove) throws
            IdentityStoreException, GroupNotFoundException;

    /**
     * Triggers post to add/remove claims of a group.
     *
     * @param uniqueGroupId Unique ID of the group.
     * @param claimsToAdd List of claims ot be added.
     * @param claimsToRemove List of claims to be removed.
     * @throws IdentityStoreException Identity store exception.
     * @throws GroupNotFoundException Group not found exception.
     */
    void doPostUpdateGroupClaims(String uniqueGroupId, List<Claim> claimsToAdd, List<Claim> claimsToRemove) throws
            IdentityStoreException, GroupNotFoundException;

    /**
     * Triggers prior to deleting a group.
     *
     * @param uniqueGroupId Unique ID of the group.
     * @throws IdentityStoreException Identity store exception.
     * @throws GroupNotFoundException Group not found exception.
     */
    void doPreDeleteGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException;

    /**
     * Triggers post to deleting a group.
     *
     * @param uniqueGroupId Unique ID of the group.
     * @throws IdentityStoreException Identity store exception.
     * @throws GroupNotFoundException Group not found exception.
     */
    void doPostDeleteGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException;

    /**
     * Triggers prior to updating users of a group.
     *
     * @param uniqueGroupId Unique ID of the group.
     * @param uniqueUserIds List of unique user IDs.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPreUpdateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIds) throws IdentityStoreException;

    /**
     * Triggers post to updating users of a group.
     *
     * @param uniqueGroupId Unique ID of the group.
     * @param uniqueUserIds List of unique user IDs.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPostUpdateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIds) throws IdentityStoreException;

    /**
     * Triggers prior to add/remove users of a group.
     *
     * @param uniqueGroupId Unique ID of the group.
     * @param uniqueUserIdsToAdd List of unique user IDs to be added.
     * @param uniqueUserIdsToRemove List of unique user IDs to be removed.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPreUpdateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIdsToAdd, List<String>
            uniqueUserIdsToRemove) throws IdentityStoreException;

    /**
     * Triggers post to add/remove users of a group.
     *
     * @param uniqueGroupId Unique ID of the group.
     * @param uniqueUserIdsToAdd List of unique user IDs to be added.
     * @param uniqueUserIdsToRemove List of unique user IDs to be removed.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPostUpdateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIdsToAdd, List<String>
            uniqueUserIdsToRemove) throws IdentityStoreException;

    /**
     * Triggers prior to authenticating the user.
     *
     * @param claim User claim.
     * @param credentials Callbacks with credentials.
     * @param domainName The domain name to authenticate the user against.
     * @throws AuthenticationFailure Authentication failure.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPreAuthenticate(Claim claim, Callback[] credentials, String domainName) throws AuthenticationFailure,
            IdentityStoreException;

    /**
     * Triggers post to authenticating the user.
     *
     * @param claim User claim.
     * @param credentials Callbacks with credentials.
     * @param domainName The domain name to authenticate the user against.
     * @param authenticationContext AuthenticationContext result to be returned from authenticate method.
     * @throws AuthenticationFailure Authentication failure.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPostAuthenticate(Claim claim, Callback[] credentials, String domainName, AuthenticationContext
            authenticationContext) throws AuthenticationFailure, IdentityStoreException;

    /**
     * Triggers prior to getting primary domain name.
     *
     * @throws IdentityStoreException Identity store exception.
     */
    void doPreGetPrimaryDomainName() throws IdentityStoreException;

    /**
     * Triggers post to getting primary domain name.
     *
     * @param primaryDomainName Primary domain name to be returned from getPrimaryDomainName method.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPostGetPrimaryDomainName(String primaryDomainName) throws IdentityStoreException;

    /**
     * Triggers prior to getting all domain names.
     *
     * @throws IdentityStoreException Identity store exception.
     */
    void doPreGetDomainNames() throws IdentityStoreException;

    /**
     * Triggers post to getting all the domain names.
     *
     * @param domainNames Set of domain names to be returned from getDomainNames method.
     * @throws IdentityStoreException Identity store exception.
     */
    void doPostGetDomainNames(Set<String> domainNames) throws IdentityStoreException;

}

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

package org.wso2.carbon.identity.mgt.test.identity.store.interceptor;

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
import org.wso2.carbon.identity.mgt.interceptor.AbstractIdentityStoreInterceptor;

import java.util.List;
import java.util.Set;
import javax.security.auth.callback.Callback;

/**
 * Test identity store interceptor.
 */
public class TestIdentityStoreInterceptor extends AbstractIdentityStoreInterceptor {


    public static final ThreadLocal<Boolean> PRE = new ThreadLocal<>();
    public static final ThreadLocal<Boolean> POST = new ThreadLocal<>();

    @Override
    public void doPreGetUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException {
        PRE.set(true);
    }

    @Override
    public void doPostGetUser(String uniqueUserId, User user) throws IdentityStoreException, UserNotFoundException {
        POST.set(true);
    }

    @Override
    public void doPreGetUser(Claim claim) throws IdentityStoreException, UserNotFoundException {
        PRE.set(true);
    }

    @Override
    public void doPostGetUser(Claim claim, User user) throws IdentityStoreException, UserNotFoundException {
        POST.set(true);
    }

    @Override
    public void doPreGetUser(Claim claim, String domainName) throws IdentityStoreException {
        PRE.set(true);
    }

    @Override
    public void doPostGetUser(Claim claim, String domainName, User user) throws IdentityStoreException {
        POST.set(true);
    }

    @Override
    public void doPreListUsers(int offset, int length) throws IdentityStoreException {
        PRE.set(true);
    }

    @Override
    public void doPostListUsers(int offset, int length, List<User> users) throws IdentityStoreException {
        POST.set(true);
    }

    @Override
    public void doPreListUsers(int offset, int length, String domainName) throws IdentityStoreException {
        PRE.set(true);
    }

    @Override
    public void doPostListUsers(int offset, int length, String domainName, List<User> users)
            throws IdentityStoreException {
        POST.set(true);
    }

    @Override
    public void doPreListUsers(Claim claim, int offset, int length) throws IdentityStoreException {
        PRE.set(true);
    }

    @Override
    public void doPostListUsers(Claim claim, int offset, int length, List<User> users) throws IdentityStoreException {
        POST.set(true);
    }

    @Override
    public void doPreListUsers(Claim claim, int offset, int length, String domainName) throws IdentityStoreException {
        PRE.set(true);
    }

    @Override
    public void doPostListUsers(Claim claim, int offset, int length, String domainName, List<User> users)
            throws IdentityStoreException {
        POST.set(true);
    }

    @Override
    public void doPreListUsers(MetaClaim metaClaim, String filterPattern, int offset, int length)
            throws IdentityStoreException {
        PRE.set(true);
    }

    @Override
    public void doPostListUsers(MetaClaim metaClaim, String filterPattern, int offset, int length, List<User> users)
            throws IdentityStoreException {
        POST.set(true);
    }

    @Override
    public void doPreListUsers(MetaClaim metaClaim, String filterPattern, int offset, int length, String domainName)
            throws IdentityStoreException {
        PRE.set(true);
    }

    @Override
    public void doPostListUsers(MetaClaim metaClaim, String filterPattern, int offset, int length, String domainName,
                                List<User> users) throws IdentityStoreException {
        POST.set(true);
    }

    @Override
    public void doPreGetGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException {
        PRE.set(true);
    }

    @Override
    public void doPostGetGroup(String uniqueGroupId, Group group)
            throws IdentityStoreException, GroupNotFoundException {
        POST.set(true);
    }

    @Override
    public void doPreGetGroup(Claim claim) throws IdentityStoreException, GroupNotFoundException {
        PRE.set(true);
    }

    @Override
    public void doPostGetGroup(Claim claim, Group group) throws IdentityStoreException, GroupNotFoundException {
        POST.set(true);
    }

    @Override
    public void doPreGetGroup(Claim claim, String domainName) throws IdentityStoreException, GroupNotFoundException {
        PRE.set(true);
    }

    @Override
    public void doPostGetGroup(Claim claim, String domainName, Group group)
            throws IdentityStoreException, GroupNotFoundException {
        POST.set(true);
    }

    @Override
    public void doPreListGroups(int offset, int length) throws IdentityStoreException {
        PRE.set(true);
    }

    @Override
    public void doPostListGroups(int offset, int length, List<Group> groups) throws IdentityStoreException {
        POST.set(true);
    }

    @Override
    public void doPreListGroups(int offset, int length, String domainName) throws IdentityStoreException {
        PRE.set(true);
    }

    @Override
    public void doPostListGroups(int offset, int length, String domainName, List<Group> groups)
            throws IdentityStoreException {
        POST.set(true);
    }

    @Override
    public void doPreListGroups(Claim claim, int offset, int length) throws IdentityStoreException {
        PRE.set(true);
    }

    @Override
    public void doPostListGroups(Claim claim, int offset, int length, List<Group> groups)
            throws IdentityStoreException {
        POST.set(true);
    }

    @Override
    public void doPreListGroups(MetaClaim metaClaim, String filterPattern, int offset, int length)
            throws IdentityStoreException {
        PRE.set(true);
    }

    @Override
    public void doPostListGroups(MetaClaim metaClaim, String filterPattern, int offset, int length, List<Group> groups)
            throws IdentityStoreException {
        POST.set(true);
    }

    @Override
    public void doPreListGroups(MetaClaim metaClaim, String filterPattern, int offset, int length, String domainName)
            throws IdentityStoreException {
        PRE.set(true);
    }

    @Override
    public void doPostListGroups(MetaClaim metaClaim, String filterPattern, int offset, int length, String domainName,
                                 List<Group> groups) throws IdentityStoreException {
        POST.set(true);
    }

    @Override
    public void doPreListGroups(Claim claim, int offset, int length, String domainName) throws IdentityStoreException {
        PRE.set(true);
    }

    @Override
    public void doPostListGroups(Claim claim, int offset, int length, String domainName, List<Group> groups)
            throws IdentityStoreException {
        POST.set(true);
    }

    @Override
    public void doPreGetGroupsOfUser(String uniqueUserId) throws IdentityStoreException {
        PRE.set(true);
    }

    @Override
    public void doPostGetGroupsOfUser(String uniqueUserId, List<Group> groups) throws IdentityStoreException {
        POST.set(true);
    }

    @Override
    public void doPreGetUsersOfGroup(String uniqueGroupId) throws IdentityStoreException {
        PRE.set(true);
    }

    @Override
    public void doPostGetUsersOfGroup(String uniqueGroupId, List<User> users) throws IdentityStoreException {
        POST.set(true);
    }

    @Override
    public void doPreIsUserInGroup(String uniqueUserId, String uniqueGroupId) throws IdentityStoreException {
        PRE.set(true);
    }

    @Override
    public void doPostIsUserInGroup(String uniqueUserId, String uniqueGroupId, Boolean isUserInGroup)
            throws IdentityStoreException {
        POST.set(true);
    }

    @Override
    public void doPreGetClaimsOfUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException {
        PRE.set(true);
    }

    @Override
    public void doPostGetClaimsOfUser(String uniqueUserId, List<Claim> claims)
            throws IdentityStoreException, UserNotFoundException {
        POST.set(true);
    }

    @Override
    public void doPreGetClaimsOfUser(String uniqueUserId, List<MetaClaim> metaClaims)
            throws IdentityStoreException, UserNotFoundException {
        PRE.set(true);
    }

    @Override
    public void doPostGetClaimsOfUser(String uniqueUserId, List<MetaClaim> metaClaims, List<Claim> claims)
            throws IdentityStoreException, UserNotFoundException {
        POST.set(true);
    }

    @Override
    public void doPreGetClaimsOfGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException {
        PRE.set(true);
    }

    @Override
    public void doPostGetClaimsOfGroup(String uniqueGroupId, List<Claim> claims)
            throws IdentityStoreException, GroupNotFoundException {
        POST.set(true);
    }

    @Override
    public void doPreGetClaimsOfGroup(String uniqueGroupId, List<MetaClaim> metaClaims)
            throws IdentityStoreException, GroupNotFoundException {
        PRE.set(true);
    }

    @Override
    public void doPostGetClaimsOfGroup(String uniqueGroupId, List<MetaClaim> metaClaims, List<Claim> claims)
            throws IdentityStoreException, GroupNotFoundException {
        POST.set(true);
    }

    @Override
    public void doPreAddUser(UserBean user) throws IdentityStoreException {
        PRE.set(true);
    }

    @Override
    public void doPostAddUser(UserBean userBean, User user) throws IdentityStoreException {
        POST.set(true);
    }

    @Override
    public void doPreAddUser(UserBean user, String domainName) throws IdentityStoreException {
        PRE.set(true);
    }

    @Override
    public void doPostAddUser(UserBean userBean, String domainName, User user) throws IdentityStoreException {
        POST.set(true);
    }

    @Override
    public void doPreAddUsers(List<UserBean> usersBeans) throws IdentityStoreException {
        PRE.set(true);
    }

    @Override
    public void doPostAddUsers(List<UserBean> userBeans, List<User> users) throws IdentityStoreException {
        POST.set(true);
    }

    @Override
    public void doPreAddUsers(List<UserBean> userBeans, String domainName) throws IdentityStoreException {
        PRE.set(true);
    }

    @Override
    public void doPostAddUsers(List<UserBean> userBeans, String domainName, List<User> users)
            throws IdentityStoreException {
        POST.set(true);
    }

    @Override
    public void doPreUpdateUserClaims(String uniqueUserId, List<Claim> claims)
            throws IdentityStoreException, UserNotFoundException {
        PRE.set(true);
    }

    @Override
    public void doPostUpdateUserClaims(String uniqueUserId, List<Claim> claims)
            throws IdentityStoreException, UserNotFoundException {
        POST.set(true);
    }

    @Override
    public void doPreUpdateUserClaims(String uniqueUserId, List<Claim> claimsToAdd, List<Claim> claimsToRemove)
            throws IdentityStoreException, UserNotFoundException {
        PRE.set(true);
    }

    @Override
    public void doPostUpdateUserClaims(String uniqueUserId, List<Claim> claimsToAdd, List<Claim> claimsToRemove)
            throws IdentityStoreException, UserNotFoundException {
        POST.set(true);
    }

    @Override
    public void doPreUpdateUserCredentials(String uniqueUserId, List<Callback> credentials)
            throws IdentityStoreException, UserNotFoundException {
        PRE.set(true);
    }

    @Override
    public void doPostUpdateUserCredentials(String uniqueUserId, List<Callback> credentials)
            throws IdentityStoreException, UserNotFoundException {
        POST.set(true);
    }

    @Override
    public void doPreUpdateUserCredentials(String uniqueUserId, List<Callback> credentialsToAdd,
                                           List<Callback> credentialsToRemove)
            throws IdentityStoreException, UserNotFoundException {
        PRE.set(true);
    }

    @Override
    public void doPostUpdateUserCredentials(String uniqueUserId, List<Callback> credentialsToAdd,
                                            List<Callback> credentialsToRemove)
            throws IdentityStoreException, UserNotFoundException {
        POST.set(true);
    }

    @Override
    public void doPreDeleteUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException {
        PRE.set(true);
    }

    @Override
    public void doPostDeleteUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException {
        POST.set(true);
    }

    @Override
    public void doPreUpdateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIds)
            throws IdentityStoreException {
        PRE.set(true);
    }

    @Override
    public void doPostUpdateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIds)
            throws IdentityStoreException {
        POST.set(true);
    }

    @Override
    public void doPreUpdateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIdsToAdd,
                                        List<String> uniqueGroupIdsToRemove) throws IdentityStoreException {
        PRE.set(true);
    }

    @Override
    public void doPostUpdateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIdsToAdd,
                                         List<String> uniqueGroupIdsToRemove) throws IdentityStoreException {
        POST.set(true);
    }

    @Override
    public void doPreAddGroup(GroupBean groupBean) throws IdentityStoreException {
        PRE.set(true);
    }

    @Override
    public void doPostAddGroup(GroupBean groupBean, Group group) throws IdentityStoreException {
        POST.set(true);
    }

    @Override
    public void doPreAddGroup(GroupBean groupBean, String domainName) throws IdentityStoreException {
        PRE.set(true);
    }

    @Override
    public void doPostAddGroup(GroupBean groupBean, String domainName, Group group) throws IdentityStoreException {
        POST.set(true);
    }

    @Override
    public void doPreAddGroups(List<GroupBean> groups) throws IdentityStoreException {
        PRE.set(true);
    }

    @Override
    public void doPostAddGroups(List<GroupBean> groupBeans, List<Group> groups) throws IdentityStoreException {
        POST.set(true);
    }

    @Override
    public void doPreAddGroups(List<GroupBean> groups, String domainName) throws IdentityStoreException {
        PRE.set(true);
    }

    @Override
    public void doPostAddGroups(List<GroupBean> groupBeans, String domainName, List<Group> groups)
            throws IdentityStoreException {
        POST.set(true);
    }

    @Override
    public void doPreUpdateGroupClaims(String uniqueGroupId, List<Claim> claims)
            throws IdentityStoreException, GroupNotFoundException {
        PRE.set(true);
    }

    @Override
    public void doPostUpdateGroupClaims(String uniqueGroupId, List<Claim> claims)
            throws IdentityStoreException, GroupNotFoundException {
        POST.set(true);
    }

    @Override
    public void doPreUpdateGroupClaims(String uniqueGroupId, List<Claim> claimsToAdd, List<Claim> claimsToRemove)
            throws IdentityStoreException, GroupNotFoundException {
        PRE.set(true);
    }

    @Override
    public void doPostUpdateGroupClaims(String uniqueGroupId, List<Claim> claimsToAdd, List<Claim> claimsToRemove)
            throws IdentityStoreException, GroupNotFoundException {
        POST.set(true);
    }

    @Override
    public void doPreDeleteGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException {
        PRE.set(true);
    }

    @Override
    public void doPostDeleteGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException {
        POST.set(true);
    }

    @Override
    public void doPreUpdateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIds)
            throws IdentityStoreException {
        PRE.set(true);
    }

    @Override
    public void doPostUpdateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIds)
            throws IdentityStoreException {
        POST.set(true);
    }

    @Override
    public void doPreUpdateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIdsToAdd,
                                        List<String> uniqueUserIdsToRemove) throws IdentityStoreException {
        PRE.set(true);
    }

    @Override
    public void doPostUpdateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIdsToAdd,
                                         List<String> uniqueUserIdsToRemove) throws IdentityStoreException {
        POST.set(true);
    }

    @Override
    public void doPreAuthenticate(Claim claim, Callback[] credentials, String domainName)
            throws AuthenticationFailure, IdentityStoreException {
        PRE.set(true);
    }

    @Override
    public void doPostAuthenticate(Claim claim, Callback[] credentials, String domainName,
                                   AuthenticationContext authenticationContext)
            throws AuthenticationFailure, IdentityStoreException {
        POST.set(true);
    }

    @Override
    public void doPreGetPrimaryDomainName() throws IdentityStoreException {
        PRE.set(true);
    }

    @Override
    public void doPostGetPrimaryDomainName(String primaryDomainName) throws IdentityStoreException {
        POST.set(true);
    }

    @Override
    public void doPreGetDomainNames() throws IdentityStoreException {
        PRE.set(true);
    }

    @Override
    public void doPostGetDomainNames(Set<String> domainNames) throws IdentityStoreException {
        POST.set(true);
    }
}

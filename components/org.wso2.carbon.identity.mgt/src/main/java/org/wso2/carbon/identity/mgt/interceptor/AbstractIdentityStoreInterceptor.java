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
 * Abstract implementation IdentityStoreInterceptor.
 * @since 1.0.0
 */
public class AbstractIdentityStoreInterceptor implements IdentityStoreInterceptor {


    @Override
    public int getExecutionOrderId() {
        return 10;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void doPreGetUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException {

    }

    @Override
    public void doPostGetUser(String uniqueUserId, User user) throws IdentityStoreException, UserNotFoundException {

    }

    @Override
    public void doPreGetUser(Claim claim) throws IdentityStoreException, UserNotFoundException {

    }

    @Override
    public void doPostGetUser(Claim claim, User user) throws IdentityStoreException, UserNotFoundException {

    }

    @Override
    public void doPreGetUser(Claim claim, String domainName) throws IdentityStoreException {

    }

    @Override
    public void doPostGetUser(Claim claim, String domainName, User user) throws IdentityStoreException {

    }

    @Override
    public void doPreListUsers(int offset, int length) throws IdentityStoreException {

    }

    @Override
    public void doPostListUsers(int offset, int length, List<User> users) throws IdentityStoreException {

    }

    @Override
    public void doPreListUsers(int offset, int length, String domainName) throws IdentityStoreException {

    }

    @Override
    public void doPostListUsers(int offset, int length, String domainName, List<User> users)
            throws IdentityStoreException {

    }

    @Override
    public void doPreListUsers(Claim claim, int offset, int length) throws IdentityStoreException {

    }

    @Override
    public void doPostListUsers(Claim claim, int offset, int length, List<User> users) throws IdentityStoreException {

    }

    @Override
    public void doPreListUsers(Claim claim, int offset, int length, String domainName) throws IdentityStoreException {

    }

    @Override
    public void doPostListUsers(Claim claim, int offset, int length, String domainName, List<User> users)
            throws IdentityStoreException {

    }

    @Override
    public void doPreListUsers(MetaClaim metaClaim, String filterPattern, int offset, int length)
            throws IdentityStoreException {

    }

    @Override
    public void doPostListUsers(MetaClaim metaClaim, String filterPattern, int offset, int length, List<User> users)
            throws IdentityStoreException {

    }

    @Override
    public void doPreListUsers(MetaClaim metaClaim, String filterPattern, int offset, int length, String domainName)
            throws IdentityStoreException {

    }

    @Override
    public void doPostListUsers(MetaClaim metaClaim, String filterPattern, int offset, int length, String domainName,
                                List<User> users) throws IdentityStoreException {

    }

    @Override
    public void doPreGetGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException {

    }

    @Override
    public void doPostGetGroup(String uniqueGroupId, Group group)
            throws IdentityStoreException, GroupNotFoundException {

    }

    @Override
    public void doPreGetGroup(Claim claim) throws IdentityStoreException, GroupNotFoundException {

    }

    @Override
    public void doPostGetGroup(Claim claim, Group group) throws IdentityStoreException, GroupNotFoundException {

    }

    @Override
    public void doPreGetGroup(Claim claim, String domainName) throws IdentityStoreException, GroupNotFoundException {

    }

    @Override
    public void doPostGetGroup(Claim claim, String domainName, Group group)
            throws IdentityStoreException, GroupNotFoundException {

    }

    @Override
    public void doPreListGroups(int offset, int length) throws IdentityStoreException {

    }

    @Override
    public void doPostListGroups(int offset, int length, List<Group> groups) throws IdentityStoreException {

    }

    @Override
    public void doPreListGroups(int offset, int length, String domainName) throws IdentityStoreException {

    }

    @Override
    public void doPostListGroups(int offset, int length, String domainName, List<Group> groups)
            throws IdentityStoreException {

    }

    @Override
    public void doPreListGroups(Claim claim, int offset, int length) throws IdentityStoreException {

    }

    @Override
    public void doPostListGroups(Claim claim, int offset, int length, List<Group> groups)
            throws IdentityStoreException {

    }

    @Override
    public void doPreListGroups(MetaClaim metaClaim, String filterPattern, int offset, int length)
            throws IdentityStoreException {

    }

    @Override
    public void doPostListGroups(MetaClaim metaClaim, String filterPattern, int offset, int length, List<Group> groups)
            throws IdentityStoreException {

    }

    @Override
    public void doPreListGroups(MetaClaim metaClaim, String filterPattern, int offset, int length, String domainName)
            throws IdentityStoreException {

    }

    @Override
    public void doPostListGroups(MetaClaim metaClaim, String filterPattern, int offset, int length, String domainName,
                                 List<Group> groups) throws IdentityStoreException {

    }

    @Override
    public void doPreListGroups(Claim claim, int offset, int length, String domainName) throws IdentityStoreException {

    }

    @Override
    public void doPostListGroups(Claim claim, int offset, int length, String domainName, List<Group> groups)
            throws IdentityStoreException {

    }

    @Override
    public void doPreGetGroupsOfUser(String uniqueUserId) throws IdentityStoreException {

    }

    @Override
    public void doPostGetGroupsOfUser(String uniqueUserId, List<Group> groups) throws IdentityStoreException {

    }

    @Override
    public void doPreGetUsersOfGroup(String uniqueGroupId) throws IdentityStoreException {

    }

    @Override
    public void doPostGetUsersOfGroup(String uniqueGroupId, List<User> users) throws IdentityStoreException {

    }

    @Override
    public void doPreIsUserInGroup(String uniqueUserId, String uniqueGroupId) throws IdentityStoreException {

    }

    @Override
    public void doPostIsUserInGroup(String uniqueUserId, String uniqueGroupId, Boolean isUserInGroup)
            throws IdentityStoreException {

    }

    @Override
    public void doPreGetClaimsOfUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException {

    }

    @Override
    public void doPostGetClaimsOfUser(String uniqueUserId, List<Claim> claims)
            throws IdentityStoreException, UserNotFoundException {

    }

    @Override
    public void doPreGetClaimsOfUser(String uniqueUserId, List<MetaClaim> metaClaims)
            throws IdentityStoreException, UserNotFoundException {

    }

    @Override
    public void doPostGetClaimsOfUser(String uniqueUserId, List<MetaClaim> metaClaims, List<Claim> claims)
            throws IdentityStoreException, UserNotFoundException {

    }

    @Override
    public void doPreGetClaimsOfGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException {

    }

    @Override
    public void doPostGetClaimsOfGroup(String uniqueGroupId, List<Claim> claims)
            throws IdentityStoreException, GroupNotFoundException {

    }

    @Override
    public void doPreGetClaimsOfGroup(String uniqueGroupId, List<MetaClaim> metaClaims)
            throws IdentityStoreException, GroupNotFoundException {

    }

    @Override
    public void doPostGetClaimsOfGroup(String uniqueGroupId, List<MetaClaim> metaClaims, List<Claim> claims)
            throws IdentityStoreException, GroupNotFoundException {

    }

    @Override
    public void doPreAddUser(UserBean user) throws IdentityStoreException {

    }

    @Override
    public void doPostAddUser(UserBean userBean, User user) throws IdentityStoreException {

    }

    @Override
    public void doPreAddUser(UserBean user, String domainName) throws IdentityStoreException {

    }

    @Override
    public void doPostAddUser(UserBean userBean, String domainName, User user) throws IdentityStoreException {

    }

    @Override
    public void doPreAddUsers(List<UserBean> users) throws IdentityStoreException {

    }

    @Override
    public void doPostAddUsers(List<UserBean> userBeans, List<User> users) throws IdentityStoreException {

    }

    @Override
    public void doPreAddUsers(List<UserBean> users, String domainName) throws IdentityStoreException {

    }

    @Override
    public void doPostAddUsers(List<UserBean> userBeans, String domainName, List<User> users)
            throws IdentityStoreException {

    }

    @Override
    public void doPreUpdateUserClaims(String uniqueUserId, List<Claim> claims)
            throws IdentityStoreException, UserNotFoundException {

    }

    @Override
    public void doPostUpdateUserClaims(String uniqueUserId, List<Claim> claims)
            throws IdentityStoreException, UserNotFoundException {

    }

    @Override
    public void doPreUpdateUserClaims(String uniqueUserId, List<Claim> claimsToAdd, List<Claim> claimsToRemove)
            throws IdentityStoreException, UserNotFoundException {

    }

    @Override
    public void doPostUpdateUserClaims(String uniqueUserId, List<Claim> claimsToAdd, List<Claim> claimsToRemove)
            throws IdentityStoreException, UserNotFoundException {

    }

    @Override
    public void doPreUpdateUserCredentials(String uniqueUserId, List<Callback> credentials)
            throws IdentityStoreException, UserNotFoundException {

    }

    @Override
    public void doPostUpdateUserCredentials(String uniqueUserId, List<Callback> credentials)
            throws IdentityStoreException, UserNotFoundException {

    }

    @Override
    public void doPreUpdateUserCredentials(String uniqueUserId, List<Callback> credentialsToAdd,
                                           List<Callback> credentialsToRemove)
            throws IdentityStoreException, UserNotFoundException {

    }

    @Override
    public void doPostUpdateUserCredentials(String uniqueUserId, List<Callback> credentialsToAdd,
                                            List<Callback> credentialsToRemove)
            throws IdentityStoreException, UserNotFoundException {

    }

    @Override
    public void doPreDeleteUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException {

    }

    @Override
    public void doPostDeleteUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException {

    }

    @Override
    public void doPreUpdateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIds)
            throws IdentityStoreException {

    }

    @Override
    public void doPostUpdateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIds)
            throws IdentityStoreException {

    }

    @Override
    public void doPreUpdateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIdsToAdd,
                                        List<String> uniqueGroupIdsToRemove) throws IdentityStoreException {

    }

    @Override
    public void doPostUpdateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIdsToAdd,
                                         List<String> uniqueGroupIdsToRemove) throws IdentityStoreException {

    }

    @Override
    public void doPreAddGroup(GroupBean groupBean) throws IdentityStoreException {

    }

    @Override
    public void doPostAddGroup(GroupBean groupBean, Group group) throws IdentityStoreException {

    }

    @Override
    public void doPreAddGroup(GroupBean groupBean, String domainName) throws IdentityStoreException {

    }

    @Override
    public void doPostAddGroup(GroupBean groupBean, String domainName, Group group) throws IdentityStoreException {

    }

    @Override
    public void doPreAddGroups(List<GroupBean> groups) throws IdentityStoreException {

    }

    @Override
    public void doPostAddGroups(List<GroupBean> groupBeans, List<Group> groups) throws IdentityStoreException {

    }

    @Override
    public void doPreAddGroups(List<GroupBean> groups, String domainName) throws IdentityStoreException {

    }

    @Override
    public void doPostAddGroups(List<GroupBean> groupsBeans, String domainName, List<Group> groups)
            throws IdentityStoreException {

    }

    @Override
    public void doPreUpdateGroupClaims(String uniqueGroupId, List<Claim> claims)
            throws IdentityStoreException, GroupNotFoundException {

    }

    @Override
    public void doPostUpdateGroupClaims(String uniqueGroupId, List<Claim> claims)
            throws IdentityStoreException, GroupNotFoundException {

    }

    @Override
    public void doPreUpdateGroupClaims(String uniqueGroupId, List<Claim> claimsToAdd, List<Claim> claimsToRemove)
            throws IdentityStoreException, GroupNotFoundException {

    }

    @Override
    public void doPostUpdateGroupClaims(String uniqueGroupId, List<Claim> claimsToAdd, List<Claim> claimsToRemove)
            throws IdentityStoreException, GroupNotFoundException {

    }

    @Override
    public void doPreDeleteGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException {

    }

    @Override
    public void doPostDeleteGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException {

    }

    @Override
    public void doPreUpdateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIds)
            throws IdentityStoreException {

    }

    @Override
    public void doPostUpdateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIds)
            throws IdentityStoreException {

    }

    @Override
    public void doPreUpdateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIdsToAdd,
                                        List<String> uniqueUserIdsToRemove) throws IdentityStoreException {

    }

    @Override
    public void doPostUpdateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIdsToAdd,
                                         List<String> uniqueUserIdsToRemove) throws IdentityStoreException {

    }

    @Override
    public void doPreAuthenticate(Claim claim, Callback[] credentials, String domainName)
            throws AuthenticationFailure, IdentityStoreException {

    }

    @Override
    public void doPostAuthenticate(Claim claim, Callback[] credentials, String domainName,
                                   AuthenticationContext authenticationContext)
            throws AuthenticationFailure, IdentityStoreException {

    }

    @Override
    public void doPreGetPrimaryDomainName() throws IdentityStoreException {

    }

    @Override
    public void doPostGetPrimaryDomainName(String primaryDomainName) throws IdentityStoreException {

    }

    @Override
    public void doPreGetDomainNames() throws IdentityStoreException {

    }

    @Override
    public void doPostGetDomainNames(Set<String> domainNames) throws IdentityStoreException {

    }
}

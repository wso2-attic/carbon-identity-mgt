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

package org.wso2.carbon.identity.mgt.impl;

import org.wso2.carbon.identity.mgt.AuthenticationContext;
import org.wso2.carbon.identity.mgt.Group;
import org.wso2.carbon.identity.mgt.IdentityStore;
import org.wso2.carbon.identity.mgt.User;
import org.wso2.carbon.identity.mgt.bean.GroupBean;
import org.wso2.carbon.identity.mgt.bean.UserBean;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.claim.MetaClaim;
import org.wso2.carbon.identity.mgt.exception.AuthenticationFailure;
import org.wso2.carbon.identity.mgt.exception.GroupNotFoundException;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.mgt.impl.config.StoreConfig;
import org.wso2.carbon.identity.mgt.impl.internal.IdentityMgtDataHolder;
import org.wso2.carbon.identity.mgt.interceptor.IdentityStoreInterceptor;
import org.wso2.carbon.kernel.utils.LambdaExceptionUtils;

import java.util.List;
import java.util.Set;
import javax.security.auth.callback.Callback;


/**
 * Interceptor for IdentityStore.
 * @since 1.0.0
 */
public class InterceptingIdentityStore implements IdentityStore {


    private IdentityStore identityStore;
    private List<IdentityStoreInterceptor> identityStoreInterceptors;


    public InterceptingIdentityStore(StoreConfig storeConfig, List<Domain> domains) throws IdentityStoreException {

        this.identityStoreInterceptors = IdentityMgtDataHolder.getInstance().getIdentityStoreInterceptors();
        if (storeConfig.isEnableCache() && storeConfig.isEnableIdentityStoreCache()) {
            identityStore = new CacheBackedIdentityStore(storeConfig.getIdentityStoreCacheConfigMap(), domains);
        } else {
            identityStore = new IdentityStoreImpl(domains);
        }
    }

    @Override
    public User getUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreGetUser(uniqueUserId)));

        User user = identityStore.getUser(uniqueUserId);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostGetUser(uniqueUserId, user)));

        return user;
    }

    @Override
    public User getUser(Claim claim) throws IdentityStoreException, UserNotFoundException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreGetUser(claim)));

        User user = identityStore.getUser(claim);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostGetUser(claim, user)));

        return user;
    }

    @Override
    public User getUser(Claim claim, String domainName) throws IdentityStoreException, UserNotFoundException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreGetUser(claim, domainName)));

        User user = identityStore.getUser(claim, domainName);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostGetUser(claim, domainName, user)));

        return user;
    }

    @Override
    public List<User> listUsers(int offset, int length) throws IdentityStoreException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreListUsers(offset, length)));

        List<User> users = identityStore.listUsers(offset, length);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostListUsers(offset, length, users)));

        return users;
    }

    @Override
    public List<User> listUsers(int offset, int length, String domainName) throws IdentityStoreException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreListUsers(offset, length, domainName)));

        List<User> users = identityStore.listUsers(offset, length, domainName);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostListUsers(offset, length, domainName,
                                                                                     users)));
        return users;
    }

    @Override
    public List<User> listUsers(Claim claim, int offset, int length) throws IdentityStoreException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreListUsers(claim, offset, length)));

        List<User> users = identityStore.listUsers(claim, offset, length);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostListUsers(claim, offset, length, users)));

        return users;
    }

    @Override
    public List<User> listUsers(Claim claim, int offset, int length, String domainName) throws IdentityStoreException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreListUsers(claim, offset, length,
                                                                                    domainName)));

        List<User> users = identityStore.listUsers(claim, offset, length, domainName);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostListUsers(claim, offset, length,
                                                                                     domainName, users)));

        return users;
    }

    @Override
    public List<User> listUsers(MetaClaim metaClaim, String filterPattern, int offset, int length)
            throws IdentityStoreException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreListUsers(metaClaim, filterPattern, offset,
                                                                                    length)));

        List<User> users = identityStore.listUsers(metaClaim, filterPattern, offset, length);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostListUsers(metaClaim, filterPattern,
                                                                                     offset, length, users)));

        return users;
    }

    @Override
    public List<User> listUsers(MetaClaim metaClaim, String filterPattern, int offset, int length, String domainName)
            throws IdentityStoreException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreListUsers(metaClaim, filterPattern, offset,
                                                                                    length, domainName)));

        List<User> users = identityStore.listUsers(metaClaim, filterPattern, offset, length, domainName);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostListUsers(metaClaim, filterPattern,
                                                                                 offset, length, domainName, users)));

        return users;
    }

    @Override
    public Group getGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreGetGroup(uniqueGroupId)));

        Group group = identityStore.getGroup(uniqueGroupId);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostGetGroup(uniqueGroupId, group)));

        return group;
    }

    @Override
    public Group getGroup(Claim claim) throws IdentityStoreException, GroupNotFoundException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreGetGroup(claim)));

        Group group = identityStore.getGroup(claim);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostGetGroup(claim, group)));

        return group;
    }

    @Override
    public Group getGroup(Claim claim, String domainName) throws IdentityStoreException, GroupNotFoundException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreGetGroup(claim, domainName)));

        Group group = identityStore.getGroup(claim, domainName);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostGetGroup(claim, domainName, group)));

        return group;
    }

    @Override
    public List<Group> listGroups(int offset, int length) throws IdentityStoreException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreListGroups(offset, length)));

        List<Group> groups = identityStore.listGroups(offset, length);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostListGroups(offset, length, groups)));

        return groups;
    }

    @Override
    public List<Group> listGroups(int offset, int length, String domainName) throws IdentityStoreException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreListGroups(offset, length, domainName)));

        List<Group> groups = identityStore.listGroups(offset, length, domainName);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostListGroups(offset, length, domainName,
                                                                                      groups)));

        return groups;
    }

    @Override
    public List<Group> listGroups(Claim claim, int offset, int length) throws IdentityStoreException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreListGroups(claim, offset, length)));

        List<Group> groups = identityStore.listGroups(claim, offset, length);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostListGroups(claim, offset, length, groups)));

        return groups;
    }

    @Override
    public List<Group> listGroups(Claim claim, int offset, int length, String domainName)
            throws IdentityStoreException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreListGroups(claim, offset, length,
                                                                                     domainName)));

        List<Group> groups = identityStore.listGroups(claim, offset, length, domainName);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostListGroups(claim, offset, length, domainName,
                                                                                      groups)));

        return groups;
    }

    @Override
    public List<Group> listGroups(MetaClaim metaClaim, String filterPattern, int offset, int length)
            throws IdentityStoreException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreListGroups(metaClaim, filterPattern,
                                                                                     offset, length)));

        List<Group> groups = identityStore.listGroups(metaClaim, filterPattern, offset, length);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostListGroups(metaClaim, filterPattern,
                                                                                      offset, length, groups)));

        return groups;
    }

    @Override
    public List<Group> listGroups(MetaClaim metaClaim, String filterPattern, int offset, int length, String domainName)
            throws IdentityStoreException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreListGroups(metaClaim, filterPattern,
                                                                                     offset, length, domainName)));

        List<Group> groups = identityStore.listGroups(metaClaim, filterPattern, offset, length, domainName);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostListGroups(metaClaim, filterPattern,
                                                                                  offset, length, domainName, groups)));

        return groups;
    }

    @Override
    public List<Group> getGroupsOfUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreGetGroupsOfUser(uniqueUserId)));

        List<Group> groups = identityStore.getGroupsOfUser(uniqueUserId);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostGetGroupsOfUser(uniqueUserId, groups)));

        return groups;
    }

    @Override
    public List<User> getUsersOfGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreGetUsersOfGroup(uniqueGroupId)));

        List<User> users = identityStore.getUsersOfGroup(uniqueGroupId);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostGetUsersOfGroup(uniqueGroupId, users)));

        return users;
    }

    @Override
    public boolean isUserInGroup(String uniqueUserId, String uniqueGroupId)
            throws IdentityStoreException, UserNotFoundException, GroupNotFoundException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreIsUserInGroup(uniqueUserId, uniqueGroupId)));

        Boolean isUserInGroup = identityStore.isUserInGroup(uniqueUserId, uniqueGroupId);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostIsUserInGroup(uniqueUserId,
                                                                                     uniqueGroupId, isUserInGroup)));
        return isUserInGroup;
    }

    @Override
    public List<Claim> getClaimsOfUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreGetClaimsOfUser(uniqueUserId)));

        List<Claim> claims = identityStore.getClaimsOfUser(uniqueUserId);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostGetClaimsOfUser(uniqueUserId, claims)));

        return claims;
    }

    @Override
    public List<Claim> getClaimsOfUser(String uniqueUserId, List<MetaClaim> metaClaims)
            throws IdentityStoreException, UserNotFoundException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreGetClaimsOfUser(uniqueUserId, metaClaims)));

        List<Claim> claims = identityStore.getClaimsOfUser(uniqueUserId, metaClaims);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostGetClaimsOfUser(uniqueUserId, metaClaims,
                                                                                           claims)));

        return claims;
    }

    @Override
    public List<Claim> getClaimsOfGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreGetClaimsOfGroup(uniqueGroupId)));

        List<Claim> claims = identityStore.getClaimsOfGroup(uniqueGroupId);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostGetClaimsOfGroup(uniqueGroupId, claims)));

        return claims;
    }

    @Override
    public List<Claim> getClaimsOfGroup(String uniqueGroupId, List<MetaClaim> metaClaims)
            throws IdentityStoreException, GroupNotFoundException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreGetClaimsOfGroup(uniqueGroupId, metaClaims)));

        List<Claim> claims = identityStore.getClaimsOfGroup(uniqueGroupId, metaClaims);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostGetClaimsOfGroup(uniqueGroupId,
                                                                                            metaClaims, claims)));

        return claims;
    }

    @Override
    public User addUser(UserBean userBean) throws IdentityStoreException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreAddUser(userBean)));

        User user = identityStore.addUser(userBean);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostAddUser(userBean, user)));

        return user;
    }

    @Override
    public User addUser(UserBean userBean, String domainName) throws IdentityStoreException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreAddUser(userBean, domainName)));

        User user = identityStore.addUser(userBean, domainName);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostAddUser(userBean, domainName, user)));

        return user;
    }

    @Override
    public List<User> addUsers(List<UserBean> userBeans) throws IdentityStoreException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreAddUsers(userBeans)));

        List<User> users = identityStore.addUsers(userBeans);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostAddUsers(userBeans, users)));

        return users;
    }

    @Override
    public List<User> addUsers(List<UserBean> userBeans, String domainName) throws IdentityStoreException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreAddUsers(userBeans, domainName)));

        List<User> users = identityStore.addUsers(userBeans);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostAddUsers(userBeans, domainName, users)));

        return users;
    }

    @Override
    public void updateUserClaims(String uniqueUserId, List<Claim> claims)
            throws IdentityStoreException, UserNotFoundException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreUpdateUserClaims(uniqueUserId, claims)));

        identityStore.updateUserClaims(uniqueUserId, claims);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostUpdateUserClaims(uniqueUserId, claims)));

    }

    @Override
    public void updateUserClaims(String uniqueUserId, List<Claim> claimsToAdd, List<Claim> claimsToRemove)
            throws IdentityStoreException, UserNotFoundException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreUpdateUserClaims(uniqueUserId, claimsToAdd,
                                                                                           claimsToRemove)));

        identityStore.updateUserClaims(uniqueUserId, claimsToAdd, claimsToRemove);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostUpdateUserClaims(uniqueUserId, claimsToAdd,
                                                                                            claimsToRemove)));

    }

    @Override
    public void updateUserCredentials(String uniqueUserId, List<Callback> credentials)
            throws IdentityStoreException, UserNotFoundException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreUpdateUserCredentials(uniqueUserId,
                                                                                                credentials)));

        identityStore.updateUserCredentials(uniqueUserId, credentials);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostUpdateUserCredentials(uniqueUserId,
                                                                                                 credentials)));
    }

    @Override
    public void updateUserCredentials(String uniqueUserId, List<Callback> credentialsToAdd,
                                      List<Callback> credentialsToRemove)
            throws IdentityStoreException, UserNotFoundException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreUpdateUserCredentials(uniqueUserId,
                                                                            credentialsToAdd, credentialsToRemove)));

        identityStore.updateUserCredentials(uniqueUserId, credentialsToAdd, credentialsToRemove);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostUpdateUserCredentials(uniqueUserId,
                                                                             credentialsToAdd, credentialsToRemove)));
    }

    @Override
    public void deleteUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreDeleteUser(uniqueUserId)));

        identityStore.deleteUser(uniqueUserId);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostDeleteUser(uniqueUserId)));

    }

    @Override
    public void updateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIds) throws IdentityStoreException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreUpdateGroupsOfUser(uniqueUserId,
                                                                                             uniqueGroupIds)));

        identityStore.updateGroupsOfUser(uniqueUserId, uniqueGroupIds);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostUpdateGroupsOfUser(uniqueUserId,
                                                                                              uniqueGroupIds)));
    }

    @Override
    public void updateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIdsToAdd,
                                   List<String> uniqueGroupIdsToRemove) throws IdentityStoreException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreUpdateGroupsOfUser(uniqueUserId,
                                                                         uniqueGroupIdsToAdd, uniqueGroupIdsToRemove)));

        identityStore.updateGroupsOfUser(uniqueUserId, uniqueGroupIdsToAdd, uniqueGroupIdsToRemove);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostUpdateGroupsOfUser(uniqueUserId,
                                                                      uniqueGroupIdsToAdd, uniqueGroupIdsToRemove)));

    }

    @Override
    public Group addGroup(GroupBean groupBean) throws IdentityStoreException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreAddGroup(groupBean)));

        Group group = identityStore.addGroup(groupBean);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostAddGroup(groupBean, group)));

        return group;
    }

    @Override
    public Group addGroup(GroupBean groupBean, String domainName) throws IdentityStoreException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreAddGroup(groupBean, domainName)));

        Group group = identityStore.addGroup(groupBean, domainName);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostAddGroup(groupBean, domainName, group)));

        return group;
    }

    @Override
    public List<Group> addGroups(List<GroupBean> groupBeans) throws IdentityStoreException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreAddGroups(groupBeans)));

        List<Group> groups = identityStore.addGroups(groupBeans);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostAddGroups(groupBeans, groups)));

        return groups;
    }

    @Override
    public List<Group> addGroups(List<GroupBean> groupBeans, String domainName) throws IdentityStoreException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreAddGroups(groupBeans, domainName)));

        List<Group> groups = identityStore.addGroups(groupBeans, domainName);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostAddGroups(groupBeans, domainName, groups)));

        return groups;
    }

    @Override
    public void updateGroupClaims(String uniqueGroupId, List<Claim> claims)
            throws IdentityStoreException, GroupNotFoundException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreUpdateGroupClaims(uniqueGroupId, claims)));

        identityStore.updateGroupClaims(uniqueGroupId, claims);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostUpdateGroupClaims(uniqueGroupId, claims)));
    }

    @Override
    public void updateGroupClaims(String uniqueGroupId, List<Claim> claimsToAdd, List<Claim> claimsToRemove)
            throws IdentityStoreException, GroupNotFoundException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreUpdateGroupClaims(uniqueGroupId,
                                                                                        claimsToAdd, claimsToRemove)));

        identityStore.updateGroupClaims(uniqueGroupId, claimsToAdd, claimsToRemove);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostUpdateGroupClaims(uniqueGroupId,
                                                                                         claimsToAdd, claimsToRemove)));

    }

    @Override
    public void deleteGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreDeleteGroup(uniqueGroupId)));

        identityStore.deleteGroup(uniqueGroupId);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostDeleteGroup(uniqueGroupId)));
    }

    @Override
    public void updateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIds) throws IdentityStoreException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreUpdateUsersOfGroup(uniqueGroupId,
                                                                                             uniqueUserIds)));

        identityStore.updateUsersOfGroup(uniqueGroupId, uniqueUserIds);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostUpdateUsersOfGroup(uniqueGroupId,
                                                                                              uniqueUserIds)));
    }

    @Override
    public void updateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIdsToAdd,
                                   List<String> uniqueUserIdsToRemove) throws IdentityStoreException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreUpdateUsersOfGroup(uniqueGroupId,
                                                                         uniqueUserIdsToAdd, uniqueUserIdsToRemove)));

        identityStore.updateUsersOfGroup(uniqueGroupId, uniqueUserIdsToAdd, uniqueUserIdsToRemove);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostUpdateUsersOfGroup(uniqueGroupId,
                                                                          uniqueUserIdsToAdd, uniqueUserIdsToRemove)));
    }

    @Override
    public AuthenticationContext authenticate(Claim claim, Callback[] credentials, String domainName)
            throws AuthenticationFailure, IdentityStoreException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPreAuthenticate(claim, credentials,
                                                                                       domainName)));

        AuthenticationContext authenticationContext = identityStore.authenticate(claim, credentials, domainName);

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostAuthenticate(claim, credentials,
                                                                                domainName, authenticationContext)));

        return authenticationContext;
    }

    @Override
    public String getPrimaryDomainName() throws IdentityStoreException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                IdentityStoreInterceptor::doPreGetPrimaryDomainName));

        String primaryDomainName = identityStore.getPrimaryDomainName();

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostGetPrimaryDomainName(primaryDomainName)));

        return primaryDomainName;
    }

    @Override
    public Set<String> getDomainNames() throws IdentityStoreException {

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                IdentityStoreInterceptor::doPreGetDomainNames));

        Set<String> domainNames = identityStore.getDomainNames();

        identityStoreInterceptors.forEach(LambdaExceptionUtils.rethrowConsumer(
                identityStoreInterceptor -> identityStoreInterceptor.doPostGetDomainNames(domainNames)));

        return domainNames;
    }
}

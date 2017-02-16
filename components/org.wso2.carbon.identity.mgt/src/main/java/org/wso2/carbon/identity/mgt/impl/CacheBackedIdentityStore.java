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

package org.wso2.carbon.identity.mgt.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.caching.CarbonCachingService;
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
import org.wso2.carbon.identity.mgt.impl.config.CacheConfig;
import org.wso2.carbon.identity.mgt.impl.internal.IdentityMgtDataHolder;
import org.wso2.carbon.identity.mgt.impl.util.CacheHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.security.auth.callback.Callback;

import static org.wso2.carbon.kernel.utils.StringUtils.isNullOrEmpty;

/**
 * Virtual identity store with the caching.
 *
 * @since 1.0.0
 */
public class CacheBackedIdentityStore implements IdentityStore {

    private static Logger log = LoggerFactory.getLogger(CacheBackedIdentityStore.class);

    private static final String UNIQUE_USER_CACHE = "uniqueUserCache";

    private static final String UNIQUE_GROUP_CACHE = "uniqueGroupCache";

    private IdentityStore identityStore;

    private Map<String, Boolean> cacheStatus = new HashMap<>();

    private CacheManager cacheManager;


    public CacheBackedIdentityStore(Map<String, CacheConfig> cacheConfigs, List<Domain> domains)
            throws IdentityStoreException {

        CarbonCachingService carbonCachingService = IdentityMgtDataHolder.getInstance().getCarbonCachingService();

        identityStore = new IdentityStoreImpl(domains);

        cacheManager = carbonCachingService.getCachingProvider().getCacheManager();

        // Initialize all caches.
        if (CacheHelper.isCacheEnabled(cacheConfigs, UNIQUE_USER_CACHE)) {
            CacheHelper.createCache(UNIQUE_USER_CACHE, String.class, User.class, CacheHelper.MEDIUM_EXPIRE_TIME,
                    cacheConfigs, cacheManager);
            cacheStatus.put(UNIQUE_USER_CACHE, true);
        } else {
            cacheStatus.put(UNIQUE_USER_CACHE, false);
        }

        if (log.isDebugEnabled()) {
            log.debug("Cache backed identity store successfully initialized.");
        }
    }

    @Override
    public boolean isUserExist(List<Claim> userClaims, String domainName) throws IdentityStoreException {
        return identityStore.isUserExist(userClaims, domainName);
    }

    @Override
    public Map<String, String> isUserExist(List<Claim> userClaims) throws IdentityStoreException {
        return identityStore.isUserExist(userClaims);
    }

    @Override
    public User getUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException {

        if (cacheStatus.get(UNIQUE_USER_CACHE) && isNullOrEmpty(uniqueUserId)) {
            return doGetUser(uniqueUserId, identityStore.getPrimaryDomainName());
        }

        User user = identityStore.getUser(uniqueUserId);
        user.setIdentityStore(this);
        return user;
    }

    @Override
    public User getUser(Claim claim) throws IdentityStoreException, UserNotFoundException {

        return identityStore.getUser(claim);
    }

    @Override
    public User getUser(Claim claim, String domainName) throws IdentityStoreException, UserNotFoundException {

        return identityStore.getUser(claim);
    }

    @Override
    public List<User> listUsers(int offset, int length) throws IdentityStoreException {

        return identityStore.listUsers(offset, length);
    }

    @Override
    public List<User> listUsers(int offset, int length, String domainName) throws IdentityStoreException {

        return identityStore.listUsers(offset, length, domainName);
    }

    @Override
    public List<User> listUsers(Claim claim, int offset, int length) throws IdentityStoreException {

        return identityStore.listUsers(claim, offset, length);
    }

    @Override
    public List<User> listUsers(Claim claim, int offset, int length, String domainName) throws IdentityStoreException {

        return identityStore.listUsers(claim, offset, length, domainName);
    }

    @Override
    public List<User> listUsers(MetaClaim metaClaim, String filterPattern, int offset, int length) throws
            IdentityStoreException {

        return identityStore.listUsers(metaClaim, filterPattern, offset, length);
    }

    @Override
    public List<User> listUsers(MetaClaim metaClaim, String filterPattern, int offset, int length, String domainName)
            throws IdentityStoreException {

        return identityStore.listUsers(metaClaim, filterPattern, offset, length, domainName);
    }

    @Override
    public List<User> listUsers(List<Claim> claims, int offset, int length) throws IdentityStoreException {
        return identityStore.listUsers(claims, offset, length);
    }

    @Override
    public List<User> listUsers(List<Claim> claims, int offset, int length, String domainName)
            throws IdentityStoreException {
        return identityStore.listUsers(claims, offset, length, domainName);
    }

    @Override
    public boolean isGroupExist(List<Claim> groupClaims, String domainName) throws IdentityStoreException {
        return identityStore.isGroupExist(groupClaims, domainName);
    }

    @Override
    public Group getGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException {

        if (cacheStatus.get(UNIQUE_GROUP_CACHE) && isNullOrEmpty(uniqueGroupId)) {
            return doGetGroup(uniqueGroupId, identityStore.getPrimaryDomainName());
        }

        Group group = identityStore.getGroup(uniqueGroupId);
        group.setIdentityStore(this);
        return group;
    }

    @Override
    public Group getGroup(Claim claim) throws IdentityStoreException, GroupNotFoundException {

        return identityStore.getGroup(claim);
    }

    @Override
    public Group getGroup(Claim claim, String domainName) throws IdentityStoreException, GroupNotFoundException {

        return identityStore.getGroup(claim, domainName);
    }

    @Override
    public List<Group> listGroups(int offset, int length) throws IdentityStoreException {

        return identityStore.listGroups(offset, length);
    }

    @Override
    public List<Group> listGroups(int offset, int length, String domainName) throws IdentityStoreException {

        return identityStore.listGroups(offset, length, domainName);
    }

    @Override
    public List<Group> listGroups(Claim claim, int offset, int length) throws IdentityStoreException {

        return identityStore.listGroups(claim, offset, length);
    }

    @Override
    public List<Group> listGroups(Claim claim, int offset, int length, String domainName) throws
            IdentityStoreException {

        return identityStore.listGroups(claim, offset, length, domainName);
    }

    @Override
    public List<Group> listGroups(MetaClaim metaClaim, String filterPattern, int offset, int length) throws
            IdentityStoreException {

        return identityStore.listGroups(metaClaim, filterPattern, offset, length);
    }

    @Override
    public List<Group> listGroups(MetaClaim metaClaim, String filterPattern, int offset, int length, String
            domainName) throws IdentityStoreException {

        return identityStore.listGroups(metaClaim, filterPattern, offset, length, domainName);
    }

    @Override
    public List<Group> getGroupsOfUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException {

        return identityStore.getGroupsOfUser(uniqueUserId);
    }

    @Override
    public List<User> getUsersOfGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException {

        return identityStore.getUsersOfGroup(uniqueGroupId);
    }

    @Override
    public boolean isUserInGroup(String uniqueUserId, String uniqueGroupId) throws IdentityStoreException,
            UserNotFoundException, GroupNotFoundException {

        return identityStore.isUserInGroup(uniqueUserId, uniqueGroupId);
    }

    @Override
    public List<Claim> getClaimsOfUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException {

        return identityStore.getClaimsOfUser(uniqueUserId);
    }

    @Override
    public List<Claim> getClaimsOfUser(String uniqueUserId, List<MetaClaim> metaClaims) throws IdentityStoreException,
            UserNotFoundException {

        return identityStore.getClaimsOfUser(uniqueUserId, metaClaims);
    }

    @Override
    public List<Claim> getClaimsOfGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException {

        return identityStore.getClaimsOfGroup(uniqueGroupId);
    }

    @Override
    public List<Claim> getClaimsOfGroup(String uniqueGroupId, List<MetaClaim> metaClaims) throws
            IdentityStoreException, GroupNotFoundException {

        return identityStore.getClaimsOfGroup(uniqueGroupId, metaClaims);
    }

    @Override
    public User addUser(UserBean userBean) throws IdentityStoreException {

        return identityStore.addUser(userBean);
    }

    @Override
    public User addUser(UserBean userBean, String domainName) throws IdentityStoreException {

        return identityStore.addUser(userBean, domainName);
    }

    @Override
    public List<User> addUsers(List<UserBean> userBeen) throws IdentityStoreException {

        return identityStore.addUsers(userBeen);
    }

    @Override
    public List<User> addUsers(List<UserBean> userBeen, String domainName) throws IdentityStoreException {

        return identityStore.addUsers(userBeen, domainName);
    }

    @Override
    public void updateUserClaims(String uniqueUserId, List<Claim> claims) throws IdentityStoreException,
            UserNotFoundException {

        identityStore.updateUserClaims(uniqueUserId, claims);
    }

    @Override
    public void updateUserClaims(String uniqueUserId, List<Claim> claimsToAdd, List<Claim> claimsToRemove) throws
            IdentityStoreException, UserNotFoundException {

        identityStore.updateUserClaims(uniqueUserId, claimsToAdd, claimsToRemove);
    }

    @Override
    public void updateUserCredentials(String uniqueUserId, List<Callback> credentials) throws IdentityStoreException,
            UserNotFoundException {

        identityStore.updateUserCredentials(uniqueUserId, credentials);
    }

    @Override
    public void updateUserCredentials(String uniqueUserId, List<Callback> credentialsToAdd, List<Callback>
            credentialsToRemove) throws IdentityStoreException, UserNotFoundException {

        identityStore.updateUserCredentials(uniqueUserId, credentialsToAdd, credentialsToRemove);
    }

    @Override
    public void deleteUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException {

        identityStore.deleteUser(uniqueUserId);

        doDeleteUser(uniqueUserId, identityStore.getPrimaryDomainName());
    }

    @Override
    public void updateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIds) throws IdentityStoreException {

        identityStore.updateGroupsOfUser(uniqueUserId, uniqueGroupIds);
    }

    @Override
    public void updateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIdsToAdd, List<String>
            uniqueGroupIdsToRemove) throws IdentityStoreException {

        identityStore.updateGroupsOfUser(uniqueUserId, uniqueGroupIdsToAdd, uniqueGroupIdsToRemove);
    }

    @Override
    public Group addGroup(GroupBean groupBean) throws IdentityStoreException {

        return identityStore.addGroup(groupBean);
    }

    @Override
    public Group addGroup(GroupBean groupBean, String domainName) throws IdentityStoreException {

        return identityStore.addGroup(groupBean, domainName);
    }

    @Override
    public List<Group> addGroups(List<GroupBean> groupBeen) throws IdentityStoreException {

        return identityStore.addGroups(groupBeen);
    }

    @Override
    public List<Group> addGroups(List<GroupBean> groupBeen, String domainName) throws IdentityStoreException {

        return identityStore.addGroups(groupBeen, domainName);
    }

    @Override
    public void updateGroupClaims(String uniqueGroupId, List<Claim> claims) throws IdentityStoreException,
            GroupNotFoundException {

        identityStore.updateGroupClaims(uniqueGroupId, claims);
    }

    @Override
    public void updateGroupClaims(String uniqueGroupId, List<Claim> claimsToAdd, List<Claim> claimsToRemove) throws
            IdentityStoreException, GroupNotFoundException {

        identityStore.updateGroupClaims(uniqueGroupId, claimsToAdd, claimsToRemove);
    }

    @Override
    public void deleteGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException {

        identityStore.deleteGroup(uniqueGroupId);

        doDeleteGroup(uniqueGroupId, identityStore.getPrimaryDomainName());
    }

    @Override
    public void updateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIds) throws IdentityStoreException {

        identityStore.updateUsersOfGroup(uniqueGroupId, uniqueUserIds);
    }

    @Override
    public void updateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIdsToAdd, List<String>
            uniqueUserIdsToRemove) throws IdentityStoreException {

        identityStore.updateUsersOfGroup(uniqueGroupId, uniqueUserIdsToAdd, uniqueUserIdsToRemove);
    }

    @Override
    public AuthenticationContext authenticate(Claim claim, Callback[] credentials, String domainName) throws
            AuthenticationFailure, IdentityStoreException {

        return identityStore.authenticate(claim, credentials, domainName);
    }

    @Override
    public String getPrimaryDomainName() throws IdentityStoreException {

        return identityStore.getPrimaryDomainName();
    }

    @Override
    public Set<String> getDomainNames() throws IdentityStoreException {

        return identityStore.getDomainNames();
    }

    private User doGetUser(String uniqueUserId, String domainName) throws IdentityStoreException,
            UserNotFoundException {

        Cache<String, User> userCache = cacheManager.getCache(UNIQUE_USER_CACHE, String.class, User.class);
        User user = userCache.get(uniqueUserId.hashCode() + ":" + domainName.hashCode());

        if (user == null) {
            user = identityStore.getUser(uniqueUserId);
            userCache.put(user.getUniqueUserId().hashCode() + ":" + user.getDomainName().hashCode(), user);
            user.setIdentityStore(this);
            return user;
        }

        user.setIdentityStore(this);
        return user;
    }

    private Group doGetGroup(String uniqueGroupId, String domainName) throws IdentityStoreException,
            GroupNotFoundException {

        Cache<String, Group> groupCache = cacheManager.getCache(UNIQUE_GROUP_CACHE, String.class, Group.class);
        Group group = groupCache.get(uniqueGroupId.hashCode() + ":" + domainName.hashCode());

        if (group == null) {
            group = identityStore.getGroup(uniqueGroupId);
            groupCache.put(group.getUniqueGroupId().hashCode() + ":" + group.getDomainName().hashCode(), group);
            group.setIdentityStore(this);
            return group;
        }

        group.setIdentityStore(this);
        return group;
    }

    private void doDeleteUser(String uniqueUserId, String domainName) {

        Cache<String, User> userCache = cacheManager.getCache(UNIQUE_USER_CACHE, String.class, User.class);
        userCache.remove(uniqueUserId.hashCode() + ":" + domainName.hashCode());
    }

    private void doDeleteGroup(String uniqueGroupId, String domainName) {

        Cache<String, Group> groupCache = cacheManager.getCache(UNIQUE_GROUP_CACHE, String.class, Group.class);
        groupCache.remove(uniqueGroupId.hashCode() + ":" + domainName.hashCode());
    }
}

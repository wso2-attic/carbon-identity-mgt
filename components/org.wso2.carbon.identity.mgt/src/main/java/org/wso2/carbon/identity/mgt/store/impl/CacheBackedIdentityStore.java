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

package org.wso2.carbon.identity.mgt.store.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.caching.CarbonCachingService;
import org.wso2.carbon.identity.mgt.bean.Domain;
import org.wso2.carbon.identity.mgt.bean.Group;
import org.wso2.carbon.identity.mgt.bean.User;
import org.wso2.carbon.identity.mgt.cache.CacheHelper;
import org.wso2.carbon.identity.mgt.cache.CachedGroup;
import org.wso2.carbon.identity.mgt.cache.CachedUser;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.claim.MetaClaim;
import org.wso2.carbon.identity.mgt.config.CacheConfig;
import org.wso2.carbon.identity.mgt.context.AuthenticationContext;
import org.wso2.carbon.identity.mgt.exception.AuthenticationFailure;
import org.wso2.carbon.identity.mgt.exception.CarbonSecurityDataHolderException;
import org.wso2.carbon.identity.mgt.exception.GroupNotFoundException;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtDataHolder;
import org.wso2.carbon.identity.mgt.model.GroupModel;
import org.wso2.carbon.identity.mgt.model.UserModel;
import org.wso2.carbon.identity.mgt.store.IdentityStore;

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

    private IdentityStore identityStore = new IdentityStoreImpl();

    private Map<String, Boolean> cacheStatus = new HashMap<>();

    private CacheManager cacheManager;

    private Map<String, CacheConfig> cacheConfigs;

    public CacheBackedIdentityStore(Map<String, CacheConfig> cacheConfigs) {
        this.cacheConfigs = cacheConfigs;
    }

    @Override
    public void init(List<Domain> domains) throws IdentityStoreException {

        CarbonCachingService carbonCachingService;

        try {
            carbonCachingService = IdentityMgtDataHolder.getInstance().getCarbonCachingService();
        } catch (CarbonSecurityDataHolderException e) {
            throw new IdentityStoreException("Caching service is not available.", e);
        }

        identityStore.init(domains);

        cacheManager = carbonCachingService.getCachingProvider().getCacheManager();

        // Initialize all caches.
        if (CacheHelper.isCacheEnabled(cacheConfigs, UNIQUE_USER_CACHE)) {
            CacheHelper.createCache(UNIQUE_USER_CACHE, String.class, CachedUser.class, CacheHelper.MEDIUM_EXPIRE_TIME,
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
    public User getUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException {

        if (cacheStatus.get(UNIQUE_USER_CACHE) && isNullOrEmpty(uniqueUserId)) {
            return doGetUser(uniqueUserId, identityStore.getPrimaryDomainName());
        }

        User user = identityStore.getUser(uniqueUserId);
        user.setIdentityStore(this);
        return user;
    }

    @Override
    public User getUser(String uniqueUserId, String domainName) throws IdentityStoreException, UserNotFoundException {

        if (isNullOrEmpty(domainName)) {
            return getUser(uniqueUserId);
        }

        if (cacheStatus.get(UNIQUE_USER_CACHE) && isNullOrEmpty(uniqueUserId)) {
            return doGetUser(uniqueUserId, domainName);
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
    public Group getGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException {

        if (cacheStatus.get(UNIQUE_GROUP_CACHE) && isNullOrEmpty(uniqueGroupId)) {
            return doGetGroup(uniqueGroupId, identityStore.getPrimaryDomainName());
        }

        Group group = identityStore.getGroup(uniqueGroupId);
        group.setIdentityStore(this);
        return group;
    }

    @Override
    public Group getGroup(String uniqueGroupId, String domainName) throws IdentityStoreException,
            GroupNotFoundException {

        if (isNullOrEmpty(domainName)) {
            return getGroup(uniqueGroupId);
        }

        if (cacheStatus.get(UNIQUE_GROUP_CACHE) && isNullOrEmpty(uniqueGroupId)) {
            return doGetGroup(uniqueGroupId, domainName);
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
    public List<Group> getGroupsOfUser(String uniqueUserId, String domainName) throws IdentityStoreException,
            UserNotFoundException {

        return identityStore.getGroupsOfUser(uniqueUserId, domainName);
    }

    @Override
    public List<User> getUsersOfGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException {

        return identityStore.getUsersOfGroup(uniqueGroupId);
    }

    @Override
    public List<User> getUsersOfGroup(String uniqueGroupId, String domainName) throws IdentityStoreException,
            GroupNotFoundException {

        return identityStore.getUsersOfGroup(uniqueGroupId, domainName);
    }

    @Override
    public boolean isUserInGroup(String uniqueUserId, String uniqueGroupId) throws IdentityStoreException,
            UserNotFoundException {

        return identityStore.isUserInGroup(uniqueUserId, uniqueGroupId);
    }

    @Override
    public boolean isUserInGroup(String uniqueUserId, String uniqueGroupId, String domainName) throws
            IdentityStoreException, UserNotFoundException {

        return identityStore.isUserInGroup(uniqueUserId, uniqueGroupId, domainName);
    }

    @Override
    public List<Claim> getClaimsOfUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException {

        return identityStore.getClaimsOfUser(uniqueUserId);
    }

    @Override
    public List<Claim> getClaimsOfUser(String uniqueUserId, String domainName) throws IdentityStoreException,
            UserNotFoundException {

        return identityStore.getClaimsOfUser(uniqueUserId, domainName);
    }

    @Override
    public List<Claim> getClaimsOfUser(String uniqueUserId, List<MetaClaim> metaClaims) throws IdentityStoreException,
            UserNotFoundException {

        return identityStore.getClaimsOfUser(uniqueUserId, metaClaims);
    }

    @Override
    public List<Claim> getClaimsOfUser(String uniqueUserId, List<MetaClaim> metaClaims, String domainName) throws
            IdentityStoreException, UserNotFoundException {

        return identityStore.getClaimsOfUser(uniqueUserId, metaClaims, domainName);
    }

    @Override
    public List<Claim> getClaimsOfGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException {

        return identityStore.getClaimsOfGroup(uniqueGroupId);
    }

    @Override
    public List<Claim> getClaimsOfGroup(String uniqueGroupId, String domainName) throws IdentityStoreException,
            GroupNotFoundException {

        return identityStore.getClaimsOfGroup(uniqueGroupId, domainName);
    }

    @Override
    public List<Claim> getClaimsOfGroup(String uniqueGroupId, List<MetaClaim> metaClaims) throws
            IdentityStoreException, GroupNotFoundException {

        return identityStore.getClaimsOfGroup(uniqueGroupId, metaClaims);
    }

    @Override
    public List<Claim> getClaimsOfGroup(String uniqueGroupId, List<MetaClaim> metaClaims, String domainName) throws
            IdentityStoreException, GroupNotFoundException {

        return identityStore.getClaimsOfGroup(uniqueGroupId, metaClaims, domainName);
    }

    @Override
    public User addUser(UserModel userModel) throws IdentityStoreException {

        return identityStore.addUser(userModel);
    }

    @Override
    public User addUser(UserModel userModel, String domainName) throws IdentityStoreException {

        return identityStore.addUser(userModel, domainName);
    }

    @Override
    public List<User> addUsers(List<UserModel> userModels) throws IdentityStoreException {

        return identityStore.addUsers(userModels);
    }

    @Override
    public List<User> addUsers(List<UserModel> userModels, String domainName) throws IdentityStoreException {

        return identityStore.addUsers(userModels, domainName);
    }

    @Override
    public void updateUserClaims(String uniqueUserId, List<Claim> claims) throws IdentityStoreException,
            UserNotFoundException {

        identityStore.updateUserClaims(uniqueUserId, claims);
    }

    @Override
    public void updateUserClaims(String uniqueUserId, List<Claim> claims, String domainName) throws
            IdentityStoreException, UserNotFoundException {

        identityStore.updateUserClaims(uniqueUserId, claims, domainName);
    }

    @Override
    public void updateUserClaims(String uniqueUserId, List<Claim> claimsToAdd, List<Claim> claimsToRemove) throws
            IdentityStoreException, UserNotFoundException {

        identityStore.updateUserClaims(uniqueUserId, claimsToAdd, claimsToRemove);
    }

    @Override
    public void updateUserClaims(String uniqueUserId, List<Claim> claimsToAdd, List<Claim> claimsToRemove, String
            domainName) throws IdentityStoreException, UserNotFoundException {

        identityStore.updateUserClaims(uniqueUserId, claimsToAdd, claimsToRemove, domainName);
    }

    @Override
    public void deleteUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException {

        identityStore.deleteUser(uniqueUserId);

        doDeleteUser(uniqueUserId, identityStore.getPrimaryDomainName());
    }

    @Override
    public void deleteUser(String uniqueUserId, String domainName) throws IdentityStoreException,
            UserNotFoundException {

        if (isNullOrEmpty(domainName)) {
            deleteUser(uniqueUserId);
            return;
        }

        identityStore.deleteUser(uniqueUserId, domainName);
        doDeleteUser(uniqueUserId, domainName);
    }

    @Override
    public void updateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIds) throws IdentityStoreException {

        identityStore.updateGroupsOfUser(uniqueUserId, uniqueGroupIds);
    }

    @Override
    public void updateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIds, String domainName) throws
            IdentityStoreException {

        identityStore.updateGroupsOfUser(uniqueUserId, uniqueGroupIds, domainName);
    }

    @Override
    public void updateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIdsToAdd, List<String>
            uniqueGroupIdsToRemove) throws IdentityStoreException {

        identityStore.updateGroupsOfUser(uniqueUserId, uniqueGroupIdsToAdd, uniqueGroupIdsToRemove);
    }

    @Override
    public void updateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIdsToAdd, List<String>
            uniqueGroupIdsToRemove, String domainName) throws IdentityStoreException {

        identityStore.updateGroupsOfUser(uniqueUserId, uniqueGroupIdsToAdd, uniqueGroupIdsToRemove, domainName);
    }

    @Override
    public Group addGroup(GroupModel groupModel) throws IdentityStoreException {

        return identityStore.addGroup(groupModel);
    }

    @Override
    public Group addGroup(GroupModel groupModel, String domainName) throws IdentityStoreException {

        return identityStore.addGroup(groupModel, domainName);
    }

    @Override
    public List<Group> addGroups(List<GroupModel> groupModels) throws IdentityStoreException {

        return identityStore.addGroups(groupModels);
    }

    @Override
    public List<Group> addGroups(List<GroupModel> groupModels, String domainName) throws IdentityStoreException {

        return identityStore.addGroups(groupModels, domainName);
    }

    @Override
    public void updateGroupClaims(String uniqueGroupId, List<Claim> claims) throws IdentityStoreException,
            GroupNotFoundException {

        identityStore.updateGroupClaims(uniqueGroupId, claims);
    }

    @Override
    public void updateGroupClaims(String uniqueGroupId, List<Claim> claims, String domainName) throws
            IdentityStoreException, GroupNotFoundException {

        identityStore.updateGroupClaims(uniqueGroupId, claims, domainName);
    }

    @Override
    public void updateGroupClaims(String uniqueGroupId, List<Claim> claimsToAdd, List<Claim> claimsToRemove) throws
            IdentityStoreException, GroupNotFoundException {

        identityStore.updateGroupClaims(uniqueGroupId, claimsToAdd, claimsToRemove);
    }

    @Override
    public void updateGroupClaims(String uniqueGroupId, List<Claim> claimsToAdd, List<Claim> claimsToRemove, String
            domainName) throws IdentityStoreException, GroupNotFoundException {

        identityStore.updateGroupClaims(uniqueGroupId, claimsToAdd, claimsToRemove, domainName);
    }

    @Override
    public void deleteGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException {

        identityStore.deleteGroup(uniqueGroupId);

        doDeleteGroup(uniqueGroupId, identityStore.getPrimaryDomainName());
    }

    @Override
    public void deleteGroup(String uniqueGroupId, String domainName) throws IdentityStoreException,
            GroupNotFoundException {

        if (isNullOrEmpty(domainName)) {
            deleteGroup(uniqueGroupId);
            return;
        }

        identityStore.deleteGroup(uniqueGroupId, domainName);

        doDeleteGroup(uniqueGroupId, domainName);
    }

    @Override
    public void updateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIds) throws IdentityStoreException {

        identityStore.updateUsersOfGroup(uniqueGroupId, uniqueUserIds);
    }

    @Override
    public void updateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIds, String domainName) throws
            IdentityStoreException {

        identityStore.updateUsersOfGroup(uniqueGroupId, uniqueUserIds, domainName);
    }

    @Override
    public void updateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIdsToAdd, List<String>
            uniqueUserIdsToRemove) throws IdentityStoreException {

        identityStore.updateUsersOfGroup(uniqueGroupId, uniqueUserIdsToAdd, uniqueUserIdsToRemove);
    }

    @Override
    public void updateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIdsToAdd, List<String>
            uniqueUserIdsToRemove, String domainName) throws IdentityStoreException {

        identityStore.updateUsersOfGroup(uniqueGroupId, uniqueUserIdsToAdd, uniqueUserIdsToRemove);
    }

    @Override
    public AuthenticationContext authenticate(Claim claim, Callback[] credentials, String domainName) throws
            AuthenticationFailure {

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

        Cache<String, CachedUser> userCache = cacheManager.getCache(UNIQUE_USER_CACHE, String.class, CachedUser.class);
        CachedUser cachedUser = userCache.get(uniqueUserId.hashCode() + ":" + domainName.hashCode());

        if (cachedUser == null) {
            User user = identityStore.getUser(uniqueUserId);
            userCache.put(user.getUniqueUserId().hashCode() + ":" + user.getDomainName().hashCode(),
                    new CachedUser(user.getUniqueUserId(), user.getDomainName()));
            user.setIdentityStore(this);
            return user;
        }

        return new User.UserBuilder()
                .setUserId(cachedUser.getUniqueUserId())
                .setDomainName(cachedUser.getDomainName())
                .setIdentityStore(this)
                .setAuthorizationStore(IdentityMgtDataHolder.getInstance().getAuthorizationStore())
                .build();
    }

    private Group doGetGroup(String uniqueGroupId, String domainName) throws IdentityStoreException,
            GroupNotFoundException {

        Cache<String, CachedGroup> groupCache = cacheManager.getCache(UNIQUE_GROUP_CACHE, String.class, CachedGroup
                .class);
        CachedGroup cachedGroup = groupCache.get(uniqueGroupId.hashCode() + ":" + domainName.hashCode());

        if (cachedGroup == null) {
            Group group = identityStore.getGroup(uniqueGroupId);
            groupCache.put(group.getUniqueGroupId().hashCode() + ":" + group.getDomainName().hashCode(),
                    new CachedGroup(group.getUniqueGroupId(), group.getDomainName()));
            group.setIdentityStore(this);
            return group;
        }

        return new Group.GroupBuilder()
                .setGroupId(cachedGroup.getUniqueGroupId())
                .setDomainName(cachedGroup.getDomainName())
                .setIdentityStore(this)
                .setAuthorizationStore(IdentityMgtDataHolder.getInstance().getAuthorizationStore())
                .build();
    }

    private void doDeleteUser(String uniqueUserId, String domainName) {

        Cache<String, CachedUser> userCache = cacheManager.getCache(UNIQUE_USER_CACHE, String.class, CachedUser.class);
        userCache.remove(uniqueUserId.hashCode() + ":" + domainName.hashCode());
    }

    private void doDeleteGroup(String uniqueGroupId, String domainName) {

        Cache<String, CachedGroup> groupCache = cacheManager.getCache(UNIQUE_GROUP_CACHE, String.class,
                CachedGroup.class);
        groupCache.remove(uniqueGroupId.hashCode() + ":" + domainName.hashCode());
    }
}

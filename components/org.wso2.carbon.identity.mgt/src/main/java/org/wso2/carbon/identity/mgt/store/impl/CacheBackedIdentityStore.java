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
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.claim.MetaClaim;
import org.wso2.carbon.identity.mgt.config.CacheConfig;
import org.wso2.carbon.identity.mgt.constant.CacheNames;
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
import org.wso2.carbon.identity.mgt.util.CacheHelper;

import java.util.List;
import java.util.Map;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.security.auth.callback.Callback;

/**
 * Virtual identity store with the caching.
 *
 * @since 1.0.0
 */
public class CacheBackedIdentityStore implements IdentityStore {

    private static Logger log = LoggerFactory.getLogger(CacheBackedIdentityStore.class);

    private IdentityStore identityStore = new IdentityStoreImpl();
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
        cacheManager = carbonCachingService.getCachingProvider().getCacheManager();
        identityStore.init(domains);

        // Initialize all caches.
        CacheHelper.createCache(CacheNames.USER_USERNAME, String.class, User.class, CacheHelper.MEDIUM_EXPIRE_TIME,
                cacheConfigs, cacheManager);
        CacheHelper.createCache(CacheNames.USER_USERID, String.class, User.class, CacheHelper.MEDIUM_EXPIRE_TIME,
                cacheConfigs, cacheManager);
        CacheHelper.createCache(CacheNames.GROUP_GROUPNAME, String.class, Group.class, CacheHelper.MEDIUM_EXPIRE_TIME,
                cacheConfigs, cacheManager);
        CacheHelper.createCache(CacheNames.GROUP_GROUPID, String.class, Group.class, CacheHelper.MEDIUM_EXPIRE_TIME,
                cacheConfigs, cacheManager);
        CacheHelper.createCache(CacheNames.GROUPS_USERID_IDENTITYSTOREID, String.class, List.class,
                CacheHelper.MEDIUM_EXPIRE_TIME, cacheConfigs, cacheManager);

        if (log.isDebugEnabled()) {
            log.debug("Cache backed identity store successfully initialized.");
        }
    }

    @Override
    public User getUser(String username)
            throws IdentityStoreException, UserNotFoundException {

        if (CacheHelper.isCacheDisabled(cacheConfigs, CacheNames.USER_USERNAME)) {
            return identityStore.getUser(username);
        }

        Cache<String, User> cache = cacheManager.getCache(CacheNames.USER_USERNAME, String.class, User.class);
        User user = cache.get(username);

        if (user == null) {
            user = identityStore.getUser(username);
            cache.put(username, user);
            if (log.isDebugEnabled()) {
                log.debug("User cached for username: {}.", username);
            }
        }

        return user;
    }

    @Override
    public User getUser(String userId, String domain) throws IdentityStoreException, UserNotFoundException {
        return identityStore.getUser(userId, domain);
    }

    @Override
    public User getUser(Claim claim) throws IdentityStoreException, UserNotFoundException {
        return identityStore.getUser(claim);
    }

    @Override
    public User getUser(Claim claim, String domainName) throws IdentityStoreException, UserNotFoundException {
        return identityStore.getUser(claim, domainName);
    }

    @Override
    public List<User> listUsers(int offset, int length) throws IdentityStoreException {
        return identityStore.listUsers(offset, length);
    }

    @Override
    public List<User> listUsers(int offset, int length, String domainName) throws IdentityStoreException {
        return identityStore.listUsers(offset, length, domainName);
    }


//    @Override
//    public User getUserBuilder(String userId, Domain domain) throws IdentityStoreException {
//
//        if (CacheHelper.isCacheDisabled(cacheConfigs, CacheNames.USER_USERID)) {
//            return identityStore.getUserBuilder(userId, domain);
//        }
//
//        Cache<String, User> cache = cacheManager.getCache(CacheNames.USER_USERID, String.class, User.class);
//        User user = cache.get(userId + domain);
//
//        if (user == null) {
//            user = identityStore.getUserBuilder(userId, domain);
//            cache.put(userId + domain, user);
//            if (log.isDebugEnabled()) {
//                log.debug("User cached for user id: {} and identity store id: {}.", user, domain);
//            }
//        }
//
//        return user;
//    }


    @Override
    public List<User> listUsers(Claim claim, int offset, int length) throws IdentityStoreException {
        // TODO: Implement this method.
        return null;
    }

    @Override
    public List<User> listUsers(Claim claim, int offset, int length, String domain) throws IdentityStoreException {
        return null;
    }

    @Override
    public List<User> listUsers(MetaClaim metaClaim, String filterPattern, int offset, int length) throws
            IdentityStoreException {
        return null;
    }

    @Override
    public List<User> listUsers(MetaClaim metaClaim, String filterPattern, int offset, int length, String domain)
            throws IdentityStoreException {
        return null;
    }


    @Override
    public Group getGroup(String groupName) throws IdentityStoreException, GroupNotFoundException {

        if (CacheHelper.isCacheDisabled(cacheConfigs, CacheNames.GROUP_GROUPNAME)) {
            return identityStore.getGroup(groupName);
        }

        Cache<String, Group> cache = cacheManager.getCache(CacheNames.GROUP_GROUPNAME, String.class, Group.class);
        Group group = cache.get(groupName);

        if (group == null) {
            group = identityStore.getGroup(groupName);
            cache.put(groupName, group);
            if (log.isDebugEnabled()) {
                log.debug("Group cached for group name: {}.", groupName);
            }
        }

        return group;
    }

    @Override
    public Group getGroup(String groupId, String domain) throws IdentityStoreException, GroupNotFoundException {
        return null;
    }

    @Override
    public Group getGroup(Claim claim) throws IdentityStoreException, GroupNotFoundException {
        // TODO: Implement this.
        return null;
    }

    @Override
    public Group getGroup(Claim claim, String domain) throws IdentityStoreException, GroupNotFoundException {
        return null;
    }

    @Override
    public List<Group> listGroups(int offset, int length) throws IdentityStoreException {
        return null;
    }

    @Override
    public List<Group> listGroups(int offset, int length, String domain) throws IdentityStoreException {
        return null;
    }

    @Override
    public List<Group> listGroups(Claim claim, int offset, int length) throws IdentityStoreException {
        return null;
    }

    @Override
    public List<Group> listGroups(Claim claim, int offset, int length, String domain) throws IdentityStoreException {
        return null;
    }

    @Override
    public List<Group> listGroups(MetaClaim metaClaim, String filterPattern, int offset, int length)
            throws IdentityStoreException {
        return null;
    }

    @Override
    public List<Group> listGroups(MetaClaim metaClaim, String filterPattern, int offset, int length, String domain)
            throws IdentityStoreException {
        return null;
    }

//    @Override
//    public Group getGroupFromId(String groupId, String identityStoreId) throws IdentityStoreException {
//
//        if (CacheHelper.isCacheDisabled(cacheConfigs, CacheNames.GROUP_GROUPID)) {
//            return identityStore.getGroupFromId(groupId, identityStoreId);
//        }
//
//        Cache<String, Group> cache = cacheManager.getCache(CacheNames.GROUP_GROUPID, String.class, Group.class);
//        Group group = cache.get(groupId + identityStoreId);
//
//        if (group == null) {
//            group = identityStore.getGroupFromId(groupId, identityStoreId);
//            cache.put(groupId + identityStoreId, group);
//            if (log.isDebugEnabled()) {
//                log.debug("Group cached for group id: {} and for identity store id: {}.", groupId, identityStoreId);
//            }
//        }
//
//        return group;
//    }


    @Override
    public List<Group> getGroupsOfUser(String userName) throws IdentityStoreException {

//        if (CacheHelper.isCacheDisabled(cacheConfigs, CacheNames.GROUPS_USERID_IDENTITYSTOREID)) {
//            return identityStore.getGroupsOfUser(userName);
//        }
//
//        Cache<String, List> cache = cacheManager.getCache(CacheNames.GROUPS_USERID_IDENTITYSTOREID, String.class,
//                List.class);
//
//        List<Group> groups = cache.get(userName);
//
//        if (groups == null) {
//            groups = identityStore.getGroupsOfUser(userName);
//            cache.put(userName, groups);
//            if (log.isDebugEnabled()) {
//                log.debug("Groups cached for user id: {} and identity store id: {}.", userName);
//            }
//        }
//
//        return groups;
        return null;
    }

    @Override
    public List<User> getUsersOfGroup(String groupID) throws IdentityStoreException {
//        return identityStore.getUsersOfGroup(groupID);
        return null;
    }

    @Override
    public List<Group> getGroupsOfUser(String userId, String domain) throws IdentityStoreException {
        return null;
    }

    @Override
    public List<User> getUsersOfGroup(String groupId, String domain) throws IdentityStoreException {
        return null;
    }

    @Override
    public boolean isUserInGroup(String userId, String groupId) throws IdentityStoreException {
//
//        if (CacheHelper.isCacheDisabled(cacheConfigs, CacheNames.GROUPS_USERID_IDENTITYSTOREID)) {
//            return identityStore.isUserInGroup(userId, groupId);
//        }
//
//        Cache<String, List> cache = cacheManager.getCache(CacheNames.GROUPS_USERID_IDENTITYSTOREID, String.class,
//                List.class);
//
//        boolean isUserInGroup = false;
//        List<Group> groups = cache.get(userId);
//
//        if (groups == null) {
//            isUserInGroup = identityStore.isUserInGroup(userId, groupId);
//        } else {
//            // If there are groups for this user id and identity store id in the cache,
//            // do the validation logic here.
//            for (Group group : groups) {
//                if (group.getUniqueGroupId().equals(groupId)) {
//                    isUserInGroup = true;
//                    break;
//                }
//            }
//        }
//        return isUserInGroup;
        return false;
    }

    @Override
    public boolean isUserInGroup(String userId, String groupId, String domain) throws IdentityStoreException {
        return false;
    }

    @Override
    public List<Claim> getClaims(String uniqueUserId) throws IdentityStoreException, UserNotFoundException {
        return null;
    }

    @Override
    public List<Claim> getClaims(String uniqueUserId, String domainName) throws IdentityStoreException,
            UserNotFoundException {
        return null;
    }

    @Override
    public List<Claim> getClaims(User user, List<String> claimURIs) throws IdentityStoreException {
        return identityStore.getClaims(user, claimURIs);
    }

    @Override
    public User addUser(UserModel user) throws IdentityStoreException {
        return null;
    }

    @Override
    public User addUser(UserModel user, String domain) throws IdentityStoreException {
        return null;
    }

    @Override
    public List<User> addUsers(List<UserModel> users) throws IdentityStoreException {
        return null;
    }

    @Override
    public List<User> addUsers(List<UserModel> users, String domain) throws IdentityStoreException {
        return null;
    }

    @Override
    public void updateUserClaims(String userId, List<Claim> userClaims) throws IdentityStoreException {

    }

    @Override
    public void updateUserClaims(String userId, List<Claim> userClaimsToAdd, List<Claim> userClaimsToRemove) throws
            IdentityStoreException {

    }

    @Override
    public void deleteUser(String userId) throws IdentityStoreException {

    }

    @Override
    public void updateGroupsOfUser(String userId, List<String> groupIds) throws IdentityStoreException {

    }

    @Override
    public void updateGroupsOfUser(String userId, List<String> groupIdsToAdd, List<String> groupIdsToRemove) throws
            IdentityStoreException {

    }

    @Override
    public Group addGroup(GroupModel groupModel) throws IdentityStoreException {
        return null;
    }

    @Override
    public Group addGroup(GroupModel groupModel, String domain) throws IdentityStoreException {
        return null;
    }

    @Override
    public List<Group> addGroups(List<GroupModel> groups) throws IdentityStoreException {
        return null;
    }

    @Override
    public List<Group> addGroups(List<GroupModel> groups, String domain) throws IdentityStoreException {
        return null;
    }

    @Override
    public void updateGroupClaims(String groupId, List<Claim> groupClaims) throws IdentityStoreException {

    }

    @Override
    public void updateGroupClaims(String groupId, List<Claim> groupClaimsToAdd, List<Claim> groupClaimsToRemove)
            throws IdentityStoreException {

    }

    @Override
    public void deleteGroup(String groupId) throws IdentityStoreException {

    }

    @Override
    public void updateUsersOfGroup(String groupId, List<String> userIds) throws IdentityStoreException {

    }

    @Override
    public void updateUsersOfGroup(String groupId, List<String> userIdsToAdd, List<String> userIdsToRemove) throws
            IdentityStoreException {

    }

    @Override
    public AuthenticationContext authenticate(Claim claim, Callback credential, String domainName) throws
            AuthenticationFailure {
        return null;
    }
}

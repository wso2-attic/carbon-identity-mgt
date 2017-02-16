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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.event.EventException;
import org.wso2.carbon.identity.event.EventService;
import org.wso2.carbon.identity.event.model.Event;
import org.wso2.carbon.identity.mgt.AuthenticationContext;
import org.wso2.carbon.identity.mgt.Group;
import org.wso2.carbon.identity.mgt.IdentityStore;
import org.wso2.carbon.identity.mgt.User;
import org.wso2.carbon.identity.mgt.bean.GroupBean;
import org.wso2.carbon.identity.mgt.bean.UserBean;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.claim.MetaClaim;
import org.wso2.carbon.identity.mgt.event.IdentityMgtMessageContext;
import org.wso2.carbon.identity.mgt.exception.AuthenticationFailure;
import org.wso2.carbon.identity.mgt.exception.GroupNotFoundException;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.mgt.impl.config.StoreConfig;
import org.wso2.carbon.identity.mgt.impl.internal.IdentityMgtDataHolder;
import org.wso2.carbon.identity.mgt.interceptor.IdentityStoreInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.security.auth.callback.Callback;

import static org.wso2.carbon.identity.mgt.constant.StoreConstants.IdentityStoreConstants;
import static org.wso2.carbon.identity.mgt.constant.StoreConstants.IdentityStoreInterceptorConstants;


/**
 * Interceptor for IdentityStore.
 * @since 1.0.0
 */
public class InterceptingIdentityStore implements IdentityStore {


    private IdentityStore identityStore;
    private List<IdentityStoreInterceptor> identityStoreInterceptors;
    private EventService eventService = IdentityMgtDataHolder.getInstance().getEventService();
    private static final Logger log = LoggerFactory.getLogger(InterceptingIdentityStore.class);


    public InterceptingIdentityStore(StoreConfig storeConfig, List<Domain> domains) throws IdentityStoreException {

        this.identityStoreInterceptors = IdentityMgtDataHolder.getInstance().getIdentityStoreInterceptors();
        if (storeConfig.isEnableCache() && storeConfig.isEnableIdentityStoreCache()) {
            identityStore = new CacheBackedIdentityStore(storeConfig.getIdentityStoreCacheConfigMap(), domains);
        } else {
            identityStore = new IdentityStoreImpl(domains);
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

        //Pre handler
        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_GET_USER_BY_ID, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_GET_USER_BY_ID);
            throw new IdentityStoreException(message, e);
        }

        User user = identityStore.getUser(uniqueUserId);

        //Post handler
        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
        eventProperties.put(IdentityStoreConstants.USER, user);

        event = new Event(IdentityStoreInterceptorConstants.POST_GET_USER_BY_ID, eventProperties);
        messageContext.setEvent(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_GET_USER_BY_ID);
            throw new IdentityStoreException(message, e);
        }

        return user;
    }

    @Override
    public User getUser(Claim claim) throws IdentityStoreException, UserNotFoundException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.CLAIM, claim);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_GET_USER_BY_CLAIM, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_GET_USER_BY_CLAIM);
            throw new IdentityStoreException(message, e);
        }

        User user = identityStore.getUser(claim);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.CLAIM, claim);
        eventProperties.put(IdentityStoreConstants.USER, user);

        event = new Event(IdentityStoreInterceptorConstants.POST_GET_USER_BY_CLAIM, eventProperties);
        messageContext.setEvent(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_GET_USER_BY_CLAIM);
            throw new IdentityStoreException(message, e);
        }
        return user;
    }

    @Override
    public User getUser(Claim claim, String domainName) throws IdentityStoreException, UserNotFoundException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.CLAIM, claim);
        eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, claim);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_GET_USER_BY_CLAIM_DOMAIN, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_GET_USER_BY_CLAIM_DOMAIN);
            throw new IdentityStoreException(message, e);
        }

        User user = identityStore.getUser(claim, domainName);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.CLAIM, claim);
        eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, claim);
        eventProperties.put(IdentityStoreConstants.USER, user);

        event = new Event(IdentityStoreInterceptorConstants.POST_GET_USER_BY_CLAIM_DOMAIN, eventProperties);
        messageContext.setEvent(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_GET_USER_BY_CLAIM_DOMAIN);
            throw new IdentityStoreException(message, e);
        }

        return user;
    }

    @Override
    public List<User> listUsers(int offset, int length) throws IdentityStoreException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.OFFSET, offset);
        eventProperties.put(IdentityStoreConstants.LENGTH, length);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_LIST_USERS, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_LIST_USERS);
            throw new IdentityStoreException(message, e);
        }

        List<User> users = identityStore.listUsers(offset, length);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.OFFSET, offset);
        eventProperties.put(IdentityStoreConstants.LENGTH, length);
        eventProperties.put(IdentityStoreConstants.USER_LIST, users);

        event = new Event(IdentityStoreInterceptorConstants.POST_LIST_USERS, eventProperties);
        messageContext.setEvent(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_LIST_USERS);
            throw new IdentityStoreException(message, e);
        }

        return users;
    }

    @Override
    public List<User> listUsers(int offset, int length, String domainName) throws IdentityStoreException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.OFFSET, offset);
        eventProperties.put(IdentityStoreConstants.LENGTH, length);
        eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_LIST_USERS_BY_DOMAIN, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_LIST_USERS_BY_DOMAIN);
            throw new IdentityStoreException(message, e);
        }

        List<User> users = identityStore.listUsers(offset, length, domainName);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.OFFSET, offset);
        eventProperties.put(IdentityStoreConstants.LENGTH, length);
        eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);
        eventProperties.put(IdentityStoreConstants.USER_LIST, users);

        event = new Event(IdentityStoreInterceptorConstants.POST_LIST_USERS_BY_DOMAIN, eventProperties);
        messageContext.setEvent(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_LIST_USERS_BY_DOMAIN);
            throw new IdentityStoreException(message, e);
        }

        return users;
    }

    @Override
    public List<User> listUsers(Claim claim, int offset, int length) throws IdentityStoreException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.CLAIM, claim);
        eventProperties.put(IdentityStoreConstants.OFFSET, offset);
        eventProperties.put(IdentityStoreConstants.LENGTH, length);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_LIST_USERS_BY_CLAIM, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_LIST_USERS_BY_CLAIM);
            throw new IdentityStoreException(message, e);
        }

        List<User> users = identityStore.listUsers(claim, offset, length);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.CLAIM, claim);
        eventProperties.put(IdentityStoreConstants.OFFSET, offset);
        eventProperties.put(IdentityStoreConstants.LENGTH, length);
        eventProperties.put(IdentityStoreConstants.USER_LIST, users);

        event = new Event(IdentityStoreInterceptorConstants.POST_LIST_USERS_BY_CLAIM, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_LIST_USERS_BY_CLAIM);
            throw new IdentityStoreException(message, e);
        }

        return users;
    }

    @Override
    public List<User> listUsers(Claim claim, int offset, int length, String domainName) throws IdentityStoreException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.CLAIM, claim);
        eventProperties.put(IdentityStoreConstants.OFFSET, offset);
        eventProperties.put(IdentityStoreConstants.LENGTH, length);
        eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_LIST_USERS_BY_CLAIM_DOMAIN, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_LIST_USERS_BY_CLAIM_DOMAIN);
            throw new IdentityStoreException(message, e);
        }

        List<User> users = identityStore.listUsers(claim, offset, length, domainName);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.CLAIM, claim);
        eventProperties.put(IdentityStoreConstants.OFFSET, offset);
        eventProperties.put(IdentityStoreConstants.LENGTH, length);
        eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);
        eventProperties.put(IdentityStoreConstants.USER_LIST, domainName);

        event = new Event(IdentityStoreInterceptorConstants.POST_LIST_USERS_BY_CLAIM_DOMAIN, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_LIST_USERS_BY_CLAIM_DOMAIN);
            throw new IdentityStoreException(message, e);
        }

        return users;
    }

    @Override
    public List<User> listUsers(MetaClaim metaClaim, String filterPattern, int offset, int length)
            throws IdentityStoreException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.META_CLAIM, metaClaim);
        eventProperties.put(IdentityStoreConstants.FILTER_PATTERN, filterPattern);
        eventProperties.put(IdentityStoreConstants.OFFSET, offset);
        eventProperties.put(IdentityStoreConstants.LENGTH, length);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_LIST_USERS_BY_META_CLAIM, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_LIST_USERS_BY_META_CLAIM);
            throw new IdentityStoreException(message, e);
        }

        List<User> users = identityStore.listUsers(metaClaim, filterPattern, offset, length);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.META_CLAIM, metaClaim);
        eventProperties.put(IdentityStoreConstants.FILTER_PATTERN, filterPattern);
        eventProperties.put(IdentityStoreConstants.OFFSET, offset);
        eventProperties.put(IdentityStoreConstants.LENGTH, length);
        eventProperties.put(IdentityStoreConstants.USER_LIST, users);

        event = new Event(IdentityStoreInterceptorConstants.POST_LIST_USERS_BY_META_CLAIM, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_LIST_USERS_BY_META_CLAIM);
            throw new IdentityStoreException(message, e);
        }
        return users;
    }

    @Override
    public List<User> listUsers(MetaClaim metaClaim, String filterPattern, int offset, int length, String domainName)
            throws IdentityStoreException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.META_CLAIM, metaClaim);
        eventProperties.put(IdentityStoreConstants.FILTER_PATTERN, filterPattern);
        eventProperties.put(IdentityStoreConstants.OFFSET, offset);
        eventProperties.put(IdentityStoreConstants.LENGTH, length);
        eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_LIST_USERS_BY_META_CLAIM_DOMAIN, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_LIST_USERS_BY_META_CLAIM_DOMAIN);
            throw new IdentityStoreException(message, e);
        }

        List<User> users = identityStore.listUsers(metaClaim, filterPattern, offset, length, domainName);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.META_CLAIM, metaClaim);
        eventProperties.put(IdentityStoreConstants.FILTER_PATTERN, filterPattern);
        eventProperties.put(IdentityStoreConstants.OFFSET, offset);
        eventProperties.put(IdentityStoreConstants.LENGTH, length);
        eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);
        eventProperties.put(IdentityStoreConstants.USER_LIST, users);

        event = new Event(IdentityStoreInterceptorConstants.POST_LIST_USERS_BY_META_CLAIM_DOMAIN, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_LIST_USERS_BY_META_CLAIM_DOMAIN);
            throw new IdentityStoreException(message, e);
        }
        return users;
    }

    @Override
    public boolean isGroupExist(List<Claim> userClaims, String domainName) throws IdentityStoreException {
        return identityStore.isGroupExist(userClaims, domainName);
    }

    @Override
    public Group getGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID, uniqueGroupId);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_GET_GROUP_BY_ID, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_GET_GROUP_BY_ID);
            throw new IdentityStoreException(message, e);
        }

        Group group = identityStore.getGroup(uniqueGroupId);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID, uniqueGroupId);
        eventProperties.put(IdentityStoreConstants.GROUP, group);

        event = new Event(IdentityStoreInterceptorConstants.POST_GET_GROUP_BY_ID, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_GET_GROUP_BY_ID);
            throw new IdentityStoreException(message, e);
        }
        return group;
    }

    @Override
    public Group getGroup(Claim claim) throws IdentityStoreException, GroupNotFoundException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.CLAIM, claim);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_GET_GROUP_BY_CLAIM, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_GET_GROUP_BY_CLAIM);
            throw new IdentityStoreException(message, e);
        }

        Group group = identityStore.getGroup(claim);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.CLAIM, claim);
        eventProperties.put(IdentityStoreConstants.GROUP, group);

        event = new Event(IdentityStoreInterceptorConstants.POST_GET_GROUP_BY_CLAIM, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_GET_GROUP_BY_CLAIM);
            throw new IdentityStoreException(message, e);
        }
        return group;
    }

    @Override
    public Group getGroup(Claim claim, String domainName) throws IdentityStoreException, GroupNotFoundException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.CLAIM, claim);
        eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_GET_GROUP_BY_CLAIM_DOMAIN, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_GET_GROUP_BY_CLAIM_DOMAIN);
            throw new IdentityStoreException(message, e);
        }

        Group group = identityStore.getGroup(claim, domainName);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.CLAIM, claim);
        eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);
        eventProperties.put(IdentityStoreConstants.GROUP, group);

        event = new Event(IdentityStoreInterceptorConstants.POST_GET_GROUP_BY_CLAIM_DOMAIN, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_GET_GROUP_BY_CLAIM_DOMAIN);
            throw new IdentityStoreException(message, e);
        }
        return group;
    }

    @Override
    public List<Group> listGroups(int offset, int length) throws IdentityStoreException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.OFFSET, offset);
        eventProperties.put(IdentityStoreConstants.LENGTH, length);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_LIST_GROUPS, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_LIST_GROUPS);
            throw new IdentityStoreException(message, e);
        }

        List<Group> groups = identityStore.listGroups(offset, length);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.OFFSET, offset);
        eventProperties.put(IdentityStoreConstants.LENGTH, length);
        eventProperties.put(IdentityStoreConstants.GROUP_LIST, groups);

        event = new Event(IdentityStoreInterceptorConstants.POST_LIST_GROUPS, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_LIST_GROUPS);
            throw new IdentityStoreException(message, e);
        }
        return groups;
    }

    @Override
    public List<Group> listGroups(int offset, int length, String domainName) throws IdentityStoreException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.OFFSET, offset);
        eventProperties.put(IdentityStoreConstants.LENGTH, length);
        eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_LIST_GROUPS_BY_DOMAIN, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_LIST_GROUPS_BY_DOMAIN);
            throw new IdentityStoreException(message, e);
        }

        List<Group> groups = identityStore.listGroups(offset, length, domainName);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.OFFSET, offset);
        eventProperties.put(IdentityStoreConstants.LENGTH, length);
        eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);
        eventProperties.put(IdentityStoreConstants.GROUP_LIST, groups);

        event = new Event(IdentityStoreInterceptorConstants.POST_LIST_GROUPS_BY_DOMAIN, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_LIST_GROUPS_BY_DOMAIN);
            throw new IdentityStoreException(message, e);
        }
        return groups;
    }

    @Override
    public List<Group> listGroups(Claim claim, int offset, int length) throws IdentityStoreException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.CLAIM, claim);
        eventProperties.put(IdentityStoreConstants.OFFSET, offset);
        eventProperties.put(IdentityStoreConstants.LENGTH, length);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_LIST_GROUPS_BY_CLAIM, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_LIST_GROUPS_BY_DOMAIN);
            throw new IdentityStoreException(message, e);
        }

        List<Group> groups = identityStore.listGroups(claim, offset, length);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.CLAIM, claim);
        eventProperties.put(IdentityStoreConstants.OFFSET, offset);
        eventProperties.put(IdentityStoreConstants.LENGTH, length);
        eventProperties.put(IdentityStoreConstants.GROUP_LIST, groups);

        event = new Event(IdentityStoreInterceptorConstants.POST_LIST_GROUPS_BY_CLAIM, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_LIST_GROUPS_BY_CLAIM);
            throw new IdentityStoreException(message, e);
        }

        return groups;
    }

    @Override
    public List<Group> listGroups(Claim claim, int offset, int length, String domainName)
            throws IdentityStoreException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.CLAIM, claim);
        eventProperties.put(IdentityStoreConstants.OFFSET, offset);
        eventProperties.put(IdentityStoreConstants.LENGTH, length);
        eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_LIST_GROUPS_BY_CLAIM_DOMAIN, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_LIST_GROUPS_BY_CLAIM_DOMAIN);
            throw new IdentityStoreException(message, e);
        }

        List<Group> groups = identityStore.listGroups(claim, offset, length, domainName);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.CLAIM, claim);
        eventProperties.put(IdentityStoreConstants.OFFSET, offset);
        eventProperties.put(IdentityStoreConstants.LENGTH, length);
        eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);

        event = new Event(IdentityStoreInterceptorConstants.POST_LIST_GROUPS_BY_CLAIM_DOMAIN, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_LIST_GROUPS_BY_CLAIM_DOMAIN);
            throw new IdentityStoreException(message, e);
        }
        return groups;
    }

    @Override
    public List<Group> listGroups(MetaClaim metaClaim, String filterPattern, int offset, int length)
            throws IdentityStoreException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.META_CLAIM, metaClaim);
        eventProperties.put(IdentityStoreConstants.FILTER_PATTERN, filterPattern);
        eventProperties.put(IdentityStoreConstants.OFFSET, offset);
        eventProperties.put(IdentityStoreConstants.LENGTH, length);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_LIST_GROUPS_BY_META_CLAIM, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_LIST_GROUPS_BY_META_CLAIM);
            throw new IdentityStoreException(message, e);
        }

        List<Group> groups = identityStore.listGroups(metaClaim, filterPattern, offset, length);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.META_CLAIM, metaClaim);
        eventProperties.put(IdentityStoreConstants.FILTER_PATTERN, filterPattern);
        eventProperties.put(IdentityStoreConstants.OFFSET, offset);
        eventProperties.put(IdentityStoreConstants.LENGTH, length);
        eventProperties.put(IdentityStoreConstants.GROUP_LIST, groups);

        event = new Event(IdentityStoreInterceptorConstants.POST_LIST_GROUPS_BY_META_CLAIM, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_LIST_GROUPS_BY_META_CLAIM);
            throw new IdentityStoreException(message, e);
        }

        return groups;
    }

    @Override
    public List<Group> listGroups(MetaClaim metaClaim, String filterPattern, int offset, int length, String domainName)
            throws IdentityStoreException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.META_CLAIM, metaClaim);
        eventProperties.put(IdentityStoreConstants.FILTER_PATTERN, filterPattern);
        eventProperties.put(IdentityStoreConstants.OFFSET, offset);
        eventProperties.put(IdentityStoreConstants.LENGTH, length);
        eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_LIST_GROUPS_BY_META_CLAIM_DOMAIN,
                                eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_LIST_GROUPS_BY_META_CLAIM_DOMAIN);
            throw new IdentityStoreException(message, e);
        }

        List<Group> groups = identityStore.listGroups(metaClaim, filterPattern, offset, length, domainName);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.META_CLAIM, metaClaim);
        eventProperties.put(IdentityStoreConstants.FILTER_PATTERN, filterPattern);
        eventProperties.put(IdentityStoreConstants.OFFSET, offset);
        eventProperties.put(IdentityStoreConstants.LENGTH, length);
        eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);
        eventProperties.put(IdentityStoreConstants.GROUP_LIST, groups);

        event = new Event(IdentityStoreInterceptorConstants.POST_LIST_GROUPS_BY_META_CLAIM_DOMAIN,
                                eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_LIST_GROUPS_BY_META_CLAIM_DOMAIN);
            throw new IdentityStoreException(message, e);
        }
        return groups;
    }

    @Override
    public List<Group> getGroupsOfUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_GET_GROUPS_OF_USER, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_GET_GROUPS_OF_USER);
            throw new IdentityStoreException(message, e);
        }

        List<Group> groups = identityStore.getGroupsOfUser(uniqueUserId);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
        eventProperties.put(IdentityStoreConstants.GROUP_LIST, groups);

        event = new Event(IdentityStoreInterceptorConstants.POST_GET_GROUPS_OF_USER, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_GET_GROUPS_OF_USER);
            throw new IdentityStoreException(message, e);
        }
        return groups;
    }

    @Override
    public List<User> getUsersOfGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID, uniqueGroupId);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_GET_USERS_OF_GROUP, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_GET_GROUPS_OF_USER);
            throw new IdentityStoreException(message, e);
        }

        List<User> users = identityStore.getUsersOfGroup(uniqueGroupId);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID, uniqueGroupId);
        eventProperties.put(IdentityStoreConstants.USER_LIST, users);

        event = new Event(IdentityStoreInterceptorConstants.POST_GET_USERS_OF_GROUP, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_GET_USERS_OF_GROUP);
            throw new IdentityStoreException(message, e);
        }
        return users;
    }

    @Override
    public boolean isUserInGroup(String uniqueUserId, String uniqueGroupId)
            throws IdentityStoreException, UserNotFoundException, GroupNotFoundException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
        eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID, uniqueGroupId);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_IS_USER_IN_GROUP, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_IS_USER_IN_GROUP);
            throw new IdentityStoreException(message, e);
        }

        Boolean isUserInGroup = identityStore.isUserInGroup(uniqueUserId, uniqueGroupId);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
        eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID, uniqueGroupId);
        eventProperties.put(IdentityStoreConstants.IS_USER_IN_GROUP, isUserInGroup);

        event = new Event(IdentityStoreInterceptorConstants.POST_IS_USER_IN_GROUP, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_IS_USER_IN_GROUP);
            throw new IdentityStoreException(message, e);
        }

        return isUserInGroup;
    }

    @Override
    public List<Claim> getClaimsOfUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_GET_CLAIMS_OF_USER_BY_ID, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_GET_CLAIMS_OF_USER_BY_ID);
            throw new IdentityStoreException(message, e);
        }

        List<Claim> claims = identityStore.getClaimsOfUser(uniqueUserId);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
        eventProperties.put(IdentityStoreConstants.CLAIM_LIST, claims);

        event = new Event(IdentityStoreInterceptorConstants.POST_GET_CLAIMS_OF_USER_BY_ID, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_GET_CLAIMS_OF_USER_BY_ID);
            throw new IdentityStoreException(message, e);
        }
        return claims;
    }

    @Override
    public List<Claim> getClaimsOfUser(String uniqueUserId, List<MetaClaim> metaClaims)
            throws IdentityStoreException, UserNotFoundException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
        eventProperties.put(IdentityStoreConstants.META_CLAIM_LIST, metaClaims);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_GET_CLAIMS_OF_USER_BY_ID_META_CLAIMS,
                                eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_GET_CLAIMS_OF_USER_BY_ID_META_CLAIMS);
            throw new IdentityStoreException(message, e);
        }

        List<Claim> claims = identityStore.getClaimsOfUser(uniqueUserId, metaClaims);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
        eventProperties.put(IdentityStoreConstants.META_CLAIM_LIST, metaClaims);
        eventProperties.put(IdentityStoreConstants.CLAIM_LIST, claims);

        event = new Event(IdentityStoreInterceptorConstants.POST_GET_CLAIMS_OF_USER_BY_ID_META_CLAIMS,
                                eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_GET_CLAIMS_OF_USER_BY_ID_META_CLAIMS);
            throw new IdentityStoreException(message, e);
        }
        return claims;
    }

    @Override
    public List<Claim> getClaimsOfGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID, uniqueGroupId);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_GET_CLAIMS_OF_GROUP_BY_ID, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_GET_CLAIMS_OF_GROUP_BY_ID);
            throw new IdentityStoreException(message, e);
        }

        List<Claim> claims = identityStore.getClaimsOfGroup(uniqueGroupId);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID, uniqueGroupId);
        eventProperties.put(IdentityStoreConstants.CLAIM_LIST, claims);

        event = new Event(IdentityStoreInterceptorConstants.POST_GET_CLAIMS_OF_GROUP_BY_ID, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_GET_CLAIMS_OF_GROUP_BY_ID);
            throw new IdentityStoreException(message, e);
        }

        return claims;
    }

    @Override
    public List<Claim> getClaimsOfGroup(String uniqueGroupId, List<MetaClaim> metaClaims)
            throws IdentityStoreException, GroupNotFoundException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID, uniqueGroupId);
        eventProperties.put(IdentityStoreConstants.META_CLAIM_LIST, metaClaims);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_GET_CLAIMS_OF_GROUP_BY_ID_META_CLAIMS,
                                eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_GET_CLAIMS_OF_GROUP_BY_ID_META_CLAIMS);
            throw new IdentityStoreException(message, e);
        }

        List<Claim> claims = identityStore.getClaimsOfGroup(uniqueGroupId, metaClaims);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID, uniqueGroupId);
        eventProperties.put(IdentityStoreConstants.META_CLAIM_LIST, metaClaims);
        eventProperties.put(IdentityStoreConstants.CLAIM_LIST, claims);

        event = new Event(IdentityStoreInterceptorConstants.POST_GET_CLAIMS_OF_GROUP_BY_ID_META_CLAIMS,
                                eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_GET_CLAIMS_OF_GROUP_BY_ID_META_CLAIMS);
            throw new IdentityStoreException(message, e);
        }

        return claims;
    }

    @Override
    public User addUser(UserBean userBean) throws IdentityStoreException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.USER_BEAN, userBean);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_ADD_USER, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_ADD_USER);
            throw new IdentityStoreException(message, e);
        }

        User user = identityStore.addUser(userBean);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.USER_BEAN, userBean);
        eventProperties.put(IdentityStoreConstants.USER, user);

        event = new Event(IdentityStoreInterceptorConstants.POST_ADD_USER, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_ADD_USER);
            throw new IdentityStoreException(message, e);
        }

        return user;
    }

    @Override
    public User addUser(UserBean userBean, String domainName) throws IdentityStoreException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.USER_BEAN, userBean);
        eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_ADD_USER_BY_DOMAIN, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_ADD_USER_BY_DOMAIN);
            throw new IdentityStoreException(message, e);
        }

        User user = identityStore.addUser(userBean, domainName);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.USER_BEAN, userBean);
        eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);
        eventProperties.put(IdentityStoreConstants.USER, user);

        event = new Event(IdentityStoreInterceptorConstants.POST_ADD_USER_BY_DOMAIN, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_ADD_USER_BY_DOMAIN);
            throw new IdentityStoreException(message, e);
        }
        return user;
    }

    @Override
    public List<User> addUsers(List<UserBean> userBeans) throws IdentityStoreException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.USER_BEAN_LIST, userBeans);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_ADD_USERS_BY_DOMAIN, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_ADD_USERS_BY_DOMAIN);
            throw new IdentityStoreException(message, e);
        }

        List<User> users = identityStore.addUsers(userBeans);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.USER_BEAN_LIST, userBeans);

        event = new Event(IdentityStoreInterceptorConstants.POST_ADD_USERS_BY_DOMAIN, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_ADD_USERS_BY_DOMAIN);
            throw new IdentityStoreException(message, e);
        }

        return users;
    }

    @Override
    public List<User> addUsers(List<UserBean> userBeans, String domainName) throws IdentityStoreException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.USER_BEAN_LIST, userBeans);
        eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_ADD_USERS_BY_DOMAIN, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_ADD_USERS_BY_DOMAIN);
            throw new IdentityStoreException(message, e);
        }

        List<User> users = identityStore.addUsers(userBeans);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.USER_BEAN_LIST, userBeans);
        eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);
        eventProperties.put(IdentityStoreConstants.USER_LIST, users);

        event = new Event(IdentityStoreInterceptorConstants.POST_ADD_USERS_BY_DOMAIN, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_ADD_USERS_BY_DOMAIN);
            throw new IdentityStoreException(message, e);
        }

        return users;
    }

    @Override
    public void updateUserClaims(String uniqueUserId, List<Claim> claims)
            throws IdentityStoreException, UserNotFoundException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
        eventProperties.put(IdentityStoreConstants.CLAIM_LIST, claims);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_UPDATE_USER_CLAIMS_PUT, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_UPDATE_USER_CLAIMS_PUT);
            throw new IdentityStoreException(message, e);
        }

        identityStore.updateUserClaims(uniqueUserId, claims);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
        eventProperties.put(IdentityStoreConstants.CLAIM_LIST, claims);

        event = new Event(IdentityStoreInterceptorConstants.POST_UPDATE_USER_CLAIMS_PUT, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_UPDATE_USER_CLAIMS_PUT);
            throw new IdentityStoreException(message, e);
        }
    }

    @Override
    public void updateUserClaims(String uniqueUserId, List<Claim> claimsToAdd, List<Claim> claimsToRemove)
            throws IdentityStoreException, UserNotFoundException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
        eventProperties.put(IdentityStoreConstants.CLAIM_LIST_TO_ADD, claimsToAdd);
        eventProperties.put(IdentityStoreConstants.CLAIM_LIST_TO_REMOVE, claimsToRemove);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_UPDATE_USER_CLAIMS_PATCH, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_UPDATE_USER_CLAIMS_PATCH);
            throw new IdentityStoreException(message, e);
        }

        identityStore.updateUserClaims(uniqueUserId, claimsToAdd, claimsToRemove);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
        eventProperties.put(IdentityStoreConstants.CLAIM_LIST_TO_ADD, claimsToAdd);
        eventProperties.put(IdentityStoreConstants.CLAIM_LIST_TO_REMOVE, claimsToRemove);

        event = new Event(IdentityStoreInterceptorConstants.POST_UPDATE_USER_CLAIMS_PATCH, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_UPDATE_USER_CLAIMS_PATCH);
            throw new IdentityStoreException(message, e);
        }

    }

    @Override
    public void updateUserCredentials(String uniqueUserId, List<Callback> credentials)
            throws IdentityStoreException, UserNotFoundException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
        eventProperties.put(IdentityStoreConstants.CREDENTIAL_LIST, credentials);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_UPDATE_USER_CREDENTIALS_PUT, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_UPDATE_USER_CREDENTIALS_PUT);
            throw new IdentityStoreException(message, e);
        }

        identityStore.updateUserCredentials(uniqueUserId, credentials);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
        eventProperties.put(IdentityStoreConstants.CREDENTIAL_LIST, credentials);

        event = new Event(IdentityStoreInterceptorConstants.POST_UPDATE_USER_CREDENTIALS_PUT, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_UPDATE_USER_CREDENTIALS_PUT);
            throw new IdentityStoreException(message, e);
        }
    }

    @Override
    public void updateUserCredentials(String uniqueUserId, List<Callback> credentialsToAdd,
                                      List<Callback> credentialsToRemove)
            throws IdentityStoreException, UserNotFoundException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
        eventProperties.put(IdentityStoreConstants.CREDENTIAL_LIST_TO_ADD, credentialsToAdd);
        eventProperties.put(IdentityStoreConstants.CREDENTIAL_LIST_TO_REMOVE, credentialsToRemove);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_UPDATE_USER_CREDENTIALS_PATCH, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_UPDATE_USER_CREDENTIALS_PATCH);
            throw new IdentityStoreException(message, e);
        }

        identityStore.updateUserCredentials(uniqueUserId, credentialsToAdd, credentialsToRemove);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
        eventProperties.put(IdentityStoreConstants.CREDENTIAL_LIST_TO_ADD, credentialsToAdd);
        eventProperties.put(IdentityStoreConstants.CREDENTIAL_LIST_TO_REMOVE, credentialsToRemove);

        event = new Event(IdentityStoreInterceptorConstants.POST_UPDATE_USER_CREDENTIALS_PATCH, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_UPDATE_USER_CREDENTIALS_PATCH);
            throw new IdentityStoreException(message, e);
        }
    }

    @Override
    public void deleteUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_DELETE_USER, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_DELETE_USER);
            throw new IdentityStoreException(message, e);
        }

        identityStore.deleteUser(uniqueUserId);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);

        event = new Event(IdentityStoreInterceptorConstants.POST_DELETE_USER, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_DELETE_USER);
            throw new IdentityStoreException(message, e);
        }

    }

    @Override
    public void updateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIds) throws IdentityStoreException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
        eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID_LIST, uniqueUserId);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_UPDATE_GROUPS_OF_USER_PUT, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_UPDATE_GROUPS_OF_USER_PUT);
            throw new IdentityStoreException(message, e);
        }

        identityStore.updateGroupsOfUser(uniqueUserId, uniqueGroupIds);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
        eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID_LIST, uniqueUserId);

        event = new Event(IdentityStoreInterceptorConstants.POST_UPDATE_GROUPS_OF_USER_PUT, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_UPDATE_GROUPS_OF_USER_PUT);
            throw new IdentityStoreException(message, e);
        }
    }

    @Override
    public void updateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIdsToAdd,
                                   List<String> uniqueGroupIdsToRemove) throws IdentityStoreException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
        eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID_LIST_TO_ADD, uniqueUserId);
        eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID_LIST_TO_REMOVE, uniqueUserId);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_UPDATE_GROUPS_OF_USER_PATCH, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_UPDATE_GROUPS_OF_USER_PUT);
            throw new IdentityStoreException(message, e);
        }

        identityStore.updateGroupsOfUser(uniqueUserId, uniqueGroupIdsToAdd, uniqueGroupIdsToRemove);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
        eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID_LIST_TO_ADD, uniqueUserId);
        eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID_LIST_TO_REMOVE, uniqueUserId);

        event = new Event(IdentityStoreInterceptorConstants.POST_UPDATE_GROUPS_OF_USER_PATCH, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_UPDATE_GROUPS_OF_USER_PUT);
            throw new IdentityStoreException(message, e);
        }

    }

    @Override
    public Group addGroup(GroupBean groupBean) throws IdentityStoreException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.GROUP_BEAN, groupBean);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_ADD_GROUP, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_ADD_GROUP);
            throw new IdentityStoreException(message, e);
        }

        Group group = identityStore.addGroup(groupBean);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.GROUP_BEAN, groupBean);
        eventProperties.put(IdentityStoreConstants.GROUP, group);

        event = new Event(IdentityStoreInterceptorConstants.POST_ADD_GROUP, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_ADD_GROUP);
            throw new IdentityStoreException(message, e);
        }

        return group;
    }

    @Override
    public Group addGroup(GroupBean groupBean, String domainName) throws IdentityStoreException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.GROUP_BEAN, groupBean);
        eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_ADD_GROUP_BY_DOMAIN, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_ADD_GROUP);
            throw new IdentityStoreException(message, e);
        }

        Group group = identityStore.addGroup(groupBean, domainName);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.GROUP_BEAN, groupBean);
        eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);
        eventProperties.put(IdentityStoreConstants.GROUP, group);

        event = new Event(IdentityStoreInterceptorConstants.POST_ADD_GROUP_BY_DOMAIN, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_ADD_GROUP_BY_DOMAIN);
            throw new IdentityStoreException(message, e);
        }

        return group;
    }

    @Override
    public List<Group> addGroups(List<GroupBean> groupBeans) throws IdentityStoreException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.GROUP_BEAN_LIST, groupBeans);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_ADD_GROUPS, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_ADD_GROUPS);
            throw new IdentityStoreException(message, e);
        }

        List<Group> groups = identityStore.addGroups(groupBeans);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.GROUP_BEAN_LIST, groupBeans);
        eventProperties.put(IdentityStoreConstants.GROUP_LIST, groups);

        event = new Event(IdentityStoreInterceptorConstants.POST_ADD_GROUPS, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_ADD_GROUPS);
            throw new IdentityStoreException(message, e);
        }

        return groups;
    }

    @Override
    public List<Group> addGroups(List<GroupBean> groupBeans, String domainName) throws IdentityStoreException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.GROUP_BEAN_LIST, groupBeans);
        eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_ADD_GROUPS_BY_DOMAIN, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_ADD_GROUPS_BY_DOMAIN);
            throw new IdentityStoreException(message, e);
        }

        List<Group> groups = identityStore.addGroups(groupBeans, domainName);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.GROUP_BEAN_LIST, groupBeans);
        eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);
        eventProperties.put(IdentityStoreConstants.GROUP_LIST, groups);

        event = new Event(IdentityStoreInterceptorConstants.POST_ADD_GROUPS_BY_DOMAIN, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_ADD_GROUPS_BY_DOMAIN);
            throw new IdentityStoreException(message, e);
        }

        return groups;
    }

    @Override
    public void updateGroupClaims(String uniqueGroupId, List<Claim> claims)
            throws IdentityStoreException, GroupNotFoundException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID, uniqueGroupId);
        eventProperties.put(IdentityStoreConstants.CLAIM_LIST, claims);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_UPDATE_GROUP_CLAIMS_PUT, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_UPDATE_GROUP_CLAIMS_PUT);
            throw new IdentityStoreException(message, e);
        }

        identityStore.updateGroupClaims(uniqueGroupId, claims);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID, uniqueGroupId);
        eventProperties.put(IdentityStoreConstants.CLAIM_LIST, claims);

        event = new Event(IdentityStoreInterceptorConstants.POST_UPDATE_GROUP_CLAIMS_PUT, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_UPDATE_GROUP_CLAIMS_PUT);
            throw new IdentityStoreException(message, e);
        }
    }

    @Override
    public void updateGroupClaims(String uniqueGroupId, List<Claim> claimsToAdd, List<Claim> claimsToRemove)
            throws IdentityStoreException, GroupNotFoundException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID, uniqueGroupId);
        eventProperties.put(IdentityStoreConstants.CLAIM_LIST_TO_ADD, claimsToAdd);
        eventProperties.put(IdentityStoreConstants.CLAIM_LIST_TO_REMOVE, claimsToRemove);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_UPDATE_GROUP_CLAIMS_PATCH, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_UPDATE_GROUP_CLAIMS_PATCH);
            throw new IdentityStoreException(message, e);
        }

        identityStore.updateGroupClaims(uniqueGroupId, claimsToAdd, claimsToRemove);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID, uniqueGroupId);
        eventProperties.put(IdentityStoreConstants.CLAIM_LIST_TO_ADD, claimsToAdd);
        eventProperties.put(IdentityStoreConstants.CLAIM_LIST_TO_REMOVE, claimsToRemove);

        event = new Event(IdentityStoreInterceptorConstants.POST_UPDATE_GROUP_CLAIMS_PATCH, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_UPDATE_GROUP_CLAIMS_PATCH);
            throw new IdentityStoreException(message, e);
        }

    }

    @Override
    public void deleteGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID, uniqueGroupId);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_DELETE_GROUP, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_DELETE_GROUP);
            throw new IdentityStoreException(message, e);
        }

        identityStore.deleteGroup(uniqueGroupId);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID, uniqueGroupId);

        event = new Event(IdentityStoreInterceptorConstants.POST_DELETE_GROUP, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_DELETE_GROUP);
            throw new IdentityStoreException(message, e);
        }
    }

    @Override
    public void updateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIds) throws IdentityStoreException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID, uniqueGroupId);
        eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID_LIST, uniqueUserIds);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_UPDATE_USERS_OF_GROUP_PUT, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_DELETE_GROUP);
            throw new IdentityStoreException(message, e);
        }

        identityStore.updateUsersOfGroup(uniqueGroupId, uniqueUserIds);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID, uniqueGroupId);
        eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID_LIST, uniqueUserIds);

        event = new Event(IdentityStoreInterceptorConstants.POST_UPDATE_USERS_OF_GROUP_PUT, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_UPDATE_USERS_OF_GROUP_PUT);
            throw new IdentityStoreException(message, e);
        }
    }

    @Override
    public void updateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIdsToAdd,
                                   List<String> uniqueUserIdsToRemove) throws IdentityStoreException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID, uniqueGroupId);
        eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID_LIST_TO_ADD, uniqueUserIdsToAdd);
        eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID_LIST_TO_REMOVE, uniqueUserIdsToRemove);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_UPDATE_USERS_OF_GROUP_PATCH, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_UPDATE_USERS_OF_GROUP_PATCH);
            throw new IdentityStoreException(message, e);
        }

        identityStore.updateUsersOfGroup(uniqueGroupId, uniqueUserIdsToAdd, uniqueUserIdsToRemove);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID, uniqueGroupId);
        eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID_LIST_TO_ADD, uniqueUserIdsToAdd);
        eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID_LIST_TO_REMOVE, uniqueUserIdsToRemove);

        event = new Event(IdentityStoreInterceptorConstants.POST_UPDATE_USERS_OF_GROUP_PATCH, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_UPDATE_USERS_OF_GROUP_PATCH);
            throw new IdentityStoreException(message, e);
        }
    }

    @Override
    public AuthenticationContext authenticate(Claim claim, Callback[] credentials, String domainName)
            throws AuthenticationFailure, IdentityStoreException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.CLAIM, claim);
        eventProperties.put(IdentityStoreConstants.CREDENTIAL_LIST, credentials);
        eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_AUTHENTICATE, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_AUTHENTICATE);
            throw new IdentityStoreException(message, e);
        }

        AuthenticationContext authenticationContext = identityStore.authenticate(claim, credentials, domainName);

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.CLAIM, claim);
        eventProperties.put(IdentityStoreConstants.CREDENTIAL_LIST, credentials);
        eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);
        eventProperties.put(IdentityStoreConstants.AUTHENTICATION_CONTEXT, authenticationContext);

        event = new Event(IdentityStoreInterceptorConstants.POST_AUTHENTICATE, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_AUTHENTICATE);
            throw new IdentityStoreException(message, e);
        }

        return authenticationContext;
    }

    @Override
    public String getPrimaryDomainName() throws IdentityStoreException {

        Map<String, Object> eventProperties = new HashMap<>();

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_GET_PRIMARY_DOMAIN_NAME, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_GET_PRIMARY_DOMAIN_NAME);
            throw new IdentityStoreException(message, e);
        }

        String primaryDomainName = identityStore.getPrimaryDomainName();

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.PRIMARY_DOMAIN_NAME, primaryDomainName);

        event = new Event(IdentityStoreInterceptorConstants.POST_GET_PRIMARY_DOMAIN_NAME, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_GET_PRIMARY_DOMAIN_NAME);
            throw new IdentityStoreException(message, e);
        }

        return primaryDomainName;
    }

    @Override
    public Set<String> getDomainNames() throws IdentityStoreException {

        Map<String, Object> eventProperties = new HashMap<>();

        Event event = new Event(IdentityStoreInterceptorConstants.PRE_GET_DOMAIN_NAMES, eventProperties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .PRE_GET_DOMAIN_NAMES);
            throw new IdentityStoreException(message, e);
        }

        Set<String> domainNames = identityStore.getDomainNames();

        eventProperties = new HashMap<>();
        eventProperties.put(IdentityStoreConstants.DOMAIN_LIST, domainNames);

        event = new Event(IdentityStoreInterceptorConstants.POST_GET_DOMAIN_NAMES, eventProperties);
        messageContext = new IdentityMgtMessageContext(event);

        try {
            eventService.handleEvent(messageContext);
        } catch (EventException e) {

            String message = String.format("Error while handling %s event.", IdentityStoreInterceptorConstants
                    .POST_GET_DOMAIN_NAMES);
            throw new IdentityStoreException(message, e);
        }

        return domainNames;
    }
}

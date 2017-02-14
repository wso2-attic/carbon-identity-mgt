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
import org.wso2.carbon.identity.event.EventService;
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
import org.wso2.carbon.identity.mgt.impl.util.builder.event.EventHandlerDelegate;
import org.wso2.carbon.identity.mgt.impl.util.builder.event.EventInterceptorTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.security.auth.callback.Callback;

import static org.wso2.carbon.identity.mgt.constant.StoreConstants.IdentityStoreConstants;
import static org.wso2.carbon.identity.mgt.constant.StoreConstants.IdentityStoreInterceptorConstants;


/**
 * Interceptor for IdentityStore.
 *
 * @since 1.0.0
 */
public class InterceptingIdentityStore implements IdentityStore {


    private IdentityStore identityStore;
    private EventService eventService = IdentityMgtDataHolder.getInstance().getEventService();
    private static final Logger log = LoggerFactory.getLogger(InterceptingIdentityStore.class);


    public InterceptingIdentityStore(StoreConfig storeConfig, List<Domain> domains) throws IdentityStoreException {

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

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<User, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);
        User user = template.pushEvent(IdentityStoreInterceptorConstants.PRE_GET_USER_BY_ID, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
        }).executeWith(new EventHandlerDelegate<User>() {

            @Override
            public User execute() throws IdentityStoreException, UserNotFoundException {
                return identityStore.getUser(uniqueUserId);
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_GET_USER_BY_ID, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
            eventProperties.put(IdentityStoreConstants.USER, template.getResult());
        }).getResult();

        return user;
    }

    @Override
    public User getUser(Claim claim) throws IdentityStoreException, UserNotFoundException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<User, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        User user = template.pushEvent(IdentityStoreInterceptorConstants.PRE_GET_USER_BY_CLAIM, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.CLAIM, claim);
        }).executeWith(new EventHandlerDelegate<User>() {
            @Override
            public User execute() throws IdentityStoreException, UserNotFoundException {
                return identityStore.getUser(claim);
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_GET_USER_BY_CLAIM, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.CLAIM, claim);
            eventProperties.put(IdentityStoreConstants.USER, template.getResult());
        }).getResult();

        return user;
    }

    @Override
    public User getUser(Claim claim, String domainName) throws IdentityStoreException, UserNotFoundException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<User, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        User user = template.pushEvent(IdentityStoreInterceptorConstants.PRE_GET_USER_BY_CLAIM_DOMAIN,
                                       (eventProperties) -> {
                                           eventProperties.put(IdentityStoreConstants.CLAIM, claim);
                                           eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, claim);
                                       })
                            .executeWith(new EventHandlerDelegate<User>() {
                                @Override
                                public User execute() throws IdentityStoreException, UserNotFoundException {
                                    return identityStore.getUser(claim, domainName);
                                }
                            }).pushEvent(IdentityStoreInterceptorConstants.POST_GET_USER_BY_CLAIM_DOMAIN,
                                         (eventProperties) -> {
                                             eventProperties.put(IdentityStoreConstants.CLAIM, claim);
                                             eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, claim);
                                             eventProperties.put(IdentityStoreConstants.USER, template.getResult());
                                         }).getResult();

        return user;
    }

    @Override
    public List<User> listUsers(int offset, int length) throws IdentityStoreException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<List<User>, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        List<User> users = template.pushEvent(IdentityStoreInterceptorConstants.PRE_LIST_USERS, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.OFFSET, offset);
            eventProperties.put(IdentityStoreConstants.LENGTH, length);
        }).executeWith(new EventHandlerDelegate<List<User>>() {
            @Override
            public List<User> execute() throws IdentityStoreException {
                return identityStore.listUsers(offset, length);
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_LIST_USERS, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.OFFSET, offset);
            eventProperties.put(IdentityStoreConstants.LENGTH, length);
            eventProperties.put(IdentityStoreConstants.USER_LIST, template.getResult());
        }).getResult();

        return users;
    }

    @Override
    public List<User> listUsers(int offset, int length, String domainName) throws IdentityStoreException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<List<User>, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        List<User> users = template.pushEvent(IdentityStoreInterceptorConstants.PRE_LIST_USERS_BY_DOMAIN,
                                              (eventProperties) -> {
                                                  eventProperties.put(IdentityStoreConstants.OFFSET, offset);
                                                  eventProperties.put(IdentityStoreConstants.LENGTH, length);
                                                  eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);
                                              }).executeWith(new EventHandlerDelegate<List<User>>() {
            @Override
            public List<User> execute() throws IdentityStoreException {
                return identityStore.listUsers(offset, length, domainName);
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_LIST_USERS_BY_DOMAIN, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.OFFSET, offset);
            eventProperties.put(IdentityStoreConstants.LENGTH, length);
            eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);
            eventProperties.put(IdentityStoreConstants.USER_LIST, template.getResult());
        }).getResult();

        return users;
    }

    @Override
    public List<User> listUsers(Claim claim, int offset, int length) throws IdentityStoreException {


        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<List<User>, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        List<User> users = template.pushEvent(IdentityStoreInterceptorConstants.PRE_LIST_USERS_BY_CLAIM,
                                              (eventProperties) -> {
                                                  eventProperties.put(IdentityStoreConstants.CLAIM, claim);
                                                  eventProperties.put(IdentityStoreConstants.OFFSET, offset);
                                                  eventProperties.put(IdentityStoreConstants.LENGTH, length);
                                              }).executeWith(new EventHandlerDelegate<List<User>>() {
            @Override
            public List<User> execute() throws IdentityStoreException {
                return identityStore.listUsers(claim, offset, length);
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_LIST_USERS_BY_CLAIM, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.CLAIM, claim);
            eventProperties.put(IdentityStoreConstants.OFFSET, offset);
            eventProperties.put(IdentityStoreConstants.LENGTH, length);
            eventProperties.put(IdentityStoreConstants.USER_LIST, template.getResult());
        }).getResult();

        return users;
    }

    @Override
    public List<User> listUsers(Claim claim, int offset, int length, String domainName) throws IdentityStoreException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<List<User>, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        List<User> users = template.pushEvent(IdentityStoreInterceptorConstants.PRE_LIST_USERS_BY_CLAIM_DOMAIN,
                                              (eventProperties) -> {
                                                  eventProperties.put(IdentityStoreConstants.CLAIM, claim);
                                                  eventProperties.put(IdentityStoreConstants.OFFSET, offset);
                                                  eventProperties.put(IdentityStoreConstants.LENGTH, length);
                                                  eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);
                                              }).executeWith(new EventHandlerDelegate<List<User>>() {
            @Override
            public List<User> execute() throws IdentityStoreException {
                return identityStore.listUsers(claim, offset, length, domainName);
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_LIST_USERS_BY_CLAIM_DOMAIN, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.CLAIM, claim);
            eventProperties.put(IdentityStoreConstants.OFFSET, offset);
            eventProperties.put(IdentityStoreConstants.LENGTH, length);
            eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);
            eventProperties.put(IdentityStoreConstants.USER_LIST, template.getResult());
        }).getResult();

        return users;
    }

    @Override
    public List<User> listUsers(MetaClaim metaClaim, String filterPattern, int offset, int length)
            throws IdentityStoreException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<List<User>, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        List<User> users = template.pushEvent(IdentityStoreInterceptorConstants.PRE_LIST_USERS_BY_META_CLAIM,
                                              (eventProperties) -> {
                                                  eventProperties.put(IdentityStoreConstants.META_CLAIM, metaClaim);
                                                  eventProperties.put(IdentityStoreConstants.FILTER_PATTERN,
                                                                      filterPattern);
                                                  eventProperties.put(IdentityStoreConstants.OFFSET, offset);
                                                  eventProperties.put(IdentityStoreConstants.LENGTH, length);
                                              }).executeWith(new EventHandlerDelegate<List<User>>() {
            @Override
            public List<User> execute() throws IdentityStoreException {
                return identityStore.listUsers(metaClaim, filterPattern, offset, length);
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_LIST_USERS_BY_META_CLAIM, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.META_CLAIM, metaClaim);
            eventProperties.put(IdentityStoreConstants.FILTER_PATTERN, filterPattern);
            eventProperties.put(IdentityStoreConstants.OFFSET, offset);
            eventProperties.put(IdentityStoreConstants.LENGTH, length);
            eventProperties.put(IdentityStoreConstants.USER_LIST, template.getResult());
        }).getResult();

        return users;
    }

    @Override
    public List<User> listUsers(MetaClaim metaClaim, String filterPattern, int offset, int length, String domainName)
            throws IdentityStoreException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<List<User>, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        List<User> users = template.pushEvent(IdentityStoreInterceptorConstants.PRE_LIST_USERS_BY_META_CLAIM_DOMAIN,
                                              (eventProperties) -> {
                                                  eventProperties.put(IdentityStoreConstants.META_CLAIM, metaClaim);
                                                  eventProperties.put(IdentityStoreConstants.FILTER_PATTERN,
                                                                      filterPattern);
                                                  eventProperties.put(IdentityStoreConstants.OFFSET, offset);
                                                  eventProperties.put(IdentityStoreConstants.LENGTH, length);
                                                  eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);
                                              }).executeWith(new EventHandlerDelegate<List<User>>() {
            @Override
            public List<User> execute() throws IdentityStoreException {
                return identityStore.listUsers(metaClaim, filterPattern, offset, length, domainName);
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_LIST_USERS_BY_META_CLAIM_DOMAIN, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.META_CLAIM, metaClaim);
            eventProperties.put(IdentityStoreConstants.FILTER_PATTERN, filterPattern);
            eventProperties.put(IdentityStoreConstants.OFFSET, offset);
            eventProperties.put(IdentityStoreConstants.LENGTH, length);
            eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);
            eventProperties.put(IdentityStoreConstants.USER_LIST, template.getResult());
        }).getResult();

        return users;
    }

    @Override
    public Group getGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<Group, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        Group group = template.pushEvent(IdentityStoreInterceptorConstants.PRE_GET_GROUP_BY_ID,
                                         (eventProperties) -> {
                                             eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID,
                                                                 uniqueGroupId);
                                         }).executeWith(new EventHandlerDelegate<Group>() {
            @Override
            public Group execute() throws IdentityStoreException, GroupNotFoundException {
                return identityStore.getGroup(uniqueGroupId);
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_GET_GROUP_BY_ID, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID, uniqueGroupId);
            eventProperties.put(IdentityStoreConstants.GROUP, template.getResult());
        }).getResult();

        return group;
    }

    @Override
    public Group getGroup(Claim claim) throws IdentityStoreException, GroupNotFoundException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<Group, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        Group group = template.pushEvent(IdentityStoreInterceptorConstants.PRE_GET_GROUP_BY_CLAIM,
                                         (eventProperties) -> {
                                             eventProperties.put(IdentityStoreConstants.CLAIM, claim);
                                         }).executeWith(new EventHandlerDelegate<Group>() {
            @Override
            public Group execute() throws IdentityStoreException, GroupNotFoundException {
                return identityStore.getGroup(claim);
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_GET_GROUP_BY_CLAIM, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.CLAIM, claim);
            eventProperties.put(IdentityStoreConstants.GROUP, template.getResult());
        }).getResult();

        return group;
    }

    @Override
    public Group getGroup(Claim claim, String domainName) throws IdentityStoreException, GroupNotFoundException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<Group, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        Group group = template.pushEvent(IdentityStoreInterceptorConstants.PRE_GET_GROUP_BY_CLAIM_DOMAIN,
                                         (eventProperties) -> {
                                             eventProperties.put(IdentityStoreConstants.CLAIM, claim);
                                             eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);
                                         }).executeWith(new EventHandlerDelegate<Group>() {
            @Override
            public Group execute() throws IdentityStoreException, GroupNotFoundException {
                return identityStore.getGroup(claim, domainName);
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_GET_GROUP_BY_CLAIM_DOMAIN, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.CLAIM, claim);
            eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);
            eventProperties.put(IdentityStoreConstants.GROUP, template.getResult());
        }).getResult();

        return group;
    }

    @Override
    public List<Group> listGroups(int offset, int length) throws IdentityStoreException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<List<Group>, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        List<Group> groups = template.pushEvent(IdentityStoreInterceptorConstants.PRE_LIST_GROUPS,
                                                (eventProperties) -> {
                                                    eventProperties.put(IdentityStoreConstants.OFFSET, offset);
                                                    eventProperties.put(IdentityStoreConstants.LENGTH, length);
                                                }).executeWith(new EventHandlerDelegate<List<Group>>() {
            @Override
            public List<Group> execute() throws IdentityStoreException {
                return identityStore.listGroups(offset, length);
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_LIST_GROUPS, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.OFFSET, offset);
            eventProperties.put(IdentityStoreConstants.LENGTH, length);
            eventProperties.put(IdentityStoreConstants.GROUP_LIST, template.getResult());
        }).getResult();

        return groups;
    }

    @Override
    public List<Group> listGroups(int offset, int length, String domainName) throws IdentityStoreException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<List<Group>, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        List<Group> groups = template.pushEvent(IdentityStoreInterceptorConstants.PRE_LIST_GROUPS_BY_DOMAIN,
                                                (eventProperties) -> {
                                                    eventProperties.put(IdentityStoreConstants.OFFSET, offset);
                                                    eventProperties.put(IdentityStoreConstants.LENGTH, length);
                                                    eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);
                                                }).executeWith(new EventHandlerDelegate<List<Group>>() {
            @Override
            public List<Group> execute() throws IdentityStoreException {
                return identityStore.listGroups(offset, length, domainName);
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_LIST_GROUPS_BY_DOMAIN, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.OFFSET, offset);
            eventProperties.put(IdentityStoreConstants.LENGTH, length);
            eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);
            eventProperties.put(IdentityStoreConstants.GROUP_LIST, template.getResult());
        }).getResult();

        return groups;
    }

    @Override
    public List<Group> listGroups(Claim claim, int offset, int length) throws IdentityStoreException {


        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<List<Group>, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        List<Group> groups = template.pushEvent(IdentityStoreInterceptorConstants.PRE_LIST_GROUPS_BY_CLAIM,
                                                (eventProperties) -> {
                                                    eventProperties.put(IdentityStoreConstants.CLAIM, claim);
                                                    eventProperties.put(IdentityStoreConstants.OFFSET, offset);
                                                    eventProperties.put(IdentityStoreConstants.LENGTH, length);
                                                }).executeWith(new EventHandlerDelegate<List<Group>>() {
            @Override
            public List<Group> execute() throws IdentityStoreException {
                return identityStore.listGroups(claim, offset, length);
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_LIST_GROUPS_BY_CLAIM, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.CLAIM, claim);
            eventProperties.put(IdentityStoreConstants.OFFSET, offset);
            eventProperties.put(IdentityStoreConstants.LENGTH, length);
            eventProperties.put(IdentityStoreConstants.GROUP_LIST, template.getResult());
        }).getResult();

        return groups;
    }

    @Override
    public List<Group> listGroups(Claim claim, int offset, int length, String domainName)
            throws IdentityStoreException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<List<Group>, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        List<Group> groups = template.pushEvent(IdentityStoreInterceptorConstants.PRE_LIST_GROUPS_BY_CLAIM_DOMAIN,
                                                (eventProperties) -> {
                                                    eventProperties.put(IdentityStoreConstants.CLAIM, claim);
                                                    eventProperties.put(IdentityStoreConstants.OFFSET, offset);
                                                    eventProperties.put(IdentityStoreConstants.LENGTH, length);
                                                    eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);
                                                }).executeWith(new EventHandlerDelegate<List<Group>>() {
            @Override
            public List<Group> execute() throws IdentityStoreException {
                return identityStore.listGroups(claim, offset, length, domainName);
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_LIST_GROUPS_BY_CLAIM_DOMAIN, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.CLAIM, claim);
            eventProperties.put(IdentityStoreConstants.OFFSET, offset);
            eventProperties.put(IdentityStoreConstants.LENGTH, length);
            eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);
            eventProperties.put(IdentityStoreConstants.GROUP_LIST, template.getResult());
        }).getResult();

        return groups;
    }

    @Override
    public List<Group> listGroups(MetaClaim metaClaim, String filterPattern, int offset, int length)
            throws IdentityStoreException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<List<Group>, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        List<Group> groups = template.pushEvent(IdentityStoreInterceptorConstants.PRE_LIST_GROUPS_BY_META_CLAIM,
                                                (eventProperties) -> {
                                                    eventProperties.put(IdentityStoreConstants.META_CLAIM, metaClaim);
                                                    eventProperties.put(IdentityStoreConstants.FILTER_PATTERN,
                                                                        filterPattern);
                                                    eventProperties.put(IdentityStoreConstants.OFFSET, offset);
                                                    eventProperties.put(IdentityStoreConstants.LENGTH, length);
                                                }).executeWith(new EventHandlerDelegate<List<Group>>() {
            @Override
            public List<Group> execute() throws IdentityStoreException {
                return identityStore.listGroups(metaClaim, filterPattern, offset, length);
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_LIST_GROUPS_BY_META_CLAIM, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.META_CLAIM, metaClaim);
            eventProperties.put(IdentityStoreConstants.FILTER_PATTERN, filterPattern);
            eventProperties.put(IdentityStoreConstants.OFFSET, offset);
            eventProperties.put(IdentityStoreConstants.LENGTH, length);
            eventProperties.put(IdentityStoreConstants.GROUP_LIST, template.getResult());
        }).getResult();

        return groups;
    }

    @Override
    public List<Group> listGroups(MetaClaim metaClaim, String filterPattern, int offset, int length, String domainName)
            throws IdentityStoreException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<List<Group>, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        List<Group> groups = template.pushEvent(IdentityStoreInterceptorConstants.PRE_LIST_GROUPS_BY_META_CLAIM_DOMAIN,
                                                (eventProperties) -> {
                                                    eventProperties.put(IdentityStoreConstants.META_CLAIM, metaClaim);
                                                    eventProperties.put(IdentityStoreConstants.FILTER_PATTERN,
                                                                        filterPattern);
                                                    eventProperties.put(IdentityStoreConstants.OFFSET, offset);
                                                    eventProperties.put(IdentityStoreConstants.LENGTH, length);
                                                    eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);
                                                }).executeWith(new EventHandlerDelegate<List<Group>>() {
            @Override
            public List<Group> execute() throws IdentityStoreException {
                return identityStore.listGroups(metaClaim, filterPattern, offset, length, domainName);
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_LIST_GROUPS_BY_META_CLAIM_DOMAIN, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.META_CLAIM, metaClaim);
            eventProperties.put(IdentityStoreConstants.FILTER_PATTERN, filterPattern);
            eventProperties.put(IdentityStoreConstants.OFFSET, offset);
            eventProperties.put(IdentityStoreConstants.LENGTH, length);
            eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);
            eventProperties.put(IdentityStoreConstants.GROUP_LIST, template.getResult());
        }).getResult();

        return groups;
    }

    @Override
    public List<Group> getGroupsOfUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<List<Group>, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        List<Group> groups = template.pushEvent(IdentityStoreInterceptorConstants.PRE_GET_GROUPS_OF_USER,
                                                (eventProperties) -> {
                                                    eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID,
                                                                        uniqueUserId);
                                                }).executeWith(new EventHandlerDelegate<List<Group>>() {
            @Override
            public List<Group> execute() throws IdentityStoreException, UserNotFoundException {
                return identityStore.getGroupsOfUser(uniqueUserId);
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_GET_GROUPS_OF_USER, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
            eventProperties.put(IdentityStoreConstants.GROUP_LIST, template.getResult());
        }).getResult();

        return groups;
    }

    @Override
    public List<User> getUsersOfGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<List<User>, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        List<User> users = template.pushEvent(IdentityStoreInterceptorConstants.PRE_GET_USERS_OF_GROUP,
                                              (eventProperties) -> {
                                                  eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID,
                                                                      uniqueGroupId);
                                              }).executeWith(new EventHandlerDelegate<List<User>>() {
            @Override
            public List<User> execute() throws IdentityStoreException, GroupNotFoundException {
                return identityStore.getUsersOfGroup(uniqueGroupId);
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_GET_GROUPS_OF_USER, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID, uniqueGroupId);
            eventProperties.put(IdentityStoreConstants.GROUP_LIST, template.getResult());
        }).getResult();

        return users;
    }

    @Override
    public boolean isUserInGroup(String uniqueUserId, String uniqueGroupId)
            throws IdentityStoreException, UserNotFoundException, GroupNotFoundException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<Boolean, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        Boolean isUserInGroup = template.pushEvent(IdentityStoreInterceptorConstants.PRE_IS_USER_IN_GROUP,
                                                   (eventProperties) -> {
                                                       eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID,
                                                                           uniqueUserId);
                                                       eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID,
                                                                           uniqueGroupId);
                                                   }).executeWith(new EventHandlerDelegate<Boolean>() {
            @Override
            public Boolean execute() throws IdentityStoreException, UserNotFoundException, GroupNotFoundException {
                return identityStore.isUserInGroup(uniqueUserId, uniqueGroupId);
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_IS_USER_IN_GROUP, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
            eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID, uniqueGroupId);
            eventProperties.put(IdentityStoreConstants.IS_USER_IN_GROUP, template.getResult());
        }).getResult();

        return isUserInGroup;
    }

    @Override
    public List<Claim> getClaimsOfUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<List<Claim>, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        List<Claim> claims = template.pushEvent(IdentityStoreInterceptorConstants.PRE_GET_CLAIMS_OF_USER_BY_ID,
                                                (eventProperties) -> {
                                                    eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID,
                                                                        uniqueUserId);
                                                }).executeWith(new EventHandlerDelegate<List<Claim>>() {
            @Override
            public List<Claim> execute() throws IdentityStoreException, UserNotFoundException {
                return identityStore.getClaimsOfUser(uniqueUserId);
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_GET_CLAIMS_OF_USER_BY_ID, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
            eventProperties.put(IdentityStoreConstants.CLAIM_LIST, template.getResult());
        }).getResult();

        return claims;
    }

    @Override
    public List<Claim> getClaimsOfUser(String uniqueUserId, List<MetaClaim> metaClaims)
            throws IdentityStoreException, UserNotFoundException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<List<Claim>, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        List<Claim> claims = template.pushEvent(IdentityStoreInterceptorConstants
                                                        .PRE_GET_CLAIMS_OF_USER_BY_ID_META_CLAIMS,
                                                (eventProperties) -> {
                                                    eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID,
                                                                        uniqueUserId);
                                                    eventProperties.put(IdentityStoreConstants.META_CLAIM_LIST,
                                                                        metaClaims);
                                                }).executeWith(new EventHandlerDelegate<List<Claim>>() {
            @Override
            public List<Claim> execute() throws IdentityStoreException, UserNotFoundException {
                return identityStore.getClaimsOfUser(uniqueUserId, metaClaims);
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_GET_CLAIMS_OF_USER_BY_ID_META_CLAIMS, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
            eventProperties.put(IdentityStoreConstants.META_CLAIM_LIST, metaClaims);
            eventProperties.put(IdentityStoreConstants.CLAIM_LIST, template.getResult());
        }).getResult();

        return claims;
    }

    @Override
    public List<Claim> getClaimsOfGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<List<Claim>, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        List<Claim> claims = template.pushEvent(IdentityStoreInterceptorConstants.PRE_GET_CLAIMS_OF_GROUP_BY_ID,
                                                (eventProperties) -> {
                                                    eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID,
                                                                        uniqueGroupId);
                                                }).executeWith(new EventHandlerDelegate<List<Claim>>() {
            @Override
            public List<Claim> execute() throws IdentityStoreException, GroupNotFoundException {
                return identityStore.getClaimsOfGroup(uniqueGroupId);
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_GET_CLAIMS_OF_GROUP_BY_ID, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID, uniqueGroupId);
            eventProperties.put(IdentityStoreConstants.CLAIM_LIST, template.getResult());
        }).getResult();

        return claims;
    }

    @Override
    public List<Claim> getClaimsOfGroup(String uniqueGroupId, List<MetaClaim> metaClaims)
            throws IdentityStoreException, GroupNotFoundException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<List<Claim>, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        List<Claim> claims = template.pushEvent(IdentityStoreInterceptorConstants
                                                        .PRE_GET_CLAIMS_OF_GROUP_BY_ID_META_CLAIMS, (eventProperties)
                                                        -> {
            eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID, uniqueGroupId);
            eventProperties.put(IdentityStoreConstants.META_CLAIM_LIST, metaClaims);
        }).executeWith(new EventHandlerDelegate<List<Claim>>() {
            @Override
            public List<Claim> execute() throws IdentityStoreException, GroupNotFoundException {
                return identityStore.getClaimsOfGroup(uniqueGroupId, metaClaims);
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_GET_CLAIMS_OF_GROUP_BY_ID_META_CLAIMS, (eventProperties)
                -> {
            eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID, uniqueGroupId);
            eventProperties.put(IdentityStoreConstants.META_CLAIM_LIST, metaClaims);
            eventProperties.put(IdentityStoreConstants.CLAIM_LIST, template.getResult());
        }).getResult();

        return claims;
    }

    @Override
    public User addUser(UserBean userBean) throws IdentityStoreException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<User, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        User user = template.pushEvent(IdentityStoreInterceptorConstants.PRE_ADD_USER, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.USER_BEAN, userBean);
        }).executeWith(new EventHandlerDelegate<User>() {
            @Override
            public User execute() throws IdentityStoreException {
                return identityStore.addUser(userBean);
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_ADD_USER, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.USER_BEAN, userBean);
            eventProperties.put(IdentityStoreConstants.USER, template.getResult());
        }).getResult();

        return user;
    }

    @Override
    public User addUser(UserBean userBean, String domainName) throws IdentityStoreException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<User, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        User user = template.pushEvent(IdentityStoreInterceptorConstants.PRE_ADD_USER_BY_DOMAIN, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.USER_BEAN, userBean);
            eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);
        }).executeWith(new EventHandlerDelegate<User>() {
            @Override
            public User execute() throws IdentityStoreException {
                return identityStore.addUser(userBean, domainName);
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_ADD_USER, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.USER_BEAN, userBean);
            eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);
            eventProperties.put(IdentityStoreConstants.USER, template.getResult());
        }).getResult();

        return user;
    }

    @Override
    public List<User> addUsers(List<UserBean> userBeans) throws IdentityStoreException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<List<User>, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        List<User> users = template.pushEvent(IdentityStoreInterceptorConstants.PRE_ADD_USERS, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.USER_BEAN_LIST, userBeans);
        }).executeWith(new EventHandlerDelegate<List<User>>() {
            @Override
            public List<User> execute() throws IdentityStoreException {
                return identityStore.addUsers(userBeans);
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_ADD_USERS, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.USER_BEAN_LIST, userBeans);
            eventProperties.put(IdentityStoreConstants.USER_LIST, template.getResult());
        }).getResult();

        return users;
    }

    @Override
    public List<User> addUsers(List<UserBean> userBeans, String domainName) throws IdentityStoreException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<List<User>, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        List<User> users = template.pushEvent(IdentityStoreInterceptorConstants.PRE_ADD_USERS_BY_DOMAIN,
                                              (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.USER_BEAN_LIST, userBeans);
            eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);
        }).executeWith(new EventHandlerDelegate<List<User>>() {
            @Override
            public List<User> execute() throws IdentityStoreException {
                return identityStore.addUsers(userBeans, domainName);
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_ADD_USERS_BY_DOMAIN, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.USER_BEAN_LIST, userBeans);
            eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);
            eventProperties.put(IdentityStoreConstants.USER_LIST, template.getResult());
        }).getResult();

        return users;
    }

    @Override
    public void updateUserClaims(String uniqueUserId, List<Claim> claims)
            throws IdentityStoreException, UserNotFoundException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<Void, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        template.pushEvent(IdentityStoreInterceptorConstants.PRE_UPDATE_USER_CLAIMS_PUT, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
            eventProperties.put(IdentityStoreConstants.CLAIM_LIST, claims);
        }).executeWith(new EventHandlerDelegate<Void>() {
            @Override
            public Void execute() throws IdentityStoreException, UserNotFoundException {
                identityStore.updateUserClaims(uniqueUserId, claims);
                return null;
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_UPDATE_USER_CLAIMS_PUT, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
            eventProperties.put(IdentityStoreConstants.CLAIM_LIST, claims);
        });

    }

    @Override
    public void updateUserClaims(String uniqueUserId, List<Claim> claimsToAdd, List<Claim> claimsToRemove)
            throws IdentityStoreException, UserNotFoundException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<Void, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);
        template.pushEvent(IdentityStoreInterceptorConstants.PRE_UPDATE_USER_CLAIMS_PATCH, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
            eventProperties.put(IdentityStoreConstants.CLAIM_LIST_TO_ADD, claimsToAdd);
            eventProperties.put(IdentityStoreConstants.CLAIM_LIST_TO_REMOVE, claimsToRemove);
        }).executeWith(new EventHandlerDelegate<Void>() {

            @Override
            public Void execute() throws IdentityStoreException, UserNotFoundException {
                identityStore.updateUserClaims(uniqueUserId, claimsToAdd, claimsToRemove);
                return null;
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_UPDATE_USER_CLAIMS_PATCH, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
            eventProperties.put(IdentityStoreConstants.CLAIM_LIST_TO_ADD, claimsToAdd);
            eventProperties.put(IdentityStoreConstants.CLAIM_LIST_TO_REMOVE, claimsToRemove);
        });
    }

    @Override
    public void updateUserCredentials(String uniqueUserId, List<Callback> credentials)
            throws IdentityStoreException, UserNotFoundException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<Void, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        template.pushEvent(IdentityStoreInterceptorConstants.PRE_UPDATE_USER_CREDENTIALS_PUT, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
            eventProperties.put(IdentityStoreConstants.CREDENTIAL_LIST, credentials);
        }).executeWith(new EventHandlerDelegate<Void>() {
            @Override
            public Void execute() throws IdentityStoreException, UserNotFoundException {
                identityStore.updateUserCredentials(uniqueUserId, credentials);
                return null;
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_UPDATE_USER_CREDENTIALS_PUT, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
            eventProperties.put(IdentityStoreConstants.CREDENTIAL_LIST, credentials);
        });

    }

    @Override
    public void updateUserCredentials(String uniqueUserId, List<Callback> credentialsToAdd,
                                      List<Callback> credentialsToRemove)
            throws IdentityStoreException, UserNotFoundException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<Void, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        template.pushEvent(IdentityStoreInterceptorConstants.PRE_UPDATE_USER_CREDENTIALS_PATCH, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
            eventProperties.put(IdentityStoreConstants.CREDENTIAL_LIST_TO_ADD, credentialsToAdd);
            eventProperties.put(IdentityStoreConstants.CREDENTIAL_LIST_TO_REMOVE, credentialsToRemove);
        }).executeWith(new EventHandlerDelegate<Void>() {
            @Override
            public Void execute() throws IdentityStoreException, UserNotFoundException {
                identityStore.updateUserCredentials(uniqueUserId, credentialsToAdd, credentialsToRemove);
                return null;
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_UPDATE_USER_CREDENTIALS_PATCH, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
            eventProperties.put(IdentityStoreConstants.CREDENTIAL_LIST_TO_ADD, credentialsToAdd);
            eventProperties.put(IdentityStoreConstants.CREDENTIAL_LIST_TO_REMOVE, credentialsToRemove);
        });

    }

    @Override
    public void deleteUser(String uniqueUserId) throws IdentityStoreException, UserNotFoundException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<Void, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        template.pushEvent(IdentityStoreInterceptorConstants.PRE_DELETE_USER, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
        }).executeWith(new EventHandlerDelegate<Void>() {
            @Override
            public Void execute() throws IdentityStoreException, UserNotFoundException {
                identityStore.deleteUser(uniqueUserId);
                return null;
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_DELETE_USER, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
        });

    }

    @Override
    public void updateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIds) throws IdentityStoreException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<Void, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        template.pushEvent(IdentityStoreInterceptorConstants.PRE_UPDATE_GROUPS_OF_USER_PUT, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
            eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID_LIST, uniqueGroupIds);
        }).executeWith(new EventHandlerDelegate<Void>() {
            @Override
            public Void execute() throws IdentityStoreException, UserNotFoundException {
                identityStore.updateGroupsOfUser(uniqueUserId, uniqueGroupIds);
                return null;
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_UPDATE_GROUPS_OF_USER_PUT, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
            eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID_LIST, uniqueGroupIds);
        });

    }

    @Override
    public void updateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIdsToAdd,
                                   List<String> uniqueGroupIdsToRemove) throws IdentityStoreException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<Void, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        template.pushEvent(IdentityStoreInterceptorConstants.PRE_UPDATE_GROUPS_OF_USER_PATCH, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
            eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID_LIST_TO_ADD, uniqueUserId);
            eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID_LIST_TO_REMOVE, uniqueUserId);
        }).executeWith(new EventHandlerDelegate<Void>() {
            @Override
            public Void execute() throws IdentityStoreException, UserNotFoundException {
                identityStore.updateGroupsOfUser(uniqueUserId, uniqueGroupIdsToAdd, uniqueGroupIdsToRemove);
                return null;
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_UPDATE_GROUPS_OF_USER_PATCH, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID, uniqueUserId);
            eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID_LIST_TO_ADD, uniqueUserId);
            eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID_LIST_TO_REMOVE, uniqueUserId);
        });

    }

    @Override
    public Group addGroup(GroupBean groupBean) throws IdentityStoreException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<Group, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        Group group = template.pushEvent(IdentityStoreInterceptorConstants.PRE_ADD_GROUP, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.GROUP_BEAN, groupBean);
        }).executeWith(new EventHandlerDelegate<Group>() {
            @Override
            public Group execute() throws IdentityStoreException {
                return identityStore.addGroup(groupBean);
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_ADD_GROUP, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.GROUP_BEAN, groupBean);
            eventProperties.put(IdentityStoreConstants.GROUP, template.getResult());
        }).getResult();

        return group;
    }

    @Override
    public Group addGroup(GroupBean groupBean, String domainName) throws IdentityStoreException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<Group, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        Group group = template.pushEvent(IdentityStoreInterceptorConstants.PRE_ADD_GROUP_BY_DOMAIN, (eventProperties)
                -> {
            eventProperties.put(IdentityStoreConstants.GROUP_BEAN, groupBean);
            eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);
        }).executeWith(new EventHandlerDelegate<Group>() {
            @Override
            public Group execute() throws IdentityStoreException {
                return identityStore.addGroup(groupBean);
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_ADD_GROUP_BY_DOMAIN, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.GROUP_BEAN, groupBean);
            eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);
            eventProperties.put(IdentityStoreConstants.GROUP, template.getResult());
        }).getResult();

        return group;
    }

    @Override
    public List<Group> addGroups(List<GroupBean> groupBeans) throws IdentityStoreException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<List<Group>, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        List<Group> groups = template.pushEvent(IdentityStoreInterceptorConstants.PRE_ADD_GROUPS, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.GROUP_BEAN_LIST, groupBeans);
        }).executeWith(new EventHandlerDelegate<List<Group>>() {
            @Override
            public List<Group> execute() throws IdentityStoreException {
                return identityStore.addGroups(groupBeans);
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_ADD_GROUP, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.GROUP_BEAN_LIST, groupBeans);
            eventProperties.put(IdentityStoreConstants.GROUP_LIST, template.getResult());
        }).getResult();

        return groups;
    }

    @Override
    public List<Group> addGroups(List<GroupBean> groupBeans, String domainName) throws IdentityStoreException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<List<Group>, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        List<Group> groups = template.pushEvent(IdentityStoreInterceptorConstants.PRE_ADD_GROUPS_BY_DOMAIN,
                                                (eventProperties) -> {
                                                    eventProperties.put(IdentityStoreConstants.GROUP_BEAN_LIST,
                                                                        groupBeans);
                                                }).executeWith(new EventHandlerDelegate<List<Group>>() {
            @Override
            public List<Group> execute() throws IdentityStoreException {
                return identityStore.addGroups(groupBeans);
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_ADD_GROUPS_BY_DOMAIN, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.GROUP_BEAN_LIST, groupBeans);
            eventProperties.put(IdentityStoreConstants.GROUP_LIST, template.getResult());
        }).getResult();

        return groups;
    }

    @Override
    public void updateGroupClaims(String uniqueGroupId, List<Claim> claims)
            throws IdentityStoreException, GroupNotFoundException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<Void, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        template.pushEvent(IdentityStoreInterceptorConstants.PRE_UPDATE_GROUP_CLAIMS_PUT,
                           (eventProperties) -> {
                               eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID, uniqueGroupId);
                               eventProperties.put(IdentityStoreConstants.CLAIM_LIST, claims);
                           }).executeWith(new EventHandlerDelegate<Void>() {
            @Override
            public Void execute() throws IdentityStoreException, GroupNotFoundException {
                identityStore.updateGroupClaims(uniqueGroupId, claims);
                return null;
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_UPDATE_GROUP_CLAIMS_PUT, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID, uniqueGroupId);
            eventProperties.put(IdentityStoreConstants.CLAIM_LIST, claims);
        });

    }

    @Override
    public void updateGroupClaims(String uniqueGroupId, List<Claim> claimsToAdd, List<Claim> claimsToRemove)
            throws IdentityStoreException, GroupNotFoundException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<Void, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        template.pushEvent(IdentityStoreInterceptorConstants.PRE_UPDATE_GROUP_CLAIMS_PATCH,
                           (eventProperties) -> {
                               eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID, uniqueGroupId);
                               eventProperties.put(IdentityStoreConstants.CLAIM_LIST_TO_ADD, claimsToAdd);
                               eventProperties.put(IdentityStoreConstants.CLAIM_LIST_TO_REMOVE, claimsToRemove);
                           }).executeWith(new EventHandlerDelegate<Void>() {
            @Override
            public Void execute() throws IdentityStoreException, GroupNotFoundException {
                identityStore.updateGroupClaims(uniqueGroupId, claimsToAdd, claimsToRemove);
                return null;
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_UPDATE_GROUP_CLAIMS_PATCH, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID, uniqueGroupId);
            eventProperties.put(IdentityStoreConstants.CLAIM_LIST_TO_ADD, claimsToAdd);
            eventProperties.put(IdentityStoreConstants.CLAIM_LIST_TO_REMOVE, claimsToRemove);
        });

    }

    @Override
    public void deleteGroup(String uniqueGroupId) throws IdentityStoreException, GroupNotFoundException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<Void, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        template.pushEvent(IdentityStoreInterceptorConstants.PRE_DELETE_GROUP, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID, uniqueGroupId);
        }).executeWith(new EventHandlerDelegate<Void>() {
            @Override
            public Void execute() throws IdentityStoreException, GroupNotFoundException {
                identityStore.deleteGroup(uniqueGroupId);
                return null;
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_DELETE_GROUP, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID, uniqueGroupId);
        });

    }

    @Override
    public void updateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIds) throws IdentityStoreException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<Void, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        template.pushEvent(IdentityStoreInterceptorConstants.PRE_UPDATE_USERS_OF_GROUP_PUT, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID, uniqueGroupId);
            eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID_LIST, uniqueUserIds);
        }).executeWith(new EventHandlerDelegate<Void>() {
            @Override
            public Void execute() throws IdentityStoreException {
                identityStore.updateUsersOfGroup(uniqueGroupId, uniqueUserIds);
                return null;
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_UPDATE_USERS_OF_GROUP_PUT, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID, uniqueGroupId);
            eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID_LIST, uniqueUserIds);
        });

    }

    @Override
    public void updateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIdsToAdd,
                                   List<String> uniqueUserIdsToRemove) throws IdentityStoreException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<Void, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        template.pushEvent(IdentityStoreInterceptorConstants.PRE_UPDATE_USERS_OF_GROUP_PATCH, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID, uniqueGroupId);
            eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID_LIST_TO_ADD, uniqueUserIdsToAdd);
            eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID_LIST_TO_REMOVE, uniqueUserIdsToRemove);
        }).executeWith(new EventHandlerDelegate<Void>() {
            @Override
            public Void execute() throws IdentityStoreException {
                identityStore.updateUsersOfGroup(uniqueGroupId, uniqueUserIdsToAdd, uniqueUserIdsToRemove);
                return null;
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_UPDATE_USERS_OF_GROUP_PATCH, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.UNIQUE_GROUP_ID, uniqueGroupId);
            eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID_LIST_TO_ADD, uniqueUserIdsToAdd);
            eventProperties.put(IdentityStoreConstants.UNIQUE_USED_ID_LIST_TO_REMOVE, uniqueUserIdsToRemove);
        });

    }

    @Override
    public AuthenticationContext authenticate(Claim claim, Callback[] credentials, String domainName)
            throws AuthenticationFailure, IdentityStoreException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<AuthenticationContext, IdentityStoreException> template = new
                EventInterceptorTemplate<>(eventService, messageContext);

        AuthenticationContext authenticationContext = template.pushEvent(IdentityStoreInterceptorConstants
                                                                                 .PRE_AUTHENTICATE, (eventProperties)
                -> {
            eventProperties.put(IdentityStoreConstants.CLAIM, claim);
            eventProperties.put(IdentityStoreConstants.CREDENTIAL_LIST, credentials);
            eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);
        }).executeWith(new EventHandlerDelegate<AuthenticationContext>() {
            @Override
            public AuthenticationContext execute() throws AuthenticationFailure, IdentityStoreException {
                return identityStore.authenticate(claim, credentials, domainName);
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_AUTHENTICATE, (eventProperties) -> {
            eventProperties.put(IdentityStoreConstants.CLAIM, claim);
            eventProperties.put(IdentityStoreConstants.CREDENTIAL_LIST, credentials);
            eventProperties.put(IdentityStoreConstants.DOMAIN_NAME, domainName);
            eventProperties.put(IdentityStoreConstants.AUTHENTICATION_CONTEXT, template.getResult());
        }).getResult();

        return authenticationContext;
    }

    @Override
    public String getPrimaryDomainName() throws IdentityStoreException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<String, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        String primaryDomainName = template.pushEvent(IdentityStoreInterceptorConstants.PRE_GET_PRIMARY_DOMAIN_NAME,
                                                      (eventProperties) -> {
                                                      }).executeWith(new EventHandlerDelegate<String>() {
            @Override
            public String execute() throws IdentityStoreException {
                return identityStore.getPrimaryDomainName();
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_GET_PRIMARY_DOMAIN_NAME, (eventProperties) -> {
        }).getResult();

        return primaryDomainName;
    }

    @Override
    public Set<String> getDomainNames() throws IdentityStoreException {

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext(null);

        EventInterceptorTemplate<Set<String>, IdentityStoreException> template = new EventInterceptorTemplate<>
                (eventService, messageContext);

        Set<String> domainNames = template.pushEvent(IdentityStoreInterceptorConstants.PRE_GET_DOMAIN_NAMES,
                                                     (eventProperties) -> {
                                                     }).executeWith(new EventHandlerDelegate<Set<String>>() {
            @Override
            public Set<String> execute() throws IdentityStoreException {
                return identityStore.getDomainNames();
            }
        }).pushEvent(IdentityStoreInterceptorConstants.POST_GET_DOMAIN_NAMES, (eventProperties) -> {
        }).getResult();

        return domainNames;
    }
}

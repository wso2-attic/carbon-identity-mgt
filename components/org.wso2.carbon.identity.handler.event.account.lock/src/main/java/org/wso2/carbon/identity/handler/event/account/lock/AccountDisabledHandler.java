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
 * limitations und
 */

package org.wso2.carbon.identity.handler.event.account.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.common.base.event.EventContext;
import org.wso2.carbon.identity.common.base.event.model.Event;
import org.wso2.carbon.identity.common.base.exception.IdentityException;
import org.wso2.carbon.identity.common.base.exception.IdentityRuntimeException;
import org.wso2.carbon.identity.common.base.handler.InitConfig;
import org.wso2.carbon.identity.common.base.message.MessageContext;
import org.wso2.carbon.identity.event.AbstractEventHandler;
import org.wso2.carbon.identity.event.EventConstants;
import org.wso2.carbon.identity.event.EventException;
import org.wso2.carbon.identity.handler.event.account.lock.bean.AccountLockBean;
import org.wso2.carbon.identity.handler.event.account.lock.constants.AccountConstants;
import org.wso2.carbon.identity.handler.event.account.lock.internal.AccountServiceDataHolder;
import org.wso2.carbon.identity.mgt.AuthenticationContext;
import org.wso2.carbon.identity.mgt.IdentityStore;
import org.wso2.carbon.identity.mgt.User;
import org.wso2.carbon.identity.mgt.UserState;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.lcm.core.LifecycleOperationManager;
import org.wso2.carbon.lcm.core.exception.LifecycleException;
import org.wso2.carbon.lcm.core.util.LifecycleOperationUtil;
import org.wso2.carbon.lcm.sql.beans.LifecycleHistoryBean;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.wso2.carbon.identity.mgt.UserState.DISABLED;
import static org.wso2.carbon.identity.mgt.constant.StoreConstants.IdentityStoreConstants;
import static org.wso2.carbon.identity.mgt.constant.StoreConstants.IdentityStoreInterceptorConstants;
import static org.wso2.carbon.kernel.utils.LambdaExceptionUtils.rethrowConsumer;
import static org.wso2.carbon.kernel.utils.StringUtils.isNullOrEmpty;
/**
 * Account Disable Handler class
 */
public class AccountDisabledHandler extends AbstractEventHandler {

    private AccountLockBean accountLockBean = new AccountLockBean();
    private static Logger log = LoggerFactory.getLogger(AccountDisabledHandler.class);
    private static String oldAccountDisabledValue = "account_disabled_status";
    private static final String TEMPLATE_TYPE = "TEMPLATE_TYPE";

    private static List<String> events = Arrays.asList(
            IdentityStoreInterceptorConstants.PRE_UPDATE_USER_CLAIMS_PUT,
            IdentityStoreInterceptorConstants.PRE_UPDATE_USER_CLAIMS_PATCH,
            IdentityStoreInterceptorConstants.POST_UPDATE_USER_CLAIMS_PATCH,
            IdentityStoreInterceptorConstants.POST_UPDATE_USER_CLAIMS_PUT,
            IdentityStoreInterceptorConstants.PRE_UPDATE_USER_CREDENTIALS_PUT,
            IdentityStoreInterceptorConstants.PRE_UPDATE_USER_CREDENTIALS_PATCH,
            IdentityStoreInterceptorConstants.PRE_DELETE_USER,
            IdentityStoreInterceptorConstants.PRE_UPDATE_GROUPS_OF_USER_PUT,
            IdentityStoreInterceptorConstants.PRE_UPDATE_GROUPS_OF_USER_PATCH,
            IdentityStoreInterceptorConstants.PRE_UPDATE_USERS_OF_GROUP_PUT,
            IdentityStoreInterceptorConstants.PRE_UPDATE_USERS_OF_GROUP_PATCH,
            IdentityStoreInterceptorConstants.POST_AUTHENTICATE);
    @Override
    public void handle(EventContext eventContext, Event event) throws EventException {
        if (!accountLockBean.isAccountInactiveEnabled()) {
            return;
        }

        IdentityStore identityStore = AccountServiceDataHolder.getInstance().getRealmService().getIdentityStore();

        try {
            if (IdentityStoreInterceptorConstants.PRE_UPDATE_USER_CLAIMS_PUT
                    .equals(event.getEventName())) {
                handlePreUpdateUserClaimsPUT(eventContext, event, identityStore);

            } else if (IdentityStoreInterceptorConstants.PRE_UPDATE_USER_CLAIMS_PATCH
                    .equals(event.getEventName())) {
                handlePreUpdateUserClaimsPATCH(eventContext, event, identityStore);

            } else if (event.getEventName().equals(IdentityStoreInterceptorConstants
                    .POST_UPDATE_USER_CLAIMS_PATCH) ||
                    event.getEventName().equals(IdentityStoreInterceptorConstants
                            .POST_UPDATE_USER_CLAIMS_PUT)) {
                //Post update user PUT and Post update user PATCH
                handlePostUpdateUserClaims(eventContext, event);
            } else if (IdentityStoreInterceptorConstants.PRE_UPDATE_USER_CREDENTIALS_PUT
                    .equals(event.getEventName())) {
                handleDisableFromUniqueUserId(event, identityStore);

            } else if (IdentityStoreInterceptorConstants.PRE_UPDATE_USER_CREDENTIALS_PATCH
                    .equals(event.getEventName())) {
                handleDisableFromUniqueUserId(event, identityStore);

            } else if (IdentityStoreInterceptorConstants.PRE_DELETE_USER
                    .equals(event.getEventName())) {
                handleDisableFromUniqueUserId(event, identityStore);

            } else if (IdentityStoreInterceptorConstants.PRE_UPDATE_GROUPS_OF_USER_PUT
                    .equals(event.getEventName())) {
                handleDisableFromUniqueUserId(event, identityStore);

            } else if (IdentityStoreInterceptorConstants.PRE_UPDATE_GROUPS_OF_USER_PATCH
                    .equals(event.getEventName())) {
                handleDisableFromUniqueUserId(event, identityStore);

            } else if (IdentityStoreInterceptorConstants.PRE_UPDATE_USERS_OF_GROUP_PUT.equals(event
                    .getEventName())) {
                List<String> uniqueUserIds = (List<String>) event.getEventProperties().get(
                        IdentityStoreConstants.UNIQUE_USED_ID_LIST);
                uniqueUserIds.forEach(rethrowConsumer(uniqueUserId -> {
                    User user = identityStore.getUser(uniqueUserId);
                    if (user != null && UserState.DISABLED.toString().equals(user.getState())) {
                        throw new EventException("User account is disabled: " + user.getUniqueUserId());
                    }
                }));
            } else if (IdentityStoreInterceptorConstants.PRE_UPDATE_USERS_OF_GROUP_PATCH
                    .equals(event.getEventName())) {
                List<String> uniqueUserIdsToAdd = (List<String>) event.getEventProperties().get(
                        IdentityStoreConstants.UNIQUE_USED_ID_LIST_TO_ADD);
                uniqueUserIdsToAdd.forEach(rethrowConsumer(uniqueUserId -> {
                    User user = identityStore.getUser(uniqueUserId);
                    if (user != null && UserState.DISABLED.toString().equals(user.getState())) {
                        throw new EventException("User account is disabled: " + user.getUniqueUserId());
                    }
                }));

                List<String> uniqueUserIdsToRemove = (List<String>) event.getEventProperties().get(
                        IdentityStoreConstants.UNIQUE_USED_ID_LIST_TO_REMOVE);
                uniqueUserIdsToRemove.forEach(rethrowConsumer(uniqueUserId -> {
                    User user = identityStore.getUser(uniqueUserId);
                    if (user != null && UserState.DISABLED.toString().equals(user.getState())) {
                        throw new EventException("User account is disabled: " + user.getUniqueUserId());
                    }
                }));

            } else if (IdentityStoreInterceptorConstants.POST_AUTHENTICATE
                    .equals(event.getEventName())) {
                AuthenticationContext authenticationContext = (AuthenticationContext) event.getEventProperties()
                        .get(IdentityStoreConstants.AUTHENTICATION_CONTEXT);

                if (authenticationContext == null) {
                    throw new EventException("No context found for authentication");
                }

                if (authenticationContext.isAuthenticated()) {
                    if (UserState.DISABLED.toString().equals(authenticationContext.getUser().getState())) {
                        throw new EventException("User account is disabled: " + authenticationContext.getUser()
                                .getUniqueUserId());
                    }
                }

            }
        } catch (IdentityStoreException | UserNotFoundException e) {
            throw new EventException("Error occurred in Account disabled handler", e);
        }
    }

    @Override
    public boolean canHandle(Event event, MessageContext messageContext) throws IdentityRuntimeException {
        return events.contains(event.getEventName());
    }

    @Override
    public void configure(InitConfig initConfig) throws IdentityException {
    }

    public int getPriority(MessageContext messageContext) {
        return 101;
    }

    public String getName() {
        return "account.disabled.handler";
    }


    /**
     * This is used to handle Post update user claims
     *
     * @param eventContext : EventContext
     * @param event        : Event
     * @throws EventException : EventException
     */
    private void handlePostUpdateUserClaims(EventContext eventContext, Event event) throws EventException {

        Object accountDisabledObject = eventContext.getParameter(oldAccountDisabledValue);
        if (accountDisabledObject != null) {
            boolean oldAccountDisabledValue = (boolean) accountDisabledObject;

            String uniqueUserId = (String) event.getEventProperties()
                    .get(IdentityStoreConstants.UNIQUE_USED_ID);

            try {
                boolean newAccountDisabledClaimValue = Boolean.parseBoolean(getUserClaimValue(uniqueUserId,
                        AccountConstants.ACCOUNT_DISABLED_CLAIM));

                if (oldAccountDisabledValue != newAccountDisabledClaimValue) {
                    if (newAccountDisabledClaimValue) {
                        disableUserAccountByAdmin(uniqueUserId);
                    } else {
                        enableUserAccountByAdmin(uniqueUserId);
                    }
                }
            } catch (IdentityStoreException | UserNotFoundException e) {
                throw new EventException("Error while reading claims of user: " + uniqueUserId);
            }
        }
    }

    /**
     * Update user to disable state by admin
     *
     * @param uniqueUserId : user unique user id
     * @throws IdentityStoreException : IdentityStoreException
     * @throws UserNotFoundException  : UserNotFoundException
     * @throws EventException         : EventException
     */
    private void disableUserAccountByAdmin(String uniqueUserId) throws IdentityStoreException, UserNotFoundException,
            EventException {
        IdentityStore identityStore = AccountServiceDataHolder.getInstance().getRealmService().getIdentityStore();
        User user = identityStore.getUser(uniqueUserId);
        updateLifeCycleState(user, true);
        if (accountLockBean.isNotificationInternallyManage()) {
            triggerNotification(uniqueUserId, AccountConstants.EMAIL_TEMPLATE_TYPE_ACC_DISABLED);
        }
    }

    /**
     * Update user to enabled state by admin
     *
     * @param uniqueUserId : user unique user id
     * @throws IdentityStoreException : IdentityStoreException
     * @throws UserNotFoundException  : UserNotFoundException
     * @throws EventException         : EventException
     */
    private void enableUserAccountByAdmin(String uniqueUserId) throws IdentityStoreException, UserNotFoundException,
            EventException {
        IdentityStore identityStore = AccountServiceDataHolder.getInstance().getRealmService().getIdentityStore();
        User user = identityStore.getUser(uniqueUserId);
        updateLifeCycleState(user, false);

        if (accountLockBean.isNotificationInternallyManage()) {
            triggerNotification(uniqueUserId, AccountConstants.EMAIL_TEMPLATE_TYPE_ACC_ENABLED);
        }
    }

    /**
     * This is used to handle pre update user claims PUT
     *
     * @param eventContext ; Event Context
     * @param event        : Event
     * @throws EventException : EventException
     */
    private void handlePreUpdateUserClaimsPUT(EventContext eventContext, Event event,
                                              IdentityStore identityStore) throws EventException,
            IdentityStoreException, UserNotFoundException {
        String uniqueUserId = (String) event.getEventProperties()
                .get(IdentityStoreConstants.UNIQUE_USED_ID);
        List<Claim> claims = (List<Claim>) event.getEventProperties()
                .get(IdentityStoreConstants.CLAIM_LIST);

        Optional<Claim> optional = claims.stream()
                .filter(claim -> AccountConstants.ACCOUNT_DISABLED_CLAIM.equals(claim.getClaimUri()))
                .findFirst();
        if (optional.isPresent()) {
            try {
                boolean oldAccountDisabledClaimValue = Boolean.parseBoolean(getUserClaimValue(uniqueUserId,
                        AccountConstants.ACCOUNT_DISABLED_CLAIM));
                boolean newAccountDisabledClaimValue = Boolean.parseBoolean(optional.get().getValue());
                if (oldAccountDisabledClaimValue != newAccountDisabledClaimValue) {
                    eventContext.addParameter(oldAccountDisabledValue, oldAccountDisabledClaimValue);
                }

            } catch (IdentityStoreException | UserNotFoundException e) {
                throw new EventException("Error while reading claims of user: " + uniqueUserId);
            }
        } else {
            handleDisableFromUniqueUserId(event, identityStore);
        }
    }

    /**
     * This is used to handle pre update user claims PATCH
     *
     * @param eventContext ; Event Context
     * @param event        : Event
     * @throws EventException : EventException
     */
    private void handlePreUpdateUserClaimsPATCH(EventContext eventContext,
                                                Event event, IdentityStore identityStore)
            throws EventException, IdentityStoreException, UserNotFoundException {
        String uniqueUserId = (String) event.getEventProperties()
                .get(IdentityStoreConstants.UNIQUE_USED_ID);
        List<Claim> claimsToAdd = (List<Claim>) event.getEventProperties()
                .get(IdentityStoreConstants.CLAIM_LIST_TO_ADD);

        Optional<Claim> optional = claimsToAdd.stream()
                .filter(claim -> AccountConstants.ACCOUNT_DISABLED_CLAIM.equals(claim.getClaimUri()))
                .findFirst();
        if (optional.isPresent()) {
            try {
                boolean oldAccountDisabledClaimValue = Boolean.parseBoolean(getUserClaimValue(uniqueUserId,
                        AccountConstants.ACCOUNT_DISABLED_CLAIM));
                boolean newAccountDisabledClaimValue = Boolean.parseBoolean(optional.get().getValue());
                if (oldAccountDisabledClaimValue != newAccountDisabledClaimValue) {
                    eventContext.addParameter(oldAccountDisabledValue, oldAccountDisabledClaimValue);
                }

            } catch (IdentityStoreException | UserNotFoundException e) {
                throw new EventException("Error while reading claims of user: " + uniqueUserId);
            }
        } else {
            handleDisableFromUniqueUserId(event, identityStore);
        }
    }

    /**
     * This method is used to check user inactivity from unique user id.
     *
     * @param event         : Event
     * @param identityStore : identityStore
     * @throws IdentityStoreException : IdentityStoreException
     * @throws UserNotFoundException  : UserNotFoundException
     * @throws EventException         : EventException
     */
    private void handleDisableFromUniqueUserId(Event event, IdentityStore identityStore)
            throws IdentityStoreException, UserNotFoundException, EventException {

        String userId = (String) event.getEventProperties().get(IdentityStoreConstants.UNIQUE_USED_ID);
        User user = identityStore.getUser(userId);
        if (user != null && UserState.DISABLED.toString().equals(user.getState())) {
            if (log.isDebugEnabled()) {
                log.debug("User account is disabled: " + user.getUniqueUserId());
            }

            throw new EventException("User account is disabled: " + user.getUniqueUserId());
        }
    }

    /**
     * This is used to get user claim value from claimURI
     *
     * @param userId   : userUniqueId
     * @param claimURI : claimURI
     * @return : claim Value
     * @throws IdentityStoreException : IdentityStoreException
     * @throws UserNotFoundException  : UserNotFoundException
     */
    private String getUserClaimValue(String userId, String claimURI) throws IdentityStoreException,
            UserNotFoundException {

        IdentityStore identityStore = AccountServiceDataHolder.getInstance().getRealmService().getIdentityStore();
        User user = identityStore.getUser(userId);
        Optional<Claim> optional = user.getClaims().stream()
                .filter(claim -> claimURI.equals(claim.getClaimUri()))
                .findFirst();
        if (optional.isPresent()) {
            Claim claim = optional.get();
            if (claim != null && claim.getValue() != null) {
                return claim.getValue();
            }
        }
        return null;
    }

    /**
     * This is used ot update user lifecycle states in Account Disabled handler
     *
     * @param user      : user
     * @param toDisable : if true, set state to disable, else set state to enable
     * @throws EventException
     */
    private void updateLifeCycleState(User user, boolean toDisable) throws EventException {

        try {
            IdentityStore identityStore = AccountServiceDataHolder.getInstance().getRealmService().getIdentityStore();
            String domainUserId = getDecodedUniqueEntityId(user.getUniqueUserId());

            if (toDisable) {
                LifecycleOperationManager.executeLifecycleEvent(DISABLED.toString(),
                        domainUserId, domainUserId, null);
                identityStore.setUserState(user.getUniqueUserId(), DISABLED.toString());
            } else {
                List<LifecycleHistoryBean> lifecycleHistoryFromId = LifecycleOperationUtil
                        .getLifecycleHistoryFromId(domainUserId);

                if (lifecycleHistoryFromId.size() == 0) {
                    throw new EventException("Lifecycle history is disabled.");
                }
                String previousState = lifecycleHistoryFromId.get(lifecycleHistoryFromId.size() - 1).getPreviousState();
                // if the user is disabled and then enabled, he should be in the unlocked state. CREATED state is not
                // suitable
                if (UserState.CREATED.toString().equals(previousState)) {
                    previousState = UserState.UNLOCKED__UNVERIFIED.toString();
                }
                LifecycleOperationManager.executeLifecycleEvent(previousState, domainUserId, domainUserId, null);
                identityStore.setUserState(user.getUniqueUserId(), previousState);
            }
        } catch (LifecycleException | UserNotFoundException | IdentityStoreException e) {
            throw new EventException("Error while executing lifecycle event for user: " + user
                    .getUniqueUserId(), e);
        }
    }

    /**
     * This is used to extract domainId from unique user id
     *
     * @param uniqueEntityId : Unique user id
     * @return : domain Id
     * @throws EventException : EventException
     */
    private String getDecodedUniqueEntityId(String uniqueEntityId) throws
            EventException {

        String[] decodedUniqueEntityIdParts = uniqueEntityId.split("\\.", 2);
        if (decodedUniqueEntityIdParts.length != 2 || isNullOrEmpty(decodedUniqueEntityIdParts[0]) ||
                isNullOrEmpty(decodedUniqueEntityIdParts[1])) {
            throw new EventException("invalid unique entity id.");
        }
        return decodedUniqueEntityIdParts[1];
    }

    private void triggerNotification(String userUniqueId, String type)
            throws EventException {
        String eventName = EventConstants.Event.TRIGGER_NOTIFICATION;
        Map<String, Object> properties = new HashMap<>();
        properties.put(EventConstants.EventProperty.USER_UNIQUE_ID, userUniqueId);

        properties.put(TEMPLATE_TYPE, type);
        Event identityMgtEvent = new Event(eventName, properties);
        EventContext eventContext = new EventContext();

        try {
            AccountServiceDataHolder.getInstance().getIdentityEventService().pushEvent(identityMgtEvent,
                    eventContext);
        } catch (IdentityException e) {
            log.warn("Error while sending email: " + type + " for user: " + userUniqueId);
        }
    }

}



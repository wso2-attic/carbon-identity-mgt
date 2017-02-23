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

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.common.base.event.EventContext;
import org.wso2.carbon.identity.common.base.event.model.Event;
import org.wso2.carbon.identity.common.base.exception.IdentityException;
import org.wso2.carbon.identity.common.base.handler.InitConfig;
import org.wso2.carbon.identity.common.base.message.MessageContext;
import org.wso2.carbon.identity.event.AbstractEventHandler;
import org.wso2.carbon.identity.event.EventConstants;
import org.wso2.carbon.identity.event.EventException;
import org.wso2.carbon.identity.handler.event.account.lock.bean.AccountLockBean;
import org.wso2.carbon.identity.handler.event.account.lock.constants.AccountConstants;
import org.wso2.carbon.identity.handler.event.account.lock.internal.AccountServiceDataHolder;
import org.wso2.carbon.identity.mgt.AuthenticationContext;
import org.wso2.carbon.identity.mgt.FailedAuthenticationContext;
import org.wso2.carbon.identity.mgt.IdentityStore;
import org.wso2.carbon.identity.mgt.User;
import org.wso2.carbon.identity.mgt.UserState;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.lcm.core.LifecycleOperationManager;
import org.wso2.carbon.lcm.core.exception.LifecycleException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.wso2.carbon.identity.mgt.UserState.LOCKED_INVALID_CREDENTIAL__UNVERIFIED;
import static org.wso2.carbon.identity.mgt.UserState.LOCKED_INVALID_CREDENTIAL__VERIFIED;
import static org.wso2.carbon.identity.mgt.UserState.LOCKED__UNVERIFIED;
import static org.wso2.carbon.identity.mgt.UserState.LOCKED__VERIFIED;
import static org.wso2.carbon.identity.mgt.UserState.UNLOCKED__UNVERIFIED;
import static org.wso2.carbon.identity.mgt.UserState.UNLOCKED__VERIFIED;
import static org.wso2.carbon.identity.mgt.constant.StoreConstants.IdentityStoreConstants;
import static org.wso2.carbon.identity.mgt.constant.StoreConstants.IdentityStoreInterceptorConstants;
import static org.wso2.carbon.identity.mgt.impl.util.IdentityUserMgtUtil.setClaimInIdentityStore;
import static org.wso2.carbon.identity.mgt.impl.util.IdentityUserMgtUtil.setClaimsInIdentityStore;
import static org.wso2.carbon.kernel.utils.StringUtils.isNullOrEmpty;

/**
 * AccountLockHandler class
 */
public class AccountLockHandler extends AbstractEventHandler {

    private AccountLockBean accountLockBean = new AccountLockBean();
    private static Logger log = LoggerFactory.getLogger(AccountLockHandler.class);
    private static String oldAccountLockValue = "account_lock_status";
    private static final String TEMPLATE_TYPE = "TEMPLATE_TYPE";

    @Override
    public void handle(EventContext eventContext, Event event) throws EventException {
        if (!accountLockBean.isEnabled()) {
            return;
        }

        if (event.getEventName().equals(IdentityStoreInterceptorConstants.POST_AUTHENTICATE)) {
            handlePostAuthentication(event);
        } else if (event.getEventName().equals(IdentityStoreInterceptorConstants.PRE_UPDATE_USER_CLAIMS_PUT)) {
            handlePreUpdateUserClaimsPUT(eventContext, event);
        } else if (event.getEventName().equals(IdentityStoreInterceptorConstants.PRE_UPDATE_USER_CLAIMS_PATCH)) {
            handlePreUpdateUserClaimsPATCH(eventContext, event);
        } else if (event.getEventName().equals(IdentityStoreInterceptorConstants.POST_UPDATE_USER_CLAIMS_PATCH) ||
                event.getEventName().equals(IdentityStoreInterceptorConstants.POST_UPDATE_USER_CLAIMS_PUT)) {
            //Post update user PUT and Post update user PATCH
            handlePostUpdateUserClaims(eventContext, event);
        }
    }

    @Override
    public void configure(InitConfig initConfig) throws IdentityException {
    }

    public int getPriority(MessageContext messageContext) {
        return 100;
    }

    public String getName() {
        return "account.lock.handler";
    }

    /**
     * This is used to handle post authentication event in account lock handler
     *
     * @param event : Post Authentication Event
     * @throws EventException : EventException
     */
    private void handlePostAuthentication(Event event) throws EventException {

        AuthenticationContext authenticationContext = (AuthenticationContext) event.getEventProperties()
                .get(IdentityStoreConstants.AUTHENTICATION_CONTEXT);

        if (authenticationContext == null) {
            throw new EventException("No context found for authentication");
        }

        if (authenticationContext.isAuthenticated()) {
            User authenticatedUser = authenticationContext.getUser();
            //Handle locked state. Throws an exception if user is locked
            handleLockedState(authenticatedUser);

            //set user failed attempts to zero upon a successful attempt
            setUserFailedAttempts(authenticatedUser, 0);

        } else {
            //Authentication failure
            if (authenticationContext instanceof FailedAuthenticationContext) {
                //Get authentication failed user list. It is a list since multiple domains can have same username
                List<User> userList = ((FailedAuthenticationContext) authenticationContext).getFailedUsers();

                for (User user : userList) {
                    try {
                        if (UserState.valueOf(user.getState()).isInGroup(UserState.Group.LOCKED)) {
                            //No need to process. Invalid credential, locked user
                            continue;
                        }

                        int userFailedAttemptsCount = 0;

                        //Read the failedCount claim
                        String userFailedAttemptsCountString = getUserClaimValue(user, AccountConstants
                                .FAILED_LOGIN_ATTEMPTS_CLAIM);
                        if (NumberUtils.isNumber(userFailedAttemptsCountString)) {
                            userFailedAttemptsCount = Integer.parseInt(userFailedAttemptsCountString);
                        }

                        //increase the failed count by 1
                        userFailedAttemptsCount++;

                        //if user failed count is more than maxFailedAttempts count, the account should be locked
                        if (userFailedAttemptsCount >= accountLockBean.getMaxFailedAttempts()) {
                            lockInvalidCredentialUserAccount(user);
                        } else {
                            // Set user failed attempts count
                            setUserFailedAttempts(user, userFailedAttemptsCount);
                        }
                    } catch (IdentityStoreException | UserNotFoundException e) {
                        throw new EventException("Unexpected error: ", e);
                    }
                }

            }
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

    /**
     * This is used to get user claim value from claimURI
     *
     * @param user     : user
     * @param claimURI : Claim URI
     * @return : claim Value
     * @throws IdentityStoreException :UserNotFoundException
     * @throws UserNotFoundException  : UserNotFoundException
     */
    private String getUserClaimValue(User user, String claimURI) throws IdentityStoreException, UserNotFoundException {

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
     * his is used to get user claim value from claimURI
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
     * This is used to handle lock state in account lock handler
     *
     * @param user : user
     * @throws EventException :EventException
     */
    private void handleLockedState(User user) throws EventException {

        if (UserState.valueOf(user.getState()) == null) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid user state :" + user.getState());
            }
            throw new EventException("Invalid user state :" + user.getState());
        }

        if (UserState.valueOf(user.getState()).isInGroup(UserState.Group.LOCKED)) {

            //Check user unlock time exceeds or not
            if (LOCKED_INVALID_CREDENTIAL__VERIFIED.toString().equals(user.getState()) ||
                    LOCKED_INVALID_CREDENTIAL__UNVERIFIED.toString().equals(user.getState())) {
                String unlockedTimeString;
                try {
                    unlockedTimeString = getUserClaimValue(user, AccountConstants.ACCOUNT_UNLOCK_TIME_CLAIM);

                    if (NumberUtils.isNumber(unlockedTimeString)) {
                        long unlockTime = Long.parseLong(unlockedTimeString);
                        if (unlockTime != 0 && System.currentTimeMillis() >= unlockTime) {
                            if (log.isDebugEnabled()) {
                                log.debug("Unlocked user account: " + user.getUniqueUserId());
                            }

                            //Account state and  unlock email will be sent in post update claim handler.
                            setClaimInIdentityStore(user.getUniqueUserId(), AccountConstants.ACCOUNT_LOCKED_CLAIM,
                                    String.valueOf(false), null);
                            return;
                        }
                    }
                } catch (IdentityStoreException | UserNotFoundException e) {
                    throw new EventException("Error reading account lock claim, user: " + user.getUniqueUserId(), e);
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("User Account is locked: " + user.getUniqueUserId());
            }
            throw new EventException("User Account is locked: " + user.getUniqueUserId());
        }
    }

    /**
     * This is used to update user lifecycle states in Account Lock handler
     *
     * @param user   : user
     * @param toLock : if true, set state to lock, else set state to unlock
     * @throws EventException
     * @Param isAdminChanged : Is admin change the state, else invalid credentials
     */
    private void updateLifeCycleState(User user, boolean toLock, boolean isAdminChanged) throws EventException {

        try {
            boolean emailVerified = Boolean.parseBoolean(getUserClaimValue(user,
                    AccountConstants.EMAIL_VERIFIED_CLAIM));
            IdentityStore identityStore = AccountServiceDataHolder.getInstance().getRealmService().getIdentityStore();
            String domainUserId = getDecodedUniqueEntityId(user.getUniqueUserId());

            if (emailVerified && toLock && isAdminChanged) {
                // set to locked email verified state
                LifecycleOperationManager.executeLifecycleEvent(LOCKED__VERIFIED.toString(),
                        domainUserId, domainUserId, null);
                identityStore.setUserState(user.getUniqueUserId(), LOCKED__VERIFIED.toString());

            } else if (emailVerified && toLock && !isAdminChanged) {
                // set to locked email verified state
                LifecycleOperationManager.executeLifecycleEvent(LOCKED_INVALID_CREDENTIAL__VERIFIED.toString(),
                        domainUserId, domainUserId, null);
                identityStore.setUserState(user.getUniqueUserId(), LOCKED_INVALID_CREDENTIAL__VERIFIED.toString());

            } else if (emailVerified && !toLock) {
                LifecycleOperationManager.executeLifecycleEvent(UNLOCKED__VERIFIED.toString(), domainUserId,
                        domainUserId, null);
                identityStore.setUserState(user.getUniqueUserId(), UNLOCKED__VERIFIED.toString());

            } else if (!emailVerified && toLock && isAdminChanged) {
                LifecycleOperationManager.executeLifecycleEvent(LOCKED__UNVERIFIED.toString(),
                        domainUserId, domainUserId, null);
                identityStore.setUserState(user.getUniqueUserId(), LOCKED__UNVERIFIED.toString());
            } else if (!emailVerified && toLock && !isAdminChanged) {
                LifecycleOperationManager.executeLifecycleEvent(LOCKED_INVALID_CREDENTIAL__UNVERIFIED.toString(),
                        domainUserId, domainUserId, null);
                identityStore.setUserState(user.getUniqueUserId(), LOCKED_INVALID_CREDENTIAL__UNVERIFIED.toString());
            } else {
                LifecycleOperationManager.executeLifecycleEvent(UNLOCKED__UNVERIFIED.toString(), domainUserId,
                        domainUserId, null);
                identityStore.setUserState(user.getUniqueUserId(), UNLOCKED__UNVERIFIED.toString());
            }

        } catch (LifecycleException | UserNotFoundException | IdentityStoreException e) {
            throw new EventException("Error while executing lifecycle event for user: " + user
                    .getUniqueUserId(), e);
        }
    }

    /**
     * This is used to lock user account for invalid credentials
     *
     * @param user : user
     * @throws EventException         EventException
     * @throws IdentityStoreException IdentityStoreException
     * @throws UserNotFoundException  UserNotFoundException
     */
    private void lockInvalidCredentialUserAccount(User user) throws EventException, IdentityStoreException,
            UserNotFoundException {
        //Update lifecycle state to lock
        updateLifeCycleState(user, true, false);

        int failedLoginLockoutCountValue = 0;

        String failedLoginLockoutCountString = getUserClaimValue(user,
                AccountConstants.FAILED_LOGIN_LOCKOUT_COUNT_CLAIM);
        if (NumberUtils.isNumber(failedLoginLockoutCountString)) {
            failedLoginLockoutCountValue = Integer.parseInt(failedLoginLockoutCountString);
        }

        //Set a unlock time
        long lockTime = (long) (TimeUnit.MINUTES.toMillis(accountLockBean
                .getAccountLockTimeInMinutes()) * Math.pow(accountLockBean.getLockedTimeRatio(),
                failedLoginLockoutCountValue));

        failedLoginLockoutCountValue++;

        long unlockTime = System.currentTimeMillis() + lockTime;

        Map<String, String> claims = new HashMap<>();
        claims.put(AccountConstants.ACCOUNT_LOCKED_CLAIM, String.valueOf(true));
        claims.put(AccountConstants.FAILED_LOGIN_ATTEMPTS_CLAIM, String.valueOf(0));
        claims.put(AccountConstants.ACCOUNT_UNLOCK_TIME_CLAIM, String.valueOf(unlockTime));
        claims.put(AccountConstants.FAILED_LOGIN_LOCKOUT_COUNT_CLAIM,
                String.valueOf(failedLoginLockoutCountValue));
        setClaimsInIdentityStore(user.getUniqueUserId(), claims, null);

        if (accountLockBean.isNotificationInternallyManage()) {
            triggerNotification(user.getUniqueUserId(), AccountConstants.EMAIL_TEMPLATE_TYPE_ACC_LOCKED);
        }
    }

    /**
     * This is used to set user failed attempts count
     *
     * @param user                    : user
     * @param userFailedAttemptsCount : userFailedAttemptsCount
     * @throws EventException : EventException
     */
    private void setUserFailedAttempts(User user, int userFailedAttemptsCount) throws EventException {
        try {
            setClaimInIdentityStore(user.getUniqueUserId(), AccountConstants
                    .FAILED_LOGIN_ATTEMPTS_CLAIM, String.valueOf(userFailedAttemptsCount), null);
        } catch (IdentityStoreException | UserNotFoundException e) {
            throw new EventException("Error while setting failed attempts, user: " + user.getUniqueUserId(), e);
        }
    }

    /**
     * This is used to handle pre update user claims PUT
     *
     * @param eventContext ; Event Context
     * @param event        : Event
     * @throws EventException : EventException
     */
    private void handlePreUpdateUserClaimsPUT(EventContext eventContext, Event event) throws EventException {
        String uniqueUserId = (String) event.getEventProperties()
                .get(IdentityStoreConstants.UNIQUE_USED_ID);
        List<Claim> claims = (List<Claim>) event.getEventProperties()
                .get(IdentityStoreConstants.CLAIM_LIST);

        Optional<Claim> optional = claims.stream()
                .filter(claim -> AccountConstants.ACCOUNT_LOCKED_CLAIM.equals(claim.getClaimUri()))
                .findFirst();
        if (optional.isPresent()) {
            try {
                boolean oldAccountLockClaimValue = Boolean.parseBoolean(getUserClaimValue(uniqueUserId,
                        AccountConstants.ACCOUNT_LOCKED_CLAIM));
                boolean newAccountLockClaimValue = Boolean.parseBoolean(optional.get().getValue());
                if (oldAccountLockClaimValue != newAccountLockClaimValue) {
                    eventContext.addParameter(oldAccountLockValue, oldAccountLockClaimValue);
                }

            } catch (IdentityStoreException | UserNotFoundException e) {
                throw new EventException("Error while reading claims of user: " + uniqueUserId);
            }
        }
    }

    /**
     * This is used to handle pre update user claims PATCH
     *
     * @param eventContext ; Event Context
     * @param event        : Event
     * @throws EventException : EventException
     */
    private void handlePreUpdateUserClaimsPATCH(EventContext eventContext, Event event) throws EventException {
        String uniqueUserId = (String) event.getEventProperties()
                .get(IdentityStoreConstants.UNIQUE_USED_ID);
        List<Claim> claimsToAdd = (List<Claim>) event.getEventProperties()
                .get(IdentityStoreConstants.CLAIM_LIST_TO_ADD);

        Optional<Claim> optional = claimsToAdd.stream()
                .filter(claim -> AccountConstants.ACCOUNT_LOCKED_CLAIM.equals(claim.getClaimUri()))
                .findFirst();
        if (optional.isPresent()) {
            try {
                boolean oldAccountLockClaimValue = Boolean.parseBoolean(getUserClaimValue(uniqueUserId,
                        AccountConstants.ACCOUNT_LOCKED_CLAIM));
                boolean newAccountLockClaimValue = Boolean.parseBoolean(optional.get().getValue());
                if (oldAccountLockClaimValue != newAccountLockClaimValue) {
                    eventContext.addParameter(oldAccountLockValue, oldAccountLockClaimValue);
                }

            } catch (IdentityStoreException | UserNotFoundException e) {
                throw new EventException("Error while reading claims of user: " + uniqueUserId);
            }
        }
    }

    /**
     * This is used to handle Post update user claims
     *
     * @param eventContext : EventContext
     * @param event        : Event
     * @throws EventException : EventException
     */
    private void handlePostUpdateUserClaims(EventContext eventContext, Event event) throws EventException {

        Object accountLockObject = eventContext.getParameter(oldAccountLockValue);
        if (accountLockObject != null) {
            boolean oldAccountLockValue = (boolean) accountLockObject;

            String uniqueUserId = (String) event.getEventProperties().get(IdentityStoreConstants.UNIQUE_USED_ID);

            try {
                boolean newAccountLockClaimValue = Boolean.parseBoolean(getUserClaimValue(uniqueUserId,
                        AccountConstants.ACCOUNT_LOCKED_CLAIM));

                if (oldAccountLockValue != newAccountLockClaimValue) {
                    if (newAccountLockClaimValue) {
                        //if the user already in locked state, then it is not a admin user lock scenario
                        if (!UserState.valueOf(getUserState(uniqueUserId)).isInGroup(UserState.Group.LOCKED)) {
                            lockUserAccountByAdmin(uniqueUserId);
                        }
                    } else {
                        unlockUserAccountByAdmin(uniqueUserId);
                    }
                }
            } catch (IdentityStoreException | UserNotFoundException e) {
                throw new EventException("Error while reading claims of user: " + uniqueUserId);
            }
        }
    }

    /**
     * Update user to unlock state by admin
     *
     * @param uniqueUserId : user unique user id
     * @throws IdentityStoreException : IdentityStoreException
     * @throws UserNotFoundException  : UserNotFoundException
     * @throws EventException         : EventException
     */
    private void unlockUserAccountByAdmin(String uniqueUserId) throws IdentityStoreException, UserNotFoundException,
            EventException {
        IdentityStore identityStore = AccountServiceDataHolder.getInstance().getRealmService().getIdentityStore();
        User user = identityStore.getUser(uniqueUserId);
        updateLifeCycleState(user, false, true);

        if (accountLockBean.isNotificationInternallyManage()) {
            triggerNotification(uniqueUserId, AccountConstants.EMAIL_TEMPLATE_TYPE_ACC_UNLOCKED);
        }
    }

    /**
     * Update user to lock state by admin
     *
     * @param uniqueUserId : user unique user id
     * @throws IdentityStoreException : IdentityStoreException
     * @throws UserNotFoundException  : UserNotFoundException
     * @throws EventException         : EventException
     */
    private void lockUserAccountByAdmin(String uniqueUserId) throws IdentityStoreException, UserNotFoundException,
            EventException {
        IdentityStore identityStore = AccountServiceDataHolder.getInstance().getRealmService().getIdentityStore();
        User user = identityStore.getUser(uniqueUserId);
        updateLifeCycleState(user, true, true);

        if (accountLockBean.isNotificationInternallyManage()) {
            triggerNotification(uniqueUserId, AccountConstants.EMAIL_TEMPLATE_TYPE_ACC_LOCKED);
        }
    }

    /**
     * Get the current state of user
     *
     * @param uniqueUserId : unique user ids
     * @return : state
     * @throws EventException : EventException
     */
    private String getUserState(String uniqueUserId) throws EventException {
        IdentityStore identityStore = AccountServiceDataHolder.getInstance().getRealmService().getIdentityStore();
        try {
            User user = identityStore.getUser(uniqueUserId);
            return user.getState();
        } catch (IdentityStoreException | UserNotFoundException e) {
            throw new EventException("Error while reading user state:" + uniqueUserId);
        }
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
            log.warn("Error while sending email : " + type + " for user: " + userUniqueId);
        }
    }

}



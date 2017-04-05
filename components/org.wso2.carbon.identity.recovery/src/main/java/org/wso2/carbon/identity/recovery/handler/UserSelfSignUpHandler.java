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

package org.wso2.carbon.identity.recovery.handler;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.common.base.event.EventContext;
import org.wso2.carbon.identity.common.base.event.model.Event;
import org.wso2.carbon.identity.common.base.exception.IdentityException;
import org.wso2.carbon.identity.common.base.handler.InitConfig;
import org.wso2.carbon.identity.common.base.message.MessageContext;
import org.wso2.carbon.identity.event.AbstractEventHandler;
import org.wso2.carbon.identity.event.EventService;
import org.wso2.carbon.identity.mgt.AuthenticationContext;
import org.wso2.carbon.identity.mgt.IdentityStore;
import org.wso2.carbon.identity.mgt.RealmService;
import org.wso2.carbon.identity.mgt.User;
import org.wso2.carbon.identity.mgt.UserState;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.mgt.impl.util.IdentityMgtConstants;
import org.wso2.carbon.identity.recovery.IdentityRecoveryClientException;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.IdentityRecoveryServerException;
import org.wso2.carbon.identity.recovery.RecoveryScenarios;
import org.wso2.carbon.identity.recovery.RecoverySteps;
import org.wso2.carbon.identity.recovery.mapping.SelfSignUpConfig;
import org.wso2.carbon.identity.recovery.model.Property;
import org.wso2.carbon.identity.recovery.model.UserRecoveryData;
import org.wso2.carbon.identity.recovery.store.JDBCRecoveryDataStore;
import org.wso2.carbon.identity.recovery.store.UserRecoveryDataStore;
import org.wso2.carbon.identity.recovery.util.Utils;
import org.wso2.carbon.lcm.core.LifecycleOperationManager;
import org.wso2.carbon.lcm.core.exception.LifecycleException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.mgt.constant.StoreConstants.IdentityStoreConstants;
import static org.wso2.carbon.identity.mgt.constant.StoreConstants.IdentityStoreInterceptorConstants;
import static org.wso2.carbon.identity.recovery.IdentityRecoveryConstants.CONFIRMATION_CODE;
import static org.wso2.carbon.identity.recovery.IdentityRecoveryConstants.ErrorCodes;
import static org.wso2.carbon.identity.recovery.IdentityRecoveryConstants.ErrorCodes.ACCOUNT_UNVERIFIED;
import static org.wso2.carbon.identity.recovery.IdentityRecoveryConstants.ErrorCodes.INVALID_USER_ID_FORMAT;
import static org.wso2.carbon.identity.recovery.IdentityRecoveryConstants.NOTIFICATION_TYPE_ACCOUNT_CONFIRM;
import static org.wso2.carbon.identity.recovery.IdentityRecoveryConstants.SELF_SIGN_UP_EVENT;
import static org.wso2.carbon.identity.recovery.IdentityRecoveryConstants.SELF_SIGN_UP_PROPERTIES;

/**
 * User SelfRegistration Handler.
 */
public class UserSelfSignUpHandler extends AbstractEventHandler {

    private static final Logger log = LoggerFactory.getLogger(UserSelfSignUpHandler.class);

    private SelfSignUpConfig config;
    private RealmService realmService;
    private EventService eventService;

    @Override
    public void configure(InitConfig initConfig) throws IdentityException {
    }

    public String getName() {
        return "user.self.sign.up";
    }

    @Override
    public void handle(EventContext eventContext, Event event) throws IdentityException {

        if (SELF_SIGN_UP_EVENT.equals(event.getEventName())) {

            // TODO: Use the new config model.
            config = new SelfSignUpConfig();
            config.setNotificationInternallyManaged(false);
            config.setSelfSignUpEnabled(true);
            config.setAccountLockOnCreation(true);

            handleSelfSignUpEvent(event, eventContext);

        } else if (IdentityStoreInterceptorConstants.POST_AUTHENTICATE.equals(event.getEventName())) {
            handlePostAuthenticateEvent(event);
        }
    }

    private void handleSelfSignUpEvent(Event event, EventContext eventContext) throws IdentityRecoveryException {

        if (!config.isSelfSignUpEnabled()) {

            if (log.isDebugEnabled()) {
                log.debug("Self sign-up is disabled in the configuration.");
            }
            return;
        }

        Map<String, Object> eventProperties = event.getEventProperties();
        User user = (User) eventProperties.get(IdentityStoreConstants.USER);

        if (user == null) {
            if (log.isDebugEnabled()) {
                log.debug("Event property: {} cannot be found in event: {}.", IdentityStoreConstants.USER,
                          SELF_SIGN_UP_EVENT);
            }
            throw Utils.handleServerException(ErrorCodes.MISSING_EVENT_PROPERTY, IdentityStoreConstants.USER);
        }

        IdentityStore identityStore = realmService.getIdentityStore();

        try {

            String domainUID = getDecodedUserEntityId(user.getUniqueUserId());

            if (config.isAccountLockOnCreation()) {

                if (log.isDebugEnabled()) {
                    log.debug("Changing user lifecycle state to {} for user: {}", UserState
                            .LOCKED_SELF_SIGN_UP.toString(), user.getUniqueUserId());
                }
                // Change lifecycle state.
                try {
                    LifecycleOperationManager.executeLifecycleEvent(UserState.LOCKED_SELF_SIGN_UP.toString(),
                                                                    domainUID, domainUID, null);
                } catch (LifecycleException e) {
                    throw Utils.handleServerException(ErrorCodes.FAILED_LIFECYCLE_EVENT, user.getUniqueUserId(), e);
                }

                identityStore.setUserState(user.getUniqueUserId(), UserState.LOCKED_SELF_SIGN_UP.toString());

                // Lock user account.
                try {

                    Claim claim = new Claim(IdentityMgtConstants.CLAIM_ROOT_DIALECT, IdentityMgtConstants
                            .ACCOUNT_LOCKED_CLAIM_URI, Boolean.TRUE.toString());

                    List<Claim> claims = Arrays.asList(claim);

                    identityStore.updateUserClaims(user.getUniqueUserId(), claims, null);
                } catch (IdentityStoreException e) {
                    throw Utils.handleServerException(ErrorCodes.FAILED_ACCOUNT_LOCK, user
                            .getUniqueUserId(), e);
                }

                UserRecoveryDataStore userRecoveryDataStore = JDBCRecoveryDataStore.getInstance();
                userRecoveryDataStore.invalidateByUserUniqueId(user.getUniqueUserId());

                String confirmationCode = Utils.generateUUID();
                UserRecoveryData recoveryDataDO = new UserRecoveryData(user.getUniqueUserId(), confirmationCode,
                                                                       RecoveryScenarios.SELF_SIGN_UP,
                                                                       RecoverySteps.CONFIRM_SIGN_UP);
                userRecoveryDataStore.store(recoveryDataDO);

                if (config.isNotificationInternallyManaged()) {

                    Property[] notificationProperties = (Property[]) eventProperties.get(SELF_SIGN_UP_PROPERTIES);
                    Utils.triggerNotification(eventService, user.getUniqueUserId(),
                                              NOTIFICATION_TYPE_ACCOUNT_CONFIRM, confirmationCode,
                                              notificationProperties);
                } else {
                    eventContext.addParameter(IdentityStoreConstants.UNIQUE_USED_ID, user.getUniqueUserId());
                    eventContext.addParameter(CONFIRMATION_CODE, confirmationCode);
                }
            } else {

                if (log.isDebugEnabled()) {
                    log.debug("Changing user lifecycle state to {} for user: {}", UserState
                            .UNLOCKED__UNVERIFIED.toString(), user.getUniqueUserId());
                }
                LifecycleOperationManager.executeLifecycleEvent(UserState.UNLOCKED__UNVERIFIED.toString(),
                                                                domainUID, domainUID, null);
                identityStore.setUserState(user.getUniqueUserId(), UserState.UNLOCKED__UNVERIFIED.toString());
            }
        } catch (LifecycleException e) {
            throw Utils.handleServerException(ErrorCodes.FAILED_LIFECYCLE_EVENT, user.getUniqueUserId(), e);
        } catch (UserNotFoundException e) {
            throw Utils.handleServerException(ErrorCodes.USER_NOT_FOUND, user.getUniqueUserId(), e);
        } catch (IdentityStoreException e) {
            throw Utils.handleServerException(ErrorCodes.FAILED_USER_STATE_UPDATE, null, e);
        }
    }

    private void handlePostAuthenticateEvent(Event event) throws IdentityRecoveryClientException {

        Map<String, Object> eventProperties = event.getEventProperties();

        AuthenticationContext authenticationContext = (AuthenticationContext) eventProperties.get
                (IdentityStoreConstants.AUTHENTICATION_CONTEXT);

        User user = authenticationContext.getUser();
        if (user != null && UserState.LOCKED_SELF_SIGN_UP.toString().equals(user.getState())) {
            throw Utils.handleClientException(ACCOUNT_UNVERIFIED, user.getUniqueUserId());
        }
    }

    @Override
    public int getPriority(MessageContext messageContext) {
        // TODO: To be removed after introducing handler config properties
        return -10;
    }

    /**
     * This is used to extract domainId from unique user id.
     *
     * @param uniqueEntityId Unique user id.
     * @return Domain Id.
     * @throws IdentityRecoveryServerException If the Unique ID is invalid.
     */
    private String getDecodedUserEntityId(String uniqueEntityId) throws IdentityRecoveryServerException {

        String[] decodedUniqueEntityIdParts = uniqueEntityId.split("\\.", 2);
        if (decodedUniqueEntityIdParts.length != 2 || StringUtils.isEmpty(decodedUniqueEntityIdParts[0]) ||
            StringUtils.isEmpty(decodedUniqueEntityIdParts[1])) {

            throw Utils.handleServerException(INVALID_USER_ID_FORMAT, uniqueEntityId);
        }
        return decodedUniqueEntityIdParts[1];
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }
}

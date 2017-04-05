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

package org.wso2.carbon.identity.recovery.signup;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.common.base.event.model.Event;
import org.wso2.carbon.identity.common.base.exception.IdentityException;
import org.wso2.carbon.identity.event.EventService;
import org.wso2.carbon.identity.mgt.Group;
import org.wso2.carbon.identity.mgt.RealmService;
import org.wso2.carbon.identity.mgt.User;
import org.wso2.carbon.identity.mgt.bean.GroupBean;
import org.wso2.carbon.identity.mgt.bean.UserBean;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.event.IdentityMgtMessageContext;
import org.wso2.carbon.identity.mgt.exception.GroupNotFoundException;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.mgt.impl.util.IdentityMgtConstants;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.RecoveryScenarios;
import org.wso2.carbon.identity.recovery.RecoverySteps;
import org.wso2.carbon.identity.recovery.bean.NotificationResponseBean;
import org.wso2.carbon.identity.recovery.mapping.SelfSignUpConfig;
import org.wso2.carbon.identity.recovery.model.Property;
import org.wso2.carbon.identity.recovery.model.UserRecoveryData;
import org.wso2.carbon.identity.recovery.store.JDBCRecoveryDataStore;
import org.wso2.carbon.identity.recovery.store.UserRecoveryDataStore;
import org.wso2.carbon.identity.recovery.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.mgt.constant.StoreConstants.IdentityStoreConstants;
import static org.wso2.carbon.identity.recovery.IdentityRecoveryConstants.ACCOUNT_LOCKED_CLAIM;
import static org.wso2.carbon.identity.recovery.IdentityRecoveryConstants.CONFIRMATION_CODE;
import static org.wso2.carbon.identity.recovery.IdentityRecoveryConstants.EMAIL_VERIFIED_CLAIM;
import static org.wso2.carbon.identity.recovery.IdentityRecoveryConstants.ErrorCodes;
import static org.wso2.carbon.identity.recovery.IdentityRecoveryConstants.NOTIFICATION_TYPE_ACCOUNT_CONFIRM;
import static org.wso2.carbon.identity.recovery.IdentityRecoveryConstants.SELF_SIGN_UP_EVENT;
import static org.wso2.carbon.identity.recovery.IdentityRecoveryConstants.SELF_SIGN_UP_PROPERTIES;

/**
 * Manager class which can be used for user self sign-up.
 */
public class UserSelfSignUpManager {

    private static final Logger log = LoggerFactory.getLogger(UserSelfSignUpManager.class);

    //TODO: Get the claim URI properly.
    private SelfSignUpConfig config = new SelfSignUpConfig();
    private RealmService realmService;
    private EventService eventService;

    /**
     * Manager class for user self sign-up service.
     */
    public UserSelfSignUpManager(RealmService realmService, EventService eventService) {

        config.setSelfSignUpEnabled(true);
        config.setSelfSignUpGroupName("ssu");
        config.setNotificationInternallyManaged(false);

        this.realmService = realmService;
        this.eventService = eventService;
    }

    /**
     * Register self sign-up user.
     *
     * @param userBean User to be registered.
     * @param domainName User domain.
     * @param properties Properties to be sent to the self sign-up handler.
     * @return NotificationResponseBean. If notification is internally managed, the confirmation code will be null in
     * the NotificationResponseBean.
     * @throws IdentityRecoveryException If error occurs while user registration.
     */
    public NotificationResponseBean registerUser(UserBean userBean, String domainName, Property[] properties) throws
            IdentityRecoveryException {

        if (!config.isSelfSignUpEnabled()) {
            throw Utils.handleClientException(ErrorCodes.DISABLE_SELF_SIGN_UP, null);
        }

        User user;

        // Add user to self sign-up role.
        try {

            List<String> groupIds = new ArrayList<>();
            groupIds.add(getSelfSignUpGroupId(domainName));
            user = realmService.getIdentityStore().addUser(userBean, groupIds, domainName);
        } catch (IdentityStoreException e) {
            throw Utils.handleServerException(ErrorCodes.SELF_SIGN_UP_STORE_ERROR, null, e);
        }

        //TODO:Add User Object.
        NotificationResponseBean notificationResponseBean = new NotificationResponseBean(user.getUniqueUserId());
        notificationResponseBean.setUser(user);

        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext();

        Map<String, Object> props = new HashMap<>();
        props.put(IdentityStoreConstants.USER, user);
        props.put(SELF_SIGN_UP_PROPERTIES, properties);

        Event event = new Event(SELF_SIGN_UP_EVENT, props);

        try {

            eventService.pushEvent(event, messageContext);

            String confirmationCode = (String) messageContext.getParameter(CONFIRMATION_CODE);

            // If notification internally managed, the confirmation code will be null.
            notificationResponseBean.setCode(confirmationCode);
        } catch (IdentityException e) {
            throw Utils.handleServerException(ErrorCodes.TRIGGER_NOTIFICATION,
                                              user.getUniqueUserId(), e);
        }
        return notificationResponseBean;
    }

    /**
     * Check whether user is already confirmed or not.
     *
     * @param uniqueUserId Unique ID of the user.
     * @return True if the user is already confirmed.
     * @throws IdentityRecoveryException If error occurs while user registration.
     */
    public boolean isUserConfirmed(String uniqueUserId) throws IdentityRecoveryException {

        if (StringUtils.isBlank(uniqueUserId)) {

            Utils.handleClientException(ErrorCodes.INVALID_USER_ID, "empty userID");
        }

        UserRecoveryDataStore userRecoveryDataStore = JDBCRecoveryDataStore.getInstance();
        UserRecoveryData recoveryUser = userRecoveryDataStore.loadByUserUniqueId(uniqueUserId);

        boolean isUserConfirmed = false;

        if (recoveryUser == null || !RecoveryScenarios.SELF_SIGN_UP.equals(recoveryUser.getRecoveryScenario())) {
            isUserConfirmed = true;
        }
        return isUserConfirmed;
    }

    /**
     * Confirm self sign-up of a user.
     *
     * @param code Confirmation code to be validated.
     * @throws IdentityRecoveryException If error occurs while user registration.
     */
    public void confirmUserSelfSignUp(String code) throws IdentityRecoveryException {

        if (!config.isSelfSignUpEnabled()) {
            throw Utils.handleClientException(ErrorCodes.DISABLE_SELF_SIGN_UP);
        }

        UserRecoveryDataStore userRecoveryDataStore = JDBCRecoveryDataStore.getInstance();

        // If load method returns data, it means the code is validated. Otherwise it returns exceptions.
        UserRecoveryData recoveryData = userRecoveryDataStore.loadByCode(code);
        String uniqueId = recoveryData.getUserUniqueId();

        if (!RecoverySteps.CONFIRM_SIGN_UP.equals(recoveryData.getRecoveryStep())) {
            throw Utils.handleClientException(ErrorCodes.INVALID_CODE, code);
        }

        try {

            Map<String, String> claims = new HashMap<>();
            claims.put(ACCOUNT_LOCKED_CLAIM, Boolean.FALSE.toString());

            if (config.isNotificationInternallyManaged()) {
                claims.put(EMAIL_VERIFIED_CLAIM, Boolean.TRUE.toString());
            }
            Utils.setClaimsInIdentityStore(realmService.getIdentityStore(), uniqueId, claims, null);
            // Invalidate code
            userRecoveryDataStore.invalidateByCode(code);
        } catch (UserNotFoundException e) {
            throw Utils.handleServerException(ErrorCodes.USER_NOT_FOUND, uniqueId, e);
        } catch (IdentityStoreException e) {
            throw Utils.handleServerException(ErrorCodes.FAILED_TO_UPDATE_USER_CLAIMS, uniqueId, e);
        }
    }

    /**
     * Resend account confirmation code.
     *
     * @param claim A unique user claim.
     * @param domainName User domain.
     * @param properties Properties to be sent to the self sign-up handler.
     * @return NotificationResponseBean. If notification is internally managed, the confirmation code will be null in
     * the NotificationResponseBean.
     * @throws IdentityRecoveryException
     */
    public NotificationResponseBean resendConfirmationCode(Claim claim, String domainName, Property[] properties) throws
            IdentityRecoveryException {

        if (!config.isSelfSignUpEnabled()) {
            throw Utils.handleClientException(ErrorCodes.DISABLE_SELF_SIGN_UP);
        }

        String uniqueUserId = Utils.getUniqueUserId(claim, domainName);

        NotificationResponseBean notificationResponseBean = new NotificationResponseBean(uniqueUserId);
        UserRecoveryDataStore userRecoveryDataStore = JDBCRecoveryDataStore.getInstance();
        UserRecoveryData userRecoveryData = userRecoveryDataStore.loadByUserUniqueId(uniqueUserId);

        if (userRecoveryData == null || StringUtils.isBlank(userRecoveryData.getCode()) || !RecoverySteps
                .CONFIRM_SIGN_UP.equals(userRecoveryData.getRecoveryStep())) {
            throw Utils.handleClientException(ErrorCodes.OLD_CODE_NOT_FOUND);
        }
        // Invalid old code
        userRecoveryDataStore.invalidateByCode(userRecoveryData.getCode());

        String code = Utils.generateUUID();
        UserRecoveryData updatedRecoveryData = new UserRecoveryData(uniqueUserId, code, RecoveryScenarios
                .SELF_SIGN_UP, RecoverySteps.CONFIRM_SIGN_UP);

        userRecoveryDataStore.store(updatedRecoveryData);

        if (config.isNotificationInternallyManaged()) {
            Utils.triggerNotification(eventService, uniqueUserId, NOTIFICATION_TYPE_ACCOUNT_CONFIRM, code,
                                      properties);
        } else {
            notificationResponseBean.setUserUniqueId(uniqueUserId);
            notificationResponseBean.setCode(code);
        }
        return notificationResponseBean;
    }

    private String getSelfSignUpGroupId(String domainName) throws IdentityRecoveryException {

        Claim claim = new Claim(IdentityMgtConstants.CLAIM_ROOT_DIALECT, IdentityMgtConstants.GROUP_NAME_CLAIM,
                                config.getSelfSignUpGroupName());
        Group ssuGroup = null;
        try {
            ssuGroup = realmService.getIdentityStore().getGroup(claim, domainName);
        } catch (IdentityStoreException e) {
            throw Utils.handleServerException(ErrorCodes.FAILED_SSU_GROUP_SEARCH,
                                              config.getSelfSignUpGroupName() + " in domain: " + domainName, e);
        } catch (GroupNotFoundException e) {

            if (log.isDebugEnabled()) {
                log.error("Self sign-up group: " + config.getSelfSignUpGroupName() + " not found in domain: " +
                          domainName, e);
            }
        }

        if (ssuGroup == null) {

            if (log.isDebugEnabled()) {
                log.debug("Creating self sign-up group: " + config.getSelfSignUpGroupName() + " in domain: " +
                          domainName);
            }

            GroupBean groupBean = new GroupBean();
            List<Claim> claims = Arrays.asList(claim);
            groupBean.setClaims(claims);

            try {

                ssuGroup = realmService.getIdentityStore().addGroup(groupBean);

                if (log.isDebugEnabled()) {
                    log.debug("Self sign-up group: {} created in domain: {}", config.getSelfSignUpGroupName(),
                              domainName);
                }
            } catch (IdentityStoreException e) {
                throw Utils.handleServerException(ErrorCodes.FAILED_SSU_GROUP_ADD,
                                                  config.getSelfSignUpGroupName() + " to domain: " + domainName, e);
            }
        }
        return ssuGroup.getUniqueGroupId();
    }
}

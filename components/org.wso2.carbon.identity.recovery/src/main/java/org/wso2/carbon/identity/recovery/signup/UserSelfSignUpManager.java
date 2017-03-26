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
import org.wso2.carbon.identity.recovery.IdentityRecoveryClientException;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.mgt.constant.StoreConstants.IdentityStoreConstants;
import static org.wso2.carbon.identity.recovery.IdentityRecoveryConstants.ACCOUNT_LOCKED_CLAIM;
import static org.wso2.carbon.identity.recovery.IdentityRecoveryConstants.CONFIRMATION_CODE;
import static org.wso2.carbon.identity.recovery.IdentityRecoveryConstants.EMAIL_VERIFIED_CLAIM;
import static org.wso2.carbon.identity.recovery.IdentityRecoveryConstants.ErrorMessages;
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

    public NotificationResponseBean registerUser(UserBean userBean, String domainName, Property[] properties) throws
            IdentityRecoveryException {

        if (!config.isSelfSignUpEnabled()) {
            throw Utils.handleClientException(ErrorMessages.ERROR_CODE_DISABLE_SELF_SIGN_UP, null);
        }

        User user;

        // Add user to self sign-up role.
        try {

            List<String> groupIds = new ArrayList<>();
            groupIds.add(getSelfSignUpGroupId(domainName));
            user = realmService.getIdentityStore().addUser(userBean, groupIds, domainName);
        } catch (IdentityStoreException e) {
            throw Utils.handleServerException(ErrorMessages.ERROR_CODE_SELF_SIGN_UP_STORE_ERROR, null, e);
        }

        NotificationResponseBean notificationResponseBean = new NotificationResponseBean(user.getUniqueUserId());


        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext();

        Map<String, Object> props = new HashMap<>();
        props.put(IdentityStoreConstants.USER, user);
        props.put(SELF_SIGN_UP_PROPERTIES, properties);

        Event event = new Event(SELF_SIGN_UP_EVENT, props);

        try {

            eventService.pushEvent(event, messageContext);

            String confirmationCode = (String) messageContext.getParameter(CONFIRMATION_CODE);

            if (StringUtils.isNotEmpty(confirmationCode)) {
                notificationResponseBean.setKey(confirmationCode);
            }
        } catch (IdentityException e) {
            throw Utils.handleServerException(ErrorMessages.ERROR_CODE_TRIGGER_NOTIFICATION,
                                              user.getUniqueUserId(), e);
        }
        return notificationResponseBean;
    }

    /**
     * Check whether user is already confirmed or not.
     *
     * @param uniqueUserId Unique ID of the user.
     * @return True if the user is already confirmed.
     * @throws IdentityRecoveryException If the
     */
    public boolean isUserConfirmed(String uniqueUserId) throws IdentityRecoveryException {

        if (StringUtils.isBlank(uniqueUserId)) {

            throw new IdentityRecoveryClientException(ErrorMessages.ERROR_CODE_INVALID_USER_ID.getCode(),
                                                      ErrorMessages.ERROR_CODE_INVALID_USER_ID.getMessage());
        }

        UserRecoveryDataStore userRecoveryDataStore = JDBCRecoveryDataStore.getInstance();
        UserRecoveryData load = userRecoveryDataStore.loadByUserUniqueId(uniqueUserId);

        boolean isUserConfirmed = false;

        if (load == null || !RecoveryScenarios.SELF_SIGN_UP.equals(load.getRecoveryScenario())) {
            isUserConfirmed = true;
        }
        return isUserConfirmed;
    }

    public void confirmUserSelfSignUp(String code) throws IdentityRecoveryException {

        UserRecoveryDataStore userRecoveryDataStore = JDBCRecoveryDataStore.getInstance();

        // If load method returns data, it means the code is validated. Otherwise it returns exceptions.
        UserRecoveryData recoveryData = userRecoveryDataStore.loadByCode(code);
        String uniqueId = recoveryData.getUserUniqueId();

        if (!config.isSelfSignUpEnabled()) {
            throw Utils.handleClientException(ErrorMessages.ERROR_CODE_DISABLE_SELF_SIGN_UP,
                                              uniqueId);
        }

        if (!RecoverySteps.CONFIRM_SIGN_UP.equals(recoveryData.getRecoveryStep())) {
            throw Utils.handleClientException(
                    ErrorMessages.ERROR_CODE_INVALID_CODE, null);
        }

        try {

            Map<String, String> claims = new HashMap<>();

            claims.put(ACCOUNT_LOCKED_CLAIM, Boolean.FALSE.toString());

            //String domainUID = getDecodedUserEntityId(uniqueId);
            if (config.isNotificationInternallyManaged()) {
                claims.put(EMAIL_VERIFIED_CLAIM, Boolean.TRUE.toString());
            }

            Utils.setClaimsInIdentityStore(uniqueId, claims, null);

            // Invalidate code
            userRecoveryDataStore.invalidateByCode(code);
        } catch (UserNotFoundException | IdentityStoreException e) {
            throw Utils.handleServerException(ErrorMessages.ERROR_CODE_UNLOCK_USER,
                                              uniqueId, e);
        }

    }

    public NotificationResponseBean resendConfirmationCode(Claim claim, String domainName, Property[] properties) throws
            IdentityRecoveryException {

        if (!config.isSelfSignUpEnabled()) {
            throw Utils.handleClientException(ErrorMessages.ERROR_CODE_DISABLE_SELF_SIGN_UP, claim.getValue());
        }

        String uniqueUserId = Utils.getUniqueUserId(claim, domainName);

        NotificationResponseBean notificationResponseBean = new NotificationResponseBean(uniqueUserId);
        UserRecoveryDataStore userRecoveryDataStore = JDBCRecoveryDataStore.getInstance();
        UserRecoveryData userRecoveryData = userRecoveryDataStore.loadByUserUniqueId(uniqueUserId);

        if (userRecoveryData == null || StringUtils.isBlank(userRecoveryData.getSecret()) || !RecoverySteps
                .CONFIRM_SIGN_UP.equals(userRecoveryData.getRecoveryStep())) {
            throw Utils.handleClientException(ErrorMessages.ERROR_CODE_OLD_CODE_NOT_FOUND, null);
        }
        // Invalid old code
        userRecoveryDataStore.invalidateByCode(userRecoveryData.getSecret());

        String secretKey = Utils.generateUUID();
        UserRecoveryData recoveryDataDO = new UserRecoveryData(uniqueUserId, secretKey, RecoveryScenarios
                .SELF_SIGN_UP, RecoverySteps.CONFIRM_SIGN_UP);

        userRecoveryDataStore.store(recoveryDataDO);

        if (config.isNotificationInternallyManaged()) {
            Utils.triggerNotification(uniqueUserId, NOTIFICATION_TYPE_ACCOUNT_CONFIRM, secretKey, properties);
        } else {
            notificationResponseBean.setUserUniqueId(uniqueUserId);
            notificationResponseBean.setKey(secretKey);
        }
        return notificationResponseBean;
    }

    private String getSelfSignUpGroupId(String domainName) throws IdentityRecoveryException {

        Claim claim = new Claim(IdentityMgtConstants.CLAIM_ROOT_DIALECT, IdentityMgtConstants.GROUP_NAME_CLAIM,
                                config.getSelfSignUpGroupName());
        Group ssuGroup;
        try {
            ssuGroup = realmService.getIdentityStore().getGroup(claim, domainName);
        } catch (IdentityStoreException e) {
            throw Utils.handleServerException(ErrorMessages.ERROR_CODE_FAILED_SSU_GROUP_SEARCH,
                                              config.getSelfSignUpGroupName() + " in domain: " + domainName, e);
        } catch (GroupNotFoundException e) {

            if (log.isDebugEnabled()) {
                log.error("Self sign-up group: " + config.getSelfSignUpGroupName() + " not found in domain: " +
                          domainName, e);
            }
            ssuGroup = null;
        }

        if (ssuGroup == null) {

            if (log.isDebugEnabled()) {
                log.debug("Creating self sign-up group: " + config.getSelfSignUpGroupName() + " in domain: " +
                          domainName);
            }

            GroupBean groupBean = new GroupBean();

            List<Claim> claims = new ArrayList<>();
            claims.add(claim);
            groupBean.setClaims(claims);

            try {

                ssuGroup = realmService.getIdentityStore().addGroup(groupBean);

                if (log.isDebugEnabled()) {
                    log.debug("Self sign-up group: " + config.getSelfSignUpGroupName() + " created in domain: " +
                              domainName);
                }

            } catch (IdentityStoreException e) {
                throw Utils.handleServerException(ErrorMessages.ERROR_CODE_FAILED_SSU_GROUP_ADD,
                                                  config.getSelfSignUpGroupName() + " to domain: " + domainName, e);
            }
        }
        return ssuGroup.getUniqueGroupId();
    }
}

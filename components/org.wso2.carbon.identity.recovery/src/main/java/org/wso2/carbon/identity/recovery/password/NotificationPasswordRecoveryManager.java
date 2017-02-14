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
package org.wso2.carbon.identity.recovery.password;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.common.base.event.EventContext;
import org.wso2.carbon.identity.common.base.event.model.Event;
import org.wso2.carbon.identity.common.base.exception.IdentityException;
import org.wso2.carbon.identity.event.EventConstants;
import org.wso2.carbon.identity.mgt.IdentityStore;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.recovery.IdentityRecoveryConstants;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.RecoveryScenarios;
import org.wso2.carbon.identity.recovery.RecoverySteps;
import org.wso2.carbon.identity.recovery.bean.NotificationResponseBean;
import org.wso2.carbon.identity.recovery.internal.IdentityRecoveryServiceDataHolder;
import org.wso2.carbon.identity.recovery.model.UserRecoveryData;
import org.wso2.carbon.identity.recovery.store.JDBCRecoveryDataStore;
import org.wso2.carbon.identity.recovery.store.UserRecoveryDataStore;
import org.wso2.carbon.identity.recovery.util.Utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;
import javax.security.auth.callback.PasswordCallback;


/**
 * Manager class which can be used to recover passwords using a notification
 */
public class NotificationPasswordRecoveryManager {

    private static final Logger log = LoggerFactory.getLogger(NotificationPasswordRecoveryManager.class);

    private static NotificationPasswordRecoveryManager instance = new NotificationPasswordRecoveryManager();

    private NotificationPasswordRecoveryManager() {

    }

    public static NotificationPasswordRecoveryManager getInstance() {
        return instance;
    }

    public NotificationResponseBean sendRecoveryNotification(String userUniqueId, Boolean notify)
            throws IdentityRecoveryException {
        boolean isNotificationInternallyManage;

        if (notify == null) {
            isNotificationInternallyManage = IdentityRecoveryServiceDataHolder.getInstance().getRecoveryLinkConfig()
                    .isNotificationInternallyManage();
        } else {
            isNotificationInternallyManage = notify.booleanValue();
        }

        //todo need to check whether user account is disabled or locked
        //        if (Utils.isAccountDisabled(user)) {
        //            throw Utils.handleClientException(
        //                    IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_DISABLED_ACCOUNT, user.getUserName());
        //        } else if (Utils.isAccountLocked(user)) {
        //            throw Utils.handleClientException(
        //                    IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_LOCKED_ACCOUNT, user.getUserName());
        //        }

        UserRecoveryDataStore userRecoveryDataStore = JDBCRecoveryDataStore.getInstance();
        userRecoveryDataStore.invalidateByUserUniqueId(userUniqueId);

        String secretKey = UUID.randomUUID().toString();
        UserRecoveryData recoveryDataDO = new UserRecoveryData(userUniqueId, secretKey,
                RecoveryScenarios.NOTIFICATION_BASED_PW_RECOVERY, RecoverySteps.UPDATE_PASSWORD);

        userRecoveryDataStore.store(recoveryDataDO);
        NotificationResponseBean notificationResponseBean = new NotificationResponseBean(userUniqueId);

        if (isNotificationInternallyManage) {
            triggerNotification(userUniqueId, IdentityRecoveryConstants.NOTIFICATION_TYPE_PASSWORD_RESET, secretKey);
        } else {
            notificationResponseBean.setKey(secretKey);
        }

        return notificationResponseBean;
    }

    public void updatePassword(String code, char[] newPassword)
            throws IdentityRecoveryException {
        UserRecoveryDataStore userRecoveryDataStore = JDBCRecoveryDataStore.getInstance();
        UserRecoveryData userRecoveryData = userRecoveryDataStore.loadByCode(code);
        //if return data from load method, it means the code is validated. Otherwise it returns exceptions

        if (!RecoverySteps.UPDATE_PASSWORD.equals(userRecoveryData.getRecoveryStep())) {
            throw Utils.handleClientException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_INVALID_CODE, null);
        }

        try {
            PasswordCallback passwordCallback = new PasswordCallback("password", false);
            passwordCallback.setPassword(newPassword);
            IdentityStore identityStore = IdentityRecoveryServiceDataHolder.getInstance().getRealmService()
                    .getIdentityStore();
            identityStore.updateUserCredentials(userRecoveryData.getUserUniqueId(),
                    Collections.singletonList(passwordCallback));

        } catch (UserNotFoundException e) {
            throw Utils.handleServerException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_UNEXPECTED, null, e);
        } catch (IdentityStoreException e) {
            throw Utils.handleServerException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_UNEXPECTED, null, e);
        }

        userRecoveryDataStore.invalidateByCode(code);
        boolean isNotificationInternallyManaged = IdentityRecoveryServiceDataHolder.getInstance()
                .getRecoveryLinkConfig().isNotificationInternallyManage();
        boolean isNotificationSendWhenSuccess = IdentityRecoveryServiceDataHolder.getInstance().getRecoveryLinkConfig()
                .isSendRecoveryNotificationSuccess();

        if (isNotificationInternallyManaged && isNotificationSendWhenSuccess) {
            try {
                triggerNotification(userRecoveryData.getUserUniqueId(),
                        IdentityRecoveryConstants.NOTIFICATION_TYPE_PASSWORD_RESET_SUCCESS, null);
            } catch (Exception e) {
                log.warn("Error while sending password reset success notification to user :" + userRecoveryData
                        .getUserUniqueId());
            }
        }
    }

    private void triggerNotification(String userUniqueId, String type, String code)
            throws IdentityRecoveryException {
        String eventName = EventConstants.Event.TRIGGER_NOTIFICATION;
        HashMap<String, Object> properties = new HashMap<>();
        properties.put(EventConstants.EventProperty.USER_UNIQUE_ID, userUniqueId);

        if (StringUtils.isNotBlank(code)) {
            properties.put(IdentityRecoveryConstants.CONFIRMATION_CODE, code);
        }
        //todo need check additional properties
//        if (metaProperties != null) {
//            for (Property metaProperty : metaProperties) {
//                if (StringUtils.isNotBlank(metaProperty.getValue()) && StringUtils.isNotBlank(metaProperty.getKey()))
//                {
//                    properties.put(metaProperty.getKey(), metaProperty.getValue());
//                }
//            }
//        }

        properties.put(IdentityRecoveryConstants.TEMPLATE_TYPE, type);
        Event identityMgtEvent = new Event(eventName, properties);
        EventContext eventContext = new EventContext(null);

        try {
            IdentityRecoveryServiceDataHolder.getInstance().getIdentityEventService().pushEvent(identityMgtEvent,
                                                                                                eventContext);
        } catch (IdentityException e) {
            throw Utils.handleServerException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_TRIGGER_NOTIFICATION,
                    userUniqueId, e);
        }
    }

}

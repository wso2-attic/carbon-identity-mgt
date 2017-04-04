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
package org.wso2.carbon.identity.recovery.handler;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.common.base.event.EventContext;
import org.wso2.carbon.identity.common.base.event.model.Event;
import org.wso2.carbon.identity.common.base.exception.IdentityException;
import org.wso2.carbon.identity.common.base.handler.InitConfig;
import org.wso2.carbon.identity.event.AbstractEventHandler;
import org.wso2.carbon.identity.event.EventConstants;
import org.wso2.carbon.identity.mgt.User;
import org.wso2.carbon.identity.mgt.constant.StoreConstants.IdentityStoreConstants;
import org.wso2.carbon.identity.mgt.constant.StoreConstants.IdentityStoreInterceptorConstants;
import org.wso2.carbon.identity.recovery.IdentityRecoveryConstants;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.RecoveryScenarios;
import org.wso2.carbon.identity.recovery.RecoverySteps;
import org.wso2.carbon.identity.recovery.internal.IdentityRecoveryServiceDataHolder;
import org.wso2.carbon.identity.recovery.model.UserRecoveryData;
import org.wso2.carbon.identity.recovery.store.JDBCRecoveryDataStore;
import org.wso2.carbon.identity.recovery.store.UserRecoveryDataStore;
import org.wso2.carbon.identity.recovery.util.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handler for User On boarding - Email Verification and Ask Password.
 */
public class AskPasswordEmailHandler extends AbstractEventHandler {

    private static final Logger log = LoggerFactory.getLogger(AskPasswordEmailHandler.class);

    @Override
    public void configure(InitConfig initConfig) throws IdentityException {
    }

    @Override
    public String getName() {
        return "ask.password.using.email";
    }

    @Override
    public void handle(EventContext eventContext, Event event) throws IdentityException {

        Map<String, Object> eventProperties = event.getEventProperties();
        User user = (User) eventProperties.get(IdentityStoreConstants.USER);
        String userUniqueId = user.getUniqueUserId();
        UserRecoveryDataStore userRecoveryDataStore = JDBCRecoveryDataStore.getInstance();
        userRecoveryDataStore.invalidateByUserUniqueId(userUniqueId);
        String secretKey = UUID.randomUUID().toString();
        UserRecoveryData recoveryDataDO = new UserRecoveryData(userUniqueId, secretKey,
                RecoveryScenarios.ASK_PASSWORD, RecoverySteps.UPDATE_PASSWORD);
        userRecoveryDataStore.store(recoveryDataDO);
        //TODO trigger this, if (userbean.containProperty(askPasswordEnable)) after the C5 kernel release.
        if (IdentityStoreInterceptorConstants.POST_ADD_USER.equals(event.getEventName()) ||
                IdentityStoreInterceptorConstants.POST_ADD_USER_BY_DOMAIN.equals(event.getEventName())) {
            triggerNotification(userUniqueId, IdentityRecoveryConstants.NOTIFICATION_TYPE_ASK_PASSWORD, user,
                    secretKey);
        }
    }

    private void triggerNotification(String userUniqueId, String type, User user, String code)
            throws IdentityRecoveryException {

        HashMap<String, Object> properties = new HashMap<>();
        properties.put(EventConstants.EventProperty.USER_UNIQUE_ID, userUniqueId);
        if (StringUtils.isNotBlank(code)) {
            properties.put(IdentityRecoveryConstants.CONFIRMATION_CODE, code);
        }
        //TODO add domain if needed
        properties.put(IdentityRecoveryConstants.TEMPLATE_TYPE, type);
        Event identityMgtEvent = new Event(EventConstants.Event.TRIGGER_NOTIFICATION, properties);
        EventContext eventContext = new EventContext();
        try {
            IdentityRecoveryServiceDataHolder.getInstance().getIdentityEventService().pushEvent(identityMgtEvent,
                    eventContext);
        } catch (IdentityException e) {
            throw Utils.handleServerException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_TRIGGER_NOTIFICATION,
                    userUniqueId, e);
        }
    }
}

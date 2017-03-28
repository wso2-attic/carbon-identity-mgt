package org.wso2.carbon.identity.recovery.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.common.base.event.EventContext;
import org.wso2.carbon.identity.common.base.event.model.Event;
import org.wso2.carbon.identity.common.base.exception.IdentityException;
import org.wso2.carbon.identity.event.EventConstants;

import org.wso2.carbon.identity.recovery.IdentityRecoveryConstants;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.RecoveryScenarios;
import org.wso2.carbon.identity.recovery.RecoverySteps;
import org.wso2.carbon.identity.recovery.internal.IdentityRecoveryServiceDataHolder;
import org.wso2.carbon.identity.recovery.model.UserRecoveryData;
import org.wso2.carbon.identity.recovery.store.JDBCRecoveryDataStore;
import org.wso2.carbon.identity.recovery.store.UserRecoveryDataStore;

import java.util.HashMap;


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

/**
 * The util to keep admin initiated password resets
 */
public class NotificationUtil {
    private static final String TEMPLATE_TYPE = "TEMPLATE_TYPE";
    private static Logger log = LoggerFactory.getLogger(NotificationUtil.class);

    /**
     * triggers email
     *
     * @param userUniqueId selected user id
     * @param templateType template type
     * @param code         generated code
     * @throws IdentityRecoveryException
     */
    public static void triggerNotification(String userUniqueId, String templateType, String code)
            throws IdentityRecoveryException {
        String eventName = EventConstants.Event.TRIGGER_NOTIFICATION;
        HashMap<String, Object> properties = new HashMap<>();
        properties.put(EventConstants.EventProperty.USER_UNIQUE_ID, userUniqueId);
        properties.put(TEMPLATE_TYPE, templateType);
        if (StringUtils.isNotBlank(code)) {
            properties.put(IdentityRecoveryConstants.CONFIRMATION_CODE, code);
        }
        properties.put(IdentityRecoveryConstants.TEMPLATE_TYPE, templateType);
        Event identityMgtEvent = new Event(eventName, properties);
        EventContext eventContext = new EventContext();

        try {
            IdentityRecoveryServiceDataHolder.getInstance().getIdentityEventService().pushEvent(identityMgtEvent,
                    eventContext);
        } catch (IdentityException e) {
            throw Utils.handleServerException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_TRIGGER_NOTIFICATION,
                    userUniqueId, e);
        }
    }

    /**
     * pass the otp to the store to persist it in the database
     *
     * @param userUniqueId selected user id
     * @param passCode     generated code
     * @param scenario     recoveryScenario
     * @throws IdentityRecoveryException
     */
    public static void persistPasscode(String userUniqueId, String passCode, RecoveryScenarios scenario) throws
            IdentityRecoveryException {
        UserRecoveryDataStore userRecoveryDataStore = JDBCRecoveryDataStore.getInstance();
        UserRecoveryData recoveryDataDO = new UserRecoveryData(userUniqueId, passCode,
                scenario, RecoverySteps.UPDATE_PASSWORD);
        userRecoveryDataStore.invalidateUserScenario(userUniqueId, String.valueOf(scenario));
        userRecoveryDataStore.store(recoveryDataDO);
        if (log.isDebugEnabled()) {
            log.debug("The generated code is persisted");
        }
    }
}

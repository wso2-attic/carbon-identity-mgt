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

package org.wso2.carbon.identity.policy.password.history;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.common.base.event.EventContext;
import org.wso2.carbon.identity.common.base.event.model.Event;
import org.wso2.carbon.identity.common.base.exception.IdentityException;
import org.wso2.carbon.identity.common.base.handler.InitConfig;
import org.wso2.carbon.identity.event.AbstractEventHandler;
import org.wso2.carbon.identity.event.EventException;
import org.wso2.carbon.identity.mgt.constant.StoreConstants;
import org.wso2.carbon.identity.policy.password.history.bean.PasswordHistoryBean;
import org.wso2.carbon.identity.policy.password.history.bean.ValidationResult;
import org.wso2.carbon.identity.policy.password.history.constants.PasswordHistoryConstants;
import org.wso2.carbon.identity.policy.password.history.exeption.IdentityPasswordHistoryException;
import org.wso2.carbon.identity.policy.password.history.store.PasswordHistoryDataStore;

import java.util.List;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;

/**
 * PasswordHistoryHandler class.
 */
public class PasswordHistoryHandler extends AbstractEventHandler {

    private PasswordHistoryBean passwordHistoryBean = new PasswordHistoryBean();
    private static Logger log = LoggerFactory.getLogger(PasswordHistoryHandler.class);
    private PasswordHistoryDataStore passwordHistoryDataStore = null;

    public PasswordHistoryHandler(PasswordHistoryDataStore passwordHistoryDataStore) {
        this.passwordHistoryDataStore = passwordHistoryDataStore;

    }

    @Override
    public void handle(EventContext eventContext, Event event) throws IdentityException {

        if (!passwordHistoryBean.isEnabled()) {
            log.debug("Password History Validation is Disabled");
            return;
        }

        if (StoreConstants.IdentityStoreInterceptorConstants.PRE_UPDATE_USER_CREDENTIALS_PATCH.equals(event.
                getEventName()) || StoreConstants.IdentityStoreInterceptorConstants.PRE_UPDATE_USER_CREDENTIALS_PUT.
                equals(event.getEventName())) {

            log.debug("Validating given Password against History in pre Update");
            handlePreUpdateCredentials(event);

        } else if (StoreConstants.IdentityStoreInterceptorConstants.POST_UPDATE_USER_CREDENTIALS_PATCH.
                equals(event.getEventName()) || StoreConstants.IdentityStoreInterceptorConstants.
                POST_UPDATE_USER_CREDENTIALS_PUT.equals(event.getEventName())) {

            log.debug("Storing new Password in post Update");
            handlePostUpdateCredentials(event);
        } else if (StoreConstants.IdentityStoreInterceptorConstants.POST_DELETE_USER.equals(event.getEventName())) {

            log.debug("Deleting associated Password History Entries...When Deleting user");
            handlePostDeleteUser(event);
        }

    }

    @Override
    public void configure(InitConfig initConfig) throws IdentityException {
    }

    @Override
    public String getName() {
        return "password.history.handler";
    }

    /**
     * Validate given password in pre update password
     * @param event
     * @throws IdentityException
     */
    private void handlePreUpdateCredentials(Event event) throws IdentityException {

        List<Callback> credentials = (List<Callback>) event.getEventProperties()
                .get(StoreConstants.IdentityStoreConstants.CREDENTIAL_LIST);

        if (credentials == null || credentials.isEmpty()) {
            return;
        }

        String uniqueUserId = (String) event.getEventProperties().get(
                StoreConstants.IdentityStoreConstants.UNIQUE_USED_ID);

        //extract new password from credentials call back list
        char[] password = ((PasswordCallback) credentials.get(0)).getPassword();

        try {
            ValidationResult validationResult = passwordHistoryDataStore.validate(uniqueUserId, password);
            if (!validationResult.isSuccess()) {

                Integer errorCode = validationResult.getErrorCode();

                if (PasswordHistoryConstants.ERROR_IN_COUNT.equals(errorCode)) {

                    throw new IdentityException(Integer.toString(PasswordHistoryConstants.ERROR_IN_COUNT),
                            validationResult.getMessage());

                } else if (PasswordHistoryConstants.ERROR_IN_TIME.equals(errorCode)) {

                    throw new IdentityException(Integer.toString(PasswordHistoryConstants.ERROR_IN_TIME),
                            validationResult.getMessage());
                }
            }
        } catch (IdentityPasswordHistoryException e) {
            throw new EventException("Error occurred while Validating Password History", e);
        }

    }

    /**
     * Store new password in post update password
     * @param event
     * @throws EventException
     */
    private void handlePostUpdateCredentials(Event event) throws EventException {

        List<Callback> credentials = (List<Callback>) event.getEventProperties()
                .get(StoreConstants.IdentityStoreConstants.CREDENTIAL_LIST);

        if (credentials == null || credentials.isEmpty()) {
            return;
        }

        String uniqueUserId = (String) event.getEventProperties().get(
                StoreConstants.IdentityStoreConstants.UNIQUE_USED_ID);

        //extract new password from credentials call back list
        char[] password = ((PasswordCallback) credentials.get(0)).getPassword();

        try {
            passwordHistoryDataStore.store(uniqueUserId, password);
        } catch (IdentityPasswordHistoryException e) {
            throw new EventException(e.getMessage());
        }

    }

    /**
     * Deleting old entries when deleting user
     * @param event
     * @throws EventException
     */
    private void handlePostDeleteUser(Event event) throws EventException {

        String uniqueUserId = (String) event.getEventProperties().get(
                StoreConstants.IdentityStoreConstants.UNIQUE_USED_ID);

        try {
            passwordHistoryDataStore.remove(uniqueUserId);
        } catch (IdentityPasswordHistoryException e) {
            throw new EventException(e.getMessage());
        }

    }


}



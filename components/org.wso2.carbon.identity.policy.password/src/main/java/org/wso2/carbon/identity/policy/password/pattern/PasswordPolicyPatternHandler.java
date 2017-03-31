/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.policy.password.pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.common.base.event.EventContext;
import org.wso2.carbon.identity.common.base.event.model.Event;
import org.wso2.carbon.identity.common.base.exception.IdentityException;
import org.wso2.carbon.identity.common.base.handler.InitConfig;
import org.wso2.carbon.identity.event.AbstractEventHandler;
import org.wso2.carbon.identity.mgt.constant.StoreConstants;
import org.wso2.carbon.identity.policy.password.pattern.bean.PasswordPolicyBean;
import org.wso2.carbon.identity.policy.password.pattern.bean.ValidationResult;
import org.wso2.carbon.identity.policy.password.pattern.registry.PolicyRegistry;
import java.util.List;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;


/**
 * Intercept update password and add user operations to check user entered password strength according to predefine
 * policies.
 */
public class PasswordPolicyPatternHandler extends AbstractEventHandler {

    private static Logger log = LoggerFactory.getLogger(PasswordPolicyPatternHandler.class);
    private PolicyRegistry policyRegistry = null;
    private static final int ERROR_CODE = 2000;
    private PasswordPolicyBean passwordPolicyBean = new PasswordPolicyBean();

    public PasswordPolicyPatternHandler(PolicyRegistry policyRegistry) {
        this.policyRegistry = policyRegistry;

    }

    @Override
    public void handle(EventContext eventContext, Event event) throws IdentityException {

        if (!passwordPolicyBean.isEnabled()) {
            log.debug("Password policy validation is disabled");
            return;
        }

        if (StoreConstants.IdentityStoreInterceptorConstants.PRE_UPDATE_USER_CREDENTIALS_PATCH.equals(event.
                getEventName()) || StoreConstants.IdentityStoreInterceptorConstants.PRE_UPDATE_USER_CREDENTIALS_PUT.
                equals(event.getEventName()) || StoreConstants.IdentityStoreInterceptorConstants.PRE_ADD_USER.
                equals(event.getEventName())) {

            log.debug("Validating given password against policy in pre update");
            handlePreUpdateCredentials(event);

        }

    }

    @Override
    public void configure(InitConfig initConfig) throws IdentityException {
    }

    @Override
    public String getName() {
        return "password.pattern.handler";
    }

    /**
     * Validate given password in pre update password
     * @param event
     * @throws IdentityException
     */
    private void handlePreUpdateCredentials(Event event) throws IdentityException {

        char[] password = getPassword(event);

        ValidationResult validationResult = policyRegistry.enforcePasswordPolicies(password);
        if (!validationResult.isSuccess()) {
            throw new IdentityException(Integer.toString(ERROR_CODE), validationResult.getMessage());
        }

    }

    /**
     * Extract password from event properties
     * @param event : event object
     * @return : password as char array
     */
    private char[] getPassword (Event event) {

        List<Callback> credentials = (List<Callback>) event.getEventProperties()
                .get(StoreConstants.IdentityStoreConstants.CREDENTIAL_LIST);

        if (credentials == null || credentials.isEmpty()) {
            return new char[0];
        }

        return ((PasswordCallback) credentials.get(0)).getPassword();

    }

}

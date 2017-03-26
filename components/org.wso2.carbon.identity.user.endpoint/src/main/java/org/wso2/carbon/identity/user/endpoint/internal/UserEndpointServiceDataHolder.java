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

package org.wso2.carbon.identity.user.endpoint.internal;

import org.wso2.carbon.identity.recovery.signup.UserSelfSignUpManager;

/**
 * Service reference holder for USerEndpoint service.
 */
public class UserEndpointServiceDataHolder {

    private UserSelfSignUpManager userSelfSignUpManager;

    private UserEndpointServiceDataHolder() {

    }

    private static final UserEndpointServiceDataHolder INSTANCE = new UserEndpointServiceDataHolder();

    public static UserEndpointServiceDataHolder getInstance() {
        return INSTANCE;
    }

    public void setUserSelfSignUpManager(UserSelfSignUpManager userSelfSignUpManager) {
        this.userSelfSignUpManager = userSelfSignUpManager;
    }

    public UserSelfSignUpManager getUserSelfSignUpManager() {
        return userSelfSignUpManager;
    }
}

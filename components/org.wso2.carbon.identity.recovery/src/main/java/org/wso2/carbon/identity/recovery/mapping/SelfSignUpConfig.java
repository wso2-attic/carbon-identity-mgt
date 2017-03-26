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

package org.wso2.carbon.identity.recovery.mapping;

/**
 * Config object for self sign-up feature.
 */
public class SelfSignUpConfig {


    private boolean notificationInternallyManaged = false;

    private boolean selfSignUpEnabled = true;

    private String selfSignUpGroupName = "ssu";

    private boolean accountLockOnCreation = true;

    public boolean isNotificationInternallyManaged() {
        return notificationInternallyManaged;
    }

    public void setNotificationInternallyManaged(boolean notificationInternallyManaged) {
        this.notificationInternallyManaged = notificationInternallyManaged;
    }

    public boolean isSelfSignUpEnabled() {
        return selfSignUpEnabled;
    }

    public void setSelfSignUpEnabled(boolean selfSignUpEnabled) {
        this.selfSignUpEnabled = selfSignUpEnabled;
    }

    public String getSelfSignUpGroupName() {
        return selfSignUpGroupName;
    }

    public void setSelfSignUpGroupName(String selfSignUpGroupName) {
        this.selfSignUpGroupName = selfSignUpGroupName;
    }

    public boolean isAccountLockOnCreation() {
        return accountLockOnCreation;
    }

    public void setAccountLockOnCreation(boolean accountLockOnCreation) {
        this.accountLockOnCreation = accountLockOnCreation;
    }
}

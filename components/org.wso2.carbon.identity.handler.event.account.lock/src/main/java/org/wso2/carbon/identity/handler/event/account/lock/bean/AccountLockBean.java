/*
 *
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.handler.event.account.lock.bean;

import java.io.Serializable;

/**
 * Bean that encapsulates the verification info.
 */
public class AccountLockBean implements Serializable {

    private static final long serialVersionUID = -2913500114444797062L;

    private boolean isEnabled = true;
    private boolean isNotificationInternallyManage = true;
    private int accountLockTimeInMinutes = 2;
    private int maxFailedAttempts = 3;
    private int lockedTimeRatio = 1;
    private boolean isAccountInactiveEnabled = true;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public boolean isNotificationInternallyManage() {
        return isNotificationInternallyManage;
    }

    public void setIsNotificationInternallyManage(boolean isNotificationInternallyManage) {
        this.isNotificationInternallyManage = isNotificationInternallyManage;
    }

    public int getAccountLockTimeInMinutes() {
        return accountLockTimeInMinutes;
    }

    public void setAccountLockTimeInMinutes(int accountLockTimeInMinutes) {
        this.accountLockTimeInMinutes = accountLockTimeInMinutes;
    }

    public int getMaxFailedAttempts() {
        return maxFailedAttempts;
    }

    public void setMaxFailedAttempts(int maxFailedAttempts) {
        this.maxFailedAttempts = maxFailedAttempts;
    }

    public int getLockedTimeRatio() {
        return lockedTimeRatio;
    }

    public void setLockedTimeRatio(int lockedTimeRatio) {
        this.lockedTimeRatio = lockedTimeRatio;
    }

    public boolean isAccountInactiveEnabled() {
        return isAccountInactiveEnabled;
    }

    public void setIsAccountInactiveEnabled(boolean isAccountInactiveEnabled) {
        this.isAccountInactiveEnabled = isAccountInactiveEnabled;
    }
}

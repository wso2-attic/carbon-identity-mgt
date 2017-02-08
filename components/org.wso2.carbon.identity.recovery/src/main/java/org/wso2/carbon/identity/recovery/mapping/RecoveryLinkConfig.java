/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
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

package org.wso2.carbon.identity.recovery.mapping;

/**
 *
 */
public class RecoveryLinkConfig {
    private boolean enableAPI = false;
    private boolean enablePortal = true;
    private int notificationExpiryTime = 3; //expiry time in minutes
    private boolean isNotificationInternallyManage = false;
    private boolean sendRecoveryNotificationSuccess = true;

    public boolean isEnableAPI() {
        return enableAPI;
    }

    public void setEnableAPI(boolean enableAPI) {
        this.enableAPI = enableAPI;
    }

    public boolean isEnablePortal() {
        return enablePortal;
    }

    public void setEnablePortal(boolean enablePortal) {
        this.enablePortal = enablePortal;
    }

    public int getNotificationExpiryTime() {
        return notificationExpiryTime;
    }

    public void setNotificationExpiryTime(int notificationExpiryTime) {
        this.notificationExpiryTime = notificationExpiryTime;
    }

    public boolean isNotificationInternallyManage() {
        return isNotificationInternallyManage;
    }

    public void setNotificationInternallyManage(boolean notificationInternallyManage) {
        this.isNotificationInternallyManage = notificationInternallyManage;
    }

    public boolean isSendRecoveryNotificationSuccess() {
        return sendRecoveryNotificationSuccess;
    }

    public void setSendRecoveryNotificationSuccess(boolean sendRecoveryNotificationSuccess) {
        this.sendRecoveryNotificationSuccess = sendRecoveryNotificationSuccess;
    }
}

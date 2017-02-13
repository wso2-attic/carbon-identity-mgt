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
 * Class to represent username recovery config
 */
public class UsernameConfig {
    private boolean enableAPI = false;
    private boolean enablePortal = true;
    private String url = "/user-portal/recovery/username";
    private UsernameRecoveryNotificationInternallyManagedConfig notificationInternallyManaged;

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public UsernameRecoveryNotificationInternallyManagedConfig
    getUsernameRecoveryNotificationInternallyManagedConfig() {
        return notificationInternallyManaged;
    }

    public void setUsernameRecoveryNotificationInternallyManagedConfig(
            UsernameRecoveryNotificationInternallyManagedConfig notificationInternallyManaged) {
        this.notificationInternallyManaged = notificationInternallyManaged;
    }

}

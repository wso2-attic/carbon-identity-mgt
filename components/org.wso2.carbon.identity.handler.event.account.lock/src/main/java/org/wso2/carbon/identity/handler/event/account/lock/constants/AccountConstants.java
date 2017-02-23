/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.handler.event.account.lock.constants;

/**
 * AccountConstants class
 */
public class AccountConstants {

    public static final String ACCOUNT_LOCKED_CLAIM = "http://wso2.org/claims/accountLocked";
    public static final String ACCOUNT_DISABLED_CLAIM = "http://wso2.org/claims/accountDisabled";
    public static final String ACCOUNT_UNLOCK_TIME_CLAIM = "http://wso2.org/claims/unlockTime";
    public static final String FAILED_LOGIN_ATTEMPTS_CLAIM = "http://wso2.org/claims/failedLoginAttempts";
    public static final String EMAIL_VERIFIED_CLAIM = "http://wso2.org/claims/emailVerifed";
    public static final String FAILED_LOGIN_LOCKOUT_COUNT_CLAIM = "http://wso2.org/claims/failedLoginLockoutCount";


    public static final String EMAIL_TEMPLATE_TYPE_ACC_LOCKED = "accountLock";
    public static final String EMAIL_TEMPLATE_TYPE_ACC_UNLOCKED = "accountUnLock";

    public static final String EMAIL_TEMPLATE_TYPE_ACC_DISABLED = "accountDisable";
    public static final String EMAIL_TEMPLATE_TYPE_ACC_ENABLED = "accountEnable";

}

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

package org.wso2.carbon.identity.handler.event.account.lock.exception;

import org.wso2.carbon.identity.common.base.exception.IdentityRuntimeException;

public class AccountLockRuntimeException extends IdentityRuntimeException {

    protected AccountLockRuntimeException(String errorDescription) {
        super(errorDescription);
    }

    protected AccountLockRuntimeException(String errorDescription, Throwable cause) {
        super(errorDescription, cause);
    }

    public static AccountLockRuntimeException error(String errorDescription) {
        return new AccountLockRuntimeException(errorDescription);
    }

    public static AccountLockRuntimeException error(String errorDescription, Throwable cause) {
        return new AccountLockRuntimeException(errorDescription, cause);
    }
}
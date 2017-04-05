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

package org.wso2.carbon.identity.recovery;

import org.wso2.carbon.identity.common.base.exception.IdentityRuntimeException;

/**
 * RuntimeException for identity recovery operations.
 */
public class IdentityRecoveryRuntimeException extends IdentityRuntimeException {

    private static final long serialVersionUID = 4719127174298398250L;

    public IdentityRecoveryRuntimeException(String message) {
        super(message);
    }

    public IdentityRecoveryRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdentityRecoveryRuntimeException(String errorCode, String message) {
        super(errorCode, message);
    }

    public IdentityRecoveryRuntimeException(String errorCode, String message, Throwable throwable) {
        super(errorCode, message, throwable);
    }
}

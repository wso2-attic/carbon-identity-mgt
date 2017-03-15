/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.policy.password.history.store;

import org.wso2.carbon.identity.policy.password.history.bean.ValidationResult;
import org.wso2.carbon.identity.policy.password.history.exeption.IdentityPasswordHistoryException;


/**
 * This interface provides to plug module for preferred persistence store.
 */
public interface PasswordHistoryDataStore {


    /**
     * Store new password in post update while deleting old unnecessary entries
     * @param uniqueUserId : unique user id
     * @param credential : password
     * @throws IdentityPasswordHistoryException : In case of DB level exception
     */
    void store(String uniqueUserId, char[] credential) throws IdentityPasswordHistoryException;

    /**
     * Remove entries when deleting user In case of DB level exception
     * @param uniqueUserId : unique user id
     * @throws IdentityPasswordHistoryException : In case of DB level exception
     */
    void remove(String uniqueUserId) throws IdentityPasswordHistoryException;

    /**
     * Validate password against history
     * @param uniqueUserId : unique user id
     * @param credential : password
     * @return Validation result contains reason to invalidate
     * @throws IdentityPasswordHistoryException : In case of DB level exception
     */
    ValidationResult validate(String uniqueUserId, char[] credential) throws IdentityPasswordHistoryException;
}

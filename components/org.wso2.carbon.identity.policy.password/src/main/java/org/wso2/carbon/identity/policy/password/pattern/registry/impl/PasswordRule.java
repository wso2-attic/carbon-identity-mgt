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
package org.wso2.carbon.identity.policy.password.pattern.registry.impl;

/**
 * Password rule definition
 */
interface PasswordRule {

    /**
     * Define rules to validate password
     * @param password
     * @return true if password valid according to rule
     */
    boolean passRule(char[] password);

    /**
     *
     * @return Error message in case password is not according to rule
     */
    String failMessage();
}

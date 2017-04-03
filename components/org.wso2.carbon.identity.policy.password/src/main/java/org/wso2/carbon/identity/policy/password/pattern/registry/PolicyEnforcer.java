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

package org.wso2.carbon.identity.policy.password.pattern.registry;

import org.wso2.carbon.identity.policy.password.pattern.bean.ValidationResult;

/**
 * This is the interface to be used by custom policy implementations such as password policy
 * enforcement.
 */
public interface PolicyEnforcer {

    /**
     * This method is used to enforce the policy forcing it to apply it and validate the outcome.
     * It returns true if the policy enforcement is successful and no violations have been occured.
     * A false return means the perticular policy have been violated and no more processing needs
     * to be done.
     *
     * @param args - arguments to the policy implementer. Order is implementation dependant.
     * @return - contains state true if policy enforcement success. false if violated with error message.
     */
    ValidationResult enforce(Object... args);

}

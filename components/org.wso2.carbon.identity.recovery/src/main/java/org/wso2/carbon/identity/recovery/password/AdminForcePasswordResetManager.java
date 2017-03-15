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

package org.wso2.carbon.identity.recovery.password;

import org.wso2.carbon.identity.common.util.IdentityUtils;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.RecoveryScenarios;
import org.wso2.carbon.identity.recovery.RecoverySteps;
import org.wso2.carbon.identity.recovery.model.UserRecoveryData;
import org.wso2.carbon.identity.recovery.store.JDBCRecoveryDataStore;
import org.wso2.carbon.identity.recovery.store.UserRecoveryDataStore;

/**
 * Manager class which can be used to reset passwords forcefully
 */
public class AdminForcePasswordResetManager {
    private static AdminForcePasswordResetManager instance = new AdminForcePasswordResetManager();

    public static AdminForcePasswordResetManager getInstance() {
        return instance;
    }

    /**
     * get the generated one time password
     *
     * @return auto-generated one time password
     */
    public String generateOTPValue() {
        return IdentityUtils.generateOTPValue();
    }

    /**
     * pass the otp to the store to persist it in the database
     *
     * @param userUniqueId selected user id
     * @param otp          generated code
     * @throws IdentityRecoveryException
     */
    public void persistOTP(String userUniqueId, String otp) throws IdentityRecoveryException {
        UserRecoveryDataStore userRecoveryDataStore = JDBCRecoveryDataStore.getInstance();
        UserRecoveryData recoveryDataDO = new UserRecoveryData(userUniqueId, otp,
                RecoveryScenarios.ADMIN_FORCED_PASSWORD_RESET_VIA_OTP, RecoverySteps.UPDATE_PASSWORD);
        userRecoveryDataStore.store(recoveryDataDO);
    }

}

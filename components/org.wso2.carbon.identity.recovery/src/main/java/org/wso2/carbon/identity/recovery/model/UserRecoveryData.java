/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.recovery.model;

import java.sql.Timestamp;

/**
 * This object represents an entry of the identity metadata database.
 */
public class UserRecoveryData {
    private String userUniqueId;
    private String code;
    private String remainingSetIds;
    private Timestamp timeCreated = null;

    private Enum recoveryScenario;
    private Enum recoveryStep;

    public UserRecoveryData(String userUniqueId, String code, Enum recoveryScenario, Enum recoveryStep) {
        this.userUniqueId = userUniqueId;
        this.code = code;
        this.recoveryScenario = recoveryScenario;
        this.recoveryStep = recoveryStep;
    }

    public UserRecoveryData(String userUniqueId, String code, Enum recoveryScenario) {
        this.userUniqueId = userUniqueId;
        this.code = code;
        this.recoveryScenario = recoveryScenario;
    }

    public String getRemainingSetIds() {
        return remainingSetIds;
    }

    public void setRemainingSetIds(String remainingSetIds) {
        this.remainingSetIds = remainingSetIds;
    }

    public String getCode() {
        return code;
    }

    public String getUserUniqueId() {

        return userUniqueId;
    }

    public Enum getRecoveryScenario() {
        return recoveryScenario;
    }

    public Enum getRecoveryStep() {
        return recoveryStep;
    }

    public void setRecoveryStep(Enum recoveryStep) {
        this.recoveryStep = recoveryStep;
    }

    public Timestamp getTimeCreated() {
        return new Timestamp(this.timeCreated.getTime());
    }

    public void setTimeCreated(Timestamp timeCreated) {
        this.timeCreated = new Timestamp(timeCreated.getTime());
    }
}

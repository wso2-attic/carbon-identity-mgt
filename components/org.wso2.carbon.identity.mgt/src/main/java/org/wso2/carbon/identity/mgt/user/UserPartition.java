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
 * limitations under the License.
 */

package org.wso2.carbon.identity.mgt.user;

/**
 * Connector User.
 */
public class UserPartition {

    private String connectorId;

    private String connectorUserId;

    private boolean identityStore;

    public UserPartition() {

    }

    public UserPartition(String connectorId, String connectorUserId, boolean identityStore) {

        this.connectorId = connectorId;
        this.connectorUserId = connectorUserId;
        this.identityStore = identityStore;
    }

    public String getConnectorId() {
        return connectorId;
    }

    public void setConnectorId(String connectorId) {
        this.connectorId = connectorId;
    }

    public String getConnectorUserId() {
        return connectorUserId;
    }

    public void setConnectorUserId(String connectorUserId) {
        this.connectorUserId = connectorUserId;
    }

    public boolean isIdentityStore() {
        return identityStore;
    }

    public void setIdentityStore(boolean identityStore) {
        this.identityStore = identityStore;
    }

    @Override
    public String toString() {
        return "Connector Id - " + connectorId + " : Connector User Id - " + connectorUserId;
    }
}

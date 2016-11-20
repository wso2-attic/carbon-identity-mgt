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

package org.wso2.carbon.identity.mgt.internal.config.connector;

import java.util.Collections;
import java.util.List;

/**
 * Connector Config File.
 *
 * @since 1.0.0
 */
public class ConnectorConfigFile {

    private List<ConnectorConfigEntry> identityStoreConnectors;

    private List<ConnectorConfigEntry> credentialStoreConnectors;

    public List<ConnectorConfigEntry> getIdentityStoreConnectors() {

        if (identityStoreConnectors == null) {
            return Collections.emptyList();
        }
        return identityStoreConnectors;
    }

    public void setIdentityStoreConnectors(List<ConnectorConfigEntry> identityStoreConnectors) {
        this.identityStoreConnectors = identityStoreConnectors;
    }

    public List<ConnectorConfigEntry> getCredentialStoreConnectors() {

        if (credentialStoreConnectors == null) {
            return Collections.emptyList();
        }
        return credentialStoreConnectors;
    }

    public void setCredentialStoreConnectors(List<ConnectorConfigEntry> credentialStoreConnectors) {
        this.credentialStoreConnectors = credentialStoreConnectors;
    }
}

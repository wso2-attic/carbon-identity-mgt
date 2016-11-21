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

package org.wso2.carbon.identity.mgt.internal.config.domain;

import java.util.Map;

/**
 * Domain IdentityStoreConnector configuration.
 */
public class DomainIdentityStoreConnectorConfigEntry {

    String identityStoreConnectorId;

    Map<String, String> attributeMappings;

    public DomainIdentityStoreConnectorConfigEntry(String identityStoreConnectorId, Map<String, String>
            attributeMappings) {
        this.identityStoreConnectorId = identityStoreConnectorId;
        this.attributeMappings = attributeMappings;
    }

    public String getIdentityStoreConnectorId() {
        return identityStoreConnectorId;
    }

    public void setIdentityStoreConnectorId(String identityStoreConnectorId) {
        this.identityStoreConnectorId = identityStoreConnectorId;
    }

    public Map<String, String> getAttributeMappings() {
        return attributeMappings;
    }

    public void setAttributeMappings(Map<String, String> attributeMappings) {
        this.attributeMappings = attributeMappings;
    }
}

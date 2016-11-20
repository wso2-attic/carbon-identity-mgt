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

import java.util.Collections;
import java.util.List;

/**
 * Store connector config entry for domain config.
 */
public class DomainStoreConnectorEntry {

    private String connectorId;

    /**
     * Domain IdentityStoreConnector attribute mapping configuration list.
     */
    private List<DomainAttributeConfigEntry> attributeMappings;

    public String getConnectorId() {
        return connectorId;
    }

    public void setConnectorId(String connectorId) {
        this.connectorId = connectorId;
    }

    public List<DomainAttributeConfigEntry> getAttributeMappings() {

        if (attributeMappings == null) {
            return Collections.emptyList();
        }
        return attributeMappings;
    }

    public void setAttributeMappings(List<DomainAttributeConfigEntry> attributeMappings) {

        this.attributeMappings = attributeMappings;
    }
}

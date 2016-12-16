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

package org.wso2.carbon.identity.mgt.impl.internal.config.domain;

import java.util.Collections;
import java.util.List;

/**
 * Domain config.
 */
public class DomainConfigFile {

    /**
     * List of domain configuration entries.
     */
    private List<DomainConfigEntry> domains;

    /**
     * Get Domain configuration entries.
     *
     * @return List&lt;DomainConfigEntry&gt; - List of domain configuration entries
     */
    public List<DomainConfigEntry> getDomains() {

        if (domains == null) {
            return Collections.emptyList();
        }
        return domains;
    }

    /**
     * Set Domain configuration entries.
     *
     * @param domains List&lt;DomainConfigEntry&gt; - List of domain configuration entries
     */
    public void setDomains(List<DomainConfigEntry> domains) {
        this.domains = domains;
    }
}

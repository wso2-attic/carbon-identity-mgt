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

package org.wso2.carbon.identity.mgt.config;

import org.wso2.carbon.identity.mgt.claim.MetaClaimMapping;

import java.util.Collections;
import java.util.List;

/**
 * Domain Config.
 */
public class DomainConfig {

    private String name;

    private int priority;

    private UniqueIdResolverConfig uniqueIdResolverConfig;

    private List<String> identityStoreConnectorIds;

    private List<String> credentialStoreConnectorIds;

    private List<MetaClaimMapping> metaClaimMappings;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public UniqueIdResolverConfig getUniqueIdResolverConfig() {
        return uniqueIdResolverConfig;
    }

    public void setUniqueIdResolverConfig(UniqueIdResolverConfig uniqueIdResolverConfig) {
        this.uniqueIdResolverConfig = uniqueIdResolverConfig;
    }

    public List<String> getIdentityStoreConnectorIds() {

        if (identityStoreConnectorIds == null) {
            return Collections.emptyList();
        }
        return identityStoreConnectorIds;
    }

    public void setIdentityStoreConnectorIds(List<String> identityStoreConnectorIds) {
        this.identityStoreConnectorIds = identityStoreConnectorIds;
    }

    public List<String> getCredentialStoreConnectorIds() {

        if (credentialStoreConnectorIds == null) {
            return Collections.emptyList();
        }
        return credentialStoreConnectorIds;
    }

    public void setCredentialStoreConnectorIds(List<String> credentialStoreConnectorIds) {
        this.credentialStoreConnectorIds = credentialStoreConnectorIds;
    }

    public List<MetaClaimMapping> getMetaClaimMappings() {

        if (metaClaimMappings == null) {
            return Collections.emptyList();
        }
        return metaClaimMappings;
    }

    public void setMetaClaimMappings(List<MetaClaimMapping> metaClaimMappings) {
        this.metaClaimMappings = metaClaimMappings;
    }
}

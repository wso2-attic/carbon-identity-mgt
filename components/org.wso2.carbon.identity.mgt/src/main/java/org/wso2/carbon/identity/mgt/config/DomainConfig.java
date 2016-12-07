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

    private int id;

    private String name;

    private int order;

    private UniqueIdResolverConfig uniqueIdResolverConfig;

    private List<IdentityStoreConnectorConfig> identityStoreConnectorConfigs;

    private List<CredentialStoreConnectorConfig> credentialStoreConnectorConfigs;

    private List<MetaClaimMapping> metaClaimMappings;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public UniqueIdResolverConfig getUniqueIdResolverConfig() {
        return uniqueIdResolverConfig;
    }

    public void setUniqueIdResolverConfig(UniqueIdResolverConfig uniqueIdResolverConfig) {
        this.uniqueIdResolverConfig = uniqueIdResolverConfig;
    }

    public List<IdentityStoreConnectorConfig> getIdentityStoreConnectorConfigs() {

        if (identityStoreConnectorConfigs == null) {
            return Collections.emptyList();
        }
        return identityStoreConnectorConfigs;
    }

    public void setIdentityStoreConnectorConfigs(List<IdentityStoreConnectorConfig> identityStoreConnectorConfigs) {
        this.identityStoreConnectorConfigs = identityStoreConnectorConfigs;
    }

    public List<CredentialStoreConnectorConfig> getCredentialStoreConnectorConfigs() {

        if (credentialStoreConnectorConfigs == null) {
            return Collections.emptyList();
        }
        return credentialStoreConnectorConfigs;
    }

    public void setCredentialStoreConnectorConfigs(List<CredentialStoreConnectorConfig>
                                                           credentialStoreConnectorConfigs) {
        this.credentialStoreConnectorConfigs = credentialStoreConnectorConfigs;
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

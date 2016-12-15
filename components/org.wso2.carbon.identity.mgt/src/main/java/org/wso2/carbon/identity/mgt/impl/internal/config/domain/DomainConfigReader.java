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

import org.wso2.carbon.identity.mgt.claim.MetaClaim;
import org.wso2.carbon.identity.mgt.claim.MetaClaimMapping;
import org.wso2.carbon.identity.mgt.connector.config.CredentialStoreConnectorConfig;
import org.wso2.carbon.identity.mgt.connector.config.IdentityStoreConnectorConfig;
import org.wso2.carbon.identity.mgt.connector.config.StoreConnectorConfig;
import org.wso2.carbon.identity.mgt.exception.CarbonIdentityMgtConfigException;
import org.wso2.carbon.identity.mgt.impl.config.DomainConfig;
import org.wso2.carbon.identity.mgt.impl.util.FileUtil;
import org.wso2.carbon.identity.mgt.impl.util.IdentityMgtConstants;
import org.wso2.carbon.identity.mgt.resolver.config.UniqueIdResolverConfig;
import org.wso2.carbon.kernel.utils.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.mgt.impl.util.IdentityMgtConstants.CLAIM_ROOT_DIALECT;
import static org.wso2.carbon.identity.mgt.impl.util.IdentityMgtConstants.UNIQUE_ID_RESOLVER_TYPE;

/**
 * Reader for retrieving Domain configurations.
 */
public class DomainConfigReader {

    private DomainConfigReader() {

    }

    /**
     * Reads domain configurations.
     *
     * @param storeConnectorConfigMap connector id to connector configuration map.
     * @return domain configurations.
     * @throws CarbonIdentityMgtConfigException
     */
    public static List<DomainConfig> getDomainConfigs(Map<String, StoreConnectorConfig> storeConnectorConfigMap)
            throws CarbonIdentityMgtConfigException {

        DomainConfigFile domainConfigFile = buildDomainConfig();

        if (domainConfigFile.getDomains().isEmpty()) {
            return Collections.emptyList();
        }

        return domainConfigFile.getDomains().stream()
                .filter(Objects::nonNull)
                .filter(domainConfigEntry -> !StringUtils.isNullOrEmpty(domainConfigEntry.getName()))
                .map(domainConfigEntry -> getDomainConfig(storeConnectorConfigMap, domainConfigEntry))
                .collect(Collectors.toList());
    }

    private static DomainConfig getDomainConfig(Map<String, StoreConnectorConfig> connectorIdToStoreConnectorConfigMap,
                                                DomainConfigEntry domainConfigEntry) {

        DomainConfig domainConfig = new DomainConfig();
        domainConfig.setId(domainConfigEntry.getId());
        domainConfig.setName(domainConfigEntry.getName());
        domainConfig.setOrder(domainConfigEntry.getOrder());

        UniqueIdResolverConfigEntry uniqueIdResolverConfigEntry = domainConfigEntry.getUniqueIdResolver();
        if (uniqueIdResolverConfigEntry == null || StringUtils.isNullOrEmpty(uniqueIdResolverConfigEntry
                .getType())) {
            domainConfig.setUniqueIdResolverConfig(new UniqueIdResolverConfig(UNIQUE_ID_RESOLVER_TYPE,
                    Collections.emptyMap()));
        } else {
            domainConfig.setUniqueIdResolverConfig((new UniqueIdResolverConfig
                    (uniqueIdResolverConfigEntry.getType(), uniqueIdResolverConfigEntry.getProperties())));
        }

        if (!domainConfigEntry.getIdentityStoreConnectors().isEmpty()) {

            List<IdentityStoreConnectorConfig> identityStoreConnectorConfigs = new ArrayList<>();
            List<MetaClaimMapping> metaClaimMappings = new ArrayList<>();

            domainConfigEntry.getIdentityStoreConnectors().stream()
                    .filter(Objects::nonNull)
                    .filter(domainStoreConnectorEntry ->
                            !StringUtils.isNullOrEmpty(domainStoreConnectorEntry.getConnectorId()))

                    .forEach(domainStoreConnectorEntry -> {
                        if (StringUtils.isNullOrEmpty(domainStoreConnectorEntry.getConnectorType())) {

                            StoreConnectorConfig storeConnectorConfig =
                                    connectorIdToStoreConnectorConfigMap.get(domainStoreConnectorEntry
                                            .getConnectorId());
                            if (storeConnectorConfig == null || !(storeConnectorConfig instanceof
                                    IdentityStoreConnectorConfig)) {
                                return;
                            }
                            identityStoreConnectorConfigs.add((IdentityStoreConnectorConfig)
                                    storeConnectorConfig);
                        } else {

                            identityStoreConnectorConfigs.add(new IdentityStoreConnectorConfig(
                                    domainStoreConnectorEntry.getConnectorId(), domainStoreConnectorEntry
                                    .getConnectorType(), domainStoreConnectorEntry.isReadOnly(),
                                    domainStoreConnectorEntry.getProperties()));
                        }

                        metaClaimMappings.addAll(getMetaClaimMappings(domainStoreConnectorEntry
                                .getConnectorId(), domainStoreConnectorEntry.getAttributeMappings()));
                    });
            domainConfig.setIdentityStoreConnectorConfigs(identityStoreConnectorConfigs);
            domainConfig.setMetaClaimMappings(metaClaimMappings);
        }

        if (!domainConfigEntry.getCredentialStoreConnectors().isEmpty()) {

            List<CredentialStoreConnectorConfig> credentialStoreConnectorConfigs = new ArrayList<>();

            domainConfigEntry.getCredentialStoreConnectors()
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(domainStoreConnectorEntry -> !StringUtils
                            .isNullOrEmpty(domainStoreConnectorEntry.getConnectorId()))

                    .forEach(domainStoreConnectorEntry -> {
                        if (StringUtils.isNullOrEmpty(domainStoreConnectorEntry.getConnectorType())) {
                            StoreConnectorConfig storeConnectorConfig =
                                    connectorIdToStoreConnectorConfigMap.get(domainStoreConnectorEntry
                                            .getConnectorId());
                            if (storeConnectorConfig == null || !(storeConnectorConfig instanceof
                                    CredentialStoreConnectorConfig)) {
                                return;
                            }
                            credentialStoreConnectorConfigs.add((CredentialStoreConnectorConfig)
                                    storeConnectorConfig);
                        } else {
                            credentialStoreConnectorConfigs.add(new CredentialStoreConnectorConfig(
                                    domainStoreConnectorEntry.getConnectorId(), domainStoreConnectorEntry
                                    .getConnectorType(), domainStoreConnectorEntry.isReadOnly(),
                                    domainStoreConnectorEntry.getProperties()));
                        }
                    });
            domainConfig.setCredentialStoreConnectorConfigs(credentialStoreConnectorConfigs);
        }
        return domainConfig;
    }

    private static DomainConfigFile buildDomainConfig() throws CarbonIdentityMgtConfigException {

        Path file = Paths.get(IdentityMgtConstants.getCarbonHomeDirectory().toString(), "conf", "identity",
                IdentityMgtConstants.DOMAIN_CONFIG_FILE);

        // domain-config.yaml is a mandatory configuration file.
        return FileUtil.readConfigFile(file, DomainConfigFile.class);
    }

    private static List<MetaClaimMapping> getMetaClaimMappings(String storeConnectorId, List<DomainAttributeConfigEntry>
            attributeConfigEntries) {

        if (attributeConfigEntries.isEmpty()) {
            return Collections.emptyList();
        }

        return attributeConfigEntries.stream()
                .filter(Objects::nonNull)
                .filter(domainAttributeConfigEntry -> !StringUtils.isNullOrEmpty(domainAttributeConfigEntry
                        .getClaimUri()) && !StringUtils.isNullOrEmpty(domainAttributeConfigEntry.getAttribute()))

                .map(domainAttributeConfigEntry -> {
                    MetaClaim metaClaim = new MetaClaim(CLAIM_ROOT_DIALECT, domainAttributeConfigEntry.getClaimUri(),
                            domainAttributeConfigEntry.getProperties());
                    return new MetaClaimMapping(metaClaim, storeConnectorId, domainAttributeConfigEntry.getAttribute());
                }).collect(Collectors.toList());

    }
}

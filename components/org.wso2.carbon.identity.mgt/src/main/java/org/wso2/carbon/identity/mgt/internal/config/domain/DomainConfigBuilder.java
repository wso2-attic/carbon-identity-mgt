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

import org.wso2.carbon.identity.mgt.claim.MetaClaim;
import org.wso2.carbon.identity.mgt.claim.MetaClaimMapping;
import org.wso2.carbon.identity.mgt.config.DomainConfig;
import org.wso2.carbon.identity.mgt.config.UniqueIdResolverConfig;
import org.wso2.carbon.identity.mgt.exception.CarbonSecurityConfigException;
import org.wso2.carbon.identity.mgt.util.FileUtil;
import org.wso2.carbon.identity.mgt.util.IdentityMgtConstants;
import org.wso2.carbon.kernel.utils.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Builder for retrieving Domain configurations.
 */
public class DomainConfigBuilder {

    private static DomainConfigBuilder instance = new DomainConfigBuilder();

    private DomainConfigBuilder() {

    }

    public static DomainConfigBuilder getInstance() {
        return instance;
    }

    public List<DomainConfig> getDomainConfigs(Map<String, MetaClaim> claimUriToMetaClaimMap) throws
            CarbonSecurityConfigException {

        DomainConfigFile domainConfigFile = buildDomainConfig();

        if (domainConfigFile.getDomains().isEmpty()) {
            return Collections.emptyList();
        }

        return domainConfigFile.getDomains().stream()
                .filter(Objects::nonNull)
                .filter(domainConfigEntry -> !StringUtils.isNullOrEmpty(domainConfigEntry.getName()))
                .map(domainConfigEntry -> {
                    DomainConfig domainConfig = new DomainConfig();
                    domainConfig.setName(domainConfigEntry.getName());
                    domainConfig.setPriority(domainConfigEntry.getPriority());

                    UniqueIdResolverConfigEntry uniqueIdResolverConfigEntry = domainConfigEntry.getUniqueIdResolver();
                    if (uniqueIdResolverConfigEntry == null || StringUtils.isNullOrEmpty(uniqueIdResolverConfigEntry
                            .getType())) {
                        domainConfig.setUniqueIdResolverConfig(new UniqueIdResolverConfig("JDBC-UUID-RESOLVER",
                                Collections.emptyMap()));
                    } else {
                        domainConfig.setUniqueIdResolverConfig((new UniqueIdResolverConfig
                                (uniqueIdResolverConfigEntry.getType(), uniqueIdResolverConfigEntry.getProperties())));
                    }

                    if (!domainConfigEntry.getIdentityStoreConnectors().isEmpty()) {

                        List<String> identityStoreConnectorIds = new ArrayList<>();
                        List<MetaClaimMapping> metaClaimMappings = new ArrayList<>();

                        domainConfigEntry.getIdentityStoreConnectors().stream()
                                .filter(Objects::nonNull)
                                .filter(domainStoreConnectorEntry -> !StringUtils
                                        .isNullOrEmpty(domainStoreConnectorEntry.getConnectorId()))
                                .forEach(domainStoreConnectorEntry -> {

                                    identityStoreConnectorIds.add(domainStoreConnectorEntry.getConnectorId());
                                    metaClaimMappings.addAll(getMetaClaimMappings(claimUriToMetaClaimMap,
                                            domainStoreConnectorEntry.getConnectorId(), domainStoreConnectorEntry
                                                    .getAttributeMappings()));

                                });
                        domainConfig.setIdentityStoreConnectorIds(identityStoreConnectorIds);
                        domainConfig.setMetaClaimMappings(metaClaimMappings);
                    }

                    if (!domainConfigEntry.getIdentityStoreConnectors().isEmpty()) {
                        List<String> credentialStoreConnectorIds = domainConfigEntry.getCredentialStoreConnectors()
                                .stream()
                                .filter(Objects::nonNull)
                                .filter(domainStoreConnectorEntry -> !StringUtils
                                        .isNullOrEmpty(domainStoreConnectorEntry.getConnectorId()))
                                .map(DomainStoreConnectorEntry::getConnectorId)
                                .collect(Collectors.toList());
                        domainConfig.setCredentialStoreConnectorIds(credentialStoreConnectorIds);
                    }
                    return domainConfig;
                }).collect(Collectors.toList());
    }

    private DomainConfigFile buildDomainConfig() throws CarbonSecurityConfigException {

        Path file = Paths.get(IdentityMgtConstants.getCarbonHomeDirectory().toString(), "conf", "identity",
                IdentityMgtConstants.DOMAIN_CONFIG_FILE);

        // domain-config.yml is a mandatory configuration file.
        return FileUtil.readConfigFile(file, DomainConfigFile.class);
    }

    private List<MetaClaimMapping> getMetaClaimMappings(Map<String, MetaClaim> claimUriToMetaClaimMap, String
            connectorId, List<DomainAttributeConfigEntry> attributeConfigEntries) {

        if (attributeConfigEntries.isEmpty()) {
            return Collections.emptyList();
        }

        return attributeConfigEntries.stream()
                .filter(Objects::nonNull)
                .filter(domainAttributeConfigEntry -> !StringUtils.isNullOrEmpty(domainAttributeConfigEntry
                        .getClaimURI()) && !StringUtils.isNullOrEmpty(domainAttributeConfigEntry.getAttribute()) &&
                        claimUriToMetaClaimMap.get(domainAttributeConfigEntry.getClaimURI()) != null)
                .map(domainAttributeConfigEntry -> {
                    MetaClaim metaClaim = claimUriToMetaClaimMap.get(domainAttributeConfigEntry.getClaimURI());
                    return new MetaClaimMapping(metaClaim, connectorId, domainAttributeConfigEntry.getAttribute(),
                            Boolean.valueOf(metaClaim.getProperties().get("unique")));
                }).collect(Collectors.toList());

    }
}

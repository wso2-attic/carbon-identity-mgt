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

import org.wso2.carbon.identity.mgt.config.CredentialStoreConnectorConfig;
import org.wso2.carbon.identity.mgt.config.IdentityStoreConnectorConfig;
import org.wso2.carbon.identity.mgt.config.StoreConnectorConfig;
import org.wso2.carbon.identity.mgt.exception.CarbonIdentityMgtConfigException;
import org.wso2.carbon.identity.mgt.util.FileUtil;
import org.wso2.carbon.identity.mgt.util.IdentityMgtConstants;
import org.wso2.carbon.kernel.utils.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Reads external identity/credential store connector config files.
 */
public class ConnectorConfigReader {

    private ConnectorConfigReader() {

    }

    /**
     * Read all external connector config files.
     *
     * @return Connector name to connector config map.
     * @throws CarbonIdentityMgtConfigException Carbon Identity Mgt Config Exception.
     */
    public static Map<String, StoreConnectorConfig> getStoreConnectorConfigs() throws CarbonIdentityMgtConfigException {

        List<StoreConnectorConfig> storeConnectorConfigs = new ArrayList<>();

        List<ConnectorConfigEntry> externalIdentityStoreConnectors = buildExternalIdentityStoreConnectorConfig();
        if (!externalIdentityStoreConnectors.isEmpty()) {

            storeConnectorConfigs.addAll(getStoreConnectorConfig(externalIdentityStoreConnectors,
                    IdentityStoreConnectorConfig.class));
        }

        List<ConnectorConfigEntry> externalCredentialStoreConnectors = buildExternalCredentialStoreConnectorConfig();
        if (!externalCredentialStoreConnectors.isEmpty()) {

            storeConnectorConfigs.addAll(getStoreConnectorConfig(externalCredentialStoreConnectors,
                    CredentialStoreConnectorConfig.class));
        }

        return storeConnectorConfigs.stream().collect(Collectors.toMap(StoreConnectorConfig::getConnectorId,
                storeConnectorConfig -> storeConnectorConfig));
    }

    /**
     * Read the IdentityStoreConnector config entries from external identity-connector.yml files.
     *
     * @return List of external connector config entries.
     * @throws CarbonIdentityMgtConfigException Carbon Identity Mgt Config Exception.
     */
    private static List<ConnectorConfigEntry> buildExternalIdentityStoreConnectorConfig() throws
            CarbonIdentityMgtConfigException {

        Path path = Paths.get(IdentityMgtConstants.getCarbonHomeDirectory().toString(), "conf", "identity");

        return FileUtil.readConfigFiles(path, ConnectorConfigEntry.class, "*-identity-connector.yml");
    }

    /**
     * Read the CredentialStoreConnector config entries from external identity-connector.yml files.
     *
     * @return List of external connector config entries.
     * @throws CarbonIdentityMgtConfigException Carbon Identity Mgt Config Exception.
     */
    private static List<ConnectorConfigEntry> buildExternalCredentialStoreConnectorConfig() throws
            CarbonIdentityMgtConfigException {

        Path path = Paths.get(IdentityMgtConstants.getCarbonHomeDirectory().toString(), "conf", "identity");

        return FileUtil.readConfigFiles(path, ConnectorConfigEntry.class, "*-credential-connector.yml");
    }

    private static  <T extends StoreConnectorConfig> List<StoreConnectorConfig> getStoreConnectorConfig
            (List<ConnectorConfigEntry> connectorConfigEntries, Class<T> classType) {

        return connectorConfigEntries.stream()
                .filter(Objects::nonNull)
                .filter(connectorConfigEntry -> !StringUtils.isNullOrEmpty(connectorConfigEntry.getConnectorId())
                        && !StringUtils.isNullOrEmpty(connectorConfigEntry.getConnectorType()))
                .map(connectorConfigEntry -> {
                    if (classType.equals(IdentityStoreConnectorConfig.class)) {
                        return new IdentityStoreConnectorConfig(connectorConfigEntry.getConnectorId(),
                                connectorConfigEntry.getConnectorType(), connectorConfigEntry.getProperties());
                    }
                    return new CredentialStoreConnectorConfig(connectorConfigEntry.getConnectorId(),
                            connectorConfigEntry.getConnectorType(), connectorConfigEntry.getProperties());
                })
                .collect(Collectors.toList());
    }

}

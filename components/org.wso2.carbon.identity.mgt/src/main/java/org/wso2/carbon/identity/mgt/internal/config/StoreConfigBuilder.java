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

package org.wso2.carbon.identity.mgt.internal.config;

import org.wso2.carbon.identity.mgt.config.CacheConfig;
import org.wso2.carbon.identity.mgt.config.StoreConfig;
import org.wso2.carbon.identity.mgt.exception.CarbonSecurityConfigException;
import org.wso2.carbon.identity.mgt.internal.config.store.CacheConfigEntry;
import org.wso2.carbon.identity.mgt.internal.config.store.StoreConfigEntry;
import org.wso2.carbon.identity.mgt.internal.config.store.StoreConfigFile;
import org.wso2.carbon.identity.mgt.util.FileUtil;
import org.wso2.carbon.identity.mgt.util.IdentityMgtConstants;
import org.wso2.carbon.kernel.utils.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Configuration builder for stores.
 *
 * @since 1.0.0
 */
public class StoreConfigBuilder {

    private static StoreConfigBuilder instance = new StoreConfigBuilder();

    private StoreConfigBuilder() {

    }

    public static StoreConfigBuilder getInstance() {
        return instance;
    }

    /**
     * Builder a config object based on the store-config.yml properties.
     *
     * @return StoreConfig
     * @throws CarbonSecurityConfigException carbon security config exception.
     */
    public StoreConfig getStoreConfig() throws CarbonSecurityConfigException {

        StoreConfigFile storeConfigFile = buildStoreConfig();
        StoreConfig storeConfig = new StoreConfig();

        if (!storeConfigFile.isEnableCache()) {
            storeConfig.setEnableCache(false);
            return storeConfig;
        }

        if (storeConfigFile.getIdentityStore() != null) {

            StoreConfigEntry storeConfigEntry = storeConfigFile.getIdentityStore();
            if (storeConfig.isEnableCache()) {
                storeConfig.setIdentityStoreCacheConfigMap(getCacheConfigs(storeConfigEntry.getCacheConfigs()));
            } else {
                storeConfig.setEnableIdentityStoreCache(false);
            }
        }

        if (storeConfigFile.getCredentialStore() != null) {

            StoreConfigEntry storeConfigEntry = storeConfigFile.getCredentialStore();
            if (storeConfig.isEnableCache()) {
                storeConfig.setCredentialStoreCacheConfigMap(getCacheConfigs(storeConfigEntry.getCacheConfigs()));
            } else {
                storeConfig.setEnableCredentialStoreCache(false);
            }
        }

        return storeConfig;
    }


    /**
     * Read store-config.yml file
     *
     * @return StoreConfig file from store-config.yml
     * @throws CarbonSecurityConfigException on error in reading file.
     */
    private StoreConfigFile buildStoreConfig() throws CarbonSecurityConfigException {

        Path file = Paths.get(IdentityMgtConstants.getCarbonHomeDirectory().toString(), "conf", "identity",
                IdentityMgtConstants.STORE_CONFIG_FILE);

        // store-config.yml is a mandatory configuration file.
        return FileUtil.readConfigFile(file, StoreConfigFile.class);
    }

    /**
     * Get cache configs for each connector.
     *
     * @param cacheConfigEntries Cache entry of the connector.
     * @return Map of CacheConfigs mapped to cache config name.
     */
    private static Map<String, CacheConfig> getCacheConfigs(List<CacheConfigEntry> cacheConfigEntries) {

        if (cacheConfigEntries.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, CacheConfig> cacheConfigMap = new HashMap<>();

        cacheConfigEntries.stream()
                .filter(Objects::nonNull)
                .filter(cacheConfigEntry -> !StringUtils.isNullOrEmpty(cacheConfigEntry.getName()))
                .forEach(cacheConfigEntry -> {
                    CacheConfig cacheConfig = new CacheConfig();
                    cacheConfig.setEnable(cacheConfigEntry.isEnableCache());
                    cacheConfig.setExpireTime(cacheConfigEntry.getExpireTime());
                    cacheConfig.setMaxCapacity(cacheConfigEntry.getMaxCapacity());
                    cacheConfig.setEnable(cacheConfigEntry.isStatisticsEnabled());
                    cacheConfigMap.put(cacheConfigEntry.getName(), cacheConfig);
                });
        return cacheConfigMap;
    }

    /**
     * Read the IdentityStoreConnector config entries from external identity-connector.yml files.
     *
     * @return List of external IdentityStoreConnector config entries.
     * @throws CarbonSecurityConfigException
     */
//    private static List<IdentityStoreConnectorConfig> getExternalIdentityStoreConnectorConfig()
//            throws CarbonSecurityConfigException {
//
//        List<IdentityStoreConnectorConfig> configEntries = new ArrayList<>();
//        Path path = Paths.get(IdentityMgtConstants.getCarbonHomeDirectory().toString(), "conf", "identity");
//
//        if (Files.exists(path)) {
//            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, "*-identity-connector.yml")) {
//                for (Path filePath : stream) {
//                    IdentityStoreConnectorConfig config = new Yaml().loadAs(Files.newInputStream(filePath),
//                            IdentityStoreConnectorConfig.class);
//
//                    configEntries.add(config);
//                }
//            } catch (DirectoryIteratorException | IOException e) {
//                throw new CarbonSecurityConfigException("Failed to read identity connector files from path: "
//                        + path.toString(), e);
//            }
//        }
//
//        return configEntries;
//    }
//    /**
//     * Read the CredentialStoreConnector config entries from external identity-connector.yml files.
//     *
//     * @return List of external CredentialStoreConnector Store config entries.
//     * @throws CarbonSecurityConfigException
//     */
//    private static List<CredentialStoreConnectorConfig> getExternalCredentialStoreConnectorConfig()
//            throws CarbonSecurityConfigException {
//
//        List<CredentialStoreConnectorConfig> configEntries = new ArrayList<>();
//        Path path = Paths.get(IdentityMgtConstants.getCarbonHomeDirectory().toString(), "conf", "identity");
//
//        if (Files.exists(path)) {
//            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, "*-credential-connector.yml")) {
//                for (Path filePath : stream) {
//                    CredentialStoreConnectorConfig config = new Yaml().loadAs(Files.newInputStream(filePath),
//                            CredentialStoreConnectorConfig.class);
//
//                    configEntries.add(config);
//                }
//            } catch (DirectoryIteratorException | IOException e) {
//                throw new CarbonSecurityConfigException("Failed to read credential store connector files from path: "
//                        + path.toString(), e);
//            }
//        }
//
//        return configEntries;
//    }

}

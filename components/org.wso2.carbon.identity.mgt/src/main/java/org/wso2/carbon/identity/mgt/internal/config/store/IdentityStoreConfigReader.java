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

package org.wso2.carbon.identity.mgt.internal.config.store;

import org.wso2.carbon.identity.mgt.config.CacheConfig;
import org.wso2.carbon.identity.mgt.config.StoreConfig;
import org.wso2.carbon.identity.mgt.exception.CarbonIdentityMgtConfigException;
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
public class IdentityStoreConfigReader {

    private IdentityStoreConfigReader() {

    }

    /**
     * Builder a config object based on the store-config.yml properties.
     *
     * @return StoreConfig
     * @throws CarbonIdentityMgtConfigException carbon security config exception.
     */
    public static StoreConfig getStoreConfig() throws CarbonIdentityMgtConfigException {

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
     * @throws CarbonIdentityMgtConfigException on error in reading file.
     */
    private static StoreConfigFile buildStoreConfig() throws CarbonIdentityMgtConfigException {

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
}

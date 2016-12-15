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

package org.wso2.carbon.identity.mgt.impl.internal.config.store;

import java.util.Collections;
import java.util.List;

/**
 * Store Config Entry.
 */
public class StoreConfigEntry {

    private boolean enableCache = true;

    private List<CacheConfigEntry> cacheConfigs;

    public boolean isEnableCache() {
        return enableCache;
    }

    public void setEnableCache(boolean enableCache) {
        this.enableCache = enableCache;
    }

    public List<CacheConfigEntry> getCacheConfigs() {

        if (cacheConfigs == null) {
            return Collections.emptyList();
        }
        return cacheConfigs;
    }

    public void setCacheConfigs(List<CacheConfigEntry> cacheConfigs) {
        this.cacheConfigs = cacheConfigs;
    }
}

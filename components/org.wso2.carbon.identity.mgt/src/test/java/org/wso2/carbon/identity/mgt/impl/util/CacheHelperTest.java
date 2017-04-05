/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.mgt.impl.util;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.mgt.impl.config.CacheConfig;

import java.util.HashMap;
import java.util.Map;
import javax.cache.CacheManager;

import static org.mockito.Mockito.mock;

/**
 * Unit tests for cache helper.
 */
public class CacheHelperTest {

    @Test
    public void testIsCacheEnabled() throws Exception {

        CacheHelper cacheHelper = new CacheHelper();

        CacheConfig cacheConfig = new CacheConfig();
        cacheConfig.setEnable(true);

        String cacheName = "testCache";
        Map<String, CacheConfig> cacheConfigMap = new HashMap<>();
        cacheConfigMap.put(cacheName, cacheConfig);

        Assert.assertTrue(CacheHelper.isCacheEnabled(cacheConfigMap, null), "Cache should be enabled by default.");
        Assert.assertTrue(CacheHelper.isCacheEnabled(cacheConfigMap, cacheName), "Cache should be enabled.");

        cacheConfig.setEnable(false);
        Assert.assertFalse(CacheHelper.isCacheEnabled(cacheConfigMap, cacheName), "Cache should be disabled.");
    }

    @Test
    public void testGetExpireTime() throws Exception {

        int defaultExpiryTime = 15;
        int expiryTime = 10;
        CacheConfig cacheConfig = new CacheConfig();
        cacheConfig.setEnable(true);

        String cacheName = "testCache";
        Map<String, CacheConfig> cacheConfigMap = new HashMap<>();
        cacheConfigMap.put(cacheName, cacheConfig);

        Assert.assertEquals(CacheHelper.getExpireTime(cacheConfigMap, null, defaultExpiryTime), defaultExpiryTime);
        Assert.assertEquals(CacheHelper.getExpireTime(cacheConfigMap, cacheName, defaultExpiryTime), defaultExpiryTime);

        cacheConfig.setExpireTime(expiryTime);
        Assert.assertEquals(CacheHelper.getExpireTime(cacheConfigMap, cacheName, defaultExpiryTime), expiryTime);
    }

    @Test
    public void testCreateCache() throws Exception {

        int defaultExpiryTime = 15;
        int expiryTime = 10;

        CacheConfig cacheConfig = new CacheConfig();
        cacheConfig.setExpireTime(expiryTime);
        cacheConfig.setStatisticsEnabled(true);

        String cacheName = "testCache";
        Map<String, CacheConfig> cacheConfigMap = new HashMap<>();
        cacheConfigMap.put(cacheName, cacheConfig);

        CacheManager cacheManager = mock(CacheManager.class);

        CacheHelper.createCache(cacheName, String.class, String.class, defaultExpiryTime, cacheConfigMap, cacheManager);
    }
}

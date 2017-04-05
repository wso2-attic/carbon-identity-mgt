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

package org.wso2.carbon.identity.mgt.claim;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for MetaClaim.
 */
public class MetaClaimTest {
    @Test
    public void testMetaClaim() throws Exception {

        String claimURI = "testClaimURI";
        String dialectURI = "testDialectURI";

        MetaClaim metaClaim = new MetaClaim();

        metaClaim.setDialectUri(dialectURI);
        metaClaim.setClaimUri(claimURI);

        Assert.assertEquals(metaClaim.getClaimUri(), claimURI);
        Assert.assertEquals(metaClaim.getDialectUri(), dialectURI);

        Assert.assertNotNull(metaClaim.getProperties());

        Map<String, String> properties = new HashMap<>(1);
        properties.put("K", "V");

        metaClaim.setProperties(properties);

        Assert.assertFalse(metaClaim.getProperties().isEmpty());
        Assert.assertEquals(metaClaim.getProperty("K"), "V");
    }
}

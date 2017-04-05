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

package org.wso2.carbon.identity.claim.mapping.claim;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for ClaimMappingEntry.
 */
public class ClaimMappingEntryTest {

    @Test
    public void testClaimMappingEntry() throws Exception {

        String inherit = "testInherit";
        String dialectURI = "testDialect";
        Map<String, String> mappings = new HashMap<>(1);

        ClaimMappingEntry entry = new ClaimMappingEntry();

        entry.setInherits(inherit);
        Assert.assertEquals(entry.getInherits(), inherit);

        entry.setMappingDialectURI(dialectURI);
        Assert.assertEquals(entry.getMappingDialectURI(), dialectURI);

        Assert.assertNotNull(entry.getMappings());
        entry.setMappings(mappings);
        Assert.assertEquals(entry.getMappings(), mappings);

    }
}

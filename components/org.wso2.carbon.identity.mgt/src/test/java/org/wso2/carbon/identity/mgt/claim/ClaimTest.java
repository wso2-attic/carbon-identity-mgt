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

/**
 * Unit tests for Claim.
 */
public class ClaimTest {

    @Test
    public void testSetClaimUri() throws Exception {

        String claimURI = "testClaimURI";

        Claim claim = new Claim();
        claim.setClaimUri(claimURI);

        Assert.assertEquals(claim.getClaimUri(), claimURI);
    }

    @Test
    public void testGetValue() throws Exception {

        String claimValue = "testClaimValue";

        Claim claim = new Claim();
        claim.setValue(claimValue);

        Assert.assertEquals(claim.getValue(), claimValue);
    }

    @Test
    public void testGetDialectUri() throws Exception {

        String dialectURI = "testDialectURI";

        Claim claim = new Claim();
        claim.setDialectUri(dialectURI);

        Assert.assertEquals(claim.getDialectUri(), dialectURI);
    }
}

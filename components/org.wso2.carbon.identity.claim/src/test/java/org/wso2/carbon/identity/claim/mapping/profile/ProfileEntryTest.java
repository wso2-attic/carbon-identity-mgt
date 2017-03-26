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

package org.wso2.carbon.identity.claim.mapping.profile;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for ProfileEntry.
 */
public class ProfileEntryTest {

    @Test
    public void testProfileEntry() throws Exception {

        String profileName = "testProfile";
        List<ClaimConfigEntry> claims = new ArrayList<>(1);

        ProfileEntry profileEntry = new ProfileEntry();

        profileEntry.setAdminProfile(true);
        Assert.assertTrue(profileEntry.isAdminProfile());

        profileEntry.setProfileName(profileName);
        Assert.assertEquals(profileEntry.getProfileName(), profileName);

        Assert.assertNotNull(profileEntry.getClaims());
        profileEntry.setClaims(claims);
        Assert.assertEquals(profileEntry.getClaims(), claims);
    }

}

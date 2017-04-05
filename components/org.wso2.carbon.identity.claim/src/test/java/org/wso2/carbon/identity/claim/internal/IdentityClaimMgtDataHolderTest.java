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

package org.wso2.carbon.identity.claim.internal;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.claim.service.ClaimResolvingService;
import org.wso2.carbon.identity.claim.service.ProfileMgtService;

import static org.wso2.carbon.identity.claim.internal.IdentityClaimMgtDataHolder.getInstance;

/**
 * Unit tests for IdentityClaimMgtDataHolder.
 */
public class IdentityClaimMgtDataHolderTest {

    private static ClaimResolvingService claimResolvingService = Mockito.mock(ClaimResolvingService.class);
    private static ProfileMgtService profileMgtService = Mockito.mock(ProfileMgtService.class);

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetClaimResolvingService() throws Exception {

        getInstance().setClaimResolvingService(claimResolvingService);
        Assert.assertEquals(getInstance().getClaimResolvingService(), claimResolvingService);

        getInstance().setClaimResolvingService(null);
        Assert.assertNull(getInstance().getClaimResolvingService());
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetProfileMgtService() throws Exception {

        getInstance().setProfileMgtService(profileMgtService);
        Assert.assertEquals(getInstance().getProfileMgtService(), profileMgtService);

        getInstance().setProfileMgtService(null);
        Assert.assertNull(getInstance().getProfileMgtService());
    }
}

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

package org.wso2.carbon.identity.recovery.test.unit;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.username.NotificationUsernameRecoveryManager;

import java.util.Collections;
import java.util.List;

/**
 * Recovery unit tests.
 */
@PrepareForTest(NotificationUsernameRecoveryManager.class)
public class UsernameRecoveryTests {
    private static NotificationUsernameRecoveryManager instance = NotificationUsernameRecoveryManager.getInstance();

    @Test
    public void testVerifyUsername() throws IdentityRecoveryException {
        List<Claim> claims;
        boolean result;

        claims = null;
        result = instance.verifyUsername(claims);
        Assert.assertEquals(result , "false");

        claims = Collections.emptyList();
        result = instance.verifyUsername(claims);
        Assert.assertEquals(result , "false");

    }
}

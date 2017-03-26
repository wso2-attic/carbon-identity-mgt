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

package org.wso2.carbon.identity.recovery.handler;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.base.event.model.Event;
import org.wso2.carbon.identity.event.EventException;
import org.wso2.carbon.identity.mgt.IdentityStore;
import org.wso2.carbon.identity.mgt.RealmService;
import org.wso2.carbon.identity.mgt.User;
import org.wso2.carbon.identity.mgt.constant.StoreConstants;
import org.wso2.carbon.identity.mgt.event.IdentityMgtMessageContext;
import org.wso2.carbon.identity.recovery.IdentityRecoveryServerException;

import java.util.HashMap;
import java.util.Map;

import static org.wso2.carbon.identity.recovery.IdentityRecoveryConstants.SELF_SIGN_UP_EVENT;

/**
 * Unit tests for UserSelfSignUpHandler.
 */
public class UserSelfSignUpHandlerTest {

    @Test(expectedExceptions = IdentityRecoveryServerException.class)
    public void testHandleNullUser() throws Exception {

        IdentityMgtMessageContext context = new IdentityMgtMessageContext();
        Map<String, Object> properties = new HashMap<>(1);

        Event event = new Event(SELF_SIGN_UP_EVENT, properties);

        UserSelfSignUpHandler handler = new UserSelfSignUpHandler();
        handler.handle(context, event);

        RealmService realmService = Mockito.mock(RealmService.class);
        handler.setRealmService(realmService);

        User user = new User.UserBuilder().setUserId("invalidUserId").build();
        properties.put(StoreConstants.IdentityStoreConstants.USER, user);

        handler.handle(context, event);
        Assert.fail("Expected IdentityRecoveryServerException.");
    }


    @Test(expectedExceptions = EventException.class)
    public void testHandleInvalidUser() throws Exception {

        IdentityMgtMessageContext context = new IdentityMgtMessageContext();
        IdentityStore identityStore = Mockito.mock(IdentityStore.class);

        Map<String, Object> properties = new HashMap<>(1);
        User user = new User.UserBuilder().setUserId("invalidUserId")
                                          .setDomainName("testDomain")
                                          .setIdentityStore(identityStore)
                                          .build();
        properties.put(StoreConstants.IdentityStoreConstants.USER, user);

        Event event = new Event(SELF_SIGN_UP_EVENT, properties);

        UserSelfSignUpHandler handler = new UserSelfSignUpHandler();

        RealmService realmService = Mockito.mock(RealmService.class);
        handler.setRealmService(realmService);

        handler.handle(context, event);
        Assert.fail("Expected EventException.");
    }
}

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

package org.wso2.carbon.identity.mgt.store.test.unit;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.mgt.IdentityStore;
import org.wso2.carbon.identity.mgt.RealmService;
import org.wso2.carbon.identity.mgt.exception.DomainException;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.impl.Domain;
import org.wso2.carbon.identity.mgt.impl.IdentityStoreImpl;
import org.wso2.carbon.identity.mgt.impl.internal.IdentityMgtDataHolder;
import org.wso2.carbon.security.caas.user.core.store.AuthorizationStore;

import java.util.ArrayList;

/**
 * Identity Store Tests.
 */
@PrepareForTest(IdentityMgtDataHolder.class)
public class IdentityStoreTests {

    @Mock
    private RealmService realmService;

    @Mock
    private AuthorizationStore authorizationStore;

    @Mock
    private IdentityMgtDataHolder identityMgtDataHolder;

    @Mock
    private Domain domain;

    private IdentityStore identityStore;

    @BeforeClass
    public void initClass() {

        MockitoAnnotations.initMocks(this);
    }

    @BeforeMethod
    public void initMethod() throws DomainException {

    }

    @AfterMethod
    public void resetMocks() {

        Mockito.reset(realmService);
        Mockito.reset(authorizationStore);
        Mockito.reset(identityMgtDataHolder);
    }

    @Test
    public void testInitMethodWithEmptyDomainList() {

        try {
            identityStore = new IdentityStoreImpl(new ArrayList<>());
        } catch (IdentityStoreException e) {
            return;
        }

        Assert.fail("Expecting an Identity Store exception.");
    }

//    @Test
//    public void testInitMethodWithValidDomainList() {
//
//        try {
//            identityStore = new IdentityStoreImpl(Collections.singletonList(domain));
//        } catch (IdentityStoreException e) {
//            Assert.fail("Does not expect an Identity Store exception.");
//        }
//    }

}

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

package org.wso2.carbon.identity.mgt.impl.internal;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.mgt.resolver.UniqueIdResolverFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for IdentityMgtComponent.
 */
public class IdentityMgtComponentTest {

    private static UniqueIdResolverFactory resolverFactory = Mockito.mock(UniqueIdResolverFactory.class);

    @Test
    public void testRegisterUniqueIdResolverFactory() throws Exception {

        Map<String, String> properties = new HashMap<>(1);
        properties.put(IdentityMgtComponent.CONNECTOR_TYPE, "test");

        IdentityMgtComponent identityMgtComponent = new IdentityMgtComponent();
        identityMgtComponent.registerUniqueIdResolverFactory(resolverFactory,  properties);

        Assert.assertNotNull(IdentityMgtDataHolder.getInstance().getUniqueIdResolverFactoryMap().get("test"));
        Assert.assertEquals(IdentityMgtDataHolder.getInstance().getUniqueIdResolverFactoryMap().get("test"),
                            resolverFactory);
    }

    @Test
    public void testUnregisterUniqueIdResolverFactory() throws Exception {

        Map<String, String> properties = new HashMap<>(1);
        properties.put(IdentityMgtComponent.CONNECTOR_TYPE, "test");

        IdentityMgtComponent identityMgtComponent = new IdentityMgtComponent();
        identityMgtComponent.registerUniqueIdResolverFactory(resolverFactory,  properties);

        identityMgtComponent.unregisterUniqueIdResolverFactory(resolverFactory);
        Assert.assertNull(IdentityMgtDataHolder.getInstance().getUniqueIdResolverFactoryMap().get("test"));

    }

}

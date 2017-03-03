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

package org.wso2.carbon.identity.mgt.store.test.unit;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.mgt.connector.Attribute;

/**
 * Tests for Attribute Class
 */
@PrepareForTest(Attribute.class)
public class AttributeTests {
    private Attribute attribute;
    private String attributeName = "testAttributeName";
    private String attributeValue = "testAttributeValue";

    @Test
    public void testConstructor() {
        attribute = new Attribute(attributeName, attributeValue);
        Assert.assertEquals(attribute.getAttributeName(), attributeName, "Attribute Name is incorrect.");
        Assert.assertEquals(attribute.getAttributeValue(), attributeValue, "Attribute Value is incorrect.");
    }

    @Test(dependsOnMethods = {"testConstructor"})
    public void testEquals() {
        Attribute attribute1 = new Attribute(attributeName, attributeValue);
        Assert.assertTrue(attribute.equals(attribute1), "Equal attributes didn't get matched.");
        Assert.assertFalse(attribute.equals(null), "Null ojb shouldn't be equal.");
    }

    @Test(dependsOnMethods = {"testConstructor"})
    public void testHashCode() {
        Assert.assertNotNull(attribute.hashCode(), "Hashcode should be returned.");
    }

    @Test(dependsOnMethods = {"testEquals"})
    public void testSetters() {
        String newName = "NewAttributeName";
        String newValue = "NewAttributeValue";

        attribute.setAttributeName(newName);
        attribute.setAttributeValue(newValue);
        Assert.assertEquals(attribute.getAttributeName(), newName, "Attribute Name is not properly set.");
        Assert.assertEquals(attribute.getAttributeValue(), newValue, "Attribute Value is not properly set.");
    }
}

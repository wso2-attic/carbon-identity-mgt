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

package org.wso2.carbon.identity.mgt.connector;

/**
 * Represents an attribute.
 */
public class Attribute {

    private String attributeName;

    private String attributeValue;

    public Attribute() {

    }

    public Attribute(String attributeName, String attributeValue) {

        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Attribute attr = (Attribute) obj;
        return this.attributeName.equals(attr.attributeName) && this.getAttributeValue().equals(attr.attributeValue);
    }

    @Override
    public int hashCode() {
        int result = attributeName != null ? attributeName.hashCode() : 0;
        result = 31 * result + (attributeValue != null ? attributeValue.hashCode() : 0);
        return result;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

}

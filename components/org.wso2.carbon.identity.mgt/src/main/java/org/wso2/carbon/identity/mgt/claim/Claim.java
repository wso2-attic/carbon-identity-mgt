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

package org.wso2.carbon.identity.mgt.claim;

import java.io.Serializable;

/**
 * Represents a claim that is associated with an entity usually a user.
 * Claims describe the capabilities associated with some entity in the system.
 * A claim is the expression of a right with respect to a particular value.
 * Hence a claim has a uri, value and many other properties.
 * This class models the properties of a claim.
 */
public class Claim implements Serializable {

    private static final long serialVersionUID = 4070228119280928289L;
    /**
     * An URI to uniquely identify the dialect of a claim.
     */
    private String dialectUri;

    /**
     * An URI to uniquely identify a given claim. This is the one used by the
     * top layers applications are aware of.
     */
    private String claimUri;

    /**
     * This is the value of the claim.
     */
    private String value;

    public Claim() {

    }

    public Claim(String dialectUri, String claimUri, String value) {
        this.dialectUri = dialectUri;
        this.claimUri = claimUri;
        this.value = value;
    }

    public String getDialectUri() {
        return dialectUri;
    }

    public void setDialectUri(String dialectUri) {
        this.dialectUri = dialectUri;
    }

    public String getClaimUri() {
        return claimUri;
    }

    public void setClaimUri(String claimUri) {
        this.claimUri = claimUri;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}

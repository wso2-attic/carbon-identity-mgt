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

import java.util.Collections;
import java.util.Map;

/**
 * Meta Claim.
 */
public class MetaClaim {

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
     * Other properties related to the claim.
     */
    private Map<String, String> properties;

    public MetaClaim() {

    }

    public MetaClaim(String dialectUri, String claimUri, Map<String, String> properties) {
        this.dialectUri = dialectUri;
        this.claimUri = claimUri;
        this.properties = properties;
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

    public String getProperty(String key) {
        return properties.get(key);
    }

    public Map<String, String> getProperties() {

        if (properties == null) {
            return Collections.emptyMap();
        }
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

}

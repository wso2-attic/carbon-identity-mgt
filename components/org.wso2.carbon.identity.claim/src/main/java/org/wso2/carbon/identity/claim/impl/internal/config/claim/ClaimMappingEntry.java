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

package org.wso2.carbon.identity.claim.impl.internal.config.claim;

import java.util.Collections;
import java.util.Map;

/**
 * Claim Mapping Entry.
 */
public class ClaimMappingEntry {

    private String dialectURI;
    private String inherits;
    private Map<String, String> mappings;

    public String getMappingDialectURI() {
        return dialectURI;
    }

    public void setMappingDialectURI(String dialectURI) {
        this.dialectURI = dialectURI;
    }

    public Map<String, String> getMappings() {
        if (mappings == null) {
            return Collections.emptyMap();
        } else {
            return mappings;
        }
    }

    public void setMappings(Map<String, String> mappings) {
        this.mappings = mappings;
    }

    public String getInherits() {
        return inherits;
    }

    public void setInherits(String inherits) {
        this.inherits = inherits;
    }

}

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

package org.wso2.carbon.identity.mgt.internal.config.claim.mapping;

import java.util.Map;

/**
 * Claim Mapping File.
 */
public class ClaimMappingFile {

    //Map<travelocity, Entry>
    private Map<String, ClaimMappingEntry> applicationMappings;
    private Map<String, ClaimMappingEntry> idpMappings;
    private Map<String, ClaimMappingEntry> standardMappings;

    public ClaimMappingEntry getApplicationClaimMapping(String name) {

        if (applicationMappings == null) {
            return null;
        }
        return applicationMappings.get(name);
    }

    public void setApplicationClaimMappings(Map<String, ClaimMappingEntry> claims) {
        this.applicationMappings = claims;
    }

    public ClaimMappingEntry getIdpMappings(String name) {

        if (idpMappings == null) {
            return null;
        }
        return idpMappings.get(name);
    }

    public void setIdpMappings(Map<String, ClaimMappingEntry> claims) {
        this.idpMappings = claims;
    }

    public ClaimMappingEntry getStandardMappings(String name) {

        if (standardMappings == null) {
            return null;
        }
        return standardMappings.get(name);
    }

    public void setStandardMappings(Map<String, ClaimMappingEntry> claims) {
        this.standardMappings = claims;
    }
}

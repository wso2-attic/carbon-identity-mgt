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

package org.wso2.carbon.identity.meta.claim.mgt.mapping.profile;

import java.util.List;

/**
 * Claim Mapping Entry.
 */
public class ProfileEntry {

    private String profileName;

    private List<ClaimConfigEntry> claims;

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public List<ClaimConfigEntry> getClaims() {
        return claims;
    }

    public void setClaims(List<ClaimConfigEntry> claims) {
        this.claims = claims;
    }
}

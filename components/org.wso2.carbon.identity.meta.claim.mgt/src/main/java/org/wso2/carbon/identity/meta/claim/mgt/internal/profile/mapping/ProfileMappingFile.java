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

package org.wso2.carbon.identity.meta.claim.mgt.internal.profile.mapping;

import java.util.Collections;
import java.util.List;

/**
 * Claim Mapping File.
 */
public class ProfileMappingFile {

    //List<Entry>
    private List<ProfileMappingEntry> users;
    private List<ProfileMappingEntry> applications;
    private List<ProfileMappingEntry> identityProviders;

    public List<ProfileMappingEntry> getApplicationClaimMapping() {

        if (applications == null) {
            return Collections.emptyList();
        }
        return applications;
    }

    public void setApplicationClaimMappings(List<ProfileMappingEntry> claims) {
        this.applications = claims;
    }

    public List<ProfileMappingEntry> getIdpMappings() {

        if (identityProviders == null) {
            return Collections.emptyList();
        }
        return identityProviders;
    }

    public void setIdpMappings(List<ProfileMappingEntry> claims) {
        this.identityProviders = claims;
    }

    public List<ProfileMappingEntry> getUserMappings() {

        if (users == null) {
            return Collections.emptyList();
        }
        return users;
    }

    public void setStandardMappings(List<ProfileMappingEntry> claims) {
        this.users = claims;
    }
}

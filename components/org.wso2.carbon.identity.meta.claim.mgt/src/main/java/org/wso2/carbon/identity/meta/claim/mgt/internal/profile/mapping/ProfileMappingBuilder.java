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

import org.wso2.carbon.identity.meta.claim.mgt.exception.ClaimMappingBuilderException;
import org.wso2.carbon.identity.meta.claim.mgt.util.ProfileMgtConstants;
import org.wso2.carbon.identity.mgt.exception.CarbonIdentityMgtConfigException;
import org.wso2.carbon.identity.mgt.util.FileUtil;
import org.wso2.carbon.identity.mgt.util.IdentityMgtConstants;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Profile Mapping Builder.
 */
public class ProfileMappingBuilder {

    private ProfileMappingFile profileConfig = null;
    //Map(ApplicationNAme, Map(External Claim : Root claim))
    private Map<String, Map<String, String>> profiles;

    private ProfileMappingBuilder() throws ClaimMappingBuilderException {

        Path file = Paths.get(IdentityMgtConstants.getCarbonHomeDirectory().toString(), "conf", "identity",
                ProfileMgtConstants.PROFILE_MAPPING_FILE);
        try {
            profileConfig = FileUtil.readConfigFile(file, ProfileMappingFile.class);
        } catch (CarbonIdentityMgtConfigException e) {
            throw new ClaimMappingBuilderException("Couldn't read the claim-mapping.yml file successfully.", e);
        }
        profiles = profileConfig.getProfileClaimMapping().stream().filter(Objects::nonNull)
                .filter(profileMappingEntry -> !profileMappingEntry.getProperties().isEmpty()).collect(Collectors
                        .toMap(profileMappingEntry -> profileMappingEntry.getClaim(),
                                profileMappingEntry -> profileMappingEntry.getProperties()));

    }

    public static ProfileMappingBuilder getInstance() throws ClaimMappingBuilderException {
        return ProfileMappingBuilderHolder.PROFILE_MAPPING_BUILDER;
    }

    /**
     * Provides the claim mappings for a defined user profile
     *
     * @param profileName : Name to identify the application
     * @return Map(application claim : root claim URI)
     */
    public Map<String, String> getUserProfileMapping(String profileName) {
        return profiles.get(profileName);

    }

    private static class ProfileMappingBuilderHolder {
        private static final ProfileMappingBuilder PROFILE_MAPPING_BUILDER;

        static {
            try {
                PROFILE_MAPPING_BUILDER = new ProfileMappingBuilder();
            } catch (ClaimMappingBuilderException e) {
                throw new ExceptionInInitializerError(e);
            }
        }
    }

}

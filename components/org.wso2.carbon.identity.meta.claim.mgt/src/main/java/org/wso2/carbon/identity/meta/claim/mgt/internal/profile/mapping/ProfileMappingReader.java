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

import org.wso2.carbon.identity.meta.claim.mgt.exception.ProfileReaderException;
import org.wso2.carbon.identity.meta.claim.mgt.util.ProfileMgtConstants;
import org.wso2.carbon.identity.mgt.exception.CarbonIdentityMgtConfigException;
import org.wso2.carbon.identity.mgt.impl.util.FileUtil;
import org.wso2.carbon.identity.mgt.impl.util.IdentityMgtConstants;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Profile Mapping Builder.
 */
public class ProfileMappingReader {

    public ProfileMappingReader() {

    }

    private static ProfileMappingFile buildProfileMappings() throws ProfileReaderException {
        ProfileMappingFile profileMappingFile = null;

        Path file = Paths.get(IdentityMgtConstants.getCarbonHomeDirectory().toString(), "conf", "identity",
                ProfileMgtConstants.PROFILE_MAPPING_FILE);
        try {
            profileMappingFile = FileUtil.readConfigFile(file, ProfileMappingFile.class);
        } catch (CarbonIdentityMgtConfigException e) {
            throw new ProfileReaderException("Couldn't read the profile-mapping.yaml file successfully.", e);
        }

        return profileMappingFile;
    }

    /**
     * Provides the set of claims with their properties for a given profile.
     *
     * @return Map(profileName : claim configurations)
     */
    public static Map<String, ProfileEntry> getProfileMappings() throws ProfileReaderException {
        ProfileMappingFile profileMappingFile = buildProfileMappings();
        List<ProfileEntry> profileEntryList = profileMappingFile.getProfileClaimMapping();
        return profileEntryList.stream().filter(Objects::nonNull)
                .filter(profileEntry -> !profileEntry.getClaims().isEmpty())
                .collect(Collectors.toMap(profileEntry -> profileEntry.getProfileName(), profileEntry -> profileEntry));

    }

}

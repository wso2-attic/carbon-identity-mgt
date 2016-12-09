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
package org.wso2.carbon.identity.meta.claim.mgt.service.impl;

import org.wso2.carbon.identity.meta.claim.mgt.exception.ProfileMgtServiceException;
import org.wso2.carbon.identity.meta.claim.mgt.exception.ProfileReaderException;
import org.wso2.carbon.identity.meta.claim.mgt.internal.profile.mapping.ClaimConfigEntry;
import org.wso2.carbon.identity.meta.claim.mgt.internal.profile.mapping.ProfileEntry;
import org.wso2.carbon.identity.meta.claim.mgt.internal.profile.mapping.ProfileMappingReader;
import org.wso2.carbon.identity.meta.claim.mgt.service.ProfileMgtService;
import org.wso2.carbon.kernel.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service implementation to retrieve claims attached with profiles.
 */
public class ProfileMgtServiceImpl implements ProfileMgtService {
    private Map<String, ProfileEntry> profiles = null;
    private Map<String, List<String>> requiredClaims = new HashMap<>();
    private Map<String, List<String>> readOnlyClaims = new HashMap<>();
    private Map<String, List<String>> verifyingClaims = new HashMap<>();
    private Map<String, List<String>> validatingClaims = new HashMap<>();
    private Map<String, List<String>> transformingClaims = new HashMap<>();

    private Map<String, ProfileEntry> buildProfileMappings() throws ProfileReaderException {
        if (profiles == null) {
            profiles = ProfileMappingReader.getProfileMappings();

            profiles.forEach(((profileName, profileEntry) -> {
                ArrayList<String> requiredList = new ArrayList<String>();
                ArrayList<String> readOnlyList = new ArrayList<String>();
                ArrayList<String> verifyingList = new ArrayList<String>();
                ArrayList<String> validatingList = new ArrayList<String>();
                ArrayList<String> transformingList = new ArrayList<String>();
                profileEntry.getClaims().stream().forEach(claimConfigEntry -> {

                    if (Boolean.parseBoolean(claimConfigEntry.getRequired())) {
                        requiredList.add(claimConfigEntry.getClaimURI());
                    }
                    if (Boolean.parseBoolean(claimConfigEntry.getReadonly())) {
                        readOnlyList.add(claimConfigEntry.getClaimURI());
                    }
                    if (!StringUtils.isNullOrEmptyAfterTrim(claimConfigEntry.getValidator())) {
                        validatingList.add(claimConfigEntry.getClaimURI());
                    }
                    if (!StringUtils.isNullOrEmptyAfterTrim(claimConfigEntry.getVerifier())) {
                        verifyingList.add(claimConfigEntry.getClaimURI());
                    }
                    if (!StringUtils.isNullOrEmptyAfterTrim(claimConfigEntry.getTransformer())) {
                        transformingList.add(claimConfigEntry.getTransformer());
                    }
                });
                requiredClaims.put(profileName, requiredList);
                readOnlyClaims.put(profileName, readOnlyList);
                verifyingClaims.put(profileName, verifyingList);
                validatingClaims.put(profileName, validatingList);
                transformingClaims.put(profileName, transformingList);
            }));
        }
        return profiles;
    }

    /**
     * Get the claims set of profiles.
     *
     * @return Map(profileName,(Map(claim, Map(Property Key: Property Value))) with the set of claims and their
     * properties.
     * @throws ProfileMgtServiceException : Error in getting the profile.
     */
    @Override
    public Map<String, ProfileEntry> getProfiles() throws ProfileMgtServiceException {
        try {
            return buildProfileMappings();
        } catch (ProfileReaderException e) {
            throw new ProfileMgtServiceException("Error in getting the profile configuration details.", e);
        }
    }

    /**
     * Get the claims set of a profile.
     *
     * @param profileName : Uniquely identifying name of the profile.
     * @return Map(claim, Map(Property Key: Property Value)) with the set of claims and their properties.
     * @throws ProfileMgtServiceException : Error in getting the profile.
     */
    @Override
    public ProfileEntry getProfile(String profileName) throws ProfileMgtServiceException {
        try {
            return buildProfileMappings().get(profileName);
        } catch (ProfileReaderException e) {
            throw new ProfileMgtServiceException(
                    String.format("Error in getting the profile configuration for profile: %s", profileName), e);
        }
    }

    /**
     * Get the properties of a particular claim of a profile.
     *
     * @param profileName : Uniquely identifying name of the profile.
     * @param claimURI    : Root claim URI for the properties to be retrieved.
     * @return Map(Property Key : Property Value)
     * @throws ProfileMgtServiceException : Error in getting the properties of a claim.
     */
    @Override
    public ClaimConfigEntry getClaimProperties(String profileName, String claimURI) throws ProfileMgtServiceException {
        try {
            return buildProfileMappings().get(profileName).getClaims().stream()
                    .filter(claimConfigEntry -> claimConfigEntry.getClaimURI().equalsIgnoreCase(claimURI)).findFirst()
                    .get();
        } catch (ProfileReaderException e) {
            throw new ProfileMgtServiceException(
                    String.format("Error in getting the claim details for profile: %s and claim: %s", profileName,
                            claimURI), e);
        }
    }

    /**
     * Get the claims marked as required for a particular profile.
     *
     * @param profileName : Uniquely identifying name of the profile.
     * @return List(Root claim URIs)
     * @throws ProfileMgtServiceException : Error in getting the claims with required property.
     */
    @Override
    public List<String> getRequiredProperties(String profileName) throws ProfileMgtServiceException {
        return requiredClaims.get(profileName);
    }

    /**
     * Get the claims marked as read-only for a particular profile.
     *
     * @param profileName : Uniquely identifying name of the profile.
     * @return List(Root claim URIs)
     * @throws ProfileMgtServiceException : Error in getting the claims with read-only property.
     */
    @Override
    public List<String> getReadOnlyProperties(String profileName) throws ProfileMgtServiceException {
        return readOnlyClaims.get(profileName);
    }

    /**
     * Get the claims marked as unique for a particular profile.
     *
     * @param profileName : Uniquely identifying name of the profile.
     * @return List(Root claim URIs)
     * @throws ProfileMgtServiceException : Error in getting the claims with unique property.
     */
    @Override
    public List<String> getUniqueProperties(String profileName) throws ProfileMgtServiceException {
        //ToDO call underneath connector layer and return this.
        return null;
    }

    /**
     * Get the claims marked as verify for a particular profile.
     *
     * @param profileName : Uniquely identifying name of the profile.
     * @return List(Root claim URIs)
     * @throws ProfileMgtServiceException : Error in getting the claims with 'verify' property.
     */
    @Override
    public List<String> getVerifyingProperties(String profileName) throws ProfileMgtServiceException {
        return verifyingClaims.get(profileName);
    }

    /**
     * Get the claims marked as verify with the verifying mechanism for a particular profile.
     *
     * @param profileName : Uniquely identifying name of the profile.
     * @return Map(Claim : Verifying mechanism)
     * @throws ProfileMgtServiceException : Error in getting the claims with verifying mechanism.
     */
    @Override
    public List<String> getValidating(String profileName) throws ProfileMgtServiceException {
        return validatingClaims.get(profileName);
    }

    /**
     * Get the claims marked for regex validations.
     *
     * @param profileName : Uniquely identifying name of the profile.
     * @return Map(Claim : Regex)
     * @throws ProfileMgtServiceException : Error in getting the claims with regex validations.
     */
    @Override
    public List<String> getTransformingProperties(String profileName) throws ProfileMgtServiceException {
        return transformingClaims.get(profileName);
    }
}

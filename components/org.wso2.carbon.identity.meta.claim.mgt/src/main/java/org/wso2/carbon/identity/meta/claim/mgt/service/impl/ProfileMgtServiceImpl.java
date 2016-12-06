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
import org.wso2.carbon.identity.meta.claim.mgt.service.ProfileMgtService;

import java.util.List;
import java.util.Map;

/**
 * Service implementation to manage claims attached with profiles.
 */
public class ProfileMgtServiceImpl implements ProfileMgtService {
    /**
     * Get the claims set of profiles.
     *
     * @return Map(profileName,(Map(claim, Map(Property Key: Property Value))) with the set of claims and their properties.
     * @throws ProfileMgtServiceException : Error in getting the profile.
     */
    @Override
    public Map<String, Map<String, Map<String, String>>> getProfiles() throws ProfileMgtServiceException {
        return null;
    }

    /**
     * Get the claims set of a profile.
     *
     * @param profileName : Uniquely identifying name of the profile.
     * @return Map(claim, Map(Property Key: Property Value)) with the set of claims and their properties.
     * @throws ProfileMgtServiceException : Error in getting the profile.
     */
    @Override
    public Map<String, Map<String, String>> getProfile(String profileName) throws ProfileMgtServiceException {
        return null;
    }

    /**
     * Get the properties of a particular claim of a profile.
     *
     * @param profileName : Uniquely identifying name of the profile.
     * @param claim       : Root claim URI for the properties to be retrieved.
     * @return Map(Property Key : Property Value)
     * @throws ProfileMgtServiceException : Error in getting the properties of a claim.
     */
    @Override
    public Map<String, String> getClaimProperties(String profileName, String claim) throws ProfileMgtServiceException {
        return null;
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
        return null;
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
        return null;
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
        return null;
    }

    /**
     * Get the claims marked as verify with the verifying mechanism for a particular profile.
     *
     * @param profileName : Uniquely identifying name of the profile.
     * @return Map(Claim : Verifying mechanism)
     * @throws ProfileMgtServiceException : Error in getting the claims with verifying mechanism.
     */
    @Override
    public Map<String, String> getVerifyingPropertyMechanisms(String profileName) throws ProfileMgtServiceException {
        return null;
    }

    /**
     * Get the claims marked for regex validations.
     *
     * @param profileName : Uniquely identifying name of the profile.
     * @return Map(Claim : Regex)
     * @throws ProfileMgtServiceException : Error in getting the claims with regex validations.
     */
    @Override
    public Map<String, String> getRegexedProperties(String profileName) throws ProfileMgtServiceException {
        return null;
    }
}

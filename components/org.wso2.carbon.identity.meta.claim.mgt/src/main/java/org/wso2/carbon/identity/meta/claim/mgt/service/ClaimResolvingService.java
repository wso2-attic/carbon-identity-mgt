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
package org.wso2.carbon.identity.meta.claim.mgt.service;

import org.wso2.carbon.identity.meta.claim.mgt.exception.ClaimResolvingServiceException;

import java.util.Map;

/**
 * Provides the claim mappings for applications, idps and standards.
 */
public interface ClaimResolvingService {

    /**
     * Provides claim mappings for the dialect.
     *
     * @param dialectURI : Uniquely identifying URI for the dialect.
     * @return Map(application claims : root claim)
     * @throws ClaimResolvingServiceException : Error in getting the claim mapping for dialect.
     */
    Map<String, String> getClaimMapping(String dialectURI) throws ClaimResolvingServiceException;

    /**
     * Provides all the claim mappings.
     *
     * @return Map(application claims : root claim)
     * @throws ClaimResolvingServiceException : Error in getting the claim mappings.
     */
    public Map<String, Map<String, String>> getClaimMapping() throws ClaimResolvingServiceException;

}

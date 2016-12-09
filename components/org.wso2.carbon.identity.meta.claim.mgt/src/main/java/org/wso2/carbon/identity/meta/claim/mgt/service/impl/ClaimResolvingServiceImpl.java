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

import org.wso2.carbon.identity.meta.claim.mgt.exception.ClaimMappingReaderException;
import org.wso2.carbon.identity.meta.claim.mgt.exception.ClaimResolvingServiceException;
import org.wso2.carbon.identity.meta.claim.mgt.internal.claim.mapping.ClaimMappingEntry;
import org.wso2.carbon.identity.meta.claim.mgt.internal.claim.mapping.ClaimMappingReader;
import org.wso2.carbon.identity.meta.claim.mgt.service.ClaimResolvingService;

import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
public class ClaimResolvingServiceImpl implements ClaimResolvingService {
    Map<String, Map<String, String>> claimMappings = null;

    private static Map<String, String> getMappings(ClaimMappingEntry claimMappingEntry) {
        return claimMappingEntry.getMappings().entrySet().stream().collect(Collectors
                .toMap(p -> appendDialect(claimMappingEntry.getMappingDialectURI(), p.getKey()), Map.Entry::getValue));

    }

    private static String appendDialect(String dialect, String claim) {
        if (dialect.isEmpty()) {
            return claim;
        }
        //In case claim dialect in not followed by '/', add it.
        if (!dialect.endsWith("/")) {
            dialect = dialect + "/";
        }
        return dialect + claim;
    }

    private Map<String, Map<String, String>> buildClaimMappings() throws ClaimMappingReaderException {
        if (claimMappings == null) {
            claimMappings = ClaimMappingReader.getClaimMappings().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> getMappings(entry.getValue())));
        }
        return claimMappings;
    }

    /**
     * Provides claim mappings for applications.
     *
     * @return Map(application claims : root claim)
     * @throws ClaimResolvingServiceException : Error in getting the claim mapping for application.
     */
    @Override
    public Map<String, Map<String, String>> getClaimMapping() throws ClaimResolvingServiceException {
        try {

            return buildClaimMappings();
        } catch (ClaimMappingReaderException e) {
            throw new ClaimResolvingServiceException("Error while getting the claim mappings.", e);
        }
    }

    /**
     * Provides claim mappings for the dialect.
     *
     * @param dialectURI : Uniquely identifying URI for the dialect.
     * @return Map(application claims : root claim)
     * @throws ClaimResolvingServiceException : Error in getting the claim mapping for dialect.
     */
    @Override
    public Map<String, String> getClaimMapping(String dialectURI) throws ClaimResolvingServiceException {
        try {
            return buildClaimMappings().get(dialectURI);
        } catch (ClaimMappingReaderException e) {
            throw new ClaimResolvingServiceException("Error while getting the claim mapping for dialect:." + dialectURI,
                    e);
        }
    }

}

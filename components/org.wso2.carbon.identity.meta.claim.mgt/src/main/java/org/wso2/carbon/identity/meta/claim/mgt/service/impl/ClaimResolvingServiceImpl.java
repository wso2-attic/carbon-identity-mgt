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
import org.wso2.carbon.kernel.utils.StringUtils;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 */
public class ClaimResolvingServiceImpl implements ClaimResolvingService {
    //Map(dialectURI, Map(external claim URI: root claim URI))
    Map<String, Map<String, String>> claimMappings = null;

    private Map<String, String> getMappings(ClaimMappingEntry claimMappingEntry) {
        return claimMappingEntry.getMappings().entrySet().stream().collect(Collectors
                .toMap(p -> appendDialect(claimMappingEntry.getMappingDialectURI(), p.getKey()), Map.Entry::getValue));

    }

    private String appendDialect(String dialect, String claim) {
        if (dialect.isEmpty()) {
            return claim;
        }
        //In case claim dialect is not followed by '/', add it.
        if (!dialect.endsWith("/")) {
            dialect = dialect + "/";
        }
        return dialect + claim;
    }

    private Map<String, Map<String, String>> buildClaimMappings() throws ClaimMappingReaderException {
        if (claimMappings == null) {
            Set<Map.Entry<String, ClaimMappingEntry>> claimEntrySet = ClaimMappingReader.getClaimMappings().entrySet();
            Map<String, Map<String, String>> initialMappings = claimEntrySet.stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> getMappings(entry.getValue())));
            claimMappings = claimEntrySet.stream().collect(Collectors
                    .toMap(Map.Entry::getKey, entry -> resolveInheritingDialects(initialMappings, entry.getValue())));

        }
        return claimMappings;
    }

    private Map<String, String> resolveInheritingDialects(Map<String, Map<String, String>> initialMappings,
            ClaimMappingEntry claimMappingEntry) {

        Map<String, String> ownClaims = initialMappings.get(claimMappingEntry.getMappingDialectURI());

        if (!StringUtils.isNullOrEmptyAfterTrim(claimMappingEntry.getInherits())) {
            Map<String, String> inheritingMap = initialMappings.get(claimMappingEntry.getInherits());
            if (claimMappingEntry.isOverridingInheritingDialectURI()) {
                inheritingMap = inheritingMap.entrySet().stream().collect(Collectors
                        .toMap(p -> appendDialect(claimMappingEntry.getMappingDialectURI(),
                                p.getKey().replaceFirst(claimMappingEntry.getInherits(), "")), Map.Entry::getValue));
                ownClaims.putAll(inheritingMap);
                return ownClaims;
            } else {
                ownClaims.putAll(inheritingMap);
                //ToDO address if both the dialects have mappings for same. No inheriting chains supported yet.
                return ownClaims;
            }
        } else {
            return ownClaims;
        }
    }

    /**
     * Provides claim mappings for applications.
     *
     * @return Map(application claims : root claim)
     * @throws ClaimResolvingServiceException : Error in getting the claim mapping for application.
     */
    @Override
    public Map<String, Map<String, String>> getClaimMappings() throws ClaimResolvingServiceException {
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

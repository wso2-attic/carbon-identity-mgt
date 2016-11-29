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

import org.wso2.carbon.identity.mgt.exception.CarbonSecurityConfigException;
import org.wso2.carbon.identity.mgt.util.FileUtil;
import org.wso2.carbon.identity.mgt.util.IdentityMgtConstants;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Claim Mapping Builder.
 */
public class ClaimMappingBuilder {

    private ClaimMappingFile claimConfig = null;
    //Map(ApplicationNAme, Map(External Claim : Root claim))
    private Map<String, Map<String, String>> applicationMappings;
    private Map<String, Map<String, String>> idpMappings;
    private Map<String, Map<String, String>> standardMappings;

    private static class ClaimMappingBuilderHolder {
        private static final ClaimMappingBuilder CLAIM_MAPPING_BUILDER;

        static {
            try {
                CLAIM_MAPPING_BUILDER = new ClaimMappingBuilder();
            } catch (CarbonSecurityConfigException e) {
                throw new ExceptionInInitializerError(e);
            }
        }
    }

    private ClaimMappingBuilder() throws CarbonSecurityConfigException {

        Path file = Paths.get(IdentityMgtConstants.getCarbonHomeDirectory().toString(), "conf", "identity",
                IdentityMgtConstants.CLAIM_MAPPING_FILE);
        claimConfig = FileUtil.readConfigFile(file, ClaimMappingFile.class);
        applicationMappings = claimConfig.getApplicationClaimMapping().stream().filter(Objects::nonNull)
                .filter(claimMappingEntry -> !claimMappingEntry.getMappings().isEmpty()).collect(Collectors
                        .toMap(claimMappingEntry -> claimMappingEntry.getName(),
                                claimMappingEntry -> getMappings(claimMappingEntry)));

        idpMappings = claimConfig.getIdpMappings().stream().filter(Objects::nonNull)
                .filter(claimMappingEntry -> !claimMappingEntry.getMappings().isEmpty()).collect(Collectors
                        .toMap(claimMappingEntry -> claimMappingEntry.getName(),
                                claimMappingEntry -> getMappings(claimMappingEntry)));

        standardMappings = claimConfig.getStandardMappings().stream().filter(Objects::nonNull)
                .filter(claimMappingEntry -> !claimMappingEntry.getMappings().isEmpty()).collect(Collectors
                        .toMap(claimMappingEntry -> claimMappingEntry.getName(),
                                claimMappingEntry -> getMappings(claimMappingEntry)));

    }

    public static ClaimMappingBuilder getInstance() throws CarbonSecurityConfigException {
        return ClaimMappingBuilderHolder.CLAIM_MAPPING_BUILDER;
    }

    /**
     * Provides the claim mappings of a given application
     *
     * @param applicationName : Name to identify the application
     * @return Map(application claim : root claim URI)
     * @throws CarbonSecurityConfigException
     */
    public Map<String, String> getApplicationClaimMapping(String applicationName) throws CarbonSecurityConfigException {
        return applicationMappings.get(applicationName);

    }

    /**
     * Provides the claim mappings of a given idp
     *
     * @param idpName : Name to identify the idp
     * @return Map(idp claim : root claim URI)
     * @throws CarbonSecurityConfigException
     */
    public Map<String, String> getIdpClaimMapping(String idpName) throws CarbonSecurityConfigException {
        return idpMappings.get(idpName);

    }

    /**
     * Provides the claim mappings of a given standard
     *
     * @param standardName : Name to identify the standard
     * @return Map(standard claim : root claim URI)
     * @throws CarbonSecurityConfigException
     */
    public Map<String, String> getStandardClaimMapping(String standardName) throws CarbonSecurityConfigException {
        return standardMappings.get(standardName);

    }

    private Map<String, String> getMappings(ClaimMappingEntry claimMappingEntry) {
        return claimMappingEntry.getMappings().entrySet().stream().collect(Collectors
                .toMap(p -> appendDialect(claimMappingEntry.getMappingDialectURI(), p.getKey()), Map.Entry::getValue));

    }

    private String appendDialect(String dialect, String claim) {
        if (dialect.isEmpty()) {
            return claim;
        }
        //In case claim dialect in not followed by '/', add it.
        if (!dialect.endsWith("/")) {
            dialect = dialect + "/";
        }
        return dialect + claim;
    }

}

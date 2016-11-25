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
import java.util.stream.Collectors;

/**
 * Claim Mapping Builder.
 */
public class ClaimMappingBuilder {

    private static ClaimMappingBuilder instance = new ClaimMappingBuilder();

    private ClaimMappingBuilder() {

    }

    public static ClaimMappingBuilder getInstance() {
        return instance;
    }

    private ClaimMappingFile buildClaimConfig() throws CarbonSecurityConfigException {

        Path file = Paths.get(IdentityMgtConstants.getCarbonHomeDirectory().toString(), "conf", "identity",
                IdentityMgtConstants.CLAIM_MAPPING_FILE);

        // claim-mapping.yml is a not a mandatory configuration file.
        return FileUtil.readConfigFile(file, ClaimMappingFile.class);
    }

    /**
     * Provides the claim mappings of a given application
     *
     * @param applicationName : Name to identify the application
     * @return Map(application claim : root claim URI)
     * @throws CarbonSecurityConfigException
     */
    public Map<String, String> getApplicationClaimMappings(String applicationName)
            throws CarbonSecurityConfigException {
        return getMappings(applicationName);

    }

    /**
     * Provides the claim mappings of a given idp
     *
     * @param idpName : Name to identify the idp
     * @return Map(idp claim : root claim URI)
     * @throws CarbonSecurityConfigException
     */
    public Map<String, String> getIdpClaimMappings(String idpName) throws CarbonSecurityConfigException {
        return getMappings(idpName);

    }

    /**
     * Provides the claim mappings of a given standard
     *
     * @param standardName : Name to identify the standard
     * @return Map(standard claim : root claim URI)
     * @throws CarbonSecurityConfigException
     */
    public Map<String, String> getStandardClaimMappings(String standardName) throws CarbonSecurityConfigException {
        return getMappings(standardName);

    }

    private Map<String, String> getMappings(String appName) throws CarbonSecurityConfigException {
        ClaimMappingFile claimMappingFile = buildClaimConfig();
        ClaimMappingEntry claimMappings = claimMappingFile.getApplicationClaimMapping(appName);

        if (claimMappings == null) {
            throw new CarbonSecurityConfigException("Invalid claim configuration found.");
        }

        return claimMappings.getMappings().entrySet().stream().collect(Collectors
                .toMap(p -> appendDialect(claimMappings.getMappingDialectURI(), p.getKey()), Map.Entry::getValue));

    }

    private String appendDialect(String dialect, String claim) {
        //In case claim dialect in not followed by '/', add it.
        if (!dialect.endsWith("/")) {
            dialect = dialect + "/";
        }
        return dialect + claim;
    }

}

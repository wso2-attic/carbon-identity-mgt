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

package org.wso2.carbon.identity.mgt.internal.config.claim;

import org.wso2.carbon.identity.mgt.claim.MetaClaim;
import org.wso2.carbon.identity.mgt.exception.CarbonSecurityConfigException;
import org.wso2.carbon.identity.mgt.util.FileUtil;
import org.wso2.carbon.identity.mgt.util.IdentityMgtConstants;
import org.wso2.carbon.kernel.utils.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Claim Config Builder.
 */
public class ClaimConfigBuilder {

    private static ClaimConfigBuilder instance = new ClaimConfigBuilder();

    private ClaimConfigBuilder() {

    }

    public static ClaimConfigBuilder getInstance() {
        return instance;
    }

    public Map<String, MetaClaim> getMetaClaims() throws CarbonSecurityConfigException {

        ClaimStoreFile claimStoreFile = buildClaimConfig();

        if (claimStoreFile.getClaims().isEmpty()) {
            throw new CarbonSecurityConfigException("Invalid claim configuration found.");
        }

        return claimStoreFile.getClaims().stream()
                .filter(Objects::nonNull)
                .filter(claimStoreEntry -> !StringUtils.isNullOrEmpty(claimStoreEntry.getClaimURI())
                        && !StringUtils.isNullOrEmpty(claimStoreEntry.getDialectURI()))
                .map(claimStoreEntry -> new MetaClaim(claimStoreEntry.getDialectURI(), claimStoreEntry.getClaimURI(),
                        claimStoreEntry.getProperties()))
                .collect(Collectors.toMap(MetaClaim::getClaimURI, metaClaim -> metaClaim));
    }

    private ClaimStoreFile buildClaimConfig() throws CarbonSecurityConfigException {

        Path file = Paths.get(IdentityMgtConstants.getCarbonHomeDirectory().toString(), "conf", "identity",
                IdentityMgtConstants.CLAIM_STORE_FILE);

        // claim-store.yml is a mandatory configuration file.
        return FileUtil.readConfigFile(file, ClaimStoreFile.class);
    }

}

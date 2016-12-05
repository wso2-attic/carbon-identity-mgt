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

package org.wso2.carbon.identity.meta.claim.mgt.internal;

import org.wso2.carbon.identity.meta.claim.mgt.exception.IdentityClaimMgtDataHolderException;
import org.wso2.carbon.identity.meta.claim.mgt.service.ClaimResolvingService;

/**
 * Carbon security data holder.
 *
 * @since 1.0.0
 */
public class IdentityClaimMgtDataHolder {

    private static IdentityClaimMgtDataHolder instance = new IdentityClaimMgtDataHolder();

    private ClaimResolvingService claimResolvingService;

    private IdentityClaimMgtDataHolder() {

    }

    /**
     * Get the instance of this class.
     *
     * @return IdentityMgtDataHolder.
     */
    public static IdentityClaimMgtDataHolder getInstance() {
        return instance;
    }

    public ClaimResolvingService getClaimResolvingService() throws IdentityClaimMgtDataHolderException {

        if (claimResolvingService == null) {
            throw new RuntimeException("Claim resolving service is null. Cannot retrieve claim mappings");
        }
        return claimResolvingService;
    }

    public void setClaimResolvingService(ClaimResolvingService claimResolvingService) {
        this.claimResolvingService = claimResolvingService;
    }
}

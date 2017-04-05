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

package org.wso2.carbon.identity.mgt.claim;

/**
 * Claim Mapping.
 */
public class MetaClaimMapping {

    /**
     * Meta metaClaim instance.
     */
    private MetaClaim metaClaim;

    /**
     * Identity Store Connector Id.
     */
    private String identityStoreConnectorId;

    /**
     * attribute name
     */
    private String attributeName;

    /**
     * Is this a unique attribute claim for the connector.
     */
    private boolean unique = false;

    public MetaClaimMapping(MetaClaim metaClaim, String identityStoreConnectorId, String attributeName) {
        this.metaClaim = metaClaim;
        this.identityStoreConnectorId = identityStoreConnectorId;
        this.attributeName = attributeName;
        this.unique = "true".equalsIgnoreCase(metaClaim.getProperties().get("unique"));
    }

    public MetaClaim getMetaClaim() {
        return metaClaim;
    }

    public String getIdentityStoreConnectorId() {
        return identityStoreConnectorId;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }
}

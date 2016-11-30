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


package org.wso2.carbon.identity.mgt.cache;

import java.io.Serializable;

/**
 * Cached Group.
 */
public class CachedGroup implements Serializable {

    private static final long serialVersionUID = 157884554477465554L;

    /**
     * Unique group id.
     */
    private String uniqueGroupId;

    /**
     * Domain in which the group belongs.
     */
    private String domainName;

    public CachedGroup(String uniqueGroupId, String domainName) {

        this.uniqueGroupId = uniqueGroupId;
        this.domainName = domainName;
    }

    public String getUniqueGroupId() {
        return uniqueGroupId;
    }

    public void setUniqueGroupId(String uniqueGroupId) {
        this.uniqueGroupId = uniqueGroupId;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }
}

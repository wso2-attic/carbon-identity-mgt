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

package org.wso2.carbon.identity.mgt.impl;

import org.wso2.carbon.identity.mgt.IdentityStore;
import org.wso2.carbon.identity.mgt.RealmService;

/**
 * Basic user realm service.
 */
public class RealmServiceImpl implements RealmService {

    /**
     * Credential store instance in the realm service.
     */
    private IdentityStore identityStore;

    public RealmServiceImpl(IdentityStore identityStore) {

        this.identityStore = identityStore;
    }

    @Override
    public IdentityStore getIdentityStore() {
        return this.identityStore;
    }
}

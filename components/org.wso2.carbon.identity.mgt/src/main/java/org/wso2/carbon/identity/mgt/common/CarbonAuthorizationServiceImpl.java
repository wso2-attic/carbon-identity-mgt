///*
// * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.wso2.carbon.identity.mgt.common;
//
//import org.wso2.carbon.identity.mgt.AuthorizationService;
//import org.wso2.carbon.identity.mgt.AuthorizationStore;
//import org.wso2.carbon.identity.mgt.AuthorizationStoreImpl;
//import org.wso2.carbon.identity.mgt.CacheBackedAuthorizationStore;
//import org.wso2.carbon.identity.mgt.exception.AuthorizationStoreException;
//import org.wso2.carbon.identity.mgt.impl.config.StoreConfig;
//
///**
// * Basic user realm service.
// */
//public class CarbonAuthorizationServiceImpl implements AuthorizationService {
//
//    private AuthorizationStore authorizationStore = new AuthorizationStoreImpl();
//
//    public CarbonAuthorizationServiceImpl(StoreConfig storeConfig) throws AuthorizationStoreException {
//
//        if (storeConfig.isEnableCache()) {
//            this.authorizationStore =
//                    new CacheBackedAuthorizationStore(storeConfig.getAuthorizationStoreCacheConfigMap());
//        }
//
//        authorizationStore.init(this, storeConfig.getAuthorizationConnectorConfigMap());
//    }
//
//    @Override
//    public AuthorizationStore getAuthorizationStore() {
//        return authorizationStore;
//    }
//}

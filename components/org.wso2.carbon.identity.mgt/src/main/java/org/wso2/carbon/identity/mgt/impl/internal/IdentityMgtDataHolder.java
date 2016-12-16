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

package org.wso2.carbon.identity.mgt.impl.internal;

import org.wso2.carbon.caching.CarbonCachingService;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.identity.mgt.RealmService;
import org.wso2.carbon.identity.mgt.connector.CredentialStoreConnectorFactory;
import org.wso2.carbon.identity.mgt.connector.IdentityStoreConnectorFactory;
import org.wso2.carbon.identity.mgt.impl.JDBCUniqueIdResolverFactory;
import org.wso2.carbon.identity.mgt.resolver.UniqueIdResolverFactory;
import org.wso2.carbon.security.caas.user.core.store.AuthorizationStore;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;

import static org.wso2.carbon.identity.mgt.impl.util.IdentityMgtConstants.UNIQUE_ID_RESOLVER_TYPE;

/**
 * Carbon security data holder.
 *
 * @since 1.0.0
 */
public class IdentityMgtDataHolder {

    private static IdentityMgtDataHolder instance = new IdentityMgtDataHolder();

    private RealmService realmService;

    private AuthorizationStore authorizationStore;

    private CarbonCachingService carbonCachingService;

    private DataSourceService dataSourceService;

    private Map<String, CredentialStoreConnectorFactory> credentialStoreConnectorFactoryMap = new HashMap<>();

    private Map<String, IdentityStoreConnectorFactory> identityStoreConnectorFactoryMap = new HashMap<>();

    private Map<String, UniqueIdResolverFactory> uniqueIdResolverFactoryMap = new HashMap<>();

    private IdentityMgtDataHolder() {

        uniqueIdResolverFactoryMap.put(UNIQUE_ID_RESOLVER_TYPE, new JDBCUniqueIdResolverFactory());
    }

    public static IdentityMgtDataHolder getInstance() {
        return instance;
    }

    void registerRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    public RealmService getRealmService() {

        if (realmService == null) {
            throw new IllegalStateException("Carbon Realm Service is null.");
        }
        return realmService;
    }

    void registerAuthorizationStore(AuthorizationStore authorizationStore) {

        this.authorizationStore = authorizationStore;
    }

    public AuthorizationStore getAuthorizationStore() {

        //TODO throw exception if null
        return authorizationStore;
    }

    public CarbonCachingService getCarbonCachingService() {

        if (carbonCachingService == null) {
            throw new IllegalStateException("Carbon caching service is null");
        }

        return carbonCachingService;
    }

    public DataSource getDataSource(String dataSourceName) throws DataSourceException {

        if (dataSourceService == null) {
            throw new IllegalStateException("Datasource service is null.");
        }

        return (DataSource) dataSourceService.getDataSource(dataSourceName);
    }

    public void setDataSourceService(DataSourceService dataSourceService) {
        this.dataSourceService = dataSourceService;
    }

    void registerCredentialStoreConnectorFactory(String key,
                                                 CredentialStoreConnectorFactory credentialStoreConnectorFactory) {

        credentialStoreConnectorFactoryMap.put(key, credentialStoreConnectorFactory);
    }

    public Map<String, CredentialStoreConnectorFactory> getCredentialStoreConnectorFactoryMap() {

        return credentialStoreConnectorFactoryMap;
    }

    public void unregisterCredentialStoreConnectorFactory(CredentialStoreConnectorFactory
                                                                  credentialStoreConnectorFactory) {

        if (credentialStoreConnectorFactory != null && !credentialStoreConnectorFactoryMap.isEmpty()) {
            Optional<String> connectorId = credentialStoreConnectorFactoryMap.entrySet().stream()
                    .filter(t -> t.getValue().equals(credentialStoreConnectorFactory))
                    .map(Map.Entry::getKey)
                    .findFirst();
            if (connectorId.isPresent()) {
                credentialStoreConnectorFactoryMap.remove(connectorId.get());
            }
        }
    }

    void registerIdentityStoreConnectorFactory(String key,
                                               IdentityStoreConnectorFactory identityStoreConnectorFactory) {
        identityStoreConnectorFactoryMap.put(key, identityStoreConnectorFactory);
    }

    public Map<String, IdentityStoreConnectorFactory> getIdentityStoreConnectorFactoryMap() {

        return identityStoreConnectorFactoryMap;
    }

    void unregisterIdentityStoreConnectorFactory(IdentityStoreConnectorFactory identityStoreConnectorFactory) {

        if (identityStoreConnectorFactory != null && !identityStoreConnectorFactoryMap.isEmpty()) {
            Optional<String> connectorId = identityStoreConnectorFactoryMap.entrySet().stream()
                    .filter(t -> t.getValue().equals(identityStoreConnectorFactory))
                    .map(Map.Entry::getKey)
                    .findFirst();
            if (connectorId.isPresent()) {
                identityStoreConnectorFactoryMap.remove(connectorId.get());
            }
        }
    }

    void registerUniqueIdResolverFactory(String key, UniqueIdResolverFactory uniqueIdResolverFactory) {

        this.uniqueIdResolverFactoryMap.put(key, uniqueIdResolverFactory);
    }

    public Map<String, UniqueIdResolverFactory> getUniqueIdResolverFactoryMap() {
        return uniqueIdResolverFactoryMap;
    }

    public void unregisterUniqueIdResolverFactory(UniqueIdResolverFactory uniqueIdResolverFactory) {

        if (uniqueIdResolverFactory != null && !uniqueIdResolverFactoryMap.isEmpty()) {
            Optional<String> resolverId = uniqueIdResolverFactoryMap.entrySet().stream()
                    .filter(t -> t.getValue().equals(uniqueIdResolverFactory))
                    .map(Map.Entry::getKey)
                    .findFirst();
            if (resolverId.isPresent()) {
                uniqueIdResolverFactoryMap.remove(resolverId.get());
            }
        }
    }

    void registerCacheService(CarbonCachingService carbonCachingService) {
        this.carbonCachingService = carbonCachingService;
    }
}

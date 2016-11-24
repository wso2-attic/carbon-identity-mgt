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

package org.wso2.carbon.identity.mgt.internal;

import org.osgi.framework.BundleContext;
import org.wso2.carbon.caching.CarbonCachingService;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.identity.mgt.exception.CarbonSecurityDataHolderException;
import org.wso2.carbon.identity.mgt.service.RealmService;
import org.wso2.carbon.identity.mgt.store.IdentityStore;
import org.wso2.carbon.identity.mgt.store.connector.CredentialStoreConnectorFactory;
import org.wso2.carbon.identity.mgt.store.connector.IdentityStoreConnectorFactory;
import org.wso2.carbon.identity.mgt.user.UniqueIdResolver;
import org.wso2.carbon.identity.mgt.user.UniqueIdResolverFactory;
import org.wso2.carbon.security.caas.user.core.store.AuthorizationStore;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

/**
 * Carbon security data holder.
 *
 * @since 1.0.0
 */
public class IdentityMgtDataHolder {

    private static IdentityMgtDataHolder instance = new IdentityMgtDataHolder();

    private RealmService<IdentityStore> realmService;

    private AuthorizationStore authorizationStore;

    private Map<String, CredentialStoreConnectorFactory> credentialStoreConnectorFactoryMap = new HashMap<>();

    private Map<String, IdentityStoreConnectorFactory> identityStoreConnectorFactoryMap = new HashMap<>();

    private Map<String, UniqueIdResolverFactory> uniqueIdResolverFactoryMap = new HashMap<>();

    private CarbonCachingService carbonCachingService;

    private BundleContext bundleContext = null;

    private UniqueIdResolver uniqueIdResolver;

    private DataSourceService dataSourceService;

    private IdentityMgtDataHolder() {
    }

    /**
     * Get the instance of this class.
     *
     * @return IdentityMgtDataHolder.
     */
    public static IdentityMgtDataHolder getInstance() {
        return instance;
    }

    void registerRealmService(RealmService<IdentityStore> realmService) {
        this.realmService = realmService;
    }

    public RealmService<IdentityStore> getRealmService() {

        if (realmService == null) {
            throw new IllegalStateException("Carbon Realm Service is null.");
        }
        return realmService;
    }

    /**
     * Register authorization store.
     *
     * @param authorizationStore authorization store.
     */
    void registerAuthorizationStore(AuthorizationStore authorizationStore) {

        this.authorizationStore = authorizationStore;
    }

    /**
     * Register credential store connector factory.
     *
     * @param key                             Id of the factory.
     * @param credentialStoreConnectorFactory CredentialStoreConnectorFactory.
     */
    void registerCredentialStoreConnectorFactory(String key,
                                                 CredentialStoreConnectorFactory credentialStoreConnectorFactory) {
        credentialStoreConnectorFactoryMap.put(key, credentialStoreConnectorFactory);
    }

    /**
     * Register identity store connector factory.
     *
     * @param key                           Id of the factory.
     * @param identityStoreConnectorFactory IdentityStoreConnectorFactory.
     */
    void registerIdentityStoreConnectorFactory(String key,
                                               IdentityStoreConnectorFactory identityStoreConnectorFactory) {
        identityStoreConnectorFactoryMap.put(key, identityStoreConnectorFactory);
    }

    public void registerUniqueIdResolverFactory(String key, UniqueIdResolverFactory uniqueIdResolverFactory) {
        this.uniqueIdResolverFactoryMap.put(key, uniqueIdResolverFactory);
    }

    public AuthorizationStore getAuthorizationStore() {

        //TODO throw exception if null
        return authorizationStore;
    }

    public Map<String, CredentialStoreConnectorFactory> getCredentialStoreConnectorFactoryMap() {
        return credentialStoreConnectorFactoryMap;
    }

    public Map<String, IdentityStoreConnectorFactory> getIdentityStoreConnectorFactoryMap() {
        return identityStoreConnectorFactoryMap;
    }

    public Map<String, UniqueIdResolverFactory> getUniqueIdResolverFactoryMap() {
        return uniqueIdResolverFactoryMap;
    }

    void registerCacheService(CarbonCachingService carbonCachingService) {
        this.carbonCachingService = carbonCachingService;
    }

    public CarbonCachingService getCarbonCachingService() throws CarbonSecurityDataHolderException {

        if (carbonCachingService == null) {
            throw new CarbonSecurityDataHolderException("Carbon caching service is null");
        }

        return carbonCachingService;
    }

    void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public BundleContext getBundleContext() {

        if (bundleContext == null) {
            throw new IllegalStateException("BundleContext is null.");
        }
        return bundleContext;
    }

    public UniqueIdResolver getUniqueIdResolver() {
        return uniqueIdResolver;
    }

    public void setUniqueIdResolver(UniqueIdResolver uniqueIdResolver) {
        this.uniqueIdResolver = uniqueIdResolver;
    }

    public DataSource getDataSource(String dataSourceName) throws DataSourceException {

        if (dataSourceService == null) {
            throw new RuntimeException("Datasource service is null. Cannot retrieve data source");
        }
        return (DataSource) dataSourceService.getDataSource(dataSourceName);
    }

    public void setDataSourceService(DataSourceService dataSourceService) {
        this.dataSourceService = dataSourceService;
    }
}

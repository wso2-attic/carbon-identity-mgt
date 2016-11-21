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
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.caching.CarbonCachingService;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.identity.mgt.bean.Domain;
import org.wso2.carbon.identity.mgt.claim.MetaClaim;
import org.wso2.carbon.identity.mgt.config.CredentialStoreConnectorConfig;
import org.wso2.carbon.identity.mgt.config.DomainConfig;
import org.wso2.carbon.identity.mgt.config.IdentityStoreConnectorConfig;
import org.wso2.carbon.identity.mgt.config.StoreConfig;
import org.wso2.carbon.identity.mgt.config.StoreConnectorConfig;
import org.wso2.carbon.identity.mgt.config.UniqueIdResolverConfig;
import org.wso2.carbon.identity.mgt.domain.DomainManager;
import org.wso2.carbon.identity.mgt.exception.CarbonSecurityConfigException;
import org.wso2.carbon.identity.mgt.exception.CredentialStoreException;
import org.wso2.carbon.identity.mgt.exception.DomainConfigException;
import org.wso2.carbon.identity.mgt.exception.DomainException;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreConnectorException;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.MetaClaimStoreException;
import org.wso2.carbon.identity.mgt.exception.UniqueIdResolverException;
import org.wso2.carbon.identity.mgt.internal.config.claim.ClaimConfigBuilder;
import org.wso2.carbon.identity.mgt.internal.config.connector.ConnectorConfigBuilder;
import org.wso2.carbon.identity.mgt.internal.config.domain.DomainConfigBuilder;
import org.wso2.carbon.identity.mgt.internal.config.store.StoreConfigBuilder;
import org.wso2.carbon.identity.mgt.service.RealmService;
import org.wso2.carbon.identity.mgt.service.impl.RealmServiceImpl;
import org.wso2.carbon.identity.mgt.store.CredentialStore;
import org.wso2.carbon.identity.mgt.store.IdentityStore;
import org.wso2.carbon.identity.mgt.store.connector.CredentialStoreConnector;
import org.wso2.carbon.identity.mgt.store.connector.CredentialStoreConnectorFactory;
import org.wso2.carbon.identity.mgt.store.connector.IdentityStoreConnector;
import org.wso2.carbon.identity.mgt.store.connector.IdentityStoreConnectorFactory;
import org.wso2.carbon.identity.mgt.store.impl.CacheBackedCredentialStore;
import org.wso2.carbon.identity.mgt.store.impl.CacheBackedIdentityStore;
import org.wso2.carbon.identity.mgt.store.impl.CredentialStoreImpl;
import org.wso2.carbon.identity.mgt.store.impl.IdentityStoreImpl;
import org.wso2.carbon.identity.mgt.user.UniqueIdResolver;
import org.wso2.carbon.identity.mgt.user.UniqueIdResolverFactory;
import org.wso2.carbon.identity.mgt.user.impl.JDBCUniqueIdResolverFactory;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;
import org.wso2.carbon.security.caas.user.core.store.AuthorizationStore;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * OSGi service component which handle identity management.
 *
 * @since 1.0.0
 */
@Component(
        name = "org.wso2.carbon.identity.mgt.internal.IdentityMgtComponent",
        immediate = true,
        property = {
                "componentName=wso2-carbon-identity-mgt"
        }
)
public class IdentityMgtComponent implements RequiredCapabilityListener {

    private static final Logger log = LoggerFactory.getLogger(IdentityMgtComponent.class);

    private ServiceRegistration realmServiceRegistration;

    @Activate
    public void registerCarbonIdentityMgtProvider(BundleContext bundleContext) {

        IdentityMgtDataHolder.getInstance().setBundleContext(bundleContext);

        // Register Default Unique Id Resolver
        IdentityMgtDataHolder.getInstance().registerUniqueIdResolverFactory("JDBC-UUID-RESOLVER",
                new JDBCUniqueIdResolverFactory());
    }

    @Deactivate
    public void unregisterCarbonIdentityMgtProvider(BundleContext bundleContext) {

        try {
            bundleContext.ungetService(realmServiceRegistration.getReference());
        } catch (Exception e) {
            log.error("Error occurred in un getting service", e);
        }

        log.info("Carbon-Security bundle deactivated successfully.");
    }

    @Reference(
            name = "IdentityStoreConnectorFactory",
            service = IdentityStoreConnectorFactory.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterIdentityStoreConnectorFactory"
    )
    protected void registerIdentityStoreConnectorFactory(IdentityStoreConnectorFactory identityStoreConnectorFactory,
                                                         Map<String, String> properties) {

        String connectorId = properties.get("connector-type");
        IdentityMgtDataHolder.getInstance().registerIdentityStoreConnectorFactory(connectorId,
                identityStoreConnectorFactory);
    }

    protected void unregisterIdentityStoreConnectorFactory(IdentityStoreConnectorFactory
                                                                   identityStoreConnectorFactory) {

    }

    @Reference(
            name = "CredentialStoreConnectorFactory",
            service = CredentialStoreConnectorFactory.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterCredentialStoreConnectorFactory"
    )
    protected void registerCredentialStoreConnectorFactory(
            CredentialStoreConnectorFactory credentialStoreConnectorFactory, Map<String, String> properties) {

        String connectorId = properties.get("connector-type");
        IdentityMgtDataHolder.getInstance().registerCredentialStoreConnectorFactory(connectorId,
                credentialStoreConnectorFactory);
    }

    protected void unregisterCredentialStoreConnectorFactory(CredentialStoreConnectorFactory
                                                                     credentialStoreConnectorFactory) {
    }

    @Reference(
            name = "AuthorizationStore",
            service = AuthorizationStore.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterAuthorizationStore"
    )
    protected void registerAuthorizationStore(AuthorizationStore authorizationStore, Map<String, String> properties) {

        IdentityMgtDataHolder.getInstance().registerAuthorizationStore(authorizationStore);
    }

    protected void unregisterAuthorizationStore(AuthorizationStore authorizationStore) {

        IdentityMgtDataHolder.getInstance().registerAuthorizationStore(null);
    }

    @Reference(
            name = "UniqueIdResolverFactory",
            service = UniqueIdResolverFactory.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterUniqueIdResolverFactory"
    )
    protected void registerUniqueIdResolverFactory(
            UniqueIdResolverFactory uniqueIdResolverFactory, Map<String, String> properties) {

        String connectorId = properties.get("connector-type");
        IdentityMgtDataHolder.getInstance().registerUniqueIdResolverFactory(connectorId,
                uniqueIdResolverFactory);
    }

    protected void unregisterUniqueIdResolverFactory(UniqueIdResolverFactory uniqueIdResolverFactory) {

    }

    @Reference(
            name = "org.wso2.carbon.datasource.DataSourceService",
            service = DataSourceService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterDataSourceService"
    )
    protected void registerDataSourceService(DataSourceService service, Map<String, String> properties) {

        if (service == null) {
            log.error("Data source service is null. Registering data source service is unsuccessful.");
            return;
        }

        IdentityMgtDataHolder.getInstance().setDataSourceService(service);

        if (log.isDebugEnabled()) {
            log.debug("Data source service registered successfully.");
        }
    }

    protected void unregisterDataSourceService(DataSourceService service) {

        if (log.isDebugEnabled()) {
            log.debug("Data source service unregistered.");
        }
        IdentityMgtDataHolder.getInstance().setDataSourceService(null);
    }

    @Reference(
            name = "carbon.caching.service",
            service = CarbonCachingService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterCachingService"
    )
    protected void registerCachingService(CarbonCachingService cachingService, Map<String, ?> properties) {

        IdentityMgtDataHolder.getInstance().registerCacheService(cachingService);
    }

    protected void unregisterCachingService(CarbonCachingService carbonCachingService) {

        IdentityMgtDataHolder.getInstance().registerCacheService(null);
    }

    @Override
    public void onAllRequiredCapabilitiesAvailable() {

        IdentityMgtDataHolder identityMgtDataHolder = IdentityMgtDataHolder.getInstance();
        BundleContext bundleContext = identityMgtDataHolder.getBundleContext();

        try {

            // Load all store connector configs
            Map<String, StoreConnectorConfig> connectorIdToStoreConnectorConfigMap = ConnectorConfigBuilder
                    .getInstance().getStoreConnectorConfigs();

            // Load all meta claims
            Map<String, MetaClaim> claimUriToMetaClaimMap = ClaimConfigBuilder.getInstance().getMetaClaims();

            // Load domain configurations
            List<DomainConfig> domainConfigs = DomainConfigBuilder.getInstance().getDomainConfigs
                    (claimUriToMetaClaimMap);

            // Build Domain manager
            DomainManager domainManager = getDomainManager(domainConfigs, connectorIdToStoreConnectorConfigMap);

            // Get the store configurations
            StoreConfig storeConfig = StoreConfigBuilder.getInstance().getStoreConfig();

            IdentityStore identityStore;
            CredentialStore credentialStore;
            //TODO
            AuthorizationStore authorizationStore = IdentityMgtDataHolder.getInstance().getAuthorizationStore();

            if (storeConfig.isEnableCache() && storeConfig.isEnableIdentityStoreCache()) {
                identityStore = new CacheBackedIdentityStore(storeConfig.getIdentityStoreCacheConfigMap());
            } else {
                identityStore = new IdentityStoreImpl();
            }
            identityStore.init(domainManager);

            if (storeConfig.isEnableCache() && storeConfig.isEnableCredentialStoreCache()) {
                credentialStore = new CacheBackedCredentialStore(storeConfig.getCredentialStoreCacheConfigMap());
            } else {
                credentialStore = new CredentialStoreImpl();
            }
            credentialStore.init(domainManager);


            // Register the carbon realm service.
            RealmServiceImpl<IdentityStore, CredentialStore> carbonRealmService
                    = new RealmServiceImpl(identityStore, credentialStore, authorizationStore);

            identityMgtDataHolder.registerCarbonRealmService(carbonRealmService);

            realmServiceRegistration = bundleContext.registerService(RealmService.class, carbonRealmService, null);
            log.info("Realm service registered successfully.");

            log.info("Carbon-Security bundle activated successfully.");

        } catch (CredentialStoreException | IdentityStoreException e) {
            log.error("Error occurred in initialising store", e);
        } catch (DomainException e) {
            log.error("Error occurred in creating the domain manager from the domain config", e);
        } catch (DomainConfigException | MetaClaimStoreException e) {
            log.error("Error occurred in building the domain configuration", e);
        } catch (UniqueIdResolverException e) {
            log.error("Error initializing unique id resolver.", e);
        } catch (CarbonSecurityConfigException e) {
            log.error("Error loading store configurations", e);
        } catch (IdentityStoreConnectorException e) {
            log.error("Error while initiating store connectors", e);
        }
    }

    private DomainManager getDomainManager(List<DomainConfig> domainConfigs,
                                           Map<String, StoreConnectorConfig> connectorIdToStoreConnectorConfigMap)
            throws DomainException, DomainConfigException, MetaClaimStoreException, UniqueIdResolverException,
            IdentityStoreException, CredentialStoreException, IdentityStoreConnectorException {

        DomainManager domainManager = new DomainManager();

        if (domainConfigs.isEmpty()) {
            throw new DomainConfigException("Invalid domain configuration found.");
        }

        Set<String> identityStoreConnectorIds = new HashSet<>();
        Set<String> credentialStoreConnectorIds = new HashSet<>();

        for (DomainConfig domainConfig : domainConfigs) {

            UniqueIdResolverConfig uniqueIdResolverConfig = domainConfig.getUniqueIdResolverConfig();
            UniqueIdResolverFactory uniqueIdResolverFactory = IdentityMgtDataHolder.getInstance()
                    .getUniqueIdResolverFactoryMap().get(uniqueIdResolverConfig.getType());

            if (uniqueIdResolverFactory == null) {
                throw new UniqueIdResolverException(String.format("Invalid unique id resolver configuration for " +
                        "domain - %s", domainConfig.getName()));
            }

            UniqueIdResolver uniqueIdResolver = uniqueIdResolverFactory.getInstance();
            if (uniqueIdResolver == null) {
                throw new UniqueIdResolverException(String.format("Failed to get unique id resolve instance for " +
                        "domain - %s", domainConfig.getName()));
            }
            uniqueIdResolver.init(uniqueIdResolverConfig);

            Domain domain = new Domain(domainConfig.getName(), domainConfig.getPriority(), uniqueIdResolver);

            domain.setMetaClaimMappings(domainConfig.getMetaClaimMappings());

            if (!domainConfig.getIdentityStoreConnectorIds().isEmpty()) {
                for (String connectorId : domainConfig.getIdentityStoreConnectorIds()) {
                    StoreConnectorConfig storeConnectorConfig = connectorIdToStoreConnectorConfigMap.get(connectorId);
                    if (storeConnectorConfig == null) {
                        throw new DomainConfigException(String.format("Connector configuration for the connector - %s" +
                                " is not found.", connectorId));
                    }

                    if (identityStoreConnectorIds.contains(connectorId)) {
                        throw new DomainConfigException(String.format("IdentityStoreConnector %s already exists in " +
                                "the identity store connector map", connectorId));
                    }
                    identityStoreConnectorIds.add(connectorId);

                    IdentityStoreConnectorFactory storeConnectorFactory = IdentityMgtDataHolder.getInstance()
                            .getIdentityStoreConnectorFactoryMap().get(storeConnectorConfig.getConnectorType());
                    if (storeConnectorFactory == null) {
                        throw new IdentityStoreConnectorException(String.format("Failed to get a connector factory " +
                                "of type - %s", storeConnectorConfig.getConnectorType()));
                    }
                    IdentityStoreConnector identityStoreConnector = storeConnectorFactory.getInstance();
                    if (identityStoreConnector == null) {
                        throw new IdentityStoreConnectorException(String.format("Failed to get a connector instance " +
                                "of type - %s", storeConnectorConfig.getConnectorType()));
                    }
                    if (storeConnectorConfig instanceof IdentityStoreConnectorConfig) {
                        identityStoreConnector.init((IdentityStoreConnectorConfig) storeConnectorConfig);
                    }
                    domain.addIdentityStoreConnector(identityStoreConnector);
                }
            }

            if (!domainConfig.getCredentialStoreConnectorIds().isEmpty()) {
                for (String connectorId : domainConfig.getCredentialStoreConnectorIds()) {
                    StoreConnectorConfig storeConnectorConfig = connectorIdToStoreConnectorConfigMap.get(connectorId);
                    if (storeConnectorConfig == null) {
                        throw new DomainConfigException(String.format("Connector configuration for the connector - %s" +
                                " is not found.", connectorId));
                    }

                    if (credentialStoreConnectorIds.contains(connectorId)) {
                        throw new DomainConfigException(String.format("CredentialStoreConnector %s already exists in " +
                                "the credential store connector map", connectorId));
                    }
                    credentialStoreConnectorIds.add(connectorId);

                    CredentialStoreConnectorFactory storeConnectorFactory = IdentityMgtDataHolder.getInstance()
                            .getCredentialStoreConnectorFactoryMap().get(storeConnectorConfig.getConnectorType());
                    if (storeConnectorFactory == null) {
                        throw new IdentityStoreConnectorException(String.format("Failed to get a connector factory " +
                                "of type - %s", storeConnectorConfig.getConnectorType()));
                    }
                    CredentialStoreConnector credentialStoreConnector = storeConnectorFactory.getInstance();
                    if (credentialStoreConnector == null) {
                        throw new IdentityStoreConnectorException(String.format("Failed to get a connector instance " +
                                "of type - %s", storeConnectorConfig.getConnectorType()));
                    }
                    if (storeConnectorConfig instanceof CredentialStoreConnectorConfig) {
                        credentialStoreConnector.init((CredentialStoreConnectorConfig) storeConnectorConfig);
                    }
                    domain.addCredentialStoreConnector(credentialStoreConnector);
                }
            }

            domainManager.addDomain(domain);
        }

        return domainManager;
    }
}


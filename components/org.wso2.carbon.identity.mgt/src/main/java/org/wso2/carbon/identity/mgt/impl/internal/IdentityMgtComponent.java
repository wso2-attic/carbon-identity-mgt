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
import org.wso2.carbon.identity.mgt.IdentityStore;
import org.wso2.carbon.identity.mgt.RealmService;
import org.wso2.carbon.identity.mgt.connector.AuthorizationStoreConnectorFactory;
import org.wso2.carbon.identity.mgt.connector.CredentialStoreConnector;
import org.wso2.carbon.identity.mgt.connector.CredentialStoreConnectorFactory;
import org.wso2.carbon.identity.mgt.connector.IdentityStoreConnector;
import org.wso2.carbon.identity.mgt.connector.IdentityStoreConnectorFactory;
import org.wso2.carbon.identity.mgt.connector.config.CredentialStoreConnectorConfig;
import org.wso2.carbon.identity.mgt.connector.config.IdentityStoreConnectorConfig;
import org.wso2.carbon.identity.mgt.connector.config.StoreConnectorConfig;
import org.wso2.carbon.identity.mgt.exception.CarbonIdentityMgtConfigException;
import org.wso2.carbon.identity.mgt.exception.CredentialStoreConnectorException;
import org.wso2.carbon.identity.mgt.exception.DomainConfigException;
import org.wso2.carbon.identity.mgt.exception.DomainException;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreConnectorException;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.MetaClaimStoreException;
import org.wso2.carbon.identity.mgt.exception.UniqueIdResolverException;
import org.wso2.carbon.identity.mgt.impl.CacheBackedIdentityStore;
import org.wso2.carbon.identity.mgt.impl.Domain;
import org.wso2.carbon.identity.mgt.impl.IdentityStoreImpl;
import org.wso2.carbon.identity.mgt.impl.RealmServiceImpl;
import org.wso2.carbon.identity.mgt.impl.config.DomainConfig;
import org.wso2.carbon.identity.mgt.impl.config.StoreConfig;
import org.wso2.carbon.identity.mgt.impl.internal.config.connector.ConnectorConfigReader;
import org.wso2.carbon.identity.mgt.impl.internal.config.domain.DomainConfigReader;
import org.wso2.carbon.identity.mgt.impl.internal.config.store.IdentityStoreConfigReader;
import org.wso2.carbon.identity.mgt.resolver.UniqueIdResolver;
import org.wso2.carbon.identity.mgt.resolver.UniqueIdResolverConfig;
import org.wso2.carbon.identity.mgt.resolver.UniqueIdResolverFactory;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;

import java.util.ArrayList;
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
        name = "org.wso2.carbon.identity.mgt.impl.internal.IdentityMgtComponent",
        immediate = true,
        property = {
                "componentName=wso2-carbon-identity-mgt"
        }
)
public class IdentityMgtComponent implements RequiredCapabilityListener {

    public static final String CONNECTOR_TYPE = "connector-type";
    private static final Logger log = LoggerFactory.getLogger(IdentityMgtComponent.class);

    private ServiceRegistration<RealmService> realmServiceRegistration;

    private BundleContext bundleContext;

    @Activate
    public void registerCarbonIdentityMgtProvider(BundleContext bundleContext) {

        this.bundleContext = bundleContext;
    }

    @Deactivate
    public void unregisterCarbonIdentityMgtProvider(BundleContext bundleContext) {

        bundleContext.ungetService(realmServiceRegistration.getReference());
    }

    @Reference(
            name = "IdentityStoreConnectorFactory",
            service = IdentityStoreConnectorFactory.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterIdentityStoreConnectorFactory"
    )
    protected void registerIdentityStoreConnectorFactory(IdentityStoreConnectorFactory identityStoreConnectorFactory,
                                                         Map<String, String> properties) {

        String connectorId = properties.get(CONNECTOR_TYPE);
        IdentityMgtDataHolder.getInstance().registerIdentityStoreConnectorFactory(connectorId,
                identityStoreConnectorFactory);
    }

    protected void unregisterIdentityStoreConnectorFactory(IdentityStoreConnectorFactory
                                                                   identityStoreConnectorFactory) {

        IdentityMgtDataHolder.getInstance().unregisterIdentityStoreConnectorFactory(identityStoreConnectorFactory);
    }

    @Reference(
            name = "CredentialStoreConnectorFactory",
            service = CredentialStoreConnectorFactory.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterCredentialStoreConnectorFactory"
    )
    protected void registerCredentialStoreConnectorFactory(
            CredentialStoreConnectorFactory credentialStoreConnectorFactory, Map<String, String> properties) {

        String connectorId = properties.get(CONNECTOR_TYPE);
        IdentityMgtDataHolder.getInstance().registerCredentialStoreConnectorFactory(connectorId,
                credentialStoreConnectorFactory);
    }

    protected void unregisterCredentialStoreConnectorFactory(CredentialStoreConnectorFactory
                                                                     credentialStoreConnectorFactory) {

        IdentityMgtDataHolder.getInstance().unregisterCredentialStoreConnectorFactory(credentialStoreConnectorFactory);
    }

    @Reference(
            name = "UniqueIdResolverFactory",
            service = UniqueIdResolverFactory.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterUniqueIdResolverFactory"
    )
    protected void registerUniqueIdResolverFactory(
            UniqueIdResolverFactory uniqueIdResolverFactory, Map<String, String> properties) {

        String connectorId = properties.get(CONNECTOR_TYPE);
        IdentityMgtDataHolder.getInstance().registerUniqueIdResolverFactory(connectorId,
                uniqueIdResolverFactory);
    }

    protected void unregisterUniqueIdResolverFactory(UniqueIdResolverFactory uniqueIdResolverFactory) {

        IdentityMgtDataHolder.getInstance().unregisterUniqueIdResolverFactory(uniqueIdResolverFactory);
    }

    //TODO make this MANDATORY in M3 release
//    @Reference(
//            name = "AuthorizationStore",
//            service = AuthorizationStore.class,
//            cardinality = ReferenceCardinality.OPTIONAL,
//            policy = ReferencePolicy.DYNAMIC,
//            unbind = "unregisterAuthorizationStore"
//    )
//    protected void registerAuthorizationStore(AuthorizationStore authorizationStore, Map<String, String> properties) {
//
//        IdentityMgtDataHolder.getInstance().registerAuthorizationStore(authorizationStore);
//    }
//
//    protected void unregisterAuthorizationStore(AuthorizationStore authorizationStore) {
//
//        IdentityMgtDataHolder.getInstance().registerAuthorizationStore(null);
//    }

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

    @Reference(
            name = "AuthorizationStoreConnectorFactory",
            service = AuthorizationStoreConnectorFactory.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterAuthorizationStoreConnectorFactory"
    )
    protected void registerAuthorizationStoreConnectorFactory(
            AuthorizationStoreConnectorFactory authorizationStoreConnectorFactory, Map<String, String> properties) {

        String connectorId = properties.get("connector-type");
        IdentityMgtDataHolder.getInstance()
                                .registerAuthorizationStoreConnectorFactory(connectorId,
                                                                            authorizationStoreConnectorFactory);
    }

    protected void unregisterAuthorizationStoreConnectorFactory(
            AuthorizationStoreConnectorFactory authorizationStoreConnectorFactory) {
    }

    protected void unregisterCachingService(CarbonCachingService carbonCachingService) {

        IdentityMgtDataHolder.getInstance().registerCacheService(null);
    }

    @Override
    public void onAllRequiredCapabilitiesAvailable() {

        constructRealmService();
    }

    private void constructRealmService() {

        IdentityMgtDataHolder identityMgtDataHolder = IdentityMgtDataHolder.getInstance();

        try {

            // Load all external store connector configs
            Map<String, StoreConnectorConfig> storeConnectorConfigs = ConnectorConfigReader
                    .getStoreConnectorConfigs();

            // Load domain configurations
            List<DomainConfig> domainConfigs = DomainConfigReader.getDomainConfigs
                    (storeConnectorConfigs);

            // Build Domains
            List<Domain> domains = constructDomains(domainConfigs);

            // Get the store configurations
            StoreConfig storeConfig = IdentityStoreConfigReader.getStoreConfig();

            IdentityStore identityStore;
            if (storeConfig.isEnableCache() && storeConfig.isEnableIdentityStoreCache()) {
                identityStore = new CacheBackedIdentityStore(storeConfig.getIdentityStoreCacheConfigMap(), domains);
            } else {
                identityStore = new IdentityStoreImpl(domains);
            }

            // Register the realm service.
            RealmService realmService = new RealmServiceImpl(identityStore);
            identityMgtDataHolder.registerRealmService(realmService);

            realmServiceRegistration = bundleContext.registerService(RealmService.class, realmService, null);
            log.info("Realm service registered successfully.");

            log.info("Carbon-Identity-Mgt bundle activated successfully.");

        } catch (CredentialStoreConnectorException | IdentityStoreException e) {
            log.error("Error occurred in initialising store", e);
        } catch (DomainException e) {
            log.error("Error occurred in creating the domain manager from the domain config", e);
        } catch (DomainConfigException | MetaClaimStoreException e) {
            log.error("Error occurred in building the domain configuration", e);
        } catch (UniqueIdResolverException e) {
            log.error("Error initializing unique id resolver.", e);
        } catch (CarbonIdentityMgtConfigException e) {
            log.error("Error loading store configurations", e);
        } catch (IdentityStoreConnectorException e) {
            log.error("Error while initiating store connectors", e);
        }
    }

    private List<Domain> constructDomains(List<DomainConfig> domainConfigs)
            throws DomainException, DomainConfigException, MetaClaimStoreException, UniqueIdResolverException,
            IdentityStoreException, CredentialStoreConnectorException, IdentityStoreConnectorException {

        if (domainConfigs.isEmpty()) {
            throw new DomainConfigException("No domain found. At least one domain should be available in the system.");
        }

        List<Domain> domains = new ArrayList<>();
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

            Domain domain = new Domain(domainConfig.getId(), domainConfig.getName(), domainConfig.getOrder(),
                    uniqueIdResolver);

            domain.setMetaClaimMappings(domainConfig.getMetaClaimMappings());

            if (!domainConfig.getIdentityStoreConnectorConfigs().isEmpty()) {
                for (IdentityStoreConnectorConfig connectorConfig : domainConfig.getIdentityStoreConnectorConfigs()) {

                    if (identityStoreConnectorIds.contains(connectorConfig.getConnectorId())) {
                        throw new DomainConfigException(String.format("IdentityStoreConnector %s already exists in " +
                                "the identity store connector map", connectorConfig));
                    }

                    IdentityStoreConnectorFactory storeConnectorFactory = IdentityMgtDataHolder.getInstance()
                            .getIdentityStoreConnectorFactoryMap().get(connectorConfig.getConnectorType());
                    if (storeConnectorFactory == null) {
                        throw new IdentityStoreConnectorException(String.format("Failed to get a connector factory " +
                                "of type - %s", connectorConfig.getConnectorType()));
                    }

                    IdentityStoreConnector identityStoreConnector = storeConnectorFactory.getInstance();
                    if (identityStoreConnector == null) {
                        throw new IdentityStoreConnectorException(String.format("Failed to get a connector instance " +
                                "of type - %s", connectorConfig.getConnectorType()));
                    }

                    identityStoreConnector.init(connectorConfig);
                    domain.addIdentityStoreConnector(identityStoreConnector);
                    identityStoreConnectorIds.add(connectorConfig.getConnectorId());
                }
            }

            if (!domainConfig.getCredentialStoreConnectorConfigs().isEmpty()) {
                for (CredentialStoreConnectorConfig connectorConfig :
                        domainConfig.getCredentialStoreConnectorConfigs()) {

                    if (credentialStoreConnectorIds.contains(connectorConfig.getConnectorId())) {
                        throw new DomainConfigException(String.format("CredentialStoreConnector %s already exists in " +
                                "the credential store connector map", connectorConfig.getConnectorId()));
                    }

                    CredentialStoreConnectorFactory storeConnectorFactory = IdentityMgtDataHolder.getInstance()
                            .getCredentialStoreConnectorFactoryMap().get(connectorConfig.getConnectorType());
                    if (storeConnectorFactory == null) {
                        throw new IdentityStoreConnectorException(String.format("Failed to get a connector factory " +
                                "of type - %s", connectorConfig.getConnectorType()));
                    }
                    CredentialStoreConnector credentialStoreConnector = storeConnectorFactory.getInstance();
                    if (credentialStoreConnector == null) {
                        throw new IdentityStoreConnectorException(String.format("Failed to get a connector instance " +
                                "of type - %s", connectorConfig.getConnectorType()));
                    }

                    credentialStoreConnector.init(connectorConfig);
                    domain.addCredentialStoreConnector(credentialStoreConnector);
                    credentialStoreConnectorIds.add(connectorConfig.getConnectorId());
                }
            }

            domains.add(domain);
        }

        return domains;
    }
}

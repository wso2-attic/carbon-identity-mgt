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
import org.wso2.carbon.identity.mgt.bean.Domain;
import org.wso2.carbon.identity.mgt.claim.FileBasedMetaClaimStore;
import org.wso2.carbon.identity.mgt.claim.MetaClaim;
import org.wso2.carbon.identity.mgt.claim.MetaClaimMapping;
import org.wso2.carbon.identity.mgt.claim.MetaClaimStore;
import org.wso2.carbon.identity.mgt.config.CredentialStoreConnectorConfig;
import org.wso2.carbon.identity.mgt.config.IdentityStoreConnectorConfig;
import org.wso2.carbon.identity.mgt.config.StoreConfig;
import org.wso2.carbon.identity.mgt.domain.DomainManager;
import org.wso2.carbon.identity.mgt.exception.AuthorizationStoreException;
import org.wso2.carbon.identity.mgt.exception.CarbonSecurityConfigException;
import org.wso2.carbon.identity.mgt.exception.CredentialStoreException;
import org.wso2.carbon.identity.mgt.exception.DomainConfigException;
import org.wso2.carbon.identity.mgt.exception.DomainException;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.MetaClaimStoreException;
import org.wso2.carbon.identity.mgt.exception.UserManagerException;
import org.wso2.carbon.identity.mgt.internal.config.StoreConfigBuilder;
import org.wso2.carbon.identity.mgt.internal.config.domain.DomainConfig;
import org.wso2.carbon.identity.mgt.internal.config.domain.DomainConfigBuilder;
import org.wso2.carbon.identity.mgt.internal.config.domain.DomainIdentityStoreConnectorConfigEntry;
import org.wso2.carbon.identity.mgt.service.RealmService;
import org.wso2.carbon.identity.mgt.service.impl.RealmServiceImpl;
import org.wso2.carbon.identity.mgt.store.AuthorizationStore;
import org.wso2.carbon.identity.mgt.store.CredentialStore;
import org.wso2.carbon.identity.mgt.store.IdentityStore;
import org.wso2.carbon.identity.mgt.store.connector.AuthorizationStoreConnectorFactory;
import org.wso2.carbon.identity.mgt.store.connector.CredentialStoreConnector;
import org.wso2.carbon.identity.mgt.store.connector.CredentialStoreConnectorFactory;
import org.wso2.carbon.identity.mgt.store.connector.IdentityStoreConnector;
import org.wso2.carbon.identity.mgt.store.connector.IdentityStoreConnectorFactory;
import org.wso2.carbon.identity.mgt.store.impl.AuthorizationStoreImpl;
import org.wso2.carbon.identity.mgt.store.impl.CacheBackedAuthorizationStore;
import org.wso2.carbon.identity.mgt.store.impl.CacheBackedIdentityStore;
import org.wso2.carbon.identity.mgt.store.impl.CredentialStoreImpl;
import org.wso2.carbon.identity.mgt.store.impl.IdentityStoreImpl;
import org.wso2.carbon.identity.mgt.user.impl.UserManagerImpl;
import org.wso2.carbon.identity.mgt.user.UserManager;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public void registerCarbonSecurityProvider(BundleContext bundleContext) {

        CarbonSecurityDataHolder.getInstance().setBundleContext(bundleContext);
    }

    @Deactivate
    public void unregisterCarbonSecurityProvider(BundleContext bundleContext) {

        try {
            bundleContext.ungetService(realmServiceRegistration.getReference());
        } catch (Exception e) {
            log.error("Error occurred in un getting service", e);
        }

        log.info("Carbon-Security bundle deactivated successfully.");
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
        CarbonSecurityDataHolder.getInstance()
                .registerAuthorizationStoreConnectorFactory(connectorId, authorizationStoreConnectorFactory);
    }

    protected void unregisterAuthorizationStoreConnectorFactory(
            AuthorizationStoreConnectorFactory authorizationStoreConnectorFactory) {
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

        String connectorId = properties.get("connector-type");
        CarbonSecurityDataHolder.getInstance()
                .registerIdentityStoreConnectorFactory(connectorId, identityStoreConnectorFactory);
    }

    protected void unregisterIdentityStoreConnectorFactory(
            IdentityStoreConnectorFactory identityStoreConnectorFactory) {
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

        String connectorId = properties.get("connector-type");
        CarbonSecurityDataHolder.getInstance()
                .registerCredentialStoreConnectorFactory(connectorId, credentialStoreConnectorFactory);
    }

    protected void unregisterCredentialStoreConnectorFactory(
            CredentialStoreConnectorFactory credentialStoreConnectorFactory) {
    }

    @Reference(
            name = "carbon.caching.service",
            service = CarbonCachingService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unRegisterCachingService"
    )
    protected void registerCachingService(CarbonCachingService cachingService, Map<String, ?> properties) {
        CarbonSecurityDataHolder.getInstance().registerCacheService(cachingService);
    }

    protected void unRegisterCachingService(CarbonCachingService carbonCachingService) {
        CarbonSecurityDataHolder.getInstance().registerCacheService(null);
    }

    @Override
    public void onAllRequiredCapabilitiesAvailable() {

        CarbonSecurityDataHolder carbonSecurityDataHolder = CarbonSecurityDataHolder.getInstance();
        BundleContext bundleContext = carbonSecurityDataHolder.getBundleContext();

        try {
            StoreConfig storeConfig = StoreConfigBuilder.getStoreConfig();

            MetaClaimStore metaClaimStore = new FileBasedMetaClaimStore();

            carbonSecurityDataHolder.setMetaClaimStore(metaClaimStore);

            UserManager userManager = new UserManagerImpl();

            carbonSecurityDataHolder.setUserManager(userManager);

            DomainConfig domainConfig = DomainConfigBuilder.getDomainConfig();
            carbonSecurityDataHolder.setDomainConfig(domainConfig);

            DomainManager domainManager = createDomainManagerFromConfig(domainConfig, storeConfig);

            AuthorizationStore authorizationStore;
            CredentialStore credentialStore;
            IdentityStore identityStore;

            if (storeConfig.isCacheEnabled()) {
                authorizationStore = new CacheBackedAuthorizationStore(storeConfig
                        .getAuthorizationStoreCacheConfigMap());
                identityStore = new CacheBackedIdentityStore(storeConfig
                        .getIdentityStoreCacheConfigMap());
            } else {
                identityStore = new IdentityStoreImpl();
                authorizationStore = new AuthorizationStoreImpl();
            }

            credentialStore = new CredentialStoreImpl();

            // Register the carbon realm service.
            RealmServiceImpl<IdentityStore, CredentialStore> carbonRealmService
                    = new RealmServiceImpl(identityStore, credentialStore, authorizationStore);

            carbonSecurityDataHolder.registerCarbonRealmService(carbonRealmService);

            credentialStore.init(domainManager);
            identityStore.init(domainManager);
            authorizationStore.init(storeConfig.getAuthorizationConnectorConfigMap());

            realmServiceRegistration = bundleContext.registerService(RealmService.class.getName(), carbonRealmService,
                    null);
            log.info("Realm service registered successfully.");

        } catch (CredentialStoreException | AuthorizationStoreException | IdentityStoreException e) {
            log.error("Error occurred in initialising store", e);
        } catch (DomainException e) {
            log.error("Error occurred in creating the domain manager from the domain config", e);
        } catch (DomainConfigException | MetaClaimStoreException e) {
            log.error("Error occurred in building the domain configuration", e);
        } catch (UserManagerException e) {
            log.error("Error initializing UserManagerImpl", e);
        } catch (CarbonSecurityConfigException e) {
            log.error("Error loading store configurations", e);
        }

        log.info("Carbon-Security bundle activated successfully.");
    }

    /**
     * Create the domains and domain manager from the domain configuration.
     *
     * @param domainConfig Domain configuration
     * @return DomainManager
     * @throws DomainException Domain Manager Exception
     */
    private DomainManager createDomainManagerFromConfig(DomainConfig domainConfig, StoreConfig storeConfig) throws
            DomainException, DomainConfigException, MetaClaimStoreException {

        DomainManager domainManager = new DomainManager();
        MetaClaimStore metaClaimStore = CarbonSecurityDataHolder.getInstance().getMetaClaimStore();

        Map<String, Integer> domainNameToDomainPriorityMap = domainConfig.getDomainNameToDomainPriorityMap();

        Map<String, IdentityStoreConnectorConfig> identityStoreConnectorConfigs =
                storeConfig.getIdentityConnectorConfigMap();

        Map<String, IdentityStoreConnectorFactory> identityStoreConnectorFactories =
                CarbonSecurityDataHolder.getInstance().getIdentityStoreConnectorFactoryMap();

        Map<String, Domain> domains = new HashMap<>();

        for (Map.Entry<String, List<DomainIdentityStoreConnectorConfigEntry>> domainConfigEntry :
                domainConfig.getDomainIdentityStoreConnectors().entrySet()) {

            String domainName = domainConfigEntry.getKey();
            int domainPriority = domainNameToDomainPriorityMap.get(domainName);

            // Create new domain
            Domain domain = new Domain(domainName, domainPriority);
            domainManager.addDomain(domain);
            domains.put(domainName, domain);

            // Domain connector meta claims mappings
            Map<String, List<MetaClaimMapping>> connectorMetaClaimMappings = new HashMap<>();

            for (DomainIdentityStoreConnectorConfigEntry domainIdentityStoreConnectorConfigEntry :
                    domainConfigEntry.getValue()) {
                String identityStoreConnectorId = domainIdentityStoreConnectorConfigEntry.getIdentityStoreConnectorId();
                IdentityStoreConnectorConfig identityStoreConnectorConfig =
                        identityStoreConnectorConfigs.get(identityStoreConnectorId);

                if (identityStoreConnectorConfig != null) {
                    IdentityStoreConnectorFactory identityStoreConnectorFactory = identityStoreConnectorFactories
                            .get(identityStoreConnectorConfig.getConnectorType());

                    if (identityStoreConnectorFactory == null) {
                        throw new DomainConfigException("Connector type "
                                + identityStoreConnectorConfig.getConnectorType() + " is not registered");
                    }

                    IdentityStoreConnector identityStoreConnector = identityStoreConnectorFactory.getConnector();

                    List<String> uniqueAttributes = identityStoreConnectorConfig.getUniqueAttributes();
                    List<String> otherAttributes = identityStoreConnectorConfig.getOtherAttributes();

                    domain.addIdentityStoreConnector(identityStoreConnector, identityStoreConnectorConfig);

                    List<MetaClaimMapping> metaClaimMappings = new ArrayList<>();

                    for (Map.Entry<String, String> attributeMapping :
                            domainIdentityStoreConnectorConfigEntry.getAttributeMappings().entrySet()) {

                        String attributeName = attributeMapping.getValue();
                        boolean unique = false;

                        if (uniqueAttributes.contains(attributeName)) {
                            unique = true;
                        } else if (!otherAttributes.contains(attributeName)) {
                            throw new DomainConfigException("Attribute " + attributeName
                                    + " not found in connector for claim mapping");
                        }

                        MetaClaim metaClaim = metaClaimStore.getMetaClaim(attributeMapping.getKey());
                        metaClaimMappings.add(new MetaClaimMapping(metaClaim, identityStoreConnectorId, attributeName,
                                unique));
                    }

                    connectorMetaClaimMappings.put(identityStoreConnectorId, metaClaimMappings);
                } else {
                    throw new DomainConfigException("IdentityStoreConfig not found for connectorId "
                            + identityStoreConnectorId);
                }

            }


            domain.setClaimMappings(connectorMetaClaimMappings);

        }

        for (Map.Entry<String, CredentialStoreConnectorConfig> credentialStoreConnectorConfigEntry :
                storeConfig.getCredentialConnectorConfigMap().entrySet()) {

            String credentialStoreConnectorId = credentialStoreConnectorConfigEntry.getKey();

            CredentialStoreConnectorConfig credentialStoreConnectorConfig =
                    credentialStoreConnectorConfigEntry.getValue();

            CredentialStoreConnector credentialStoreConnector = CarbonSecurityDataHolder.getInstance()
                    .getCredentialStoreConnectorFactoryMap()
                    .get(credentialStoreConnectorConfig.getConnectorType()).getInstance();

            try {
                credentialStoreConnector.init(credentialStoreConnectorConfig);

                String domainName = credentialStoreConnectorConfig.getDomainName();
                Domain domain = domains.get(domainName);

                if (domain != null) {
                    domain.addCredentialStoreConnector(credentialStoreConnector);
                } else {
                    log.error("Domain " + domainName + " was not found when creating CredentialStoreConnector "
                            + credentialStoreConnectorId);
                }
            } catch (CredentialStoreException e) {
                log.error("Error initializing CredentialStoreConnector " + credentialStoreConnectorId);
            }
        }

        return domainManager;
    }
}


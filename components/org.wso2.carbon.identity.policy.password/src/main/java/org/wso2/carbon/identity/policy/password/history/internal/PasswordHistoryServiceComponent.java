/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * limitations und
 */

package org.wso2.carbon.identity.policy.password.history.internal;


import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.jndi.JNDIContextManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.identity.common.jdbc.JdbcTemplate;
import org.wso2.carbon.identity.event.AbstractEventHandler;
import org.wso2.carbon.identity.policy.password.history.PasswordHistoryHandler;
import org.wso2.carbon.identity.policy.password.history.store.PasswordHistoryDataStore;
import org.wso2.carbon.identity.policy.password.history.store.impl.DefaultPasswordHistoryDataStore;
import org.wso2.carbon.identity.policy.password.validation.PasswordValidationService;
import org.wso2.carbon.identity.policy.password.validation.impl.PasswordValidationServiceImpl;

import java.util.Map;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * PasswordHistoryServiceComponent class.
 */
@Component(
        name = "PasswordHistoryServiceComponent",
        immediate = true,
        property = {
                "componentName=handler.password.history"
        }
)
public class
PasswordHistoryServiceComponent {

    private static Logger log = LoggerFactory.getLogger(PasswordHistoryServiceComponent.class);
    private JdbcTemplate jdbcTemplate;
    private DataSourceService dataSourceService;
    private PasswordHistoryDataStore passwordHistoryDataStore = null;

    @Activate
    protected void activate(ComponentContext context) {

        if (passwordHistoryDataStore == null) {
            passwordHistoryDataStore = new DefaultPasswordHistoryDataStore(jdbcTemplate, dataSourceService);
        }
        PasswordValidationService passwordValidationService = new PasswordValidationServiceImpl();
        context.getBundleContext().registerService(AbstractEventHandler.class.getName(),
                new PasswordHistoryHandler(passwordHistoryDataStore), null);
        context.getBundleContext().registerService(PasswordValidationService.class, passwordValidationService, null);
        if (log.isDebugEnabled()) {
            log.debug("PasswordHistoryHandler is registered");
        }

    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("PasswordHistoryHandler is unregistered");
        }
        passwordHistoryDataStore = null;
    }

    @Reference(
            name = "passwordHistoryDataStoreService",
            service = PasswordHistoryDataStore.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetPasswordHistoryDataStoreService")

    protected void setPasswordHistoryDataStoreService(PasswordHistoryDataStore passwordHistoryDataStore) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the Password History Data Store Service");
        }
        this.passwordHistoryDataStore = passwordHistoryDataStore;
    }

    protected void unsetPasswordHistoryDataStoreService(PasswordHistoryDataStore passwordHistoryDataStore) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting the Password History Data Store Service");
        }
        this.passwordHistoryDataStore = null;
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

        this.dataSourceService = service;

        if (log.isDebugEnabled()) {
            log.debug("Data source service registered successfully.");
        }
    }

    protected void unregisterDataSourceService(DataSourceService service) {

        if (log.isDebugEnabled()) {
            log.debug("Data source service unregistered.");
        }
        this.dataSourceService = null;
    }

    @Reference(
            name = "org.wso2.carbon.datasource.jndi",
            service = JNDIContextManager.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "onJNDIUnregister") protected void onJNDIReady(JNDIContextManager jndiContextManager) {
        try {
            Context ctx = jndiContextManager.newInitialContext();
            DataSource dsObject = (DataSource) ctx.lookup("java:comp/env/jdbc/WSO2CARBON_DB");
            if (dsObject != null) {
                jdbcTemplate = new JdbcTemplate(dsObject);
                initializeDao(jdbcTemplate);
            } else {
                log.error("Could not find WSO2CarbonDB");
            }
        } catch (NamingException e) {
            log.error("Error occurred while looking up the Datasource", e);
        }
    }

    protected void onJNDIUnregister(JNDIContextManager jndiContextManager) {
        log.info("Un-registering data sources");
    }

    private void initializeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}

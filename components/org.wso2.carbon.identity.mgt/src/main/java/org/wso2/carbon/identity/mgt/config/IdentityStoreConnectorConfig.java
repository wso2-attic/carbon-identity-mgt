package org.wso2.carbon.identity.mgt.config;

import java.util.Map;

/**
 * Config entry for IdentityStoreConnector.
 */
public class IdentityStoreConnectorConfig extends StoreConnectorConfig {

    public IdentityStoreConnectorConfig() {

    }

    public IdentityStoreConnectorConfig(String connectorId, String connectorType, Map<String, String> properties) {

        super(connectorId, connectorType, properties);
    }
}

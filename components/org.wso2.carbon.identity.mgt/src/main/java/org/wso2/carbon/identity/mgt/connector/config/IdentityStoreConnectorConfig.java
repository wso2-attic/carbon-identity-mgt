package org.wso2.carbon.identity.mgt.connector.config;

import java.util.Map;

/**
 * Config entry for IdentityStoreConnector.
 */
public class IdentityStoreConnectorConfig extends StoreConnectorConfig {

    public IdentityStoreConnectorConfig() {

    }

    public IdentityStoreConnectorConfig(String connectorId, String connectorType, boolean readOnly,
                                        Map<String, String> properties) {

        super(connectorId, connectorType, readOnly, properties);
    }
}

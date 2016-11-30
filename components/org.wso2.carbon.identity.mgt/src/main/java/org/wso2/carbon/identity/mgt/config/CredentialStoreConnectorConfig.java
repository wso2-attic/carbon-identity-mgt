package org.wso2.carbon.identity.mgt.config;

import java.util.Map;

/**
 * Config entry for CredentialStoreConnector..
 */
public class CredentialStoreConnectorConfig extends StoreConnectorConfig {

    public CredentialStoreConnectorConfig() {

    }

    public CredentialStoreConnectorConfig(String connectorId, String connectorType, Map<String, String> properties) {

        super(connectorId, connectorType, properties);
    }
}

package org.wso2.carbon.identity.mgt.connector.config;

import java.util.Map;

/**
 * Config entry for CredentialStoreConnector..
 */
public class CredentialStoreConnectorConfig extends StoreConnectorConfig {

    public CredentialStoreConnectorConfig() {

    }

    public CredentialStoreConnectorConfig(String connectorId, String connectorType, boolean readOnly,
                                          Map<String, String> properties) {

        super(connectorId, connectorType, readOnly, properties);
    }
}

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

package org.wso2.carbon.identity.mgt.store.connector.inmemory;

import org.wso2.carbon.identity.mgt.config.CredentialStoreConnectorConfig;
import org.wso2.carbon.identity.mgt.exception.AuthenticationFailure;
import org.wso2.carbon.identity.mgt.exception.CredentialStoreConnectorException;
import org.wso2.carbon.identity.mgt.store.connector.CredentialStoreConnector;

import javax.security.auth.callback.Callback;

/**
 * In Memory Credential Store Connector.
 */
public class InMemoryCredentialStoreConnector implements CredentialStoreConnector {

    private CredentialStoreConnectorConfig credentialStoreConnectorConfig;

    @Override
    public void init(CredentialStoreConnectorConfig credentialStoreConnectorConfig) throws
            CredentialStoreConnectorException {

        this.credentialStoreConnectorConfig = credentialStoreConnectorConfig;
    }

    @Override
    public String getCredentialStoreConnectorId() {
        return null;
    }

    @Override
    public void authenticate(Callback[] callbacks) throws CredentialStoreConnectorException, AuthenticationFailure {

    }

    @Override
    public boolean canHandle(Callback[] callbacks) {
        return false;
    }

    @Override
    public boolean canStore(Callback[] callbacks) {
        return false;
    }

    @Override
    public CredentialStoreConnectorConfig getCredentialStoreConfig() {
        return credentialStoreConnectorConfig;
    }

    @Override
    public String addCredential(Callback[] callbacks) throws CredentialStoreConnectorException {
        return null;
    }

    @Override
    public void updateCredential(Callback[] callbacks) throws CredentialStoreConnectorException {

    }

    @Override
    public void updateCredential(String username, Callback[] credentialCallbacks) throws
            CredentialStoreConnectorException {

    }

    @Override
    public void addCredential(String username, Callback[] credentialCallbacks) throws
            CredentialStoreConnectorException {

    }

    @Override
    public void deleteCredential(String username) throws CredentialStoreConnectorException {

    }
}

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

import org.wso2.carbon.identity.mgt.connector.CredentialStoreConnector;
import org.wso2.carbon.identity.mgt.connector.config.CredentialStoreConnectorConfig;
import org.wso2.carbon.identity.mgt.exception.AuthenticationFailure;
import org.wso2.carbon.identity.mgt.exception.CredentialStoreConnectorException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;

/**
 * In Memory Credential Store Connector.
 */
public class InMemoryCredentialStoreConnector implements CredentialStoreConnector {

    private CredentialStoreConnectorConfig credentialStoreConnectorConfig;

    private Map<String, char[]> credentialMap = new HashMap<>();

    @Override
    public void init(CredentialStoreConnectorConfig credentialStoreConnectorConfig) throws
            CredentialStoreConnectorException {

        this.credentialStoreConnectorConfig = credentialStoreConnectorConfig;
    }

    @Override
    public String getCredentialStoreConnectorId() {
        return "INMEM_CSC";
    }

    @Override
    public void authenticate(String connectorUserId, Callback[] callbacks) throws CredentialStoreConnectorException,
            AuthenticationFailure {
        char[] password = credentialMap.get(connectorUserId);

        for (Callback callback : callbacks) {
            if (callback instanceof PasswordCallback) {
                char[] credential = ((PasswordCallback) callback).getPassword();
                if (Arrays.equals(password, credential)) {
                    return;
                } else {
                    throw new AuthenticationFailure("Invalid Credentials");
                }
            }
        }
        throw new CredentialStoreConnectorException("Invalid Callback");
    }

    @Override
    public boolean canHandle(Callback[] callbacks) {
        return true;
    }

    @Override
    public boolean canStore(Callback[] callbacks) {
        return true;
    }

    @Override
    public CredentialStoreConnectorConfig getCredentialStoreConfig() {
        return credentialStoreConnectorConfig;
    }

    @Override
    public String addCredential(List<Callback> callbacks) throws CredentialStoreConnectorException {
        String userId = UUID.randomUUID().toString();

        char[] password = null;
        for (Callback callback : callbacks) {
            if (callback instanceof PasswordCallback) {
                password = ((PasswordCallback) callback).getPassword();
                credentialMap.put(userId, password);
                return userId;
            }
        }
        throw new CredentialStoreConnectorException("Error while saving credentials");
    }

    @Override
    public Map<String, String> addCredentials(Map<String, List<Callback>> userUniqueIdToCallbacksMap) throws
            CredentialStoreConnectorException {
        return null;
    }

    public void updateCredentials(Callback[] callbacks) throws CredentialStoreConnectorException {

    }

    @Override
    public String updateCredentials(String username, List<Callback> credentialCallbacks) throws
            CredentialStoreConnectorException {

        for (Callback callback : credentialCallbacks) {
            if (callback instanceof PasswordCallback) {
                char[] credential = ((PasswordCallback) callback).getPassword();
                credentialMap.replace(username, credential);
            }

        }
        return username;
    }

    @Override
    public String updateCredentials(String userIdentifier, List<Callback> credentialsToAdd, List<Callback>
            credentialsToRemove) throws CredentialStoreConnectorException {
        return null;
    }

    @Override
    public void deleteCredential(String username) throws CredentialStoreConnectorException {

    }
}

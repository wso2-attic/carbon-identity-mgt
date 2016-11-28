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

package org.wso2.carbon.identity.mgt.store.connector;

import org.wso2.carbon.identity.mgt.config.CredentialStoreConnectorConfig;
import org.wso2.carbon.identity.mgt.exception.AuthenticationFailure;
import org.wso2.carbon.identity.mgt.exception.CredentialStoreConnectorException;

import javax.security.auth.callback.Callback;

/**
 * Credential store connector.
 */
public interface CredentialStoreConnector {

    /**
     * Initialize the Credential store connector.
     *
     * @param credentialStoreConnectorConfig Credential store configurations for this connector.
     * @throws CredentialStoreConnectorException Credential Store Exception.
     */
    void init(CredentialStoreConnectorConfig credentialStoreConnectorConfig)
            throws CredentialStoreConnectorException;

    /**
     * Get the Id of this CredentialStoreConnector.
     *
     * @return Id of the CredentialStoreConnector.
     */
    String getCredentialStoreConnectorId();

    /**
     * Authenticate user using callbacks. Throws {@link AuthenticationFailure} if authentication is not successful.
     *
     * @param callbacks Callbacks to get the user attributes.
     * @throws CredentialStoreConnectorException Credential Store Exception.
     * @throws AuthenticationFailure    Authentication failure.
     */
    void authenticate(Callback[] callbacks) throws CredentialStoreConnectorException, AuthenticationFailure;

    /**
     * Checks whether this connector can handle the given callbacks.
     *
     * @param callbacks Array of callbacks.
     * @return True if there are all of the callbacks required for this connector.
     */
    boolean canHandle(Callback[] callbacks);

    /**
     * Checks whether this connector can store the given callbacks.
     *
     * @param callbacks Array of callbacks.
     * @return True if there are all of the callbacks required for this connector.
     */
    boolean canStore(Callback[] callbacks);

    /**
     * Get the Credential store config.
     *
     * @return CredentialStoreConnectorConfig.
     */
    CredentialStoreConnectorConfig getCredentialStoreConfig();

    /**
     * Add user credentials.
     *
     * @param callbacks Array of callbacks.
     * @return connector user id
     * @throws CredentialStoreConnectorException Credential Store Exception.
     */
    String addCredential(Callback[] callbacks) throws CredentialStoreConnectorException;


    /**
     * Update user credentials.
     *
     * @param callbacks Array of callbacks.
     * @throws CredentialStoreConnectorException Credential Store Exception.
     */
    void updateCredential(Callback[] callbacks) throws CredentialStoreConnectorException;

    /**
     * Update user credentials.
     *
     * @param username            Username of the user.
     * @param credentialCallbacks Array of callbacks which contains credentials.
     * @throws CredentialStoreConnectorException Credential Store Exception.
     */
    void updateCredential(String username, Callback[] credentialCallbacks) throws CredentialStoreConnectorException;

    /**
     * Add user credentials.
     *
     * @param username            Username of the user.
     * @param credentialCallbacks Array of callbacks which contains credentials.
     * @throws CredentialStoreConnectorException Credential Store Exception.
     */
    void addCredential(String username, Callback[] credentialCallbacks) throws CredentialStoreConnectorException;

    /**
     * Delete credential
     *
     * @param username unique user id of the connector
     * @throws CredentialStoreConnectorException CredentialStore Exception
     */
    void deleteCredential(String username) throws CredentialStoreConnectorException;
}

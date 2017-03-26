/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.identity.mgt.impl.util;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Identity Management Constants.
 *
 * @since 1.0.0
 */
public class IdentityMgtConstants {

    private IdentityMgtConstants() {

    }

    public static final String CARBON_HOME = "carbon.home";

    //Config file names
    public static final String STORE_CONFIG_FILE = "store-config.yaml";
    public static final String CLAIM_MAPPING_FILE = "claim-mapping.yaml";
    public static final String DOMAIN_CONFIG_FILE = "domain-config.yaml";

    public static final String UNIQUE_ID_RESOLVER_TYPE = "JDBCUniqueIdResolver";

    public static final String CLAIM_ROOT_DIALECT = "http://wso2.org/claims";

    public static final String USERNAME_CLAIM = "http://wso2.org/claims/username";
    public static final String GROUP_NAME_CLAIM = "http://wso2.org/claims/groupName";
    public static final String ACCOUNT_LOCKED_CLAIM_URI = "http://wso2.org/claims/accountLocked";

    public static final String HTTP_AUTHORIZATION_PREFIX_BEARER = "Bearer";
    public static final String HTTP_AUTHORIZATION_PREFIX_BASIC = "Basic";
    public static final String USERNAME_PASSWORD_LOGIN_MODULE = "USERNAME_PASSWORD_LM";
    public static final String JWT_LOGIN_MODULE = "JWT_LM";
    public static final String SAML_LOGIN_MODULE = "SAML_LM";
    public static final String CREDENTIAL_STORE = "credentialStore";
    public static final String IDENTITY_STORE = "identityStore";
    public static final String AUTHORIZATION_STORE = "authorizationStore";
    public static final String STORE_CONNECTORS = "storeConnectors";
    public static final String PERMISSION_CONFIG_FILE = "permissions.yml";
    public static final String CLAIM_CONFIG_FILE = "claim-config.yml";

    public static Path getCarbonHomeDirectory() {
        return Paths.get(System.getProperty(CARBON_HOME));
    }
}

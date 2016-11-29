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

package org.wso2.carbon.identity.mgt.util;

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
    public static final String STORE_CONFIG_FILE = "store-config.yml";
    public static final String PERMISSION_CONFIG_FILE = "permissions.yml";
    public static final String CLAIM_STORE_FILE = "claim-store.yml";
    public static final String CLAIM_MAPPING_FILE = "claim-mapping.yml";
    public static final String DOMAIN_CONFIG_FILE = "domain-config.yml";
    public static final String CONNECTOR_CONFIG_FILE = "connector-config.yml";

    public static final String UNIQUE_ID_RESOLVER_TYPE = "JDBCUniqueIdResolver";

    public static final String CLAIM_ROOT_DIALECT = "http://wso2.org/claims";

    public static final String USERNAME_CLAIM = "http://wso2.org/claims/username";

    // Delimiters
    public static final String URL_SPLITTER = "/";

    public static Path getCarbonHomeDirectory() {
        return Paths.get(System.getProperty(CARBON_HOME));
    }

    public static final String USER_USERNAME = "user-username";
    public static final String USER_USERID = "user-userid";
    public static final String GROUP_GROUPNAME = "group-groupname";
    public static final String GROUP_GROUPID = "group-groupid";
    public static final String GROUPS_USERID_IDENTITYSTOREID = "groups-userid-identitystoreid";
}

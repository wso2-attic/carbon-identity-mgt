/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.mgt.impl.util;

/**
 * User Manager Constants
 */
public class UniqueIdResolverConstants {

    public static final String IDENTITY_STORE_CONNECTOR = "I";
    public static final String CREDENTIAL_STORE_CONNECTOR = "C";
    public static final String DATA_SOURCE = "dataSource";


    /**
     * SQL Placeholders
     */
    public static final class SQLPlaceholders {
        public static final String CONNECTOR_ID = "connector_id";
        public static final String CONNECTOR_USER_ID = "connector_user_id";
        public static final String CONNECTOR_GROUP_ID = "connector_group_id";
        public static final String USER_ID = "user_id";
        public static final String GROUP_ID = "group_id";
        public static final String DOMAIN_ID = "domain_id";
        public static final String MAPPING_DOMAIN_ID = "mapping_domain_id";
        public static final String CONNECTOR_TYPE = "connector_type";

        public static final String LIMIT = "limit";
        public static final String OFFSET = "offset";
    }

    /**
     * Database Column Names
     */
    public static final class ColumnNames {
        public static final String USER_ID = "USER_ID";
        public static final String GROUP_ID = "GROUP_ID";
        public static final String CONNECTOR_USER_ID = "CONNECTOR_USER_ID";
        public static final String CONNECTOR_GROUP_ID = "CONNECTOR_GROUP_ID";
        public static final String CONNECTOR_ID = "CONNECTOR_ID";
        public static final String CONNECTOR_TYPE = "CONNECTOR_TYPE";
        public static final String DOMAIN_ID = "DOMAIN_ID";
    }
}

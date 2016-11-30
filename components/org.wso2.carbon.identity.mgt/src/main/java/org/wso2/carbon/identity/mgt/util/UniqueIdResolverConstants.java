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

package org.wso2.carbon.identity.mgt.util;

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
        public static final String CONNECTOR_ENTITY_ID = "connector_entity_id";
        public static final String CONNECTOR_ID = "connector_id";
        public static final String ENTITY_UUID = "entity_uuid";
        public static final String USER_UUID = "user_uuid";
        public static final String GROUP_UUID = "group_uuid";
        public static final String DOMAIN = "domain";
        public static final String CONNECTOR_TYPE = "connector_type";

        public static final String LIMIT = "limit";
        public static final String OFFSET = "offset";
    }

    /**
     * Database Column Names
     */
    public static final class DatabaseColumnNames {
        public static final String ENTITY_UUID = "ENTITY_UUID";
        public static final String CONNECTOR_ENTITY_ID = "CONNECTOR_ENTITY_ID";
        public static final String CONNECTOR_ID = "CONNECTOR_ID";
        public static final String CONNECTOR_TYPE = "CONNECTOR_TYPE";
        public static final String DOMAIN = "DOMAIN";
    }
}

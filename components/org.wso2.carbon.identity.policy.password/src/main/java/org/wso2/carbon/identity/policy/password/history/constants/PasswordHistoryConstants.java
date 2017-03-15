/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * limitations und
 */

package org.wso2.carbon.identity.policy.password.history.constants;

/**
 * PasswordHistoryConstants class.
 */
public class PasswordHistoryConstants {

    public static final String SALT_VALUE = "SALT_VALUE";
    public static final String HASH = "HASH";
    public static final Integer ERROR_IN_COUNT = 1000;
    public static final Integer ERROR_IN_TIME = 1001;
    public static final String SHA_1_PRNG = "SHA1PRNG";

    /**
     * SQL queries for password policy store
     */
    public static class SQLQueries {

        public static final String LOAD_HISTORY_DATA_BY_COUNT = "SELECT * " +
                "FROM IDM_PASSWORD_HISTORY_DATA WHERE USER_UNIQUE_ID = ? ORDER BY TIME_CREATED DESC LIMIT ?";

        public static final String LOAD_HISTORY_DATA_BY_TIME = "SELECT * FROM IDM_PASSWORD_HISTORY_DATA WHERE " +
                "USER_UNIQUE_ID = ? AND TIME_CREATED > ?";

        public static final String DELETE_HISTORY_RECORD = "DELETE FROM IDM_PASSWORD_HISTORY_DATA WHERE ID NOT IN " +
                "((SELECT ID FROM (SELECT ID FROM IDM_PASSWORD_HISTORY_DATA WHERE USER_UNIQUE_ID =? ORDER BY " +
                "TIME_CREATED DESC LIMIT ?)) UNION (SELECT ID FROM (SELECT ID FROM IDM_PASSWORD_HISTORY_DATA" +
                " WHERE USER_UNIQUE_ID=? AND TIME_CREATED > ?)))";

        public static final String STORE_HISTORY_DATA = "INSERT INTO  IDM_PASSWORD_HISTORY_DATA "
                + "(USER_UNIQUE_ID, SALT_VALUE, HASH, TIME_CREATED)"
                + "VALUES (?,?,?,?)";

        public static final String DELETE_USER_HISTORY = "DELETE FROM IDM_PASSWORD_HISTORY_DATA WHERE USER_UNIQUE_ID" +
                " = ? ";

    }

}

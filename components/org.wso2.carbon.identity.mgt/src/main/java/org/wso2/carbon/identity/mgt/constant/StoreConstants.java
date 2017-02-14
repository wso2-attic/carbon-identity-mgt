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
 * limitations under the License.
 */

package org.wso2.carbon.identity.mgt.constant;

/**
 * This class holds the constants related to all the store types.
 */
public class StoreConstants {

    /**
     * This class holds the constants related to identity stores.
     */
    public static class IdentityStoreConstants {

        public static final String UNIQUE_USED_ID = "UNIQUE_USED_ID";
        public static final String USER = "USER";
        public static final String USER_BEAN = "USER_BEAN";
        public static final String UNIQUE_USED_ID_LIST = "UNIQUE_USED_ID_LIST";
        public static final String USER_LIST = "USER_LIST";
        public static final String USER_BEAN_LIST = "USER_BEAN_LIST";

        public static final String UNIQUE_GROUP_ID = "UNIQUE_GROUP_ID";
        public static final String GROUP = "GROUP";
        public static final String GROUP_BEAN = "GROUP_BEAN";
        public static final String UNIQUE_GROUP_ID_LIST = "UNIQUE_GROUP_ID_LIST";
        public static final String GROUP_LIST = "GROUP_LIST";
        public static final String GROUP_BEAN_LIST = "GROUP_BEAN_LIST";


        public static final String CLAIM = "CLAIM";
        public static final String CLAIM_LIST = "CLAIM_LIST";
        public static final String META_CLAIM = "META_CLAIM";
        public static final String META_CLAIM_LIST = "META_CLAIM_LIST";
        public static final String DOMAIN_NAME = "DOMAIN_NAME";
        public static final String OFFSET = "OFFSET";
        public static final String LENGTH = "LENGTH";
        public static final String FILTER_PATTERN = "FILTER_PATTERN";
        public static final String IS_USER_IN_GROUP = "IS_USER_IN_GROUP";
        public static final String CLAIM_LIST_TO_ADD = "CLAIM_LIST_TO_ADD";
        public static final String CLAIM_LIST_TO_REMOVE = "CLAIM_LIST_TO_REMOVE";
        public static final String CREDENTIAL_LIST = "CREDENTIAL_LIST";
        public static final String CREDENTIAL_LIST_TO_ADD = "CREDENTIAL_LIST_TO_ADD";
        public static final String CREDENTIAL_LIST_TO_REMOVE = "CREDENTIAL_LIST_TO_REMOVE";
        public static final String UNIQUE_GROUP_ID_LIST_TO_ADD = "UNIQUE_GROUP_ID_LIST_TO_ADD";
        public static final String UNIQUE_GROUP_ID_LIST_TO_REMOVE = "UNIQUE_GROUP_ID_LIST_TO_REMOVE";
        public static final String UNIQUE_USED_ID_LIST_TO_ADD = "UNIQUE_USED_ID_LIST_TO_ADD";
        public static final String UNIQUE_USED_ID_LIST_TO_REMOVE = "UNIQUE_USED_ID_LIST_TO_REMOVE";
        public static final String AUTHENTICATION_CONTEXT = "AUTHENTICATION_CONTEXT";
        public static final String PRIMARY_DOMAIN_NAME = "PRIMARY_DOMAIN_NAME";
        public static final String DOMAIN_LIST = "DOMAIN_LIST";
    }

    /**
     * This class holds the constants related to identity store interceptors.
     */
    public static class IdentityStoreInterceptorConstants {

        public static final String PRE_GET_USER_BY_ID = "PRE_GET_USER_BY_ID";
        public static final String POST_GET_USER_BY_ID = "POST_GET_USER_BY_ID";
        public static final String PRE_GET_USER_BY_CLAIM = "PRE_GET_USER_BY_CLAIM";
        public static final String POST_GET_USER_BY_CLAIM = "POST_GET_USER_BY_CLAIM";
        public static final String PRE_GET_USER_BY_CLAIM_DOMAIN = "PRE_GET_USER_BY_CLAIM_DOMAIN";
        public static final String POST_GET_USER_BY_CLAIM_DOMAIN = "POST_GET_USER_BY_CLAIM_DOMAIN";

        public static final String PRE_LIST_USERS = "PRE_LIST_USERS";
        public static final String POST_LIST_USERS = "POST_LIST_USERS";
        public static final String PRE_LIST_USERS_BY_DOMAIN = "PRE_LIST_USERS_BY_DOMAIN";
        public static final String POST_LIST_USERS_BY_DOMAIN = "POST_LIST_USERS_BY_DOMAIN";
        public static final String PRE_LIST_USERS_BY_CLAIM = "PRE_LIST_USERS_BY_CLAIM";
        public static final String POST_LIST_USERS_BY_CLAIM = "POST_LIST_USERS_BY_CLAIM";
        public static final String PRE_LIST_USERS_BY_CLAIM_DOMAIN = "PRE_LIST_USERS_BY_CLAIM_DOMAIN";
        public static final String POST_LIST_USERS_BY_CLAIM_DOMAIN = "POST_LIST_USERS_BY_CLAIM_DOMAIN";
        public static final String PRE_LIST_USERS_BY_META_CLAIM = "PRE_LIST_USERS_BY_CLAIM_DOMAIN";
        public static final String POST_LIST_USERS_BY_META_CLAIM = "POST_LIST_USERS_BY_CLAIM_DOMAIN";
        public static final String PRE_LIST_USERS_BY_META_CLAIM_DOMAIN = "PRE_LIST_USERS_BY_META_CLAIM_DOMAIN";
        public static final String POST_LIST_USERS_BY_META_CLAIM_DOMAIN = "POST_LIST_USERS_BY_META_CLAIM_DOMAIN";

        public static final String PRE_GET_GROUP_BY_ID = "PRE_GET_GROUP_BY_ID";
        public static final String POST_GET_GROUP_BY_ID = "POST_GET_GROUP_BY_ID";
        public static final String PRE_GET_GROUP_BY_CLAIM = "PRE_GET_GROUP_BY_CLAIM";
        public static final String POST_GET_GROUP_BY_CLAIM = "POST_GET_GROUP_BY_CLAIM";
        public static final String PRE_GET_GROUP_BY_CLAIM_DOMAIN = "PRE_GET_GROUP_BY_CLAIM_DOMAIN";
        public static final String POST_GET_GROUP_BY_CLAIM_DOMAIN = "POST_GET_GROUP_BY_CLAIM_DOMAIN";

        public static final String PRE_LIST_GROUPS = "PRE_LIST_GROUPS";
        public static final String POST_LIST_GROUPS = "POST_LIST_GROUPS";
        public static final String PRE_LIST_GROUPS_BY_DOMAIN = "PRE_LIST_GROUPS_BY_DOMAIN";
        public static final String POST_LIST_GROUPS_BY_DOMAIN = "POST_LIST_GROUPS_BY_DOMAIN";
        public static final String PRE_LIST_GROUPS_BY_CLAIM = "PRE_LIST_GROUPS_BY_CLAIM";
        public static final String POST_LIST_GROUPS_BY_CLAIM = "POST_LIST_GROUPS_BY_CLAIM";
        public static final String PRE_LIST_GROUPS_BY_CLAIM_DOMAIN = "PRE_LIST_GROUPS_BY_CLAIM_DOMAIN";
        public static final String POST_LIST_GROUPS_BY_CLAIM_DOMAIN = "POST_LIST_GROUPS_BY_CLAIM_DOMAIN";
        public static final String PRE_LIST_GROUPS_BY_META_CLAIM = "PRE_LIST_GROUPS_BY_META_CLAIM";
        public static final String POST_LIST_GROUPS_BY_META_CLAIM = "POST_LIST_GROUPS_BY_META_CLAIM";
        public static final String PRE_LIST_GROUPS_BY_META_CLAIM_DOMAIN = "PRE_LIST_GROUPS_BY_META_CLAIM_DOMAIN";
        public static final String POST_LIST_GROUPS_BY_META_CLAIM_DOMAIN = "POST_LIST_GROUPS_BY_META_CLAIM_DOMAIN";

        public static final String PRE_GET_GROUPS_OF_USER = "PRE_GET_GROUPS_OF_USER";
        public static final String POST_GET_GROUPS_OF_USER = "POST_GET_GROUPS_OF_USER";
        public static final String PRE_GET_USERS_OF_GROUP = "PRE_GET_USERS_OF_GROUP";
        public static final String POST_GET_USERS_OF_GROUP = "POST_GET_USERS_OF_GROUP";
        public static final String PRE_IS_USER_IN_GROUP = "PRE_IS_USER_IN_GROUP";
        public static final String POST_IS_USER_IN_GROUP = "POST_IS_USER_IN_GROUP";

        public static final String PRE_GET_CLAIMS_OF_USER_BY_ID = "PRE_GET_CLAIMS_OF_USER_BY_ID";
        public static final String POST_GET_CLAIMS_OF_USER_BY_ID = "POST_GET_CLAIMS_OF_USER_BY_ID";
        public static final String PRE_GET_CLAIMS_OF_USER_BY_ID_META_CLAIMS =
                "PRE_GET_CLAIMS_OF_USER_BY_ID_META_CLAIMS";
        public static final String POST_GET_CLAIMS_OF_USER_BY_ID_META_CLAIMS =
                "POST_GET_CLAIMS_OF_USER_BY_ID_META_CLAIMS";

        public static final String PRE_GET_CLAIMS_OF_GROUP_BY_ID = "PRE_GET_CLAIMS_OF_GROUP_BY_ID";
        public static final String POST_GET_CLAIMS_OF_GROUP_BY_ID = "POST_GET_CLAIMS_OF_GROUP_BY_ID";
        public static final String PRE_GET_CLAIMS_OF_GROUP_BY_ID_META_CLAIMS =
                "PRE_GET_CLAIMS_OF_GROUP_BY_ID_META_CLAIMS";
        public static final String POST_GET_CLAIMS_OF_GROUP_BY_ID_META_CLAIMS =
                "POST_GET_CLAIMS_OF_GROUP_BY_ID_META_CLAIMS";

        public static final String PRE_ADD_USER = "PRE_ADD_USER";
        public static final String POST_ADD_USER = "POST_ADD_USER";
        public static final String PRE_ADD_USER_BY_DOMAIN = "PRE_ADD_USER_BY_DOMAIN";
        public static final String POST_ADD_USER_BY_DOMAIN = "POST_ADD_USER_BY_DOMAIN";
        public static final String PRE_ADD_USERS = "PRE_ADD_USERS";
        public static final String POST_ADD_USERS = "POST_ADD_USERS";
        public static final String PRE_ADD_USERS_BY_DOMAIN = "PRE_ADD_USERS_BY_DOMAIN";
        public static final String POST_ADD_USERS_BY_DOMAIN = "POST_ADD_USERS_BY_DOMAIN";

        public static final String PRE_UPDATE_USER_CLAIMS_PUT = "PRE_UPDATE_USER_CLAIMS_PUT";
        public static final String POST_UPDATE_USER_CLAIMS_PUT = "POST_UPDATE_USER_CLAIMS_PUT";
        public static final String PRE_UPDATE_USER_CLAIMS_PATCH = "PRE_UPDATE_USER_CLAIMS_PATCH";
        public static final String POST_UPDATE_USER_CLAIMS_PATCH = "POST_UPDATE_USER_CLAIMS_PATCH";
        public static final String PRE_UPDATE_USER_CREDENTIALS_PUT = "PRE_UPDATE_USER_CREDENTIALS_PUT";
        public static final String POST_UPDATE_USER_CREDENTIALS_PUT = "POST_UPDATE_USER_CREDENTIALS_PUT";
        public static final String PRE_UPDATE_USER_CREDENTIALS_PATCH = "PRE_UPDATE_USER_CREDENTIALS_PATCH";
        public static final String POST_UPDATE_USER_CREDENTIALS_PATCH = "POST_UPDATE_USER_CREDENTIALS_PATCH";

        public static final String PRE_DELETE_USER = "PRE_DELETE_USER";
        public static final String POST_DELETE_USER = "POST_DELETE_USER";

        public static final String PRE_UPDATE_GROUPS_OF_USER_PUT = "PRE_UPDATE_GROUPS_OF_USER_PUT";
        public static final String POST_UPDATE_GROUPS_OF_USER_PUT = "POST_UPDATE_GROUPS_OF_USER_PUT";
        public static final String PRE_UPDATE_GROUPS_OF_USER_PATCH = "PRE_UPDATE_GROUPS_OF_USER_PATCH";
        public static final String POST_UPDATE_GROUPS_OF_USER_PATCH = "POST_UPDATE_GROUPS_OF_USER_PATCH";

        public static final String PRE_ADD_GROUP = "PRE_ADD_GROUP";
        public static final String POST_ADD_GROUP = "POST_ADD_GROUP";
        public static final String PRE_ADD_GROUP_BY_DOMAIN = "PRE_ADD_GROUP_BY_DOMAIN";
        public static final String POST_ADD_GROUP_BY_DOMAIN = "POST_ADD_GROUP_BY_DOMAIN";
        public static final String PRE_ADD_GROUPS = "PRE_ADD_GROUPS";
        public static final String POST_ADD_GROUPS = "POST_ADD_GROUPS";
        public static final String PRE_ADD_GROUPS_BY_DOMAIN = "PRE_ADD_GROUPS_BY_DOMAIN";
        public static final String POST_ADD_GROUPS_BY_DOMAIN = "POST_ADD_GROUPS_BY_DOMAIN";

        public static final String PRE_UPDATE_GROUP_CLAIMS_PUT = "PRE_UPDATE_GROUP_CLAIMS_PUT";
        public static final String POST_UPDATE_GROUP_CLAIMS_PUT = "POST_UPDATE_GROUP_CLAIMS_PUT";
        public static final String PRE_UPDATE_GROUP_CLAIMS_PATCH = "PRE_UPDATE_GROUP_CLAIMS_PATCH";
        public static final String POST_UPDATE_GROUP_CLAIMS_PATCH = "POST_UPDATE_GROUP_CLAIMS_PATCH";

        public static final String PRE_DELETE_GROUP = "PRE_DELETE_GROUP";
        public static final String POST_DELETE_GROUP = "POST_DELETE_GROUP";

        public static final String PRE_UPDATE_USERS_OF_GROUP_PUT = "PRE_UPDATE_USERS_OF_GROUP_PUT";
        public static final String POST_UPDATE_USERS_OF_GROUP_PUT = "POST_UPDATE_USERS_OF_GROUP_PUT";
        public static final String PRE_UPDATE_USERS_OF_GROUP_PATCH = "PRE_UPDATE_USERS_OF_GROUP_PATCH";
        public static final String POST_UPDATE_USERS_OF_GROUP_PATCH = "POST_UPDATE_USERS_OF_GROUP_PATCH";

        public static final String PRE_AUTHENTICATE = "PRE_AUTHENTICATE";
        public static final String POST_AUTHENTICATE = "POST_AUTHENTICATE";

        public static final String PRE_GET_PRIMARY_DOMAIN_NAME = "PRE_GET_PRIMARY_DOMAIN_NAME";
        public static final String POST_GET_PRIMARY_DOMAIN_NAME = "POST_GET_PRIMARY_DOMAIN_NAME";
        public static final String PRE_GET_DOMAIN_NAMES = "PRE_GET_DOMAIN_NAMES";
        public static final String POST_GET_DOMAIN_NAMES = "POST_GET_DOMAIN_NAMES";
    }
}

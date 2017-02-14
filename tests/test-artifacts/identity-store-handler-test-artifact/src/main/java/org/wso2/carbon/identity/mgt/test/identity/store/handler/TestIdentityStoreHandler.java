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

package org.wso2.carbon.identity.mgt.test.identity.store.handler;

import org.wso2.carbon.identity.common.base.event.EventContext;
import org.wso2.carbon.identity.common.base.event.model.Event;
import org.wso2.carbon.identity.common.base.exception.IdentityException;
import org.wso2.carbon.identity.common.base.handler.InitConfig;
import org.wso2.carbon.identity.event.AbstractEventHandler;
import org.wso2.carbon.identity.event.EventException;

/**
 * Test identity store event handler.
 */
public class TestIdentityStoreHandler extends AbstractEventHandler {

    public static final ThreadLocal<Boolean> PRE = new ThreadLocal<>();
    public static final ThreadLocal<Boolean> POST = new ThreadLocal<>();

    @Override
    public void handle(EventContext eventMessageContext, Event event) throws EventException {

        if ("PRE_GET_USER_BY_ID".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_GET_USER_BY_ID".equals(event.getEventName())) {
            throw new EventException("Rollback test");
        } else if ("PRE_GET_USER_BY_CLAIM".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_GET_USER_BY_CLAIM".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_GET_USER_BY_CLAIM_DOMAIN".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_GET_USER_BY_CLAIM_DOMAIN".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_LIST_USERS".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_LIST_USERS".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_LIST_USERS_BY_DOMAIN".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_LIST_USERS_BY_DOMAIN".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_LIST_USERS_BY_CLAIM".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_LIST_USERS_BY_CLAIM".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_LIST_USERS_BY_CLAIM_DOMAIN".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_LIST_USERS_BY_CLAIM_DOMAIN".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_LIST_USERS_BY_CLAIM_DOMAIN".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_LIST_USERS_BY_CLAIM_DOMAIN".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_LIST_USERS_BY_META_CLAIM_DOMAIN".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_LIST_USERS_BY_META_CLAIM_DOMAIN".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_GET_GROUP_BY_ID".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_GET_GROUP_BY_ID".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_GET_GROUP_BY_CLAIM".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_GET_GROUP_BY_CLAIM".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_GET_GROUP_BY_CLAIM_DOMAIN".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_GET_GROUP_BY_CLAIM_DOMAIN".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_LIST_GROUPS".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_LIST_GROUPS".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_LIST_GROUPS_BY_DOMAIN".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_LIST_GROUPS_BY_DOMAIN".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_LIST_GROUPS_BY_CLAIM".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_LIST_GROUPS_BY_CLAIM".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_LIST_GROUPS_BY_CLAIM_DOMAIN".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_LIST_GROUPS_BY_CLAIM_DOMAIN".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_LIST_GROUPS_BY_META_CLAIM".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_LIST_GROUPS_BY_META_CLAIM".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_LIST_GROUPS_BY_META_CLAIM_DOMAIN".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_LIST_GROUPS_BY_META_CLAIM_DOMAIN".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_GET_GROUPS_OF_USER".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_GET_GROUPS_OF_USER".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_GET_USERS_OF_GROUP".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_GET_USERS_OF_GROUP".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_IS_USER_IN_GROUP".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_IS_USER_IN_GROUP".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_GET_CLAIMS_OF_USER_BY_ID".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_GET_CLAIMS_OF_USER_BY_ID".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_GET_CLAIMS_OF_USER_BY_ID_META_CLAIMS".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_GET_CLAIMS_OF_USER_BY_ID_META_CLAIMS".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_GET_CLAIMS_OF_GROUP_BY_ID".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_GET_CLAIMS_OF_GROUP_BY_ID".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_GET_CLAIMS_OF_GROUP_BY_ID_META_CLAIMS".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_GET_CLAIMS_OF_GROUP_BY_ID_META_CLAIMS".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_ADD_USER".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_ADD_USER".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_ADD_USER_BY_DOMAIN".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_ADD_USER_BY_DOMAIN".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_ADD_USERS".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_ADD_USERS".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_ADD_USERS_BY_DOMAIN".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_ADD_USERS_BY_DOMAIN".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_UPDATE_USER_CLAIMS_PUT".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_UPDATE_USER_CLAIMS_PUT".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_UPDATE_USER_CLAIMS_PATCH".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_UPDATE_USER_CLAIMS_PATCH".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_UPDATE_USER_CREDENTIALS_PUT".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_UPDATE_USER_CREDENTIALS_PUT".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_UPDATE_USER_CREDENTIALS_PATCH".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_UPDATE_USER_CREDENTIALS_PATCH".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_DELETE_USER".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_DELETE_USER".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_UPDATE_GROUPS_OF_USER_PUT".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_UPDATE_GROUPS_OF_USER_PUT".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_UPDATE_GROUPS_OF_USER_PATCH".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_UPDATE_GROUPS_OF_USER_PATCH".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_ADD_GROUP".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_ADD_GROUP".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_ADD_GROUP_BY_DOMAIN".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_ADD_GROUP_BY_DOMAIN".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_ADD_GROUPS".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_ADD_GROUPS".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_ADD_GROUPS_BY_DOMAIN".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_ADD_GROUPS_BY_DOMAIN".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_UPDATE_GROUP_CLAIMS_PUT".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_UPDATE_GROUP_CLAIMS_PUT".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_UPDATE_GROUP_CLAIMS_PATCH".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_UPDATE_GROUP_CLAIMS_PATCH".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_DELETE_GROUP".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_DELETE_GROUP".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_UPDATE_USERS_OF_GROUP_PUT".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_UPDATE_USERS_OF_GROUP_PUT".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_UPDATE_USERS_OF_GROUP_PATCH".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_UPDATE_USERS_OF_GROUP_PATCH".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_AUTHENTICATE".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_AUTHENTICATE".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_GET_PRIMARY_DOMAIN_NAME".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_GET_PRIMARY_DOMAIN_NAME".equals(event.getEventName())) {
            POST.set(true);
        } else if ("PRE_GET_DOMAIN_NAMES".equals(event.getEventName())) {
            PRE.set(true);
        } else if ("POST_GET_DOMAIN_NAMES".equals(event.getEventName())) {
            POST.set(true);
        }

    }

    @Override
    public void configure(InitConfig initConfig) throws IdentityException {

    }

    @Override
    public void onFault(EventContext eventContext, Event event) throws IdentityException {
        if ("POST_GET_USER_BY_ID".equals(event.getEventName())) {
            POST.set(Boolean.TRUE);
        }
    }

    @Override
    public String getName() {
        return "test.identity.store.handler";
    }
}

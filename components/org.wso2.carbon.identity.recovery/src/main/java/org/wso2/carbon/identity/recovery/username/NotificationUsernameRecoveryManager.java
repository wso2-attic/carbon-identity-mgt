/*
 *
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.recovery.username;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.common.base.event.EventContext;
import org.wso2.carbon.identity.common.base.event.model.Event;
import org.wso2.carbon.identity.common.base.exception.IdentityException;
import org.wso2.carbon.identity.event.EventConstants;
import org.wso2.carbon.identity.mgt.IdentityStore;
import org.wso2.carbon.identity.mgt.RealmService;
import org.wso2.carbon.identity.mgt.User;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.recovery.IdentityRecoveryConstants;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.internal.IdentityRecoveryServiceDataHolder;
import org.wso2.carbon.identity.recovery.mapping.UsernameConfig;
import org.wso2.carbon.identity.recovery.util.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;

/**
 * Manager class which can be used to recover passwords using a notification.
 */
public class NotificationUsernameRecoveryManager {

    private static final Logger log = LoggerFactory.getLogger(NotificationUsernameRecoveryManager.class);
    private static NotificationUsernameRecoveryManager instance = new NotificationUsernameRecoveryManager();

    private static UsernameConfig usernameConfig;

    private NotificationUsernameRecoveryManager() {
    }

    public static NotificationUsernameRecoveryManager getInstance() {
        // TODO will read from kernel.
        usernameConfig = new UsernameConfig();
        return instance;
    }

    public boolean verifyUsername(List<Claim> claims) throws
            IdentityRecoveryException {

        return recoverUserByClaims(claims);

    }

    private void triggerNotification(String userUniqueId, String type, User user)
            throws IdentityRecoveryException {
        String eventName = EventConstants.Event.TRIGGER_NOTIFICATION;
        HashMap<String, Object> properties = new HashMap<>();
        properties.put(EventConstants.EventProperty.USER_UNIQUE_ID, userUniqueId);
        properties.put(EventConstants.EventProperty.USER_STORE_DOMAIN, user.getDomainName());
        properties.put(IdentityRecoveryConstants.TEMPLATE_TYPE, type);
        Event identityMgtEvent = new Event(eventName, properties);
        EventContext eventContext = new EventContext();

        try {
            IdentityRecoveryServiceDataHolder.getInstance().getIdentityEventService().pushEvent(identityMgtEvent,
                    eventContext);
        } catch (IdentityException e) {
            throw Utils.handleServerException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_TRIGGER_NOTIFICATION,
                    userUniqueId, e);
        }
    }

    private boolean recoverUserByClaims(List<Claim> claims)
            throws IdentityRecoveryException {

        if (claims == null || claims.isEmpty()) {

            if (log.isDebugEnabled()) {
                log.debug("No claims are recieved.");
            }
            return false;
        }

        User user;
        List<User> resultedUserList = getUserList(claims);

        if (resultedUserList.size() == 1) {
            user = resultedUserList.get(0);
            if (log.isDebugEnabled()) {
                log.debug("There are more than one user in the result set : "
                        + user.toString());
            }
            // Send email an email with the username to the user.
            if (usernameConfig.isNotificationInternallyManaged()) {
                triggerNotification(user.getUniqueUserId(),
                        IdentityRecoveryConstants.NOTIFICATION_ACCOUNT_ID_RECOVERY, user);
            }
            return true;

        } else {
            if (log.isDebugEnabled()) {

                StringJoiner joiner = new StringJoiner(",");

                resultedUserList.forEach((user1) -> {
                    joiner.add(user1.getUniqueUserId());
                });

                log.debug("Can not identify a unique user, instead found: " + joiner.toString());
            }
            return false;
        }
    }

    private static List<User> getUserList(List<Claim> claims) throws IdentityRecoveryException {

        RealmService realmService = IdentityRecoveryServiceDataHolder.getInstance().getRealmService();
        IdentityStore identityStore = realmService.getIdentityStore();

        try {
            return identityStore.listUsers(claims, 0, 100);
        } catch (IdentityStoreException e) {
            String msg = "Unable to retrieve the user list from claim.";
            throw new IdentityRecoveryException(msg, e);
        }
    }
}

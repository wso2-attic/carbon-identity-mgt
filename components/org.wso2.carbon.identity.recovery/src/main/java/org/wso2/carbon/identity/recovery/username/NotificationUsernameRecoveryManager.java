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
import org.wso2.carbon.identity.mgt.IdentityStore;
import org.wso2.carbon.identity.mgt.RealmService;
import org.wso2.carbon.identity.mgt.User;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.IdentityRecoveryServerException;
import org.wso2.carbon.identity.recovery.internal.IdentityRecoveryServiceDataHolder;
import org.wso2.carbon.identity.recovery.mapping.UsernameConfig;
import org.wso2.carbon.identity.recovery.util.Utils;

import java.util.List;

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
        try {
            usernameConfig = Utils.getRecoveryConfigs().getRecovery().getUsername();
        } catch (IdentityRecoveryServerException e) {
            log.error("Error while Loading recovery-config file.", e);
        }
        return instance;
    }

    public boolean verifyUsername(List<Claim> claims) throws
            IdentityRecoveryException {

        return recoverUserByClaims(claims);

    }


    // TODO trigger Notification Method

    public boolean recoverUserByClaims(List<Claim> claims)
            throws IdentityRecoveryException {

        /* No need of checking recovery enable from back end side it is already checked from API side
         And portal side.

         */

        if (claims == null || claims.isEmpty()) {

            if (log.isDebugEnabled()) {
                log.debug("No claims are recieved");
            }
            return false;
            //TODO send exception
        }

        User user;
        List<User> resultedUserList = getUserList(claims);


        if (resultedUserList.size() == 1) {
            user = resultedUserList.get(0);
            if (log.isDebugEnabled()) {
                log.debug("There are more than one user in the result set : "
                        + user.toString());
            }

            //TODO send the username to the user as an email
            return true;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("There are more than one user in the result set : "
                        + resultedUserList.toString());
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

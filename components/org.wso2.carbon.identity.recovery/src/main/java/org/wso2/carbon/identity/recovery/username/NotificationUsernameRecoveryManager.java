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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.mgt.IdentityStore;
import org.wso2.carbon.identity.mgt.RealmService;
import org.wso2.carbon.identity.mgt.User;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.recovery.IdentityRecoveryConstants;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.internal.IdentityRecoveryServiceDataHolder;
import org.wso2.carbon.identity.recovery.model.UserClaim;
import org.wso2.carbon.identity.recovery.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Manager class which can be used to recover passwords using a notification.
 */
public class NotificationUsernameRecoveryManager {

    private static final Log log = LogFactory.getLog(NotificationUsernameRecoveryManager.class);

    private static NotificationUsernameRecoveryManager instance = new NotificationUsernameRecoveryManager();

    private NotificationUsernameRecoveryManager() {
    }

    public static NotificationUsernameRecoveryManager getInstance() {
        return instance;
    }


    public String verifyUsername(ArrayList<UserClaim> claims, Boolean notify) throws
            IdentityRecoveryException {

        // Check whether username recovery enable
        boolean isRecoveryUsernameEnable = Utils.getRecoveryConfigs().getUsername().isEnable();

        if (!isRecoveryUsernameEnable) {
            throw Utils.handleClientException(
                    IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_USERNAME_RECOVERY_NOT_ENABLE, null);
        }
        boolean isNotificationInternallyManaged;
        if (notify == null) {
            isNotificationInternallyManaged = Utils.getRecoveryConfigs().getUsername().getNotification().getInternalyManage().isEnable();
        } else {
            isNotificationInternallyManaged = notify.booleanValue();
        }


        User user = getUserByClaims(claims);

        if (user != null) {
            if (isNotificationInternallyManaged) {
                triggerNotification(user);
            } else {
                return user.getUniqueUserId();
            }
        }
        throw Utils.handleClientException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_NO_VALID_USERNAME, null);
    }

    private void triggerNotification(User user) {

        log.debug("The user's name is "+ user.getDomainName());
    }
//    private void triggerNotification(User user, String type) throws IdentityRecoveryException {
//
//        RealmService realmService = IdentityRecoveryServiceDataHolder.getInstance().getRealmService();
//        IdentityStore identityStore = realmService.getIdentityStore();
//
//        Claim claim = new Claim();
//        claim.setClaimUri(claimUri);
//        claim.setValue(value);
//
//        try {
//            return identityStore.listUsers(claim, 0, 100);
//        } catch (IdentityStoreException e) {
//            String msg = "Unable to retrieve the user list from claim";
//            throw new IdentityRecoveryException(msg, e);
//        }
//    }
//}

    private User getUserByClaims(ArrayList<UserClaim> claims)
            throws IdentityRecoveryException {

        if (claims == null || claims.size() < 1) {
            throw Utils.handleClientException(
                    IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_NO_FIELD_FOUND_FOR_USER_RECOVERY, null);
        }

        List<User> resultedUserList = null;
        User user;

        // Need to populate the claim email as the first element in the
        // passed array.
        for (int i = 0; i < claims.size(); i++) {

            UserClaim claim = claims.get(i);
            if (claim.getClaimURI() != null && claim.getClaimValue() != null) {

                if (log.isDebugEnabled()) {
                    log.debug("Searching users for " + claim.getClaimURI() + " with the value :"
                              + claim.getClaimValue());
                }
                List<User> matchedUserList = getUserList(claim.getClaimURI(), claim.getClaimValue());

                if (matchedUserList.isEmpty()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Matched userList : " + Arrays.toString(matchedUserList.toArray()));
                    }
                    //If more than one user find the first matching user list. Hence need to define unique claims
                    if (resultedUserList != null) {
                        resultedUserList = new ArrayList<>();
                        for (User resultedUser : resultedUserList) {
                            for (User matchedUser : matchedUserList) {
                                if (resultedUser.getUniqueUserId().equals(matchedUser.getUniqueUserId())) {
                                    resultedUserList.add(matchedUser);
                                }
                            }
                        }
                        if (resultedUserList.size() > 0) {
                            if (log.isDebugEnabled()) {
                                log.debug("Current matching temporary userlist :"
                                          + Arrays.toString(resultedUserList.toArray()));
                            }
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug("There are no users for " + claim.getClaimURI() + " with the value : "
                                          + claim.getClaimValue() + " in the previously filtered user list");
                            }
                            throw Utils.handleClientException(
                                    IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_NO_USER_FOUND_FOR_RECOVERY,
                                    null);
                        }
                    } else {
                        resultedUserList = new ArrayList<>();
                        resultedUserList.addAll(matchedUserList);
                        if (log.isDebugEnabled()) {
                            log.debug("Current matching temporary userlist :"
                                      + Arrays.toString(resultedUserList.toArray()));
                        }
                    }

                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("There are no matching users for " + claim.getClaimURI() + " with the value : "
                                  + claim.getClaimValue());
                    }
                    throw Utils.handleClientException(
                            IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_NO_USER_FOUND_FOR_RECOVERY, null);
                }
            }
        }

        if (resultedUserList.size() == 1) {
            user = resultedUserList.get(0);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("There are more than one user in the result set : "
                          + Arrays.toString(resultedUserList.toArray()));
            }
            throw Utils.handleClientException(
                    IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_NO_USER_FOUND_FOR_RECOVERY, null);
        }
        return user;
    }

    private static List<User> getUserList(String claimUri, String value) throws IdentityRecoveryException {

        RealmService realmService = IdentityRecoveryServiceDataHolder.getInstance().getRealmService();
        IdentityStore identityStore = realmService.getIdentityStore();

        Claim claim = new Claim();
        claim.setClaimUri(claimUri);
        claim.setValue(value);

        try {
            return identityStore.listUsers(claim, 0, 100);
        } catch (IdentityStoreException e) {
            String msg = "Unable to retrieve the user list from claim";
            throw new IdentityRecoveryException(msg, e);
        }
    }
}

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



import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.mgt.IdentityStore;
import org.wso2.carbon.identity.mgt.RealmService;
import org.wso2.carbon.identity.mgt.User;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.recovery.IdentityRecoveryConstants;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.IdentityRecoveryServerException;
import org.wso2.carbon.identity.recovery.internal.IdentityRecoveryServiceDataHolder;
import org.wso2.carbon.identity.recovery.mapping.UsernameConfig;
import org.wso2.carbon.identity.recovery.model.UserClaim;
import org.wso2.carbon.identity.recovery.util.Utils;


import java.util.ArrayList;
import java.util.Arrays;
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

    public boolean verifyUsername(ArrayList<UserClaim> claims) throws
            IdentityRecoveryException {

        // Check whether username recovery enable
        boolean isRecoveryUsernameEnable = usernameConfig.isEnable();

        if (!isRecoveryUsernameEnable) {
            throw Utils.handleClientException(
                    IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_USERNAME_RECOVERY_NOT_ENABLE, null);
        }

        boolean isNotificationInternallyManaged = usernameConfig.getNotificationInternallyManaged().isEnable();

        if (isNotificationInternallyManaged) {
            return  recoverUserByClaims(claims);
        }
        throw Utils.handleClientException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_NO_VALID_USERNAME, null);
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

    public boolean recoverUserByClaims(ArrayList<UserClaim> claims)
            throws IdentityRecoveryException {

        // Check whether username recovery enable
        boolean isRecoveryUsernameEnable = Utils.getRecoveryConfigs().getRecovery().getUsername().isEnable();

        if (!isRecoveryUsernameEnable) {

            if (log.isDebugEnabled()) {
                log.debug("Username Recovery is not enabled");
            }
            return false;
            //TODO send exception
//            throw Utils.handleClientException(
//                    IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_USERNAME_RECOVERY_NOT_ENABLE, null);
        }

        if (claims == null || claims.size() < 1) {

            if (log.isDebugEnabled()) {
                log.debug("No claims are recieved");
            }
            return false;
            //TODO send exception
//            throw Utils.handleClientException(
//                    IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_NO_FIELD_FOUND_FOR_USER_RECOVERY, null);
        }

        List<User> resultedUserList = new ArrayList<>();
        User user;

        // Need to populate the claim email as the first element in the
        // passed array.
        for (int i = 0; i < claims.size(); i++) {

            UserClaim claim = claims.get(i);
            if (StringUtils.isBlank(claim.getClaimValue())) {
                continue;
            }
            if (claim.getClaimURI() != null && claim.getClaimValue() != null) {

                if (log.isDebugEnabled()) {
                    log.debug("Searching users for " + claim.getClaimURI() + " with the value :"
                            + claim.getClaimValue());
                }
                List<User> matchedUserList = getUserList(claim.getClaimURI(), claim.getClaimValue());

                if (!matchedUserList.isEmpty()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Matched userList : " + Arrays.toString(matchedUserList.toArray()));
                    }
                    //If more than one user find the first matching user list. Hence need to define unique claims
                    if (!resultedUserList.isEmpty()) {
                        List<User> users = new ArrayList<>();
                        for (User resultedUser : resultedUserList) {
                            for (User matchedUser : matchedUserList) {
                                if (resultedUser.getUniqueUserId().equals(matchedUser.getUniqueUserId())) {
                                    users.add(matchedUser);
                                }
                            }
                        }
                        if (users.size() > 0) {
                            resultedUserList = new ArrayList<>(users.size());
                            resultedUserList.addAll(users);
                            if (log.isDebugEnabled()) {
                                log.debug("Current matching temporary userlist :"
                                        + resultedUserList.toString());
                            }
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug("There are no users for " + claim.getClaimURI() + " with the value : "
                                        + claim.getClaimValue() + " in the previously filtered user list");
                            }
                            return false;
                        }
                    } else {
                        resultedUserList.addAll(matchedUserList);
                        if (log.isDebugEnabled()) {
                            log.debug("Current matching temporary userlist :"
                                    + resultedUserList.toString());
                        }
                    }

                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("There are no matching users for " + claim.getClaimURI() + " with the value : "
                                + claim.getClaimValue());
                    }
                    return false;
//                    throw Utils.handleClientException(
//                            IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_NO_USER_FOUND_FOR_RECOVERY, null);
                }
            }
        }

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

    private static List<User> getUserList(String claimUri, String value) throws IdentityRecoveryException {

        RealmService realmService = IdentityRecoveryServiceDataHolder.getInstance().getRealmService();
        IdentityStore identityStore = realmService.getIdentityStore();

        Claim claim = new Claim();
        claim.setClaimUri(claimUri);
        claim.setValue(value);
        claim.setDialectUri("http://wso2.org/claims");

        try {
            return identityStore.listUsers(claim, 0, 100);
        } catch (IdentityStoreException e) {
            String msg = "Unable to retrieve the user list from claim";
            throw new IdentityRecoveryException(msg, e);
        }
    }
}

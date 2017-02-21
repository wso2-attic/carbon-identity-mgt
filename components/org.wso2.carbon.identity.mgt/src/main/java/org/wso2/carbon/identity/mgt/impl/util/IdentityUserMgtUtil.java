/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.mgt.impl.util;

import org.wso2.carbon.identity.common.base.exception.IdentityRuntimeException;
import org.wso2.carbon.identity.mgt.IdentityStore;
import org.wso2.carbon.identity.mgt.RealmService;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.mgt.impl.internal.IdentityMgtDataHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * Identity Management Util.
 */
public class IdentityUserMgtUtil {

    private IdentityUserMgtUtil() {

    }

    /**
     * Generate UUID.
     *
     * @return UUID as a string.
     */
    public static String generateUUID() {

        String random = UUID.randomUUID().toString();
        random = random.replace("/", "_");
        random = random.replace("=", "a");
        random = random.replace("+", "f");
        return random;
    }

    /**
     * Add/Update a claim of a user.
     *
     * @param uniqueUserId Unique ID of the user.
     * @param claimUri Claim URI to be added or updated.
     * @param value Value of the claim.
     * @param claimDialect Claim dialect of the claim URI. If dialect is null, root dialect will be used.
     * @throws IdentityStoreException If error occurs while updating the claim.
     * @throws UserNotFoundException If the user does not exist.
     */
    public static void setClaimInIdentityStore(String uniqueUserId, String claimUri, String value, @Nullable String
            claimDialect) throws IdentityStoreException, UserNotFoundException {

        RealmService realmService = IdentityMgtDataHolder.getInstance().getRealmService();
        IdentityStore identityStore = realmService.getIdentityStore();

        if (identityStore == null) {
            throw new IdentityRuntimeException("Error while obtaining the identity store. Null reference to Identity " +
                                               "store.");
        }

        if (claimDialect == null) {
            claimDialect = IdentityMgtConstants.CLAIM_ROOT_DIALECT;
        }

        List<Claim> claimsList = new ArrayList<>(1);
        claimsList.add(new Claim(claimDialect, claimUri, value));

        identityStore.updateUserClaims(uniqueUserId, claimsList, null);
    }

    /**
     * Add/Update multiple claims of a user.
     *
     * @param uniqueUserId Unique ID of the user.
     * @param claims Map of claim URIs and values to be updated
     * @param claimDialect Claim dialect of the claim URI. If dialect is null, root dialect will be used.
     * @throws IdentityStoreException If error occurs while updating the claim.
     * @throws UserNotFoundException If the user does not exist.
     */
    public static void setClaimsInIdentityStore(String uniqueUserId, Map<String, String> claims, @Nullable String
            claimDialect) throws IdentityStoreException, UserNotFoundException {

        RealmService realmService = IdentityMgtDataHolder.getInstance().getRealmService();
        IdentityStore identityStore = realmService.getIdentityStore();

        if (identityStore == null) {
            throw new IdentityRuntimeException("Error while obtaining the identity store. Null reference to Identity " +
                                               "store.");
        }

        if (claimDialect == null) {
            claimDialect = IdentityMgtConstants.CLAIM_ROOT_DIALECT;
        }

        String dialect = claimDialect;
        List<Claim> claimsList = claims.entrySet().stream()
                                       .map((x) -> new Claim(dialect, x.getKey(), x.getValue()))
                                       .collect(Collectors.toList());

        identityStore.updateUserClaims(uniqueUserId, claimsList, null);
    }

}

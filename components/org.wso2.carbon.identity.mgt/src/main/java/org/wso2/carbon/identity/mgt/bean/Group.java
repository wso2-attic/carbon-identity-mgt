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

package org.wso2.carbon.identity.mgt.bean;

import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.claim.MetaClaim;
import org.wso2.carbon.identity.mgt.exception.GroupNotFoundException;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.StoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.mgt.store.IdentityStore;
import org.wso2.carbon.security.caas.user.core.bean.Permission;
import org.wso2.carbon.security.caas.user.core.bean.Role;
import org.wso2.carbon.security.caas.user.core.exception.AuthorizationStoreException;
import org.wso2.carbon.security.caas.user.core.store.AuthorizationStore;

import java.util.List;

/**
 * Group represents a group of users.
 */
public class Group {

    /**
     * Unique group id.
     */
    private String uniqueGroupId;

    /**
     * Domain in which the group belongs.
     */
    private String domainName;

    /**
     * The IdentityStore this user originates from.
     */
    private IdentityStore identityStore;

    /**
     * The AuthorizationStore that manages permissions of this user.
     */
    private AuthorizationStore authorizationStore;

    private Group(String uniqueGroupId, String domainName, IdentityStore identityStore, AuthorizationStore
            authorizationStore) {

        this.uniqueGroupId = uniqueGroupId;
        this.domainName = domainName;
        this.identityStore = identityStore;
        this.authorizationStore = authorizationStore;
    }

    /**
     * Get the group id.
     *
     * @return Group id.
     */
    public String getUniqueGroupId() {
        return uniqueGroupId;
    }

    /**
     * Get this group's domainName name.
     *
     * @return Domain name of this group.
     */
    public String getDomainName() {
        return domainName;
    }

    /**
     * Get the users assigned to this group.
     *
     * @return List of users assigned to this group.
     * @throws IdentityStoreException Identity store exception.
     */
    public List<User> getUsers() throws IdentityStoreException, GroupNotFoundException {
        return identityStore.getUsersOfGroup(uniqueGroupId);
    }

    /**
     * Get Roles assigned to this Group.
     *
     * @return List of Roles.
     * @throws AuthorizationStoreException Authorization store exception.
     */
    public List<Role> getRoles() throws AuthorizationStoreException {
        //return authorizationStore.getRolesOfGroup(uniqueGroupId, domainName);
        return null;
    }

    /**
     * Checks whether this Group is authorized for given Permission.
     *
     * @param permission Permission to be checked.
     * @return True if authorized.
     * @throws AuthorizationStoreException Authorization store exception.
     */
    public boolean isAuthorized(Permission permission) throws AuthorizationStoreException {
        //return authorizationStore.isGroupAuthorized(uniqueGroupId, domainName, permission);
        return false;
    }

    /**
     * Checks whether the User in this Group.
     *
     * @param userId Id of the User to be checked.
     * @return True if User is in this Group.
     * @throws IdentityStoreException Identity store exception.
     */
    public boolean hasUser(String userId) throws IdentityStoreException, UserNotFoundException {
        return identityStore.isUserInGroup(userId, uniqueGroupId);
    }

    /**
     * Checks whether this Group has the Role.
     *
     * @param roleName Name of the Role to be checked.
     * @return True if this Group has the Role.
     * @throws AuthorizationStoreException Authorization store exception.
     */
    public boolean hasRole(String roleName) throws AuthorizationStoreException {
        //TODO
        return authorizationStore.isGroupInRole(uniqueGroupId, null, roleName);
    }

    /**
     * Add a new Role list by <b>replacing</b> the existing Role list. (PUT)
     *
     * @param newRoleList List of Roles needs to be assigned to this Group.
     * @throws AuthorizationStoreException Authorization store exception.
     */
    public void updateRoles(List<Role> newRoleList) throws AuthorizationStoreException {
        //authorizationStore.updateRolesInGroup(uniqueGroupId, domainName, newRoleList);
    }

    /**
     * Assign a new list of Roles to existing list and/or un-assign Roles from existing list. (PATCH)
     *
     * @param assignList   List to be added to the new list.
     * @param unAssignList List to be removed from the existing list.
     * @throws AuthorizationStoreException Authorization store exception.
     */
    public void updateRoles(List<Role> assignList, List<Role> unAssignList) throws AuthorizationStoreException {
        //authorizationStore.updateRolesInGroup(uniqueGroupId, domainName, assignList, unAssignList);
    }

    /**
     * Change the identity store
     * @param identityStore identity store instance
     */
    public void setIdentityStore(IdentityStore identityStore) {
        this.identityStore = identityStore;
    }


    /**
     * Get claims of this group.
     *
     * @return List of Group claims.
     * @throws IdentityStoreException Identity store exception.
     */
    public List<Claim> getClaims() throws IdentityStoreException, GroupNotFoundException {
        return identityStore.getClaimsOfGroup(this.uniqueGroupId, this.domainName);
    }

    /**
     * Get claims of this group for given URIs.
     *
     * @param metaClaims Claim URIs that needs to be retrieved.
     * @return List of Group claims.
     * @throws IdentityStoreException Identity store exception.
     */
    public List<Claim> getClaims(List<MetaClaim> metaClaims) throws IdentityStoreException, GroupNotFoundException {
        return identityStore.getClaimsOfGroup(uniqueGroupId, metaClaims, domainName);
    }


    /**
     * Builder for group bean.
     */
    public static class GroupBuilder {

        private String groupId;

        private String domainName;

        private IdentityStore identityStore;

        private AuthorizationStore authorizationStore;

        public String getGroupId() {
            return groupId;
        }

        public String getDomainName() {
            return domainName;
        }

        public IdentityStore getIdentityStore() {
            return identityStore;
        }

        public AuthorizationStore getAuthorizationStore() {
            return authorizationStore;
        }

        public GroupBuilder setGroupId(String groupId) {
            this.groupId = groupId;
            return this;
        }

        public GroupBuilder setDomainName(String domainName) {
            this.domainName = domainName;
            return this;
        }

        public GroupBuilder setIdentityStore(IdentityStore identityStore) {
            this.identityStore = identityStore;
            return this;
        }

        public GroupBuilder setAuthorizationStore(AuthorizationStore authorizationStore) {
            this.authorizationStore = authorizationStore;
            return this;
        }

        public Group build() {

            //TODO add authorizationStore == null
            if (groupId == null || identityStore == null || domainName == null) {
                throw new StoreException("Required data missing for building group.");
            }

            return new Group(groupId, domainName, identityStore, authorizationStore);
        }
    }
}

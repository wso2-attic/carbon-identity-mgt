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

package org.wso2.carbon.identity.mgt;

import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.claim.MetaClaim;
import org.wso2.carbon.identity.mgt.exception.GroupNotFoundException;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.StoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a user in the user core. All of the user related identity operations can be
 * done through this class.
 */
public class User implements Serializable {

    private static final long serialVersionUID = 21578845544565554L;

    /**
     * The globally unique uniqueUserId of the user.
     */
    private String uniqueUserId;

    /**
     * The domain name this user belongs to.
     */
    private String domainName;

    /**
     * The IdentityStore this user originates from.
     */
    private transient IdentityStore identityStore;

    private User(String uniqueUserId, String domainName) {

        this.uniqueUserId = uniqueUserId;
        this.domainName = domainName;
    }

    /**
     * Get user name.
     *
     * @return User name.
     */
    public String getUniqueUserId() {
        return uniqueUserId;
    }

    /**
     * Get the user's domain name.
     *
     * @return domain name of the user.
     */
    public String getDomainName() {
        return this.domainName;
    }

    /**
     * Get claims of this user.
     *
     * @return List of User claims.
     * @throws IdentityStoreException Identity store exception.
     */
    public List<Claim> getClaims() throws IdentityStoreException, UserNotFoundException {

        return identityStore.getClaimsOfUser(this.uniqueUserId);
    }

    /**
     * Get claims of this user for given URIs.
     *
     * @param metaClaims Claim URIs that needs to be retrieved.
     * @return List of User claims.
     * @throws IdentityStoreException Identity store exception.
     */
    public List<Claim> getClaims(List<MetaClaim> metaClaims) throws IdentityStoreException, UserNotFoundException {

        return identityStore.getClaimsOfUser(uniqueUserId, metaClaims);
    }

    /**
     * Get the groups assigned to this user.
     *
     * @return List of Groups assigned to this user.
     * @throws IdentityStoreException Identity store exception.
     */
    public List<Group> getGroups() throws IdentityStoreException, GroupNotFoundException, UserNotFoundException {

        return identityStore.getGroupsOfUser(uniqueUserId);
    }

    /**
     * Checks whether this user is in the given Group.
     *
     * @param groupName Name of the Group.
     * @return True if this User is in the group.
     * @throws IdentityStoreException Identity store exception.
     */
    public boolean isInGroup(String groupName) throws IdentityStoreException, UserNotFoundException,
            GroupNotFoundException {

        return identityStore.isUserInGroup(uniqueUserId, groupName);
    }

    public void setIdentityStore(IdentityStore identityStore) {
        this.identityStore = identityStore;
    }

    @Override
    public String toString() {
        return "User{" +
                "uniqueUserId='" + uniqueUserId + '\'' +
                ", domainName='" + domainName + '\'' +
                '}';
    }

    //    /**
//     * Get the roles assigned to this user.
//     *
//     * @return List of Roles assigned to this user.
//     * @throws AuthorizationStoreException Authorization store exception,
//     */
//    public List<Role> getRoles() throws AuthorizationStoreException {
//        //return authorizationStore.getRolesOfUser(uniqueUserId, domainName);
//        return null;
//    }

//    /**
//     * Get permissions filtered from the given resource.
//     *
//     * @param resource Resource to filter.
//     * @return List of permissions.
//     * @throws AuthorizationStoreException authorization store exception.
//     */
//    public List<Permission> getPermissions(Resource resource) throws AuthorizationStoreException {
//        //return authorizationStore.getPermissionsOfUser(uniqueUserId, domainName, resource);
//        return null;
//    }

//    /**
//     * Get permissions filtered from the given action.
//     *
//     * @param action Action to filter.
//     * @return List of permissions.
//     * @throws AuthorizationStoreException Authorization store exception.
//     */
//    public List<Permission> getPermissions(Action action) throws AuthorizationStoreException {
//        //return authorizationStore.getPermissionsOfUser(uniqueUserId, domainName, action);
//        return null;
//    }

//    /**
//     * Checks whether this user is authorized for given Permission.
//     *
//     * @param permission Permission that should check on this user.
//     * @return True if authorized.
//     * @throws AuthorizationStoreException Authorization store exception.
//     * @throws IdentityStoreException      Identity store exception.
//     */
//    public boolean isAuthorized(Permission permission) throws AuthorizationStoreException, IdentityStoreException {
//        return authorizationStore.isUserAuthorized(uniqueUserId, permission, domainName);
//    }

//    /**
//     * Checks whether this User is in the given Role.
//     *
//     * @param roleName Name of the Role.
//     * @return True if this user is in the Role.
//     * @throws AuthorizationStoreException Authorization store exception.
//     */
//    public boolean isInRole(String roleName) throws AuthorizationStoreException {
//        return authorizationStore.isUserInRole(uniqueUserId, roleName);
//    }

//    /**
//     * Add a new Role list by <b>replacing</b> the existing Role list. (PUT)
//     *
//     * @param newRolesList List of Roles needs to be assigned to this User.
//     * @throws AuthorizationStoreException Authorization store exception,
//     * @throws IdentityStoreException      Identity store exception.
//     */
//    public void updateRoles(List<Role> newRolesList) throws AuthorizationStoreException, IdentityStoreException {
//        authorizationStore.updateRolesInUser(uniqueUserId, domainName, newRolesList);
//    }

//    /**
//     * Assign a new list of Roles to existing list and/or un-assign Roles from existing list. (PATCH)
//     *
//     * @param assignList   List to be added to the new list.
//     * @param unAssignList List to be removed from the existing list.
//     * @throws AuthorizationStoreException Authorization Store Exception.
//     */
//    public void updateRoles(List<Role> assignList, List<Role> unAssignList) throws AuthorizationStoreException {
//        authorizationStore.updateRolesInUser(uniqueUserId, domainName, assignList, unAssignList);
//    }

    /**
     * Builder for the user bean.
     */
    public static class UserBuilder {

        private String userId;

        private String domainName;

        private IdentityStore identityStore;

        private AuthorizationStore authorizationStore;

        public String getUserId() {
            return userId;
        }

        public IdentityStore getIdentityStore() {
            return identityStore;
        }

        public AuthorizationStore getAuthorizationStore() {
            return authorizationStore;
        }

        public UserBuilder setUserId(String userName) {
            this.userId = userName;
            return this;
        }

        public UserBuilder setDomainName(String domainName) {
            this.domainName = domainName;
            return this;
        }

        public UserBuilder setIdentityStore(IdentityStore identityStore) {
            this.identityStore = identityStore;
            return this;
        }

        public UserBuilder setAuthorizationStore(AuthorizationStore authorizationStore) {
            this.authorizationStore = authorizationStore;
            return this;
        }

        public User build() {

            //TODO Add authorizationStore with M3
            if (userId == null || identityStore == null || domainName == null) {
                throw new StoreException("Required data missing for building user.");
            }

            User user = new User(userId, domainName);
            user.setIdentityStore(identityStore);
            return user;
        }
    }
}

/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.mgt.store.connector.inmemory;

import org.wso2.carbon.identity.mgt.Action;
import org.wso2.carbon.identity.mgt.Group;
import org.wso2.carbon.identity.mgt.Permission;
import org.wso2.carbon.identity.mgt.Resource;
import org.wso2.carbon.identity.mgt.Role;
import org.wso2.carbon.identity.mgt.User;
import org.wso2.carbon.identity.mgt.connector.AuthorizationStoreConnector;
import org.wso2.carbon.identity.mgt.connector.config.AuthorizationStoreConnectorConfig;
import org.wso2.carbon.identity.mgt.exception.AuthorizationStoreException;
import org.wso2.carbon.identity.mgt.exception.PermissionNotFoundException;
import org.wso2.carbon.identity.mgt.exception.RoleNotFoundException;

import java.util.List;

/**
 * In memory authorization connector for tests
 */
public class InMemoryAuthorizationStoreConnector implements AuthorizationStoreConnector {

    private AuthorizationStoreConnectorConfig authorizationStoreConnectorConfig;
    private String storeID;

    @Override
    public void init(String storeId, AuthorizationStoreConnectorConfig authorizationStoreConnectorConfig)
            throws AuthorizationStoreException {
        this.authorizationStoreConnectorConfig = authorizationStoreConnectorConfig;
        this.storeID = storeId;
    }

    @Override
    public Role.RoleBuilder getRole(String roleId) throws RoleNotFoundException, AuthorizationStoreException {
        return null;
    }

    @Override
    public int getRoleCount() throws AuthorizationStoreException {
        return 0;
    }

    @Override
    public List<Role.RoleBuilder> listRoles(String filterPattern, int offset, int length)
            throws AuthorizationStoreException {
        return null;
    }

    @Override
    public Permission.PermissionBuilder getPermission(Resource resource, Action action)
            throws PermissionNotFoundException, AuthorizationStoreException {
        return null;
    }

    @Override
    public int getPermissionCount() throws AuthorizationStoreException {
        return 0;
    }

    @Override
    public List<Permission.PermissionBuilder> listPermissions(String resourcePattern, String actionPattern,
                                                              int offset, int length)
            throws AuthorizationStoreException {
        return null;
    }

    @Override
    public List<Resource.ResourceBuilder> getResources(String resourcePattern) throws AuthorizationStoreException {
        return null;
    }

    @Override
    public List<Action.ActionBuilder> getActions(String actionPattern) throws AuthorizationStoreException {
        return null;
    }

    @Override
    public List<Role.RoleBuilder> getRolesForUser(String userId) throws AuthorizationStoreException {
        return null;
    }

    @Override
    public List<Role.RoleBuilder> getRolesForGroup(String groupId) throws AuthorizationStoreException {
        return null;
    }

    @Override
    public List<Permission.PermissionBuilder> getPermissionsForRole(String roleId, Resource resource)
            throws AuthorizationStoreException {
        return null;
    }

    @Override
    public List<Permission.PermissionBuilder> getPermissionsForRole(String roleId, Action action)
            throws AuthorizationStoreException {
        return null;
    }

    @Override
    public Resource.ResourceBuilder addResource(String resourceNamespace, String resourceId, String userId)
            throws AuthorizationStoreException {
        return null;
    }

    @Override
    public Action addAction(String actionNamespace, String actionName) throws AuthorizationStoreException {
        return null;
    }

    @Override
    public Permission.PermissionBuilder addPermission(Resource resource, Action action)
            throws AuthorizationStoreException {
        return null;
    }

    @Override
    public Role.RoleBuilder addRole(String roleName, List<Permission> permissions) throws AuthorizationStoreException {
        return null;
    }

    @Override
    public boolean isUserInRole(String userId, String roleName) throws AuthorizationStoreException {
        return false;
    }

    @Override
    public boolean isGroupInRole(String groupId, String roleName) throws AuthorizationStoreException {
        return false;
    }

    @Override
    public List<User.UserBuilder> getUsersOfRole(String roleId) throws AuthorizationStoreException {
        return null;
    }

    @Override
    public List<Group.GroupBuilder> getGroupsOfRole(String roleId) throws AuthorizationStoreException {
        return null;
    }

    @Override
    public void deleteRole(String roleId) throws AuthorizationStoreException {

    }

    @Override
    public void deletePermission(String permissionId) throws AuthorizationStoreException {

    }

    @Override
    public void deleteResource(Resource resource) throws AuthorizationStoreException {

    }

    @Override
    public void deleteAction(Action action) throws AuthorizationStoreException {

    }

    @Override
    public void updateRolesInUser(String userId, List<Role> newRoleList) throws AuthorizationStoreException {

    }

    @Override
    public void updateUsersInRole(String roleId, List<User> newUserList) throws AuthorizationStoreException {

    }

    @Override
    public void updateRolesInGroup(String groupId, List<Role> newRoleList) throws AuthorizationStoreException {

    }

    @Override
    public void updateGroupsInRole(String roleId, List<Group> newGroupList) throws AuthorizationStoreException {

    }

    @Override
    public void updatePermissionsInRole(String roleId, List<Permission> newPermissionList)
            throws AuthorizationStoreException {

    }

    @Override
    public void updatePermissionsInRole(String roleId, List<Permission> permissionsToBeAssign,
                                        List<Permission> permissionsToBeUnassign) throws AuthorizationStoreException {

    }

    @Override
    public void updateRolesInUser(String userId, List<Role> rolesToBeAssign, List<Role> rolesToBeUnassign)
            throws AuthorizationStoreException {

    }

    @Override
    public void updateUsersInRole(String roleId, List<User> usersToBeAssign, List<User> usersToBeUnassign)
            throws AuthorizationStoreException {

    }

    @Override
    public void updateGroupsInRole(String roleId, List<Group> groupToBeAssign, List<Group> groupToBeUnassign)
            throws AuthorizationStoreException {

    }

    @Override
    public void updateRolesInGroup(String groupId, List<Role> rolesToBeAssign, List<Role> rolesToBeUnassigned)
            throws AuthorizationStoreException {

    }

    @Override
    public AuthorizationStoreConnectorConfig getAuthorizationStoreConfig() {
        return authorizationStoreConnectorConfig;
    }

    @Override
    public String getAuthorizationStoreId() {
        return storeID;
    }
}

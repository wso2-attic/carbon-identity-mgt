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

package org.wso2.carbon.identity.mgt.user.impl;

import org.wso2.carbon.identity.mgt.exception.UserManagerException;
import org.wso2.carbon.identity.mgt.user.ConnectedGroup;
import org.wso2.carbon.identity.mgt.user.ConnectedUser;
import org.wso2.carbon.identity.mgt.user.UserManager;
import org.wso2.carbon.identity.mgt.util.IdentityMgtConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * UserManager implementation.
 */
public class UserManagerImpl implements UserManager {

    public UserManagerImpl() throws UserManagerException {

    }

    @Override
    public String getUniqueUserId(String connectorUserId, String connectorId) throws UserManagerException {
        return null;
    }

    @Override
    public String getConnectorUserId(String uniqueUserId, String connectorId) throws UserManagerException {
        return null;
    }

    @Override
    public void addUser(String uniqueUserId, List<ConnectedUser> connectedUsers) throws UserManagerException {

    }

    @Override
    public void addUsers(Map<String, List<ConnectedUser>> connectedUsersMap) throws UserManagerException {

    }

    @Override
    public void updateUser(String uniqueUserId, Map<String, String> connectorUserIdMap) throws UserManagerException {

    }

    @Override
    public void deleteUser(String uniqueUserId) throws UserManagerException {

    }

    @Override
    public Map<String, String> getConnectorUserIds(String userUniqueId) throws UserManagerException {
        return null;
    }

    @Override
    public String getDomainNameFromUserUniqueId(String uniqueUserId) throws UserManagerException {
        return null;
    }

    @Override
    public String getDomainNameFromGroupUniqueId(String uniqueUserId) throws UserManagerException {
        return null;
    }

    @Override
    public Map<String, String> getConnectorGroupIds(String uniqueGroupId) throws UserManagerException {
        return null;
    }

    @Override
    public void addGroup(String uniqueGroupId, List<ConnectedGroup> connectedGroups) throws UserManagerException {

    }

    @Override
    public void addGroups(Map<String, List<ConnectedGroup>> connectedGroupsMap) throws UserManagerException {

    }

    @Override
    public void updateGroup(String uniqueGroupId, Map<String, String> connectorGroupIdMap) throws UserManagerException {

    }

    @Override
    public void deleteGroup(String uniqueGroupId) throws UserManagerException {

    }

    @Override
    public void updateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIds) throws UserManagerException {

    }

    @Override
    public void updateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIdsToUpdate,
                                   List<String> uniqueGroupIdsToRemove) throws UserManagerException {

    }

    @Override
    public void updateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIds) throws UserManagerException {

    }

    @Override
    public void updateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIdsToUpdate,
                                   List<String> uniqueUserIdsToRemove) throws UserManagerException {

    }
}

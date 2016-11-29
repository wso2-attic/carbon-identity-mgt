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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.identity.mgt.config.UniqueIdResolverConfig;
import org.wso2.carbon.identity.mgt.exception.GroupNotFoundException;
import org.wso2.carbon.identity.mgt.exception.UniqueIdResolverException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtDataHolder;
import org.wso2.carbon.identity.mgt.user.UniqueGroup;
import org.wso2.carbon.identity.mgt.user.UniqueIdResolver;
import org.wso2.carbon.identity.mgt.user.UniqueUser;
import org.wso2.carbon.identity.mgt.user.UserPartition;
import org.wso2.carbon.identity.mgt.util.NamedPreparedStatement;
import org.wso2.carbon.identity.mgt.util.UniqueIdResolverConstants;
import org.wso2.carbon.identity.mgt.util.UnitOfWork;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

/**
 * UserManager implementation.
 */
public class JDBCUniqueIdResolver implements UniqueIdResolver {

    private static final Logger log = LoggerFactory.getLogger(JDBCUniqueIdResolver.class);

    private DataSource dataSource;

    public JDBCUniqueIdResolver() {

    }

    @Override
    public void init(UniqueIdResolverConfig uniqueIdResolverConfig) throws UniqueIdResolverException {

        try {
            dataSource = IdentityMgtDataHolder.getInstance()
                    .getDataSource(uniqueIdResolverConfig.getProperties().get(UniqueIdResolverConstants.DATA_SOURCE));
        } catch (DataSourceException e) {
            throw new UniqueIdResolverException("Error occurred while initiating data source.", e);
        }
    }

    @Override
    public UniqueUser getUniqueUser(String uniqueUserId) throws UniqueIdResolverException, UserNotFoundException {
        return null;
    }

    @Override
    public UniqueUser getUniqueUserFromConnectorUserId(String connectorUserId, String connectorId) throws
            UniqueIdResolverException {

        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
            final String selectUserUuid = "SELECT USER_UUID, CONNECTOR_TYPE, CONNECTOR_ID, CONNECTOR_USER_ID FROM " +
                    "IDM_ENTITY WHERE USER_UUID = ( " +
                    "SELECT USER_UUID FROM IDM_ENTITY " +
                    "WHERE CONNECTOR_USER_ID = :connector_user_id; " +
                    "AND CONNECTOR_ID = :connector_id;)";

            String userUUID = null;
            UniqueUser uniqueUser = new UniqueUser();
            List<UserPartition> userPartitions = new ArrayList<>();
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    selectUserUuid);
            namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.CONNECTOR_USER_ID,
                    connectorUserId);
            namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.CONNECTOR_ID, connectorId);
            try (ResultSet resultSet = namedPreparedStatement.getPreparedStatement().executeQuery()) {

                while (resultSet.next()) {
                    UserPartition userPartition = new UserPartition();
                    userUUID = resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames.USER_UUID);
                    userPartition.setConnectorId(resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames
                            .CONNECTOR_ID));
                    userPartition.setConnectorUserId(resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames
                            .CONNECTOR_USER_ID));
                    userPartition.setIdentityStore(UniqueIdResolverConstants.IDENTITY_STORE_CONNECTOR.equals(resultSet
                            .getString(UniqueIdResolverConstants.DatabaseColumnNames.CONNECTOR_TYPE)));
                    userPartitions.add(userPartition);
                }
            }
            if (userUUID == null) {
                throw new UniqueIdResolverException("No user found.");
            }
            uniqueUser.setUniqueUserId(userUUID);
            uniqueUser.setUserPartitions(userPartitions);
            return uniqueUser;

        } catch (SQLException e) {
            throw new UniqueIdResolverException("Error while searching user.", e);
        }
    }

    @Override
    public List<UniqueUser> getUniqueUsers(List<String> connectorUserIds, String connectorId) throws
            UniqueIdResolverException {
        //TODO
        return null;
    }

    @Override
    public boolean isUserExists(String uniqueUserId) throws UniqueIdResolverException {

        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
            final String selectUser = "SELECT ID FROM IDM_ENTITY " +
                    "WHERE USER_UUID = :user_uuid;";

            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    selectUser);
            namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.USER_UUID, uniqueUserId);
            try (ResultSet resultSet = namedPreparedStatement.getPreparedStatement().executeQuery()) {

                if (resultSet.next()) {
                    return true;
                } else {
                    return false;
                }
            }

        } catch (SQLException e) {
            throw new UniqueIdResolverException("Error while searching user.", e);
        }
    }

    @Override
    public List<UniqueUser> listUsers(int offset, int length) throws UniqueIdResolverException {
        //TODO
        return null;
    }

    @Override
    public UniqueGroup getUniqueGroup(String uniqueGroupId) throws UniqueIdResolverException, GroupNotFoundException {
        return null;
    }

    @Override
    public UniqueGroup getUniqueGroupFromConnectorGroupId(String connectorGroupId, String connectorId) throws
            UniqueIdResolverException {
        return null;
    }

//    @Override
//    public String getConnectorUserId(String uniqueUserId, String connectorId) throws UniqueIdResolverException {
//
//        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
//            final String selectUserUuid = "SELECT CONNECTOR_USER_ID FROM IDM_ENTITY " +
//                    "WHERE USER_UUID = :user_uuid; " +
//                    "AND CONNECTOR_ID = :connector_id;";
//
//            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
//                    unitOfWork.getConnection(),
//                    selectUserUuid);
//            namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.USER_UUID, uniqueUserId);
//            namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.CONNECTOR_ID, connectorId);
//            try (ResultSet resultSet = namedPreparedStatement.getPreparedStatement().executeQuery()) {
//
//                if (resultSet.next()) {
//                    return resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames.CONNECTOR_USER_ID);
//                } else {
//                    throw new UniqueIdResolverException("User not found.");
//                }
//            }
//
//        } catch (SQLException e) {
//            throw new UniqueIdResolverException("Error while searching user.", e);
//        }
//    }

    @Override
    public void addUser(UniqueUser uniqueUser, String domainName) throws
            UniqueIdResolverException {

        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection(), false)) {
            final String addUser = "INSERT INTO IDM_ENTITY " +
                    "(USER_UUID, CONNECTOR_USER_ID, CONNECTOR_ID, DOMAIN, CONNECTOR_TYPE) " +
                    "VALUES (:user_uuid;, :connector_user_id;, :connector_id;, :domain;, :connector_type;)";
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(), addUser);
            for (UserPartition userPartition : uniqueUser.getUserPartitions()) {
                namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.USER_UUID, uniqueUser
                        .getUniqueUserId());
                namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.CONNECTOR_USER_ID,
                        userPartition.getConnectorUserId());
                namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.CONNECTOR_ID,
                        userPartition.getConnectorId());
                namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.DOMAIN, domainName);
                namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.CONNECTOR_TYPE,
                        userPartition.isIdentityStore() ? UniqueIdResolverConstants.IDENTITY_STORE_CONNECTOR :
                                UniqueIdResolverConstants.CREDENTIAL_STORE_CONNECTOR);
                namedPreparedStatement.getPreparedStatement().addBatch();
            }

            namedPreparedStatement.getPreparedStatement().executeBatch();
            unitOfWork.endTransaction();
        } catch (SQLException e) {
            throw new UniqueIdResolverException("Error while adding user.", e);
        }
    }

    @Override
    public void addUsers(List<UniqueUser> uniqueUsers, String domainName) throws UniqueIdResolverException {

    }
//
//    @Override
//    public void addUsers(Map<String, List<UserPartition>> connectedUsersMap) throws UniqueIdResolverException {
//
//    }

    @Override
    public void updateUser(String uniqueUserId, Map<String, String> connectorUserIdMap) throws
            UniqueIdResolverException {

        //TODO need to check whether the entry is already there before updating. else need to add
        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection(), false)) {
            final String updateUser = "UPDATE IDM_ENTITY " +
                    "SET CONNECTOR_USER_ID = :connector_user_id;" +
                    "WHERE USER_UUID = :user_uuid; AND CONNECTOR_ID = :connector_id;";
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(), updateUser);
            for (Map.Entry<String, String> entry : connectorUserIdMap.entrySet()) {
                namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.USER_UUID, uniqueUserId);
                namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.CONNECTOR_USER_ID,
                        entry.getValue());
                namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.CONNECTOR_ID,
                        entry.getKey());
                namedPreparedStatement.getPreparedStatement().addBatch();
            }
            namedPreparedStatement.getPreparedStatement().executeBatch();
            unitOfWork.endTransaction();
        } catch (SQLException e) {
            throw new UniqueIdResolverException("Error while adding user.", e);
        }
    }

    @Override
    public void deleteUser(String uniqueUserId) throws UniqueIdResolverException {

        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection(), false)) {
            final String deleteUser = "DELETE FROM IDM_ENTITY " +
                    "WHERE USER_UUID = :user_uuid;";
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(), deleteUser);
            namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.USER_UUID, uniqueUserId);

            namedPreparedStatement.getPreparedStatement().executeUpdate();
            unitOfWork.endTransaction();
        } catch (SQLException e) {
            throw new UniqueIdResolverException("Error while adding user.", e);
        }
    }

//    @Override
//    public Map<String, String> getConnectorUserIds(String userUniqueId) throws UniqueIdResolverException {
//
//        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
//            final String selectUserUuid = "SELECT CONNECTOR_ID, CONNECTOR_USER_ID FROM " +
//                    "IDM_ENTITY WHERE USER_UUID = :user_uuid;";
//
//            Map<String, String> connectorUserIds = new HashMap<>();
//            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
//                    unitOfWork.getConnection(),
//                    selectUserUuid);
//            namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.USER_UUID, userUniqueId);
//            try (ResultSet resultSet = namedPreparedStatement.getPreparedStatement().executeQuery()) {
//
//                while (resultSet.next()) {
//                    String connectorId = resultSet.getString(
//                            UniqueIdResolverConstants.DatabaseColumnNames.CONNECTOR_ID);
//                    String connectorUserId = resultSet.getString(
//                            UniqueIdResolverConstants.DatabaseColumnNames.CONNECTOR_USER_ID);
//                    connectorUserIds.put(connectorId, connectorUserId);
//                }
//            }
//
//            return connectorUserIds;
//
//        } catch (SQLException e) {
//            throw new UniqueIdResolverException("Error while searching user.", e);
//        }
//    }

//    @Override
//    public String getDomainNameFromUserUniqueId(String uniqueUserId) throws UniqueIdResolverException {
//
//        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
//            //TODO Do we need to limit 1 result?
//            final String selectUserUuid = "SELECT DOMAIN FROM " +
//                    "IDM_ENTITY WHERE USER_UUID = :user_uuid;";
//
//            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
//                    unitOfWork.getConnection(),
//                    selectUserUuid);
//            namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.USER_UUID, uniqueUserId);
//            try (ResultSet resultSet = namedPreparedStatement.getPreparedStatement().executeQuery()) {
//
//                if (resultSet.next()) {
//                    return resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames.DOMAIN);
//                } else {
//                    throw new UniqueIdResolverException("User not found with the given user id.");
//                }
//            }
//
//        } catch (SQLException e) {
//            throw new UniqueIdResolverException("Error while searching user.", e);
//        }
//    }

    @Override
    public void addGroup(UniqueGroup uniqueGroup, String domainName) throws UniqueIdResolverException {

    }

    @Override
    public void addGroups(List<UniqueGroup> uniqueGroups, String domainName) throws UniqueIdResolverException {

    }

    @Override
    public void updateGroup(String uniqueGroupId, Map<String, String> connectorGroupIdMap) throws
            UniqueIdResolverException {

    }

    @Override
    public void deleteGroup(String uniqueGroupId) throws UniqueIdResolverException {

    }

    @Override
    public boolean isGroupExists(String uniqueGroupId) throws UniqueIdResolverException {
        return false;
    }

    @Override
    public List<UniqueGroup> listGroups(int offset, int length) throws UniqueIdResolverException {
        return null;
    }

    @Override
    public List<UniqueGroup> getUniqueGroups(List<String> connectorGroupIds, String connectorId) throws
            UniqueIdResolverException {
        return null;
    }

    @Override
    public List<UniqueGroup> getGroupsOfUser(String uniqueUserId) throws UniqueIdResolverException {
        return null;
    }

    @Override
    public List<UniqueUser> getUsersOfGroup(String uniqueGroupId) throws UniqueIdResolverException {
        return null;
    }

    @Override
    public boolean isUserInGroup(String uniqueUserId, String uniqueGroupId) throws UniqueIdResolverException {
        return false;
    }

    @Override
    public void updateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIds) throws UniqueIdResolverException {

    }

    @Override
    public void updateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIdsToUpdate, List<String>
            uniqueGroupIdsToRemove) throws UniqueIdResolverException {

    }

    @Override
    public void updateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIds) throws UniqueIdResolverException {

    }

    @Override
    public void updateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIdsToUpdate, List<String>
            uniqueUserIdsToRemove) throws UniqueIdResolverException {

    }
}

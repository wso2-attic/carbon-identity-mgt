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
import org.wso2.carbon.identity.mgt.user.GroupPartition;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

/**
 * JDBC based UniqueIdResolver implementation.
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
        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
            final String selectUniqueUser = "SELECT CONNECTOR_TYPE, CONNECTOR_ID, CONNECTOR_ENTITY_ID FROM " +
                    "IDM_ENTITY WHERE ENTITY_UUID = :entity_uuid;";

            UniqueUser uniqueUser = new UniqueUser();
            List<UserPartition> userPartitions = new ArrayList<>();
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    selectUniqueUser);
            namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.ENTITY_UUID,
                    uniqueUserId);
            try (ResultSet resultSet = namedPreparedStatement.getPreparedStatement().executeQuery()) {

                while (resultSet.next()) {
                    UserPartition userPartition = new UserPartition();
                    userPartition.setConnectorId(resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames
                            .CONNECTOR_ID));
                    userPartition.setConnectorUserId(resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames
                            .CONNECTOR_ENTITY_ID));
                    userPartition.setIdentityStore(UniqueIdResolverConstants.IDENTITY_STORE_CONNECTOR.equals(resultSet
                            .getString(UniqueIdResolverConstants.DatabaseColumnNames.CONNECTOR_TYPE)));
                    userPartitions.add(userPartition);
                }
            }

            uniqueUser.setUniqueUserId(uniqueUserId);
            uniqueUser.setUserPartitions(userPartitions);
            return uniqueUser;

        } catch (SQLException e) {
            throw new UniqueIdResolverException("Error while searching user.", e);
        }
    }

    @Override
    public UniqueUser getUniqueUserFromConnectorUserId(String connectorUserId, String connectorId) throws
            UniqueIdResolverException {

        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
            final String selectUniqueUser = "SELECT ENTITY_UUID, CONNECTOR_TYPE, CONNECTOR_ID, CONNECTOR_ENTITY_ID " +
                    "FROM IDM_ENTITY WHERE ENTITY_UUID = ( " +
                    "SELECT ENTITY_UUID FROM IDM_ENTITY " +
                    "WHERE CONNECTOR_ENTITY_ID = :connector_entity_id; " +
                    "AND CONNECTOR_ID = :connector_id;)";

            String userUUID = null;
            UniqueUser uniqueUser = new UniqueUser();
            List<UserPartition> userPartitions = new ArrayList<>();
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    selectUniqueUser);
            namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.CONNECTOR_ENTITY_ID,
                    connectorUserId);
            namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.CONNECTOR_ID, connectorId);
            try (ResultSet resultSet = namedPreparedStatement.getPreparedStatement().executeQuery()) {

                while (resultSet.next()) {
                    UserPartition userPartition = new UserPartition();
                    userUUID = resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames.ENTITY_UUID);
                    userPartition.setConnectorId(resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames
                            .CONNECTOR_ID));
                    userPartition.setConnectorUserId(resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames
                            .CONNECTOR_ENTITY_ID));
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

        UniqueIdResolverException uniqueIdResolverException = new UniqueIdResolverException();
        List<UniqueUser> uniqueUsers = new ArrayList<>();
        connectorUserIds.stream().forEach(entry -> {
            try {
                UniqueUser uniqueUser = getUniqueUserFromConnectorUserId(entry, connectorId);
                uniqueUsers.add(uniqueUser);
            } catch (UniqueIdResolverException e) {
                uniqueIdResolverException.addSuppressed(e);
            }
        });

        if (uniqueIdResolverException.getSuppressed().length > 0) {
            throw uniqueIdResolverException;
        }

        return uniqueUsers;
    }

    @Override
    public boolean isUserExists(String uniqueUserId) throws UniqueIdResolverException {

        try {
            return isEntityExists(uniqueUserId);
        } catch (SQLException e) {
            throw new UniqueIdResolverException("Error while searching user.", e);
        }
    }

    @Override
    public List<UniqueUser> listUsers(int offset, int length) throws UniqueIdResolverException {

        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
            final String selectUniqueUser = "SELECT ENTITY_UUID, CONNECTOR_TYPE, CONNECTOR_ID, CONNECTOR_ENTITY_ID " +
                    "FROM IDM_ENTITY LIMIT :limit; OFFSET :offset;";

            Map<String, UniqueUser> userMap = new HashMap<>();
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    selectUniqueUser);
            namedPreparedStatement.setInt(UniqueIdResolverConstants.SQLPlaceholders.LIMIT, length);
            namedPreparedStatement.setInt(UniqueIdResolverConstants.SQLPlaceholders.OFFSET, offset);
            try (ResultSet resultSet = namedPreparedStatement.getPreparedStatement().executeQuery()) {

                while (resultSet.next()) {
                    UserPartition userPartition = new UserPartition();
                    String userUUID = resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames.ENTITY_UUID);
                    userPartition.setConnectorId(resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames
                            .CONNECTOR_ID));
                    userPartition.setConnectorUserId(resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames
                            .CONNECTOR_ENTITY_ID));
                    userPartition.setIdentityStore(UniqueIdResolverConstants.IDENTITY_STORE_CONNECTOR.equals(resultSet
                            .getString(UniqueIdResolverConstants.DatabaseColumnNames.CONNECTOR_TYPE)));

                    UniqueUser user;
                    if ((user = userMap.get(userUUID)) != null) {
                        user.addUserPartition(userPartition);
                    } else {
                        user = new UniqueUser();
                        user.setUniqueUserId(userUUID);
                        user.addUserPartition(userPartition);
                        userMap.put(userUUID, user);
                    }
                }
            }

            return new ArrayList<>(userMap.values());

        } catch (SQLException e) {
            throw new UniqueIdResolverException("Error while listing users.", e);
        }
    }

    @Override
    public UniqueGroup getUniqueGroup(String uniqueGroupId) throws UniqueIdResolverException, GroupNotFoundException {

        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
            final String selectUniqueUser = "SELECT CONNECTOR_ID, CONNECTOR_ENTITY_ID FROM " +
                    "IDM_ENTITY WHERE ENTITY_UUID = :entity_uuid;";

            UniqueGroup uniqueGroup = new UniqueGroup();
            List<GroupPartition> groupPartitions = new ArrayList<>();
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    selectUniqueUser);
            namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.ENTITY_UUID, uniqueGroupId);
            try (ResultSet resultSet = namedPreparedStatement.getPreparedStatement().executeQuery()) {
                while (resultSet.next()) {
                    GroupPartition userPartition = new GroupPartition();
                    userPartition.setConnectorId(resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames
                            .CONNECTOR_ID));
                    userPartition.setConnectorGroupId(resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames
                            .CONNECTOR_ENTITY_ID));
                    groupPartitions.add(userPartition);
                }
            }

            uniqueGroup.setUniqueGroupId(uniqueGroupId);
            uniqueGroup.setGroupPartitions(groupPartitions);
            return uniqueGroup;

        } catch (SQLException e) {
            throw new UniqueIdResolverException("Error while searching group.", e);
        }
    }

    @Override
    public UniqueGroup getUniqueGroupFromConnectorGroupId(String connectorGroupId, String connectorId) throws
            UniqueIdResolverException {

        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
            final String selectUniqueGroup = "SELECT ENTITY_UUID, CONNECTOR_ID, CONNECTOR_ENTITY_ID " +
                    "FROM IDM_ENTITY WHERE ENTITY_UUID = ( " +
                    "SELECT ENTITY_UUID FROM IDM_ENTITY " +
                    "WHERE CONNECTOR_ENTITY_ID = :connector_entity_id; " +
                    "AND CONNECTOR_ID = :connector_id;)";

            String groupUUID = null;
            UniqueGroup uniqueGroup = new UniqueGroup();
            List<GroupPartition> groupPartitions = new ArrayList<>();
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    selectUniqueGroup);
            namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.CONNECTOR_ENTITY_ID,
                    connectorGroupId);
            namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.CONNECTOR_ID, connectorId);
            try (ResultSet resultSet = namedPreparedStatement.getPreparedStatement().executeQuery()) {

                while (resultSet.next()) {
                    GroupPartition groupPartition = new GroupPartition();
                    groupUUID = resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames.ENTITY_UUID);
                    groupPartition.setConnectorId(resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames
                            .CONNECTOR_ID));
                    groupPartition.setConnectorGroupId(resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames
                            .CONNECTOR_ENTITY_ID));
                    groupPartitions.add(groupPartition);
                }
            }
            if (groupUUID == null) {
                throw new UniqueIdResolverException("No group found.");
            }
            uniqueGroup.setUniqueGroupId(groupUUID);
            uniqueGroup.setGroupPartitions(groupPartitions);
            return uniqueGroup;

        } catch (SQLException e) {
            throw new UniqueIdResolverException("Error while searching group.", e);
        }
    }

    @Override
    public void addUser(UniqueUser uniqueUser, String domainName) throws
            UniqueIdResolverException {

        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection(), false)) {
            final String addUser = "INSERT INTO IDM_ENTITY " +
                    "(ENTITY_UUID, CONNECTOR_ENTITY_ID, CONNECTOR_ID, DOMAIN, CONNECTOR_TYPE) " +
                    "VALUES (:entity_uuid;, :connector_entity_id;, :connector_id;, :domain;, :connector_type;)";
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(), addUser);
            for (UserPartition userPartition : uniqueUser.getUserPartitions()) {
                namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.ENTITY_UUID, uniqueUser
                        .getUniqueUserId());
                namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.CONNECTOR_ENTITY_ID,
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

        UniqueIdResolverException uniqueIdResolverException = new UniqueIdResolverException();
        uniqueUsers.stream().forEach(uniqueUser -> {
            try {
                addUser(uniqueUser, domainName);
            } catch (UniqueIdResolverException e) {
                uniqueIdResolverException.addSuppressed(e);
            }
        });

        if (uniqueIdResolverException.getSuppressed().length > 0) {
            throw uniqueIdResolverException;
        }
    }

    @Override
    public void updateUser(String uniqueUserId, Map<String, String> connectorUserIdMap) throws
            UniqueIdResolverException {

        // Put operation
        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection(), false)) {
            deleteEntity(uniqueUserId, unitOfWork);
            final String addUser = "INSERT INTO IDM_ENTITY " +
                    "(ENTITY_UUID, CONNECTOR_ENTITY_ID, CONNECTOR_ID, DOMAIN, CONNECTOR_TYPE) " +
                    "VALUES (:entity_uuid;, :connector_entity_id;, :connector_id;, :domain;, :connector_type;)";
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(), addUser);
            for (Map.Entry<String, String> entry : connectorUserIdMap.entrySet()) {
                namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.ENTITY_UUID, uniqueUserId);
                namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.CONNECTOR_ENTITY_ID,
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

        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
            deleteEntity(uniqueUserId, unitOfWork);
            deleteAllUserGroupMapping(uniqueUserId, unitOfWork);
            unitOfWork.endTransaction();
        } catch (SQLException e) {
            throw new UniqueIdResolverException("Error while adding user.", e);
        }
    }

    @Override
    public void addGroup(UniqueGroup uniqueGroup, String domainName) throws UniqueIdResolverException {

        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection(), false)) {
            final String addGroup = "INSERT INTO IDM_ENTITY " +
                    "(ENTITY_UUID, CONNECTOR_ENTITY_ID, CONNECTOR_ID, DOMAIN) " +
                    "VALUES (:entity_uuid;, :connector_entity_id;, :connector_id;, :domain;)";
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(), addGroup);
            for (GroupPartition groupPartition : uniqueGroup.getGroupPartitions()) {
                namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.ENTITY_UUID, uniqueGroup
                        .getUniqueGroupId());
                namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.CONNECTOR_ENTITY_ID,
                        groupPartition.getConnectorGroupId());
                namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.CONNECTOR_ID,
                        groupPartition.getConnectorId());
                namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.DOMAIN, domainName);

                namedPreparedStatement.getPreparedStatement().addBatch();
            }

            namedPreparedStatement.getPreparedStatement().executeBatch();
            unitOfWork.endTransaction();
        } catch (SQLException e) {
            throw new UniqueIdResolverException("Error while adding group.", e);
        }
    }

    @Override
    public void addGroups(List<UniqueGroup> uniqueGroups, String domainName) throws UniqueIdResolverException {

        UniqueIdResolverException uniqueIdResolverException = new UniqueIdResolverException();
        uniqueGroups.stream().forEach(uniqueGroup -> {
            try {
                addGroup(uniqueGroup, domainName);
            } catch (UniqueIdResolverException e) {
                uniqueIdResolverException.addSuppressed(e);
            }
        });

        if (uniqueIdResolverException.getSuppressed().length > 0) {
            throw uniqueIdResolverException;
        }
    }

    @Override
    public void updateGroup(String uniqueGroupId, Map<String, String> connectorGroupIdMap) throws
            UniqueIdResolverException {

        // Put operation
        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection(), false)) {
            deleteEntity(uniqueGroupId, unitOfWork);
            final String addGroup = "INSERT INTO IDM_ENTITY " +
                    "(ENTITY_UUID, CONNECTOR_ENTITY_ID, CONNECTOR_ID, DOMAIN, CONNECTOR_TYPE) " +
                    "VALUES (:entity_uuid;, :connector_entity_id;, :connector_id;, :domain;, :connector_type;)";
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(), addGroup);
            for (Map.Entry<String, String> entry : connectorGroupIdMap.entrySet()) {
                namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.ENTITY_UUID, uniqueGroupId);
                namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.CONNECTOR_ENTITY_ID,
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
    public void deleteGroup(String uniqueGroupId) throws UniqueIdResolverException {

        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
            deleteEntity(uniqueGroupId, unitOfWork);
            deleteAllUserGroupMapping(uniqueGroupId, unitOfWork);
            unitOfWork.endTransaction();
        } catch (SQLException e) {
            throw new UniqueIdResolverException("Error while adding group.", e);
        }
    }

    @Override
    public boolean isGroupExists(String uniqueGroupId) throws UniqueIdResolverException {

        try {
            return isEntityExists(uniqueGroupId);
        } catch (SQLException e) {
            throw new UniqueIdResolverException("Error while searching user.", e);
        }
    }

    @Override
    public List<UniqueGroup> listGroups(int offset, int length) throws UniqueIdResolverException {

        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
            final String selectUniqueUser = "SELECT ENTITY_UUID, CONNECTOR_ID, CONNECTOR_ENTITY_ID " +
                    "FROM IDM_ENTITY LIMIT :limit; OFFSET :offset;";

            Map<String, UniqueGroup> groupMap = new HashMap<>();
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    selectUniqueUser);
            namedPreparedStatement.setInt(UniqueIdResolverConstants.SQLPlaceholders.LIMIT, length);
            namedPreparedStatement.setInt(UniqueIdResolverConstants.SQLPlaceholders.OFFSET, offset);
            try (ResultSet resultSet = namedPreparedStatement.getPreparedStatement().executeQuery()) {

                while (resultSet.next()) {
                    GroupPartition groupPartition = new GroupPartition();
                    String groupUUID = resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames.ENTITY_UUID);
                    groupPartition.setConnectorId(resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames
                            .CONNECTOR_ID));
                    groupPartition.setConnectorGroupId(resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames
                            .CONNECTOR_ENTITY_ID));

                    UniqueGroup group;
                    if ((group = groupMap.get(groupUUID)) != null) {
                        group.addGroupPartition(groupPartition);
                    } else {
                        group = new UniqueGroup();
                        group.setUniqueGroupId(groupUUID);
                        group.addGroupPartition(groupPartition);
                        groupMap.put(groupUUID, group);
                    }
                }
            }

            return new ArrayList<>(groupMap.values());

        } catch (SQLException e) {
            throw new UniqueIdResolverException("Error while listing groups.", e);
        }
    }

    @Override
    public List<UniqueGroup> getUniqueGroups(List<String> connectorGroupIds, String connectorId) throws
            UniqueIdResolverException {

        UniqueIdResolverException uniqueIdResolverException = new UniqueIdResolverException();
        List<UniqueGroup> uniqueGroups = new ArrayList<>();
        connectorGroupIds.stream().forEach(entry -> {
            try {
                UniqueGroup uniqueGroup = getUniqueGroupFromConnectorGroupId(entry, connectorId);
                uniqueGroups.add(uniqueGroup);
            } catch (UniqueIdResolverException e) {
                uniqueIdResolverException.addSuppressed(e);
            }
        });

        if (uniqueIdResolverException.getSuppressed().length > 0) {
            throw uniqueIdResolverException;
        }

        return uniqueGroups;
    }

    @Override
    public List<UniqueGroup> getGroupsOfUser(String uniqueUserId) throws UniqueIdResolverException {

        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
            final String selectGroupsOfUser = "SELECT ENTITY_UUID, CONNECTOR_ID, CONNECTOR_ENTITY_ID " +
                    "FROM IDM_ENTITY WHERE ENTITY_UUID IN ( " +
                    "SELECT GROUP_UUID " +
                    "FROM IDM_USER_GROUP_MAPPING " +
                    "WHERE USER_UUID = :user_uuid; )";

            Map<String, UniqueGroup> groupMap = new HashMap<>();
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    selectGroupsOfUser);
            namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.USER_UUID, uniqueUserId);
            try (ResultSet resultSet = namedPreparedStatement.getPreparedStatement().executeQuery()) {

                while (resultSet.next()) {
                    GroupPartition groupPartition = new GroupPartition();
                    String groupUUID = resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames.ENTITY_UUID);
                    groupPartition.setConnectorId(resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames
                            .CONNECTOR_ID));
                    groupPartition.setConnectorGroupId(resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames
                            .CONNECTOR_ENTITY_ID));

                    UniqueGroup group;
                    if ((group = groupMap.get(groupUUID)) != null) {
                        group.addGroupPartition(groupPartition);
                    } else {
                        group = new UniqueGroup();
                        group.setUniqueGroupId(groupUUID);
                        group.addGroupPartition(groupPartition);
                        groupMap.put(groupUUID, group);
                    }
                }
            }

            return new ArrayList<>(groupMap.values());

        } catch (SQLException e) {
            throw new UniqueIdResolverException("Error while listing groups of user.", e);
        }
    }

    @Override
    public List<UniqueUser> getUsersOfGroup(String uniqueGroupId) throws UniqueIdResolverException {

        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
            final String selectUsersOfGroup = "SELECT ENTITY_UUID, CONNECTOR_ID, CONNECTOR_ENTITY_ID, CONNECTOR_TYPE " +
                    "FROM IDM_ENTITY WHERE ENTITY_UUID IN ( " +
                    "SELECT USER_UUID " +
                    "FROM IDM_USER_GROUP_MAPPING " +
                    "WHERE GROUP_UUID = :group_uuid; )";

            Map<String, UniqueUser> userMap = new HashMap<>();
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    selectUsersOfGroup);
            namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.GROUP_UUID, uniqueGroupId);
            try (ResultSet resultSet = namedPreparedStatement.getPreparedStatement().executeQuery()) {

                while (resultSet.next()) {
                    UserPartition userPartition = new UserPartition();
                    String userUUID = resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames.ENTITY_UUID);
                    userPartition.setConnectorId(resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames
                            .CONNECTOR_ID));
                    userPartition.setConnectorUserId(resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames
                            .CONNECTOR_ENTITY_ID));
                    userPartition.setIdentityStore(UniqueIdResolverConstants.IDENTITY_STORE_CONNECTOR.equals(resultSet
                            .getString(UniqueIdResolverConstants.DatabaseColumnNames.CONNECTOR_TYPE)));

                    UniqueUser user;
                    if ((user = userMap.get(userUUID)) != null) {
                        user.addUserPartition(userPartition);
                    } else {
                        user = new UniqueUser();
                        user.setUniqueUserId(userUUID);
                        user.addUserPartition(userPartition);
                        userMap.put(userUUID, user);
                    }
                }
            }

            return new ArrayList<>(userMap.values());

        } catch (SQLException e) {
            throw new UniqueIdResolverException("Error while listing users of group.", e);
        }
    }

    @Override
    public boolean isUserInGroup(String uniqueUserId, String uniqueGroupId) throws UniqueIdResolverException {

        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
            final String selectUsersOfGroup = "SELECT ID " +
                    "FROM IDM_USER_GROUP_MAPPING " +
                    "WHERE GROUP_UUID = :group_uuid; AND USER_UUID = :user_uuid; )";

            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    selectUsersOfGroup);
            namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.GROUP_UUID, uniqueGroupId);
            namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.USER_UUID, uniqueUserId);
            try (ResultSet resultSet = namedPreparedStatement.getPreparedStatement().executeQuery()) {

                if (resultSet.next()) {
                    return true;
                }
            }

        } catch (SQLException e) {
            throw new UniqueIdResolverException("Error while listing users of group.", e);
        }
        return false;
    }

    @Override
    public void updateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIds) throws UniqueIdResolverException {

        // Put operation
        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
            deleteAllUserGroupMapping(uniqueUserId, unitOfWork);
            final String insertGroupsOfUser = "INSERT INTO IDM_USER_GROUP_MAPPING (USER_UUID, GROUP_UUID) " +
                    "VALUES ( :user_uuid;, :group_uuid; ) ";
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    insertGroupsOfUser);
            for (String uniqueGroupId : uniqueGroupIds) {
                namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.USER_UUID, uniqueUserId);
                namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.GROUP_UUID, uniqueGroupId);
                namedPreparedStatement.getPreparedStatement().addBatch();
            }
            namedPreparedStatement.getPreparedStatement().executeBatch();

            unitOfWork.endTransaction();

        } catch (SQLException e) {
            throw new UniqueIdResolverException("Error while updating groups of user", e);
        }
    }

    @Override
    public void updateGroupsOfUser(String uniqueUserId, List<String> uniqueGroupIdsToUpdate, List<String>
            uniqueGroupIdsToRemove) throws UniqueIdResolverException {

        // Patch operation
        
    }

    @Override
    public void updateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIds) throws UniqueIdResolverException {

        // Put operation
        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
            deleteAllUserGroupMapping(uniqueGroupId, unitOfWork);
            final String insertUsersOfGroup = "INSERT INTO IDM_USER_GROUP_MAPPING (USER_UUID, GROUP_UUID) " +
                    "VALUES ( :user_uuid;, :group_uuid; ) ";
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    insertUsersOfGroup);
            for (String uniqueUserId : uniqueUserIds) {
                namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.USER_UUID, uniqueUserId);
                namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.GROUP_UUID, uniqueGroupId);
                namedPreparedStatement.getPreparedStatement().addBatch();
            }
            namedPreparedStatement.getPreparedStatement().executeBatch();

            unitOfWork.endTransaction();

        } catch (SQLException e) {
            throw new UniqueIdResolverException("Error while updating groups of user", e);
        }
    }

    @Override
    public void updateUsersOfGroup(String uniqueGroupId, List<String> uniqueUserIdsToUpdate, List<String>
            uniqueUserIdsToRemove) throws UniqueIdResolverException {

    }

    private boolean isEntityExists(String uniqueEntityId) throws UniqueIdResolverException, SQLException {

        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
            final String selectUser = "SELECT ID FROM IDM_ENTITY " +
                    "WHERE ENTITY_UUID = :entity_uuid;";

            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    selectUser);
            namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.ENTITY_UUID, uniqueEntityId);
            try (ResultSet resultSet = namedPreparedStatement.getPreparedStatement().executeQuery()) {

                if (resultSet.next()) {
                    return true;
                } else {
                    return false;
                }
            }

        }
    }

    private void deleteEntity(String uniqueEntityId, UnitOfWork unitOfWork) throws SQLException {

        final String deleteGroup = "DELETE FROM IDM_ENTITY " +
                "WHERE ENTITY_UUID = :entity_uuid;";
        NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                unitOfWork.getConnection(), deleteGroup);
        namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.ENTITY_UUID, uniqueEntityId);

        namedPreparedStatement.getPreparedStatement().executeUpdate();

    }

    private void deleteAllUserGroupMapping(String uniqueEntityId, UnitOfWork unitOfWork) throws SQLException {

        final String deleteUserGroupMapping = "DELETE FROM IDM_USER_GROUP_MAPPING " +
                "WHERE USER_UUID = :user_uuid; OR GROUP_UUID = :group_uuid; ";
        NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                unitOfWork.getConnection(), deleteUserGroupMapping);
        namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.USER_UUID, uniqueEntityId);
        namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.GROUP_UUID, uniqueEntityId);

        namedPreparedStatement.getPreparedStatement().executeUpdate();

    }
}

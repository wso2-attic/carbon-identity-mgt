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
import org.wso2.carbon.identity.mgt.user.DomainGroup;
import org.wso2.carbon.identity.mgt.user.DomainUser;
import org.wso2.carbon.identity.mgt.user.GroupPartition;
import org.wso2.carbon.identity.mgt.user.UniqueIdResolver;
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
import java.util.stream.Collectors;
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

    //TODO
    @Override
    public DomainUser getUser(String domainUserId, int domainId) throws UniqueIdResolverException,
            UserNotFoundException {
        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
            final String selectUniqueUser = "SELECT CONNECTOR_TYPE, CONNECTOR_ID, CONNECTOR_USER_ID FROM " +
                    "IDM_USER WHERE USER_UUID = :user_uuid;";

            DomainUser domainUser = new DomainUser();
            List<UserPartition> userPartitions = new ArrayList<>();
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    selectUniqueUser);
            namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.USER_UUID,
                    domainUserId);
            try (ResultSet resultSet = namedPreparedStatement.getPreparedStatement().executeQuery()) {

                while (resultSet.next()) {
                    UserPartition userPartition = new UserPartition();
                    userPartition.setConnectorId(resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames
                            .CONNECTOR_ID));
                    userPartition.setConnectorUserId(resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames
                            .CONNECTOR_USER_ID));
                    userPartition.setIdentityStore(UniqueIdResolverConstants.IDENTITY_STORE_CONNECTOR.equals(resultSet
                            .getString(UniqueIdResolverConstants.DatabaseColumnNames.CONNECTOR_TYPE)));
                    userPartitions.add(userPartition);
                }
            }

            domainUser.setDomainUserId(domainUserId);
            domainUser.setUserPartitions(userPartitions);
            return domainUser;

        } catch (SQLException e) {
            throw new UniqueIdResolverException("Error while searching user.", e);
        }
    }

    @Override
    public DomainUser getUserFromConnectorUserId(String connectorUserId, String connectorId, int domainId) throws
            UniqueIdResolverException {

        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
            final String selectUniqueUser = "SELECT USER_UUID, CONNECTOR_TYPE, CONNECTOR_ID, CONNECTOR_USER_ID " +
                    "FROM IDM_USER WHERE USER_UUID = ( " +
                    "SELECT USER_UUID FROM IDM_USER " +
                    "WHERE CONNECTOR_USER_ID = :connector_user_id; " +
                    "AND CONNECTOR_ID = :connector_id;)";

            String userUUID = null;
            DomainUser domainUser = new DomainUser();
            List<UserPartition> userPartitions = new ArrayList<>();
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    selectUniqueUser);
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
            domainUser.setDomainUserId(userUUID);
            domainUser.setUserPartitions(userPartitions);
            return domainUser;

        } catch (SQLException e) {
            throw new UniqueIdResolverException("Error while searching user.", e);
        }
    }

    @Override
    public List<DomainUser> getUsers(List<String> connectorUserIds, String connectorId, int domainId) throws
            UniqueIdResolverException {

        UniqueIdResolverException uniqueIdResolverException = new UniqueIdResolverException();
        List<DomainUser> domainUsers = new ArrayList<>();
        connectorUserIds.stream().forEach(entry -> {
            try {
                DomainUser domainUser = getUserFromConnectorUserId(entry, connectorId, domainId);
                domainUsers.add(domainUser);
            } catch (UniqueIdResolverException e) {
                uniqueIdResolverException.addSuppressed(e);
            }
        });

        if (uniqueIdResolverException.getSuppressed().length > 0) {
            throw uniqueIdResolverException;
        }

        return domainUsers;
    }

    @Override
    public boolean isUserExists(String domainUserId, int domainId) throws UniqueIdResolverException {

        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
            final String selectUser = "SELECT ID FROM IDM_USER " +
                    "WHERE USER_UUID = :user_uuid;";

            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    selectUser);
            namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.USER_UUID, domainUserId);
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
    public List<DomainUser> listDomainUsers(int offset, int length, int domainId) throws UniqueIdResolverException {

        // In listDomainUsers API offset is actually the start index and start with 1. For the database start value is 0
        if (offset > 0) {
            offset--;
        }

        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
            final String selectUniqueUser = "SELECT USER_UUID, CONNECTOR_TYPE, CONNECTOR_ID, CONNECTOR_USER_ID " +
                    "FROM IDM_USER LIMIT :limit; OFFSET :offset;";

            Map<String, DomainUser> userMap = new HashMap<>();
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    selectUniqueUser);
            namedPreparedStatement.setInt(UniqueIdResolverConstants.SQLPlaceholders.LIMIT, length);
            namedPreparedStatement.setInt(UniqueIdResolverConstants.SQLPlaceholders.OFFSET, offset);
            try (ResultSet resultSet = namedPreparedStatement.getPreparedStatement().executeQuery()) {

                while (resultSet.next()) {
                    UserPartition userPartition = new UserPartition();
                    String userUUID = resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames.USER_UUID);
                    userPartition.setConnectorId(resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames
                            .CONNECTOR_ID));
                    userPartition.setConnectorUserId(resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames
                            .CONNECTOR_USER_ID));
                    userPartition.setIdentityStore(UniqueIdResolverConstants.IDENTITY_STORE_CONNECTOR.equals(resultSet
                            .getString(UniqueIdResolverConstants.DatabaseColumnNames.CONNECTOR_TYPE)));

                    DomainUser user;
                    if ((user = userMap.get(userUUID)) != null) {
                        user.addUserPartition(userPartition);
                    } else {
                        user = new DomainUser();
                        user.setDomainUserId(userUUID);
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
    public DomainGroup getGroup(String domainGroupId, int domainId) throws UniqueIdResolverException,
            GroupNotFoundException {

        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
            final String selectUniqueUser = "SELECT CONNECTOR_ID, CONNECTOR_GROUP_ID FROM " +
                    "IDM_GROUP WHERE GROUP_UUID = :group_uuid;";

            DomainGroup domainGroup = new DomainGroup();
            List<GroupPartition> groupPartitions = new ArrayList<>();
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    selectUniqueUser);
            namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.GROUP_UUID, domainGroupId);
            try (ResultSet resultSet = namedPreparedStatement.getPreparedStatement().executeQuery()) {
                while (resultSet.next()) {
                    GroupPartition userPartition = new GroupPartition();
                    userPartition.setConnectorId(resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames
                            .CONNECTOR_ID));
                    userPartition.setConnectorGroupId(resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames
                            .CONNECTOR_GROUP_ID));
                    groupPartitions.add(userPartition);
                }
            }

            domainGroup.setDomainGroupId(domainGroupId);
            domainGroup.setGroupPartitions(groupPartitions);
            return domainGroup;

        } catch (SQLException e) {
            throw new UniqueIdResolverException("Error while searching group.", e);
        }
    }

    @Override
    public DomainGroup getGroupFromConnectorGroupId(String connectorGroupId, String connectorId, int domainId)
            throws UniqueIdResolverException {

        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
            final String selectUniqueGroup = "SELECT GROUP_UUID, CONNECTOR_ID, CONNECTOR_GROUP_ID " +
                    "FROM IDM_GROUP WHERE GROUP_UUID = ( " +
                    "SELECT GROUP_UUID FROM IDM_GROUP " +
                    "WHERE CONNECTOR_GROUP_ID = :connector_group_id; " +
                    "AND CONNECTOR_ID = :connector_id;)";

            String groupUUID = null;
            DomainGroup domainGroup = new DomainGroup();
            List<GroupPartition> groupPartitions = new ArrayList<>();
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    selectUniqueGroup);
            namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.CONNECTOR_GROUP_ID,
                    connectorGroupId);
            namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.CONNECTOR_ID, connectorId);
            try (ResultSet resultSet = namedPreparedStatement.getPreparedStatement().executeQuery()) {

                while (resultSet.next()) {
                    GroupPartition groupPartition = new GroupPartition();
                    groupUUID = resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames.GROUP_UUID);
                    groupPartition.setConnectorId(resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames
                            .CONNECTOR_ID));
                    groupPartition.setConnectorGroupId(resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames
                            .CONNECTOR_GROUP_ID));
                    groupPartitions.add(groupPartition);
                }
            }
            if (groupUUID == null) {
                throw new UniqueIdResolverException("No group found.");
            }
            domainGroup.setDomainGroupId(groupUUID);
            domainGroup.setGroupPartitions(groupPartitions);
            return domainGroup;

        } catch (SQLException e) {
            throw new UniqueIdResolverException("Error while searching group.", e);
        }
    }

    @Override
    public String addUser(DomainUser domainUser, String domainName, int domainId) throws UniqueIdResolverException {

        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection(), false)) {
            final String addUser = "INSERT INTO IDM_USER " +
                    "(USER_UUID, CONNECTOR_USER_ID, CONNECTOR_ID, DOMAIN, CONNECTOR_TYPE) " +
                    "VALUES (:user_uuid;, :connector_user_id;, :connector_id;, :domain;, :connector_type;)";
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(), addUser);
            for (UserPartition userPartition : domainUser.getUserPartitions()) {
                namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.USER_UUID, domainUser
                        .getDomainUserId());
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

        return domainUser.getDomainUserId();
    }

    @Override
    public List<String> addUsers(List<DomainUser> domainUsers, String domainName, int domainId)
            throws UniqueIdResolverException {

        UniqueIdResolverException uniqueIdResolverException = new UniqueIdResolverException();
        domainUsers.stream().forEach(uniqueUser -> {
            try {
                addUser(uniqueUser, domainName, domainId);
            } catch (UniqueIdResolverException e) {
                uniqueIdResolverException.addSuppressed(e);
            }
        });

        if (uniqueIdResolverException.getSuppressed().length > 0) {
            throw uniqueIdResolverException;
        }

        return domainUsers.stream()
                .map(DomainUser::getDomainUserId)
                .collect(Collectors.toList());
    }

    @Override
    public void updateUser(String domainUserId, Map<String, String> connectorUserIdMap, int domainId) throws
            UniqueIdResolverException {

        // Put operation
        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection(), false)) {
            deleteUser(domainUserId, unitOfWork, domainId);
            final String addUser = "INSERT INTO IDM_USER " +
                    "(USER_UUID, CONNECTOR_USER_ID, CONNECTOR_ID, DOMAIN, CONNECTOR_TYPE) " +
                    "VALUES (:user_uuid;, :connector_user_id;, :connector_id;, :domain;, :connector_type;)";
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(), addUser);
            for (Map.Entry<String, String> entry : connectorUserIdMap.entrySet()) {
                namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.USER_UUID, domainUserId);
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
    public void deleteUser(String domainUserId, int domainId) throws UniqueIdResolverException {

        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
            deleteUser(domainUserId, unitOfWork, domainId);
            deleteUserGroupMappingsForUser(domainUserId, unitOfWork, domainId);
            unitOfWork.endTransaction();
        } catch (SQLException e) {
            throw new UniqueIdResolverException("Error while adding user.", e);
        }
    }

    @Override
    public String addGroup(DomainGroup domainGroup, String domainName, int domainId) throws UniqueIdResolverException {

        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection(), false)) {
            final String addGroup = "INSERT INTO IDM_GROUP " +
                    "(GROUP_UUID, CONNECTOR_GROUP_ID, CONNECTOR_ID, DOMAIN) " +
                    "VALUES (:group_uuid;, :connector_group_id;, :connector_id;, :domain;)";
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(), addGroup);
            for (GroupPartition groupPartition : domainGroup.getGroupPartitions()) {
                namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.GROUP_UUID, domainGroup
                        .getDomainGroupId());
                namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.CONNECTOR_GROUP_ID,
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

        return domainGroup.getDomainGroupId();
    }

    @Override
    public List<String> addGroups(List<DomainGroup> domainGroups, String domainName, int domainId)
            throws UniqueIdResolverException {

        UniqueIdResolverException uniqueIdResolverException = new UniqueIdResolverException();
        domainGroups.stream().forEach(uniqueGroup -> {
            try {
                addGroup(uniqueGroup, domainName, domainId);
            } catch (UniqueIdResolverException e) {
                uniqueIdResolverException.addSuppressed(e);
            }
        });

        if (uniqueIdResolverException.getSuppressed().length > 0) {
            throw uniqueIdResolverException;
        }

        return domainGroups.stream()
                .map(DomainGroup::getDomainGroupId)
                .collect(Collectors.toList());
    }

    @Override
    public void updateGroup(String domainGroupId, Map<String, String> connectorGroupIdMap, int domainId) throws
            UniqueIdResolverException {

        // Put operation
        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection(), false)) {
            deleteGroup(domainGroupId, unitOfWork, domainId);
            final String addGroup = "INSERT INTO IDM_GROUP " +
                    "(GROUP_UUID, CONNECTOR_GROUP_ID, CONNECTOR_ID, DOMAIN, CONNECTOR_TYPE) " +
                    "VALUES (:group_uuid;, :connector_group_id;, :connector_id;, :domain;, :connector_type;)";
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(), addGroup);
            for (Map.Entry<String, String> entry : connectorGroupIdMap.entrySet()) {
                namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.GROUP_UUID, domainGroupId);
                namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.CONNECTOR_GROUP_ID,
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
    public void deleteGroup(String domainGroupId, int domainId) throws UniqueIdResolverException {

        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
            deleteGroup(domainGroupId, unitOfWork, domainId);
            deleteUserGroupMappingsForGroup(domainGroupId, unitOfWork, domainId);
            unitOfWork.endTransaction();
        } catch (SQLException e) {
            throw new UniqueIdResolverException("Error while adding group.", e);
        }
    }

    @Override
    public boolean isGroupExists(String uniqueGroupId, int domainId) throws UniqueIdResolverException {

        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
            final String selectUser = "SELECT ID FROM IDM_GROUP " +
                    "WHERE GROUP_UUID = :group_uuid;";

            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    selectUser);
            namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.GROUP_UUID, uniqueGroupId);
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
    public List<DomainGroup> listGroups(int offset, int length, int domainId) throws UniqueIdResolverException {

        // In listDomainGroups API offset is actually the start index and start with 1. For the database start value
        // is 0
        if (offset > 0) {
            offset--;
        }

        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
            final String selectUniqueUser = "SELECT GROUP_UUID, CONNECTOR_ID, CONNECTOR_GROUP_ID " +
                    "FROM IDM_GROUP LIMIT :limit; OFFSET :offset;";

            Map<String, DomainGroup> groupMap = new HashMap<>();
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    selectUniqueUser);
            namedPreparedStatement.setInt(UniqueIdResolverConstants.SQLPlaceholders.LIMIT, length);
            namedPreparedStatement.setInt(UniqueIdResolverConstants.SQLPlaceholders.OFFSET, offset);
            try (ResultSet resultSet = namedPreparedStatement.getPreparedStatement().executeQuery()) {

                while (resultSet.next()) {
                    GroupPartition groupPartition = new GroupPartition();
                    String groupUUID = resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames.GROUP_UUID);
                    groupPartition.setConnectorId(resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames
                            .CONNECTOR_ID));
                    groupPartition.setConnectorGroupId(resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames
                            .CONNECTOR_GROUP_ID));

                    DomainGroup group;
                    if ((group = groupMap.get(groupUUID)) != null) {
                        group.addGroupPartition(groupPartition);
                    } else {
                        group = new DomainGroup();
                        group.setDomainGroupId(groupUUID);
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
    public List<DomainGroup> getGroups(List<String> connectorGroupIds, String connectorId, int domainId) throws
            UniqueIdResolverException {

        UniqueIdResolverException uniqueIdResolverException = new UniqueIdResolverException();
        List<DomainGroup> domainGroups = new ArrayList<>();
        connectorGroupIds.stream().forEach(entry -> {
            try {
                DomainGroup domainGroup = getGroupFromConnectorGroupId(entry, connectorId, domainId);
                domainGroups.add(domainGroup);
            } catch (UniqueIdResolverException e) {
                uniqueIdResolverException.addSuppressed(e);
            }
        });

        if (uniqueIdResolverException.getSuppressed().length > 0) {
            throw uniqueIdResolverException;
        }

        return domainGroups;
    }

    @Override
    public List<DomainGroup> getGroupsOfUser(String domainUserId, int domainId) throws UniqueIdResolverException {

        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
            final String selectGroupsOfUser = "SELECT GROUP_UUID, CONNECTOR_ID, CONNECTOR_GROUP_ID " +
                    "FROM IDM_GROUP WHERE GROUP_UUID IN ( " +
                    "SELECT GROUP_UUID " +
                    "FROM IDM_USER_GROUP_MAPPING " +
                    "WHERE USER_UUID = :user_uuid; )";

            Map<String, DomainGroup> groupMap = new HashMap<>();
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    selectGroupsOfUser);
            namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.USER_UUID, domainUserId);
            try (ResultSet resultSet = namedPreparedStatement.getPreparedStatement().executeQuery()) {

                while (resultSet.next()) {
                    GroupPartition groupPartition = new GroupPartition();
                    String groupUUID = resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames.GROUP_UUID);
                    groupPartition.setConnectorId(resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames
                            .CONNECTOR_ID));
                    groupPartition.setConnectorGroupId(resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames
                            .CONNECTOR_GROUP_ID));

                    DomainGroup group;
                    if ((group = groupMap.get(groupUUID)) != null) {
                        group.addGroupPartition(groupPartition);
                    } else {
                        group = new DomainGroup();
                        group.setDomainGroupId(groupUUID);
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
    public List<DomainUser> getUsersOfGroup(String domainGroupId, int domainId) throws UniqueIdResolverException {

        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
            final String selectUsersOfGroup = "SELECT USER_UUID, CONNECTOR_ID, CONNECTOR_USER_ID, CONNECTOR_TYPE " +
                    "FROM IDM_USER WHERE USER_UUID IN ( " +
                    "SELECT USER_UUID " +
                    "FROM IDM_USER_GROUP_MAPPING " +
                    "WHERE GROUP_UUID = :group_uuid; )";

            Map<String, DomainUser> userMap = new HashMap<>();
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    selectUsersOfGroup);
            namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.GROUP_UUID, domainGroupId);
            try (ResultSet resultSet = namedPreparedStatement.getPreparedStatement().executeQuery()) {

                while (resultSet.next()) {
                    UserPartition userPartition = new UserPartition();
                    String userUUID = resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames.USER_UUID);
                    userPartition.setConnectorId(resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames
                            .CONNECTOR_ID));
                    userPartition.setConnectorUserId(resultSet.getString(UniqueIdResolverConstants.DatabaseColumnNames
                            .CONNECTOR_USER_ID));
                    userPartition.setIdentityStore(UniqueIdResolverConstants.IDENTITY_STORE_CONNECTOR.equals(resultSet
                            .getString(UniqueIdResolverConstants.DatabaseColumnNames.CONNECTOR_TYPE)));

                    DomainUser user;
                    if ((user = userMap.get(userUUID)) != null) {
                        user.addUserPartition(userPartition);
                    } else {
                        user = new DomainUser();
                        user.setDomainUserId(userUUID);
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
    public boolean isUserInGroup(String domainUserId, String domainGroupId, int domainId)
            throws UniqueIdResolverException {

        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
            final String selectUsersOfGroup = "SELECT ID " +
                    "FROM IDM_USER_GROUP_MAPPING " +
                    "WHERE GROUP_UUID = :group_uuid; AND USER_UUID = :user_uuid;";

            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    selectUsersOfGroup);
            namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.GROUP_UUID, domainGroupId);
            namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.USER_UUID, domainUserId);
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
    public void updateGroupsOfUser(String domainUserId, List<String> domainGroupIds, int domainId)
            throws UniqueIdResolverException {

        // Put operation
        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
            deleteUserGroupMappingsForUser(domainUserId, unitOfWork, domainId);
            final String insertGroupsOfUser = "INSERT INTO IDM_USER_GROUP_MAPPING (USER_UUID, GROUP_UUID) " +
                    "VALUES ( :user_uuid;, :group_uuid; ) ";
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    insertGroupsOfUser);
            for (String uniqueGroupId : domainGroupIds) {
                namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.USER_UUID, domainUserId);
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
    public void updateGroupsOfUser(String domainUserId, List<String> domainGroupIdsToUpdate, List<String>
            domainGroupIdsToRemove, int domainId) throws UniqueIdResolverException {

        // Patch operation
        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
            // Delete the user group mappings in uniqueGroupIdsToRemove
            final String deleteUserGroupMapping = "DELETE FROM IDM_USER_GROUP_MAPPING " +
                    "WHERE USER_UUID = :user_uuid; AND GROUP_UUID = :group_uuid; ";
            NamedPreparedStatement deleteNamedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(), deleteUserGroupMapping);
            for (String uniqueGroupId : domainGroupIdsToRemove) {
                deleteNamedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.USER_UUID,
                        domainUserId);
                deleteNamedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.GROUP_UUID,
                        uniqueGroupId);
                deleteNamedPreparedStatement.getPreparedStatement().addBatch();
            }

            deleteNamedPreparedStatement.getPreparedStatement().executeBatch();

            // Add the user group mappings in uniqueGroupIdsToUpdate
            final String insertGroupsOfUser = "INSERT INTO IDM_USER_GROUP_MAPPING (USER_UUID, GROUP_UUID) " +
                    "VALUES ( :user_uuid;, :group_uuid; ) ";
            NamedPreparedStatement addNamedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    insertGroupsOfUser);
            for (String uniqueGroupId : domainGroupIdsToUpdate) {
                addNamedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.USER_UUID, domainUserId);
                addNamedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.GROUP_UUID,
                        uniqueGroupId);
                addNamedPreparedStatement.getPreparedStatement().addBatch();
            }
            addNamedPreparedStatement.getPreparedStatement().executeBatch();

            unitOfWork.endTransaction();

        } catch (SQLException e) {
            throw new UniqueIdResolverException("Error while updating groups of user", e);
        }
    }

    @Override
    public void updateUsersOfGroup(String domainGroupId, List<String> domainUserIds, int domainId)
            throws UniqueIdResolverException {

        // Put operation
        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
            deleteUserGroupMappingsForGroup(domainGroupId, unitOfWork, domainId);
            final String insertUsersOfGroup = "INSERT INTO IDM_USER_GROUP_MAPPING (USER_UUID, GROUP_UUID) " +
                    "VALUES ( :user_uuid;, :group_uuid; ) ";
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    insertUsersOfGroup);
            for (String uniqueUserId : domainUserIds) {
                namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.USER_UUID, uniqueUserId);
                namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.GROUP_UUID, domainGroupId);
                namedPreparedStatement.getPreparedStatement().addBatch();
            }
            namedPreparedStatement.getPreparedStatement().executeBatch();

            unitOfWork.endTransaction();

        } catch (SQLException e) {
            throw new UniqueIdResolverException("Error while updating groups of user", e);
        }
    }

    @Override
    public void updateUsersOfGroup(String doaminGroupId, List<String> domainUserIdsToUpdate, List<String>
            domainUserIdsToRemove, int domainId) throws UniqueIdResolverException {
        // Patch operation
        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
            // Delete the user group mappings in uniqueUserIdsToRemove
            final String deleteUserGroupMapping = "DELETE FROM IDM_USER_GROUP_MAPPING " +
                    "WHERE USER_UUID = :user_uuid; AND GROUP_UUID = :group_uuid; ";
            NamedPreparedStatement deleteNamedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(), deleteUserGroupMapping);
            for (String uniqueUserId : domainUserIdsToRemove) {
                deleteNamedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.USER_UUID,
                        uniqueUserId);
                deleteNamedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.GROUP_UUID,
                        doaminGroupId);
                deleteNamedPreparedStatement.getPreparedStatement().addBatch();
            }

            deleteNamedPreparedStatement.getPreparedStatement().executeBatch();

            // Add the user group mappings in uniqueUserIdsToUpdate
            final String insertUsersOfGroup = "INSERT INTO IDM_USER_GROUP_MAPPING (USER_UUID, GROUP_UUID) " +
                    "VALUES ( :user_uuid;, :group_uuid; ) ";
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    insertUsersOfGroup);
            for (String uniqueUserId : domainUserIdsToUpdate) {
                namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.USER_UUID, uniqueUserId);
                namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.GROUP_UUID, doaminGroupId);
                namedPreparedStatement.getPreparedStatement().addBatch();
            }
            namedPreparedStatement.getPreparedStatement().executeBatch();

            unitOfWork.endTransaction();

        } catch (SQLException e) {
            throw new UniqueIdResolverException("Error while updating groups of user", e);
        }
    }

    private void deleteUser(String uniqueUserId, UnitOfWork unitOfWork, int domainId) throws SQLException {

        final String deleteUser = "DELETE FROM IDM_USER " +
                "WHERE USER_UUID = :user_uuid;";
        NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                unitOfWork.getConnection(), deleteUser);
        namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.USER_UUID, uniqueUserId);

        namedPreparedStatement.getPreparedStatement().executeUpdate();

    }

    private void deleteGroup(String uniqueGroupId, UnitOfWork unitOfWork, int domainId) throws SQLException {

        final String deleteGroup = "DELETE FROM IDM_GROUP " +
                "WHERE GROUP_UUID = :group_uuid;";
        NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                unitOfWork.getConnection(), deleteGroup);
        namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.GROUP_UUID, uniqueGroupId);

        namedPreparedStatement.getPreparedStatement().executeUpdate();

    }

    private void deleteUserGroupMappingsForUser(String uniqueUserId, UnitOfWork unitOfWork, int domainId)
            throws SQLException {

        final String deleteUserGroupMapping = "DELETE FROM IDM_USER_GROUP_MAPPING " +
                "WHERE GROUP_UUID = :group_uuid; ";
        NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                unitOfWork.getConnection(), deleteUserGroupMapping);
        namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.GROUP_UUID, uniqueUserId);

        namedPreparedStatement.getPreparedStatement().executeUpdate();

    }

    private void deleteUserGroupMappingsForGroup(String uniqueGroupId, UnitOfWork unitOfWork, int domainId)
            throws SQLException {

        final String deleteUserGroupMapping = "DELETE FROM IDM_USER_GROUP_MAPPING " +
                "WHERE USER_UUID = :user_uuid;";
        NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                unitOfWork.getConnection(), deleteUserGroupMapping);
        namedPreparedStatement.setString(UniqueIdResolverConstants.SQLPlaceholders.USER_UUID, uniqueGroupId);

        namedPreparedStatement.getPreparedStatement().executeUpdate();

    }
}

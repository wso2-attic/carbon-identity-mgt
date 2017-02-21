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

package org.wso2.carbon.identity.mgt.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.identity.mgt.exception.GroupNotFoundException;
import org.wso2.carbon.identity.mgt.exception.UniqueIdResolverException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.mgt.impl.internal.IdentityMgtDataHolder;
import org.wso2.carbon.identity.mgt.impl.util.NamedPreparedStatement;
import org.wso2.carbon.identity.mgt.impl.util.UniqueIdResolverConstants;
import org.wso2.carbon.identity.mgt.impl.util.UnitOfWork;
import org.wso2.carbon.identity.mgt.resolver.DomainGroup;
import org.wso2.carbon.identity.mgt.resolver.DomainUser;
import org.wso2.carbon.identity.mgt.resolver.GroupPartition;
import org.wso2.carbon.identity.mgt.resolver.UniqueIdResolver;
import org.wso2.carbon.identity.mgt.resolver.UniqueIdResolverConfig;
import org.wso2.carbon.identity.mgt.resolver.UserPartition;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;

import static org.wso2.carbon.identity.mgt.impl.util.UniqueIdResolverConstants.ColumnNames;
import static org.wso2.carbon.identity.mgt.impl.util.UniqueIdResolverConstants.SQLPlaceholders;

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
            final String selectUniqueUser = "SELECT CONNECTOR_TYPE, CONNECTOR_ID, CONNECTOR_USER_ID," +
                    " STATE FROM IDM_USER WHERE USER_ID = :" + SQLPlaceholders.USER_ID + "; AND " +
                    "DOMAIN_ID = :" + SQLPlaceholders.DOMAIN_ID + "; ";

            DomainUser domainUser = new DomainUser();
            List<UserPartition> userPartitions = new ArrayList<>();
            String state = null;
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    selectUniqueUser);
            namedPreparedStatement.setString(SQLPlaceholders.USER_ID, domainUserId);
            namedPreparedStatement.setInt(SQLPlaceholders.DOMAIN_ID, domainId);
            try (ResultSet resultSet = namedPreparedStatement.getPreparedStatement().executeQuery()) {
                while (resultSet.next()) {
                    UserPartition userPartition = new UserPartition();
                    userPartition.setConnectorId(resultSet.getString(ColumnNames.CONNECTOR_ID));
                    userPartition.setConnectorUserId(resultSet.getString(ColumnNames.CONNECTOR_USER_ID));
                    userPartition.setIdentityStore(UniqueIdResolverConstants.IDENTITY_STORE_CONNECTOR.equals(resultSet
                            .getString(ColumnNames.CONNECTOR_TYPE)));
                    userPartitions.add(userPartition);
                    state = resultSet.getString(SQLPlaceholders.STATE);
                }
            }

            domainUser.setDomainUserId(domainUserId);
            domainUser.setUserPartitions(userPartitions);
            domainUser.setState(state);
            return domainUser;

        } catch (SQLException e) {
            throw new UniqueIdResolverException("Error while searching user.", e);
        }
    }

    @Override
    public DomainUser getUserFromConnectorUserId(String connectorUserId, String connectorId, int domainId) throws
            UserNotFoundException, UniqueIdResolverException {

        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
            final String selectUniqueUser = "SELECT USER_ID, CONNECTOR_TYPE, CONNECTOR_ID, CONNECTOR_USER_ID, STATE " +
                    "FROM IDM_USER WHERE USER_ID = ( " +
                    "SELECT USER_ID FROM IDM_USER " +
                    "WHERE CONNECTOR_USER_ID = :" + SQLPlaceholders.CONNECTOR_USER_ID + "; " +
                    "AND CONNECTOR_ID = :" + SQLPlaceholders.CONNECTOR_ID + "; AND " +
                    "DOMAIN_ID = :" + SQLPlaceholders.MAPPING_DOMAIN_ID + ";) AND " +
                    "DOMAIN_ID = :" + SQLPlaceholders.DOMAIN_ID + ";";

            String userUUID = null;
            DomainUser domainUser = new DomainUser();
            List<UserPartition> userPartitions = new ArrayList<>();
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    selectUniqueUser);
            namedPreparedStatement.setString(SQLPlaceholders.CONNECTOR_USER_ID, connectorUserId);
            namedPreparedStatement.setString(SQLPlaceholders.CONNECTOR_ID, connectorId);
            namedPreparedStatement.setInt(SQLPlaceholders.MAPPING_DOMAIN_ID, domainId);
            namedPreparedStatement.setInt(SQLPlaceholders.DOMAIN_ID, domainId);
            String state = null;
            try (ResultSet resultSet = namedPreparedStatement.getPreparedStatement().executeQuery()) {

                while (resultSet.next()) {
                    UserPartition userPartition = new UserPartition();
                    userUUID = resultSet.getString(ColumnNames.USER_ID);
                    userPartition.setConnectorId(resultSet.getString(ColumnNames.CONNECTOR_ID));
                    userPartition.setConnectorUserId(resultSet.getString(ColumnNames.CONNECTOR_USER_ID));
                    userPartition.setIdentityStore(UniqueIdResolverConstants.IDENTITY_STORE_CONNECTOR.equals(resultSet
                            .getString(ColumnNames.CONNECTOR_TYPE)));
                    state = resultSet.getString(SQLPlaceholders.STATE);
                    userPartitions.add(userPartition);
                }
            }
            if (userUUID == null) {
                throw new UserNotFoundException("No user found.");
            }
            domainUser.setDomainUserId(userUUID);
            domainUser.setUserPartitions(userPartitions);
            domainUser.setState(state);
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
            } catch (UniqueIdResolverException | UserNotFoundException e) {
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
                    "WHERE USER_ID = :" + SQLPlaceholders.USER_ID + "; AND " +
                    "DOMAIN_ID = :" + SQLPlaceholders.DOMAIN_ID + ";";

            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    selectUser);
            namedPreparedStatement.setString(SQLPlaceholders.USER_ID, domainUserId);
            namedPreparedStatement.setInt(SQLPlaceholders.DOMAIN_ID, domainId);
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
            final String selectUniqueUser = "SELECT USER_ID, CONNECTOR_TYPE, CONNECTOR_ID, CONNECTOR_USER_ID, STATE " +
                    "FROM IDM_USER WHERE DOMAIN_ID = :" + SQLPlaceholders.DOMAIN_ID + "; LIMIT :limit; OFFSET :offset;";

            Map<String, DomainUser> userMap = new HashMap<>();
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    selectUniqueUser);
            namedPreparedStatement.setInt(SQLPlaceholders.DOMAIN_ID, domainId);
            namedPreparedStatement.setInt(SQLPlaceholders.LIMIT, length);
            namedPreparedStatement.setInt(SQLPlaceholders.OFFSET, offset);
            String state;
            try (ResultSet resultSet = namedPreparedStatement.getPreparedStatement().executeQuery()) {

                while (resultSet.next()) {
                    UserPartition userPartition = new UserPartition();
                    String userUUID = resultSet.getString(ColumnNames.USER_ID);
                    userPartition.setConnectorId(resultSet.getString(ColumnNames.CONNECTOR_ID));
                    userPartition.setConnectorUserId(resultSet.getString(ColumnNames.CONNECTOR_USER_ID));
                    userPartition.setIdentityStore(UniqueIdResolverConstants.IDENTITY_STORE_CONNECTOR.equals(resultSet
                            .getString(ColumnNames.CONNECTOR_TYPE)));
                    state = resultSet.getString(SQLPlaceholders.STATE);

                    DomainUser user;
                    if ((user = userMap.get(userUUID)) != null) {
                        user.addUserPartition(userPartition);
                    } else {
                        user = new DomainUser();
                        user.setDomainUserId(userUUID);
                        user.addUserPartition(userPartition);
                        user.setState(state);
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
                    "IDM_GROUP WHERE GROUP_ID = :" + SQLPlaceholders.GROUP_ID + "; AND " +
                    "DOMAIN_ID = :" + SQLPlaceholders.DOMAIN_ID + ";";

            DomainGroup domainGroup = new DomainGroup();
            List<GroupPartition> groupPartitions = new ArrayList<>();
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    selectUniqueUser);
            namedPreparedStatement.setString(SQLPlaceholders.GROUP_ID, domainGroupId);
            namedPreparedStatement.setInt(SQLPlaceholders.DOMAIN_ID, domainId);
            try (ResultSet resultSet = namedPreparedStatement.getPreparedStatement().executeQuery()) {
                while (resultSet.next()) {
                    GroupPartition userPartition = new GroupPartition();
                    userPartition.setConnectorId(resultSet.getString(ColumnNames.CONNECTOR_ID));
                    userPartition.setConnectorGroupId(resultSet.getString(ColumnNames.CONNECTOR_GROUP_ID));
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
            throws UniqueIdResolverException, GroupNotFoundException {

        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection())) {
            final String selectUniqueGroup = "SELECT GROUP_ID, CONNECTOR_ID, CONNECTOR_GROUP_ID " +
                    "FROM IDM_GROUP WHERE GROUP_ID = ( " +
                    "SELECT GROUP_ID FROM IDM_GROUP " +
                    "WHERE CONNECTOR_GROUP_ID = :" + SQLPlaceholders.CONNECTOR_GROUP_ID + "; AND " +
                    "CONNECTOR_ID = :" + SQLPlaceholders.CONNECTOR_ID + "; AND " +
                    "DOMAIN_ID = :" + SQLPlaceholders.MAPPING_DOMAIN_ID + ";) AND " +
                    "DOMAIN_ID = :" + SQLPlaceholders.DOMAIN_ID + ";";

            String groupUUID = null;
            DomainGroup domainGroup = new DomainGroup();
            List<GroupPartition> groupPartitions = new ArrayList<>();
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    selectUniqueGroup);
            namedPreparedStatement.setString(SQLPlaceholders.CONNECTOR_GROUP_ID, connectorGroupId);
            namedPreparedStatement.setString(SQLPlaceholders.CONNECTOR_ID, connectorId);
            namedPreparedStatement.setInt(SQLPlaceholders.MAPPING_DOMAIN_ID, domainId);
            namedPreparedStatement.setInt(SQLPlaceholders.DOMAIN_ID, domainId);
            try (ResultSet resultSet = namedPreparedStatement.getPreparedStatement().executeQuery()) {

                while (resultSet.next()) {
                    GroupPartition groupPartition = new GroupPartition();
                    groupUUID = resultSet.getString(ColumnNames.GROUP_ID);
                    groupPartition.setConnectorId(resultSet.getString(ColumnNames.CONNECTOR_ID));
                    groupPartition.setConnectorGroupId(resultSet.getString(ColumnNames.CONNECTOR_GROUP_ID));
                    groupPartitions.add(groupPartition);
                }
            }
            if (groupUUID == null) {
                throw new GroupNotFoundException("No group found.");
            }
            domainGroup.setDomainGroupId(groupUUID);
            domainGroup.setGroupPartitions(groupPartitions);
            return domainGroup;

        } catch (SQLException e) {
            throw new UniqueIdResolverException("Error while searching group.", e);
        }
    }

    @Override
    public String addUser(DomainUser domainUser, int domainId) throws UniqueIdResolverException {
        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection(), false)) {
            final String addUser = "INSERT INTO IDM_USER " +
                    "(USER_ID, CONNECTOR_USER_ID, CONNECTOR_ID, DOMAIN_ID, CONNECTOR_TYPE, STATE) " +
                    "VALUES (:" + SQLPlaceholders.USER_ID + ";, :" + SQLPlaceholders.CONNECTOR_USER_ID + ";, " +
                    ":" + SQLPlaceholders.CONNECTOR_ID + ";, :" + SQLPlaceholders.DOMAIN_ID + ";, " +
                    ":" + SQLPlaceholders.CONNECTOR_TYPE + ";, :" + SQLPlaceholders.STATE + ";)";

            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(), addUser);
            for (UserPartition userPartition : domainUser.getUserPartitions()) {
                namedPreparedStatement.setString(SQLPlaceholders.USER_ID, domainUser.getDomainUserId());
                namedPreparedStatement.setString(SQLPlaceholders.CONNECTOR_USER_ID, userPartition.getConnectorUserId());
                namedPreparedStatement.setString(SQLPlaceholders.CONNECTOR_ID, userPartition.getConnectorId());
                namedPreparedStatement.setInt(SQLPlaceholders.DOMAIN_ID, domainId);
                namedPreparedStatement.setString(SQLPlaceholders.CONNECTOR_TYPE,
                        userPartition.isIdentityStore() ? UniqueIdResolverConstants.IDENTITY_STORE_CONNECTOR :
                                UniqueIdResolverConstants.CREDENTIAL_STORE_CONNECTOR);
                namedPreparedStatement.setString(SQLPlaceholders.STATE, domainUser.getState());
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
    public List<String> addUsers(List<DomainUser> domainUsers, int domainId)
            throws UniqueIdResolverException {

        UniqueIdResolverException uniqueIdResolverException = new UniqueIdResolverException();
        domainUsers.stream().forEach(uniqueUser -> {
            try {
                addUser(uniqueUser, domainId);
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
                    "(USER_ID, CONNECTOR_USER_ID, CONNECTOR_ID, DOMAIN_ID, CONNECTOR_TYPE) " +
                    "VALUES (:" + SQLPlaceholders.USER_ID + ";, :" + SQLPlaceholders.CONNECTOR_USER_ID + ";, " +
                    ":" + SQLPlaceholders.CONNECTOR_ID + ";, :" + SQLPlaceholders.DOMAIN_ID + ";, " +
                    ":" + SQLPlaceholders.CONNECTOR_TYPE + ";)";
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(), addUser);
            for (Map.Entry<String, String> entry : connectorUserIdMap.entrySet()) {
                namedPreparedStatement.setString(SQLPlaceholders.USER_ID, domainUserId);
                namedPreparedStatement.setString(SQLPlaceholders.CONNECTOR_USER_ID, entry.getValue());
                namedPreparedStatement.setString(SQLPlaceholders.CONNECTOR_ID, entry.getKey());
                namedPreparedStatement.setInt(SQLPlaceholders.DOMAIN_ID, domainId);
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
    public String addGroup(DomainGroup domainGroup, int domainId) throws UniqueIdResolverException {

        try (UnitOfWork unitOfWork = UnitOfWork.beginTransaction(dataSource.getConnection(), false)) {
            final String addGroup = "INSERT INTO IDM_GROUP " +
                    "(GROUP_ID, CONNECTOR_GROUP_ID, CONNECTOR_ID, DOMAIN_ID) " +
                    "VALUES (:" + SQLPlaceholders.GROUP_ID + ";, :" + SQLPlaceholders.CONNECTOR_GROUP_ID + ";, " +
                    ":" + SQLPlaceholders.CONNECTOR_ID + ";, :" + SQLPlaceholders.DOMAIN_ID + ";)";
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(), addGroup);
            for (GroupPartition groupPartition : domainGroup.getGroupPartitions()) {
                namedPreparedStatement.setString(SQLPlaceholders.GROUP_ID, domainGroup.getDomainGroupId());
                namedPreparedStatement.setString(SQLPlaceholders.CONNECTOR_GROUP_ID,
                        groupPartition.getConnectorGroupId());
                namedPreparedStatement.setString(SQLPlaceholders.CONNECTOR_ID, groupPartition.getConnectorId());
                namedPreparedStatement.setInt(SQLPlaceholders.DOMAIN_ID, domainId);

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
    public List<String> addGroups(List<DomainGroup> domainGroups, int domainId)
            throws UniqueIdResolverException {

        UniqueIdResolverException uniqueIdResolverException = new UniqueIdResolverException();
        domainGroups.stream().forEach(uniqueGroup -> {
            try {
                addGroup(uniqueGroup, domainId);
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
                    "(GROUP_ID, CONNECTOR_GROUP_ID, CONNECTOR_ID, DOMAIN_ID) " +
                    "VALUES (:" + SQLPlaceholders.GROUP_ID + ";, :" + SQLPlaceholders.CONNECTOR_GROUP_ID + ";, " +
                    ":" + SQLPlaceholders.CONNECTOR_ID + ";, :" + SQLPlaceholders.DOMAIN_ID + ";)";
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(), addGroup);
            for (Map.Entry<String, String> entry : connectorGroupIdMap.entrySet()) {
                namedPreparedStatement.setString(SQLPlaceholders.GROUP_ID, domainGroupId);
                namedPreparedStatement.setString(SQLPlaceholders.CONNECTOR_GROUP_ID,
                        entry.getValue());
                namedPreparedStatement.setString(SQLPlaceholders.CONNECTOR_ID,
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
                    "WHERE GROUP_ID = :" + SQLPlaceholders.GROUP_ID + "; AND " +
                    "DOMAIN_ID = :" + SQLPlaceholders.DOMAIN_ID + ";";

            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    selectUser);
            namedPreparedStatement.setString(SQLPlaceholders.GROUP_ID, uniqueGroupId);
            namedPreparedStatement.setInt(SQLPlaceholders.DOMAIN_ID, domainId);
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
            final String selectUniqueUser = "SELECT GROUP_ID, CONNECTOR_ID, CONNECTOR_GROUP_ID " +
                    "FROM IDM_GROUP WHERE DOMAIN_ID = :" + SQLPlaceholders.DOMAIN_ID + "; " +
                    "LIMIT :limit; OFFSET :offset;";

            Map<String, DomainGroup> groupMap = new HashMap<>();
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    selectUniqueUser);
            namedPreparedStatement.setInt(SQLPlaceholders.DOMAIN_ID, domainId);
            namedPreparedStatement.setInt(SQLPlaceholders.LIMIT, length);
            namedPreparedStatement.setInt(SQLPlaceholders.OFFSET, offset);
            try (ResultSet resultSet = namedPreparedStatement.getPreparedStatement().executeQuery()) {

                while (resultSet.next()) {
                    GroupPartition groupPartition = new GroupPartition();
                    String groupUUID = resultSet.getString(ColumnNames.GROUP_ID);
                    groupPartition.setConnectorId(resultSet.getString(ColumnNames.CONNECTOR_ID));
                    groupPartition.setConnectorGroupId(resultSet.getString(ColumnNames.CONNECTOR_GROUP_ID));

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
            } catch (UniqueIdResolverException | GroupNotFoundException e) {
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
            final String selectGroupsOfUser = "SELECT GROUP_ID, CONNECTOR_ID, CONNECTOR_GROUP_ID " +
                    "FROM IDM_GROUP WHERE GROUP_ID IN ( " +
                    "SELECT GROUP_ID " +
                    "FROM IDM_USER_GROUP_MAPPING " +
                    "WHERE USER_ID = :" + SQLPlaceholders.USER_ID + "; AND " +
                    "DOMAIN_ID = :" + SQLPlaceholders.MAPPING_DOMAIN_ID + ";) AND " +
                    "DOMAIN_ID = :" + SQLPlaceholders.DOMAIN_ID + ";";

            Map<String, DomainGroup> groupMap = new HashMap<>();
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    selectGroupsOfUser);
            namedPreparedStatement.setString(SQLPlaceholders.USER_ID, domainUserId);
            namedPreparedStatement.setInt(SQLPlaceholders.MAPPING_DOMAIN_ID, domainId);
            namedPreparedStatement.setInt(SQLPlaceholders.DOMAIN_ID, domainId);
            try (ResultSet resultSet = namedPreparedStatement.getPreparedStatement().executeQuery()) {

                while (resultSet.next()) {
                    GroupPartition groupPartition = new GroupPartition();
                    String groupUUID = resultSet.getString(ColumnNames.GROUP_ID);
                    groupPartition.setConnectorId(resultSet.getString(ColumnNames.CONNECTOR_ID));
                    groupPartition.setConnectorGroupId(resultSet.getString(ColumnNames.CONNECTOR_GROUP_ID));

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
            final String selectUsersOfGroup = "SELECT USER_ID, CONNECTOR_ID, CONNECTOR_USER_ID, CONNECTOR_TYPE, STATE" +
                    " FROM IDM_USER WHERE USER_ID IN ( " +
                    "SELECT USER_ID " +
                    "FROM IDM_USER_GROUP_MAPPING " +
                    "WHERE GROUP_ID = :" + SQLPlaceholders.GROUP_ID + "; AND " +
                    "DOMAIN_ID = :" + SQLPlaceholders.MAPPING_DOMAIN_ID + "; ) AND " +
                    "DOMAIN_ID = :" + SQLPlaceholders.DOMAIN_ID + ";";

            Map<String, DomainUser> userMap = new HashMap<>();
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    selectUsersOfGroup);
            namedPreparedStatement.setString(SQLPlaceholders.GROUP_ID, domainGroupId);
            namedPreparedStatement.setInt(SQLPlaceholders.MAPPING_DOMAIN_ID, domainId);
            namedPreparedStatement.setInt(SQLPlaceholders.DOMAIN_ID, domainId);
            String state;
            try (ResultSet resultSet = namedPreparedStatement.getPreparedStatement().executeQuery()) {

                while (resultSet.next()) {
                    UserPartition userPartition = new UserPartition();
                    String userUUID = resultSet.getString(ColumnNames.USER_ID);
                    userPartition.setConnectorId(resultSet.getString(ColumnNames.CONNECTOR_ID));
                    userPartition.setConnectorUserId(resultSet.getString(ColumnNames.CONNECTOR_USER_ID));
                    userPartition.setIdentityStore(UniqueIdResolverConstants.IDENTITY_STORE_CONNECTOR.equals(resultSet
                            .getString(ColumnNames.CONNECTOR_TYPE)));
                    state = resultSet.getString(SQLPlaceholders.STATE);
                    DomainUser user;
                    if ((user = userMap.get(userUUID)) != null) {
                        user.addUserPartition(userPartition);
                    } else {
                        user = new DomainUser();
                        user.setDomainUserId(userUUID);
                        user.addUserPartition(userPartition);
                        user.setState(state);
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
                    "WHERE GROUP_ID = :" + SQLPlaceholders.GROUP_ID + "; AND " +
                    "USER_ID = :" + SQLPlaceholders.USER_ID + "; AND " +
                    "DOMAIN_ID = :" + SQLPlaceholders.DOMAIN_ID + ";";

            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    selectUsersOfGroup);
            namedPreparedStatement.setString(SQLPlaceholders.GROUP_ID, domainGroupId);
            namedPreparedStatement.setString(SQLPlaceholders.USER_ID, domainUserId);
            namedPreparedStatement.setInt(SQLPlaceholders.DOMAIN_ID, domainId);
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
            final String insertGroupsOfUser = "INSERT INTO IDM_USER_GROUP_MAPPING (USER_ID, GROUP_ID, DOMAIN_ID) " +
                    "VALUES ( :" + SQLPlaceholders.USER_ID + ";, :" + SQLPlaceholders.GROUP_ID + ";, " +
                    ":" + SQLPlaceholders.DOMAIN_ID + "; ) ";
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    insertGroupsOfUser);
            for (String uniqueGroupId : domainGroupIds) {
                namedPreparedStatement.setString(SQLPlaceholders.USER_ID, domainUserId);
                namedPreparedStatement.setString(SQLPlaceholders.GROUP_ID, uniqueGroupId);
                namedPreparedStatement.setInt(SQLPlaceholders.DOMAIN_ID, domainId);
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
                    "WHERE USER_ID = :" + SQLPlaceholders.USER_ID + "; AND " +
                    "GROUP_ID = :" + SQLPlaceholders.GROUP_ID + "; AND " +
                    "DOMAIN_ID = :" + SQLPlaceholders.DOMAIN_ID + "; ";
            NamedPreparedStatement deleteNamedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(), deleteUserGroupMapping);
            for (String uniqueGroupId : domainGroupIdsToRemove) {
                deleteNamedPreparedStatement.setString(SQLPlaceholders.USER_ID, domainUserId);
                deleteNamedPreparedStatement.setString(SQLPlaceholders.GROUP_ID, uniqueGroupId);
                deleteNamedPreparedStatement.setInt(SQLPlaceholders.DOMAIN_ID, domainId);
                deleteNamedPreparedStatement.getPreparedStatement().addBatch();
            }

            deleteNamedPreparedStatement.getPreparedStatement().executeBatch();

            // Add the user group mappings in uniqueGroupIdsToUpdate
            final String insertGroupsOfUser = "INSERT INTO IDM_USER_GROUP_MAPPING (USER_ID, GROUP_ID, DOMAIN_ID) " +
                    "VALUES ( :" + SQLPlaceholders.USER_ID + ";, :" + SQLPlaceholders.GROUP_ID + ";, " +
                    ":" + SQLPlaceholders.DOMAIN_ID + "; ) ";
            NamedPreparedStatement addNamedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    insertGroupsOfUser);
            for (String uniqueGroupId : domainGroupIdsToUpdate) {
                addNamedPreparedStatement.setString(SQLPlaceholders.USER_ID, domainUserId);
                addNamedPreparedStatement.setString(SQLPlaceholders.GROUP_ID, uniqueGroupId);
                addNamedPreparedStatement.setInt(SQLPlaceholders.DOMAIN_ID, domainId);
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
            final String insertUsersOfGroup = "INSERT INTO IDM_USER_GROUP_MAPPING (USER_ID, GROUP_ID, DOMAIN_ID) " +
                    "VALUES ( :" + SQLPlaceholders.USER_ID + ";, :" + SQLPlaceholders.GROUP_ID + ";, " +
                    ":" + SQLPlaceholders.DOMAIN_ID + "; ) ";
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    insertUsersOfGroup);
            for (String uniqueUserId : domainUserIds) {
                namedPreparedStatement.setString(SQLPlaceholders.USER_ID, uniqueUserId);
                namedPreparedStatement.setString(SQLPlaceholders.GROUP_ID, domainGroupId);
                namedPreparedStatement.setInt(SQLPlaceholders.DOMAIN_ID, domainId);
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
                    "WHERE USER_ID = :" + SQLPlaceholders.USER_ID + "; AND " +
                    "GROUP_ID = :" + SQLPlaceholders.GROUP_ID + "; AND " +
                    "DOMAIN_ID = :" + SQLPlaceholders.DOMAIN_ID + ";";
            NamedPreparedStatement deleteNamedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(), deleteUserGroupMapping);
            for (String uniqueUserId : domainUserIdsToRemove) {
                deleteNamedPreparedStatement.setString(SQLPlaceholders.USER_ID, uniqueUserId);
                deleteNamedPreparedStatement.setString(SQLPlaceholders.GROUP_ID, doaminGroupId);
                deleteNamedPreparedStatement.setInt(SQLPlaceholders.DOMAIN_ID, domainId);
                deleteNamedPreparedStatement.getPreparedStatement().addBatch();
            }

            deleteNamedPreparedStatement.getPreparedStatement().executeBatch();

            // Add the user group mappings in uniqueUserIdsToUpdate
            final String insertUsersOfGroup = "INSERT INTO IDM_USER_GROUP_MAPPING (USER_ID, GROUP_ID, DOMAIN_ID) " +
                    "VALUES ( :" + SQLPlaceholders.USER_ID + ";, :" + SQLPlaceholders.GROUP_ID + ";, " +
                    ":" + SQLPlaceholders.DOMAIN_ID + "; ) ";
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                    unitOfWork.getConnection(),
                    insertUsersOfGroup);
            for (String uniqueUserId : domainUserIdsToUpdate) {
                namedPreparedStatement.setString(SQLPlaceholders.USER_ID, uniqueUserId);
                namedPreparedStatement.setString(SQLPlaceholders.GROUP_ID, doaminGroupId);
                namedPreparedStatement.setInt(SQLPlaceholders.DOMAIN_ID, domainId);
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
                "WHERE USER_ID = :" + SQLPlaceholders.USER_ID + "; AND " +
                "DOMAIN_ID = :" + SQLPlaceholders.DOMAIN_ID + ";";
        NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                unitOfWork.getConnection(), deleteUser);
        namedPreparedStatement.setString(SQLPlaceholders.USER_ID, uniqueUserId);
        namedPreparedStatement.setInt(SQLPlaceholders.DOMAIN_ID, domainId);

        namedPreparedStatement.getPreparedStatement().executeUpdate();

    }

    private void deleteGroup(String uniqueGroupId, UnitOfWork unitOfWork, int domainId) throws SQLException {

        final String deleteGroup = "DELETE FROM IDM_GROUP " +
                "WHERE GROUP_ID = :" + SQLPlaceholders.GROUP_ID + "; AND " +
                "DOMAIN_ID = :" + SQLPlaceholders.DOMAIN_ID + ";";
        NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                unitOfWork.getConnection(), deleteGroup);
        namedPreparedStatement.setString(SQLPlaceholders.GROUP_ID, uniqueGroupId);
        namedPreparedStatement.setInt(SQLPlaceholders.DOMAIN_ID, domainId);

        namedPreparedStatement.getPreparedStatement().executeUpdate();

    }

    private void deleteUserGroupMappingsForUser(String uniqueUserId, UnitOfWork unitOfWork, int domainId)
            throws SQLException {

        final String deleteUserGroupMapping = "DELETE FROM IDM_USER_GROUP_MAPPING " +
                "WHERE GROUP_ID = :" + SQLPlaceholders.GROUP_ID + "; AND " +
                "DOMAIN_ID = :" + SQLPlaceholders.DOMAIN_ID + ";";
        NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                unitOfWork.getConnection(), deleteUserGroupMapping);
        namedPreparedStatement.setString(SQLPlaceholders.GROUP_ID, uniqueUserId);
        namedPreparedStatement.setInt(SQLPlaceholders.DOMAIN_ID, domainId);

        namedPreparedStatement.getPreparedStatement().executeUpdate();

    }

    private void deleteUserGroupMappingsForGroup(String uniqueGroupId, UnitOfWork unitOfWork, int domainId)
            throws SQLException {

        final String deleteUserGroupMapping = "DELETE FROM IDM_USER_GROUP_MAPPING " +
                "WHERE USER_ID = :" + SQLPlaceholders.USER_ID + "; AND " +
                "DOMAIN_ID = :" + SQLPlaceholders.DOMAIN_ID + ";";
        NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(
                unitOfWork.getConnection(), deleteUserGroupMapping);
        namedPreparedStatement.setString(SQLPlaceholders.USER_ID, uniqueGroupId);
        namedPreparedStatement.setInt(SQLPlaceholders.DOMAIN_ID, domainId);

        namedPreparedStatement.getPreparedStatement().executeUpdate();

    }
}

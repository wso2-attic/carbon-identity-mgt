/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.policy.password.history.store.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.identity.common.jdbc.JdbcTemplate;
import org.wso2.carbon.identity.policy.password.history.bean.PasswordHistoryBean;
import org.wso2.carbon.identity.policy.password.history.bean.ValidationResult;
import org.wso2.carbon.identity.policy.password.history.constants.PasswordHistoryConstants;
import org.wso2.carbon.identity.policy.password.history.exeption.IdentityPasswordHistoryException;
import org.wso2.carbon.identity.policy.password.history.store.PasswordHistoryDataStore;
import org.wso2.carbon.identity.policy.password.history.util.DatabaseUtil;
import org.wso2.carbon.identity.policy.password.history.util.PasswordHistoryUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import javax.sql.DataSource;


/**
 * This interface provides to plug module for preferred persistence store.
 */
public class DefaultPasswordHistoryDataStore implements PasswordHistoryDataStore {

    private static final Logger log = LoggerFactory.getLogger(DefaultPasswordHistoryDataStore.class);
    private PasswordHistoryBean passwordHistoryBean = new PasswordHistoryBean();
    //todo: JdbcTemplate will not be used ATM, need to improve it to use in this case
    private JdbcTemplate jdbcTemplate = null;
    private DataSource dataSource = null;
    private static final String WSO2_IDENTITY_DB = "WSO2_CARBON_DB";

    public DefaultPasswordHistoryDataStore(JdbcTemplate jdbcTemplate, DataSourceService dataSourceService) {

        try {
            dataSource = (DataSource) dataSourceService.getDataSource(WSO2_IDENTITY_DB);
        } catch (DataSourceException e) {
            log.error("Error Loading DataStore");
        }
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void store(String uniqueUserId, char[] credential) throws IdentityPasswordHistoryException {

        //First delete old unnecessary records according to history count and time period
        deleteOldEntries(uniqueUserId);
        //Store new password entry in the table
        storeNewEntry(uniqueUserId, credential);

    }

    @Override
    public void remove(String uniqueUserId) throws IdentityPasswordHistoryException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(PasswordHistoryConstants.SQLQueries.DELETE_USER_HISTORY);
            preparedStatement.setString(1, uniqueUserId);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new IdentityPasswordHistoryException("Error while validating password history", e);
        } finally {
            DatabaseUtil.closeAllConnections(connection, null, preparedStatement);
        }
    }

    @Override
    public ValidationResult validate(String uniqueUserId, char[] credential) throws IdentityPasswordHistoryException {

        ValidationResult validationResult = new ValidationResult();
        if (!validateByCount(uniqueUserId, credential)) {
            validationResult.setSuccess(false);
            validationResult.setErrorCode(PasswordHistoryConstants.ERROR_IN_COUNT);
            validationResult.setMessage("Your Password has already been used within last " +
                    passwordHistoryBean.getMinCountToAllowRepitition() + " Attemps");
            return validationResult;
        } else if (!validateByTime(uniqueUserId, credential)) {
            validationResult.setSuccess(false);
            validationResult.setErrorCode(PasswordHistoryConstants.ERROR_IN_TIME);
            validationResult.setMessage("Your Password has already been used within last " +
                    passwordHistoryBean.getMinAgeToAllowRepitition() + " Days");
            return validationResult;
        }
        validationResult.setSuccess(true);
        return validationResult;
    }

    /**
     * Delete unnecessary history records
     * @param uniqueUserId : unique user id
     * @throws IdentityPasswordHistoryException : In case of DB level exception
     */
    protected void deleteOldEntries (String uniqueUserId) throws IdentityPasswordHistoryException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        Timestamp beginTimeStamp = new Timestamp(new java.util.Date().getTime() -
                PasswordHistoryUtil.convertDaysToMilliseconds(passwordHistoryBean.getMinAgeToAllowRepitition()));

        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(PasswordHistoryConstants.SQLQueries.DELETE_HISTORY_RECORD);
            preparedStatement.setString(1, uniqueUserId);
            preparedStatement.setInt(2, passwordHistoryBean.getMinCountToAllowRepitition());
            preparedStatement.setString(3, uniqueUserId);
            preparedStatement.setTimestamp(4, beginTimeStamp);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new IdentityPasswordHistoryException("Error while validating password history", e);

        } finally {
            DatabaseUtil.closeAllConnections(connection, null, preparedStatement);
        }

    }

    /**
     * Store new record with current password
     * @param uniqueUserId : unique user id
     * @param credential : password
     * @throws IdentityPasswordHistoryException : In case of DB level exception
     */
    protected void storeNewEntry (String uniqueUserId, char[] credential) throws IdentityPasswordHistoryException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        String saltValue = PasswordHistoryUtil.generateSaltValue();

        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(PasswordHistoryConstants.SQLQueries.STORE_HISTORY_DATA);
            preparedStatement.setString(1, uniqueUserId);
            preparedStatement.setString(2, saltValue);
            preparedStatement.setString(3, PasswordHistoryUtil.generateSaltedHash(credential, saltValue,
                    passwordHistoryBean.getHashingAlgorithm()));
            preparedStatement.setTimestamp(4, new Timestamp(new java.util.Date().getTime()));
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new IdentityPasswordHistoryException("Error while validating password history", e);

        } finally {
            DatabaseUtil.closeAllConnections(connection, null, preparedStatement);
        }

    }

    /**
     * Validate history against number of last attempts
     * @param uniqueUserId : unique user id
     * @param credential : password
     * @return : validity state
     * @throws IdentityPasswordHistoryException : In case of DB level exception
     */
    protected boolean validateByCount(String uniqueUserId, char[] credential) throws IdentityPasswordHistoryException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(PasswordHistoryConstants.SQLQueries.
                    LOAD_HISTORY_DATA_BY_COUNT);
            preparedStatement.setString(1, uniqueUserId);
            preparedStatement.setInt(2, passwordHistoryBean.getMinCountToAllowRepitition());
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String saltValue = resultSet.getString(PasswordHistoryConstants.SALT_VALUE);
                String hash = resultSet.getString(PasswordHistoryConstants.HASH);
                if (isHistoryExists(saltValue, hash, credential)) {
                    return false;
                }
            }

        } catch (SQLException e) {
            throw new IdentityPasswordHistoryException("Error while validating password history", e);
        } finally {
            DatabaseUtil.closeAllConnections(connection, resultSet, preparedStatement);
        }

        return true;
    }

    /**
     * Validate history against time
     * @param uniqueUserId : unique user id
     * @param credential : password
     * @return : validity state
     * @throws IdentityPasswordHistoryException : In case of DB level exception
     */
    protected boolean validateByTime (String uniqueUserId, char[] credential) throws IdentityPasswordHistoryException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Timestamp beginTimeStamp = new Timestamp(new java.util.Date().getTime() -
                PasswordHistoryUtil.convertDaysToMilliseconds(passwordHistoryBean.getMinAgeToAllowRepitition()));

        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(PasswordHistoryConstants.SQLQueries.
                    LOAD_HISTORY_DATA_BY_TIME);
            preparedStatement.setString(1, uniqueUserId);
            preparedStatement.setTimestamp(2, beginTimeStamp);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String saltValue = resultSet.getString(PasswordHistoryConstants.SALT_VALUE);
                String hash = resultSet.getString(PasswordHistoryConstants.HASH);
                if (isHistoryExists(saltValue, hash, credential)) {
                    return false;
                }
            }

        } catch (SQLException e) {
            throw new IdentityPasswordHistoryException("Error while validating password history", e);

        } finally {
            DatabaseUtil.closeAllConnections(connection, resultSet, preparedStatement);
        }

        return true;
    }

    private boolean isHistoryExists(String saltValue, String storedPassword, char[] credential) throws
            IdentityPasswordHistoryException {

        String password;
        password = PasswordHistoryUtil.generateSaltedHash(credential, saltValue,
                passwordHistoryBean.getHashingAlgorithm());
        if ((storedPassword != null) && (storedPassword.equals(password))) {
            return true;
        }
        return false;
    }

}

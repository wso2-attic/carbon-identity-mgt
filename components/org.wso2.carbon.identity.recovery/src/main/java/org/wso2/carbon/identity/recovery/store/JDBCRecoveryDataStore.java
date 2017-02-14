/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.recovery.store;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.identity.common.jdbc.DataAccessException;
import org.wso2.carbon.identity.common.jdbc.JdbcTemplate;
import org.wso2.carbon.identity.recovery.IdentityRecoveryClientException;
import org.wso2.carbon.identity.recovery.IdentityRecoveryConstants;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.RecoveryScenarios;
import org.wso2.carbon.identity.recovery.RecoverySteps;
import org.wso2.carbon.identity.recovery.internal.IdentityRecoveryServiceDataHolder;
import org.wso2.carbon.identity.recovery.model.UserRecoveryData;
import org.wso2.carbon.identity.recovery.util.Utils;

import java.sql.Timestamp;
import java.util.Date;

/**
 * DAO class which can be used for database operations.
 */
public class JDBCRecoveryDataStore implements UserRecoveryDataStore {

    private static UserRecoveryDataStore jdbcRecoveryDataStore = new JDBCRecoveryDataStore();
    private JdbcTemplate jdbcTemplate;

    static final String USER_UNIQUE_ID = "user_unique_id";
    static final String CODE = "code";
    static final String SCENARIO = "scenario";
    static final String STEP = "step";
    static final String TIME_CREATED = "time_created";
    static final String REMAINING_SETS = "remaining_sets";

    private JDBCRecoveryDataStore() {

    }

    public static UserRecoveryDataStore getInstance() {
        return jdbcRecoveryDataStore;
    }

    @Override
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void store(UserRecoveryData recoveryDataDO) throws IdentityRecoveryException {
        final String storeRecoveryData =
                "INSERT INTO IDN_RECOVERY_DATA " + "(USER_UNIQUE_ID, CODE, SCENARIO,STEP, TIME_CREATED, REMAINING_SETS)"
                        + "VALUES (:" + USER_UNIQUE_ID + ";, :" + CODE + ";, :" + SCENARIO + ";, :" + STEP + ";, :"
                        + TIME_CREATED + ";, :" + REMAINING_SETS + ";)";

        try {
            jdbcTemplate.executeInsert(storeRecoveryData, (namedPreparedStatement) -> {
                namedPreparedStatement.setString(USER_UNIQUE_ID, recoveryDataDO.getUserUniqueId());
                namedPreparedStatement.setString(CODE, recoveryDataDO.getSecret());
                namedPreparedStatement.setString(SCENARIO, String.valueOf(recoveryDataDO.getRecoveryScenario()));
                namedPreparedStatement.setString(STEP, String.valueOf(recoveryDataDO.getRecoveryStep()));
                namedPreparedStatement.setTimeStamp(TIME_CREATED, new Timestamp(new Date().getTime()));
                namedPreparedStatement.setString(REMAINING_SETS, recoveryDataDO.getRemainingSetIds());
            }, recoveryDataDO, false);
        } catch (DataAccessException e) {
            throw Utils.handleServerException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_STORING_RECOVERY_DATA,
                    null, e);
        }
    }

    @Override
    public UserRecoveryData load(String userUniqueId, Enum recoveryScenario, Enum recoveryStep, String code)
            throws IdentityRecoveryException {
        final String loadRecoveryData =
                "SELECT " + "* FROM IDN_RECOVERY_DATA WHERE USER_UNIQUE_ID = :" + USER_UNIQUE_ID + "; AND CODE = :" +
                        CODE + "; " + "AND " + "SCENARIO = :" + SCENARIO + "; AND STEP = :" + STEP + ";";
        UserRecoveryData userRecoveryDataObject = null;

        try {
            userRecoveryDataObject = jdbcTemplate.fetchSingleRecord(loadRecoveryData, (resultSet, rowNumber) -> {
                UserRecoveryData userRecoveryData = new UserRecoveryData(userUniqueId, code, recoveryScenario,
                        recoveryStep);
                if (StringUtils.isNotBlank(resultSet.getString("REMAINING_SETS"))) {
                    userRecoveryData.setRemainingSetIds(resultSet.getString("REMAINING_SETS"));
                }

                userRecoveryData.setTimeCreated(resultSet.getTimestamp("TIME_CREATED"));

                return userRecoveryData;
            }, namedPreparedStatement -> {
                namedPreparedStatement.setString(USER_UNIQUE_ID, userUniqueId);
                namedPreparedStatement.setString(CODE, code);
                namedPreparedStatement.setString(SCENARIO, String.valueOf(recoveryScenario));
                namedPreparedStatement.setString(STEP, String.valueOf(recoveryStep));
            });
        } catch (DataAccessException e) {

            throw Utils.handleServerException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_UNEXPECTED, null, e);
        }

        if (isCodeExpired(userRecoveryDataObject)) {
            throw Utils.handleClientException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_EXPIRED_CODE, code);
        }

        return userRecoveryDataObject;
    }

    @Override
    public UserRecoveryData loadByCode(String code) throws IdentityRecoveryException {
        final String loadRecoveryDataFromCode = "SELECT * FROM IDN_RECOVERY_DATA WHERE CODE = :" + CODE + ";";
        UserRecoveryData userRecoveryDataObject = null;

        try {
            userRecoveryDataObject = jdbcTemplate
                    .fetchSingleRecord(loadRecoveryDataFromCode, (resultSet, rowNumber) -> {
                        String userUniqueId = resultSet.getString("USER_UNIQUE_ID");
                        String recoveryScenario = resultSet.getString("SCENARIO");
                        String recoveryStep = resultSet.getString("STEP");

                        UserRecoveryData userRecoveryData = new UserRecoveryData(userUniqueId, code,
                                RecoveryScenarios.valueOf(recoveryScenario), RecoverySteps.valueOf(recoveryStep));

                        if (StringUtils.isNotBlank(resultSet.getString("REMAINING_SETS"))) {
                            userRecoveryData.setRemainingSetIds(resultSet.getString("REMAINING_SETS"));
                        }
                        userRecoveryData.setTimeCreated(resultSet.getTimestamp("TIME_CREATED"));
                        return userRecoveryData;

                    }, namedPreparedStatement -> {
                        namedPreparedStatement.setString(CODE, code);
                    });
        } catch (DataAccessException e) {
            throw Utils.handleServerException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_UNEXPECTED, null, e);
        }

        if (userRecoveryDataObject == null) {
            throw Utils.handleClientException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_INVALID_CODE, code);
        }

        if (isCodeExpired(userRecoveryDataObject)) {
            throw Utils.handleClientException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_EXPIRED_CODE, code);
        }


        return userRecoveryDataObject;
    }

    @Override
    public void invalidateByCode(String code) throws IdentityRecoveryException {
        final String invalidateCode = "DELETE FROM IDN_RECOVERY_DATA WHERE CODE = :" + CODE + ";";
        try {
            jdbcTemplate.executeUpdate(invalidateCode, namedPreparedStatement -> {
                namedPreparedStatement.setString(CODE, code);
            });
        } catch (DataAccessException e) {
            throw Utils.handleServerException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_UNEXPECTED, null, e);
        }
    }

    @Override
    public UserRecoveryData loadByUserUniqueId(String userUniqueId) throws IdentityRecoveryException {
        final String loadRecoveryDataOfUser =
                "SELECT " + "* FROM IDN_RECOVERY_DATA WHERE USER_UNIQUE_ID = :" + USER_UNIQUE_ID + ";";
        UserRecoveryData userRecoveryDataObject = null;

        try {
            userRecoveryDataObject = jdbcTemplate
                    .fetchSingleRecord(loadRecoveryDataOfUser, (resultSet, rowNumber) -> {
                        UserRecoveryData userRecoveryData = new UserRecoveryData(userUniqueId,
                                resultSet.getString("CODE"), RecoveryScenarios.valueOf(resultSet.getString("SCENARIO")),
                                RecoverySteps.valueOf(resultSet.getString("STEP")));
                        if (StringUtils.isNotBlank(resultSet.getString("REMAINING_SETS"))) {
                            userRecoveryData.setRemainingSetIds(resultSet.getString("REMAINING_SETS"));
                        }

                        return userRecoveryData;

                    }, namedPreparedStatement -> {
                        namedPreparedStatement.setString(USER_UNIQUE_ID, userUniqueId);
                    });
        } catch (DataAccessException e) {
            throw Utils.handleServerException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_UNEXPECTED, null, e);
        }

        return userRecoveryDataObject;
    }

    @Override
    public void invalidateByUserUniqueId(String userUniqueId) throws IdentityRecoveryException {
        final String invalidateUserCodes = "DELETE FROM IDN_RECOVERY_DATA WHERE USER_UNIQUE_ID = :" +
                USER_UNIQUE_ID + ";";
        try {
            jdbcTemplate.executeUpdate(invalidateUserCodes, namedPreparedStatement -> {
                namedPreparedStatement.setString(USER_UNIQUE_ID, userUniqueId);
            });
        } catch (DataAccessException e) {
            throw Utils.handleServerException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_UNEXPECTED, null, e);
        }
    }

    private boolean isCodeExpired(UserRecoveryData userRecoveryDataObject) throws IdentityRecoveryClientException {
        long createdTimeStamp = userRecoveryDataObject.getTimeCreated().getTime();
        int notificationExpiryTimeInMinutes = IdentityRecoveryServiceDataHolder.getInstance().getRecoveryLinkConfig()
                .getNotificationExpiryTime(); //Notification expiry time in minutes
        long expiryTime = createdTimeStamp + notificationExpiryTimeInMinutes * 60L * 1000L;

        if (System.currentTimeMillis() > expiryTime) {
            return true;
        }

        return false;
    }

}

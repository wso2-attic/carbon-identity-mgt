package org.wso2.carbon.identity.recovery.store;

//import org.apache.commons.lang.StringUtils;
//import org.wso2.carbon.identity.application.common.model.User;

import org.apache.commons.lang3.StringUtils;
//import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
//import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
//import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.common.jdbc.DataAccessException;
import org.wso2.carbon.identity.common.jdbc.JdbcTemplate;
import org.wso2.carbon.identity.mgt.User;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.RecoveryScenarios;
import org.wso2.carbon.identity.recovery.RecoverySteps;
import org.wso2.carbon.identity.recovery.model.UserRecoveryData;

import java.sql.*;
import java.util.Date;

public class JDBCRecoveryDataStore implements UserRecoveryDataStore {

    private static UserRecoveryDataStore jdbcRecoveryDataStore = new JDBCRecoveryDataStore();

    private JdbcTemplate jdbcTemplate;

    final String USER_UNIQUE_ID = "user_unique_id";
    final String CODE = "code";
    final String SCENARIO = "scenario";
    final String STEP = "step";
    final String TIME_CREATED = "time_created";
    final String REMAINING_SETS = "remaining_sets";

    private JDBCRecoveryDataStore() {

    }

    public static UserRecoveryDataStore getInstance() {
        return jdbcRecoveryDataStore;
    }

    @Override public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void store(UserRecoveryData recoveryDataDO) throws IdentityRecoveryException {
        final String STORE_RECOVERY_DATA =
                "INSERT INTO IDN_RECOVERY_DATA " + "(USER_UNIQUE_ID, CODE, SCENARIO,STEP, TIME_CREATED, REMAINING_SETS)"
                        + "VALUES (:" + USER_UNIQUE_ID + ";, :" + CODE + ";, :" + SCENARIO + ";, :" + STEP + ";, :"
                        + TIME_CREATED + ";, :" + REMAINING_SETS + ";)";

        try {
            jdbcTemplate.executeInsert(STORE_RECOVERY_DATA, (namedPreparedStatement) -> {
                namedPreparedStatement.setString(USER_UNIQUE_ID, recoveryDataDO.getUser().getUniqueUserId());
                namedPreparedStatement.setString(CODE, recoveryDataDO.getSecret());
                namedPreparedStatement.setString(SCENARIO, String.valueOf(recoveryDataDO.getRecoveryScenario()));
                namedPreparedStatement.setString(STEP, String.valueOf(recoveryDataDO.getRecoveryStep()));
                namedPreparedStatement.setTimeStamp(TIME_CREATED, new Timestamp(new Date().getTime()));
                namedPreparedStatement.setString(REMAINING_SETS, recoveryDataDO.getRemainingSetIds());
            }, recoveryDataDO, false);
        } catch (DataAccessException e) {
            //todo need to handle exception
            e.printStackTrace();
        }
    }

    @Override
    public UserRecoveryData load(User user, Enum recoveryScenario, Enum recoveryStep, String code)
            throws IdentityRecoveryException {
        final String LOAD_RECOVERY_DATA =
                "SELECT " + "* FROM IDN_RECOVERY_DATA WHERE USER_UNIQUE_ID = :" + USER_UNIQUE_ID + "; AND CODE = :" +
                        CODE + "; " + "AND " + "SCENARIO = :" + SCENARIO + "; AND STEP = :" + STEP + ";";
        UserRecoveryData userRecoveryDataObject = null;

        try {
            userRecoveryDataObject = jdbcTemplate.fetchSingleRecord(LOAD_RECOVERY_DATA, (resultSet, rowNumber) -> {
                UserRecoveryData userRecoveryData = new UserRecoveryData(user, code, recoveryScenario, recoveryStep);
                if (StringUtils.isNotBlank(resultSet.getString("REMAINING_SETS"))) {
                    userRecoveryData.setRemainingSetIds(resultSet.getString("REMAINING_SETS"));
                }
                //TODO need to read from the configuration file
                //                            Timestamp timeCreated = resultSet.getTimestamp("TIME_CREATED");
                //                            long createdTimeStamp = timeCreated.getTime();
                //                            int notificationExpiryTimeInMinutes = Integer.parseInt(Utils.getRecoveryConfigs(IdentityRecoveryConstants
                //                                    .ConnectorConfig.EXPIRY_TIME, user.getTenantDomain())); //Notification expiry time in minutes
                //                            long expiryTime = createdTimeStamp + notificationExpiryTimeInMinutes * 60 * 1000L;
                //
                //                            if (System.currentTimeMillis() > expiryTime) {
                //                                throw Utils.handleClientException(IdentityRecoveryConstants.ErrorMessages
                //                                        .ERROR_CODE_EXPIRED_CODE, code);
                //                            }
                return userRecoveryData;
            }, namedPreparedStatement -> {
                namedPreparedStatement.setString(USER_UNIQUE_ID, user.getUniqueUserId());
                namedPreparedStatement.setString(CODE, code);
                namedPreparedStatement.setString(SCENARIO, String.valueOf(recoveryScenario));
                namedPreparedStatement.setString(STEP, String.valueOf(recoveryStep));
            });
        } catch (DataAccessException e) {
            //todo need to handle exception
            e.printStackTrace();
        }
        return userRecoveryDataObject;
    }

    @Override
    public UserRecoveryData load(String code) throws IdentityRecoveryException {
        final String LOAD_RECOVERY_DATA_FROM_CODE = "SELECT * FROM IDN_RECOVERY_DATA WHERE CODE = :" + CODE + ";";
        UserRecoveryData userRecoveryDataObject = null;

        try {
            jdbcTemplate.fetchSingleRecord(LOAD_RECOVERY_DATA_FROM_CODE, (resultSet, rowNumber) -> {

                User user = new User.UserBuilder().setUserId(resultSet.getString("USER_UNIQUE_ID")).build();

                String recoveryScenario = resultSet.getString("SCENARIO");
                String recoveryStep = resultSet.getString("STEP");

                UserRecoveryData userRecoveryData = new UserRecoveryData(user, code,
                        RecoveryScenarios.valueOf(recoveryScenario), RecoverySteps.valueOf(recoveryStep));

                if (StringUtils.isNotBlank(resultSet.getString("REMAINING_SETS"))) {
                    userRecoveryData.setRemainingSetIds(resultSet.getString("REMAINING_SETS"));
                }
                //TODO need to read from the configuration file
                //                Timestamp timeCreated = resultSet.getTimestamp("TIME_CREATED");
                //                long createdTimeStamp = timeCreated.getTime();
                //                int notificationExpiryTimeInMinutes = Integer.parseInt(Utils.getRecoveryConfigs(IdentityRecoveryConstants
                //                        .ConnectorConfig.EXPIRY_TIME, user.getTenantDomain())); //Notification expiry time in minutes
                //                long expiryTime = createdTimeStamp + notificationExpiryTimeInMinutes * 60 * 1000L;

                //                if (System.currentTimeMillis() > expiryTime) {
                //                    throw Utils.handleClientException(IdentityRecoveryConstants.ErrorMessages
                //                            .ERROR_CODE_EXPIRED_CODE, code);
                //                }

                return userRecoveryData;

            }, namedPreparedStatement -> {
                namedPreparedStatement.setString(CODE, code);
            });
        } catch (DataAccessException e) {
            //todo need handle ex
            e.printStackTrace();
        }

        return userRecoveryDataObject;
    }

    @Override
    public void invalidate(String code) throws IdentityRecoveryException {
        final String INVALIDATE_CODE = "DELETE FROM IDN_RECOVERY_DATA WHERE CODE = :" + CODE + ";";
        try {
            jdbcTemplate.executeUpdate(INVALIDATE_CODE, namedPreparedStatement -> {
                namedPreparedStatement.setString(CODE, code);
            });
        } catch (DataAccessException e) {
            //todo need handle exception
            e.printStackTrace();
        }
    }

    @Override
    public UserRecoveryData load(User user) throws IdentityRecoveryException {
        final String LOAD_RECOVERY_DATA_OF_USER =
                "SELECT " + "* FROM IDN_RECOVERY_DATA WHERE USER_UNIQUE_ID = :" + USER_UNIQUE_ID + ";";
        UserRecoveryData userRecoveryDataObject = null;

        try {
            userRecoveryDataObject = jdbcTemplate
                    .fetchSingleRecord(LOAD_RECOVERY_DATA_OF_USER, (resultSet, rowNumber) -> {
                        UserRecoveryData userRecoveryData = new UserRecoveryData(user, resultSet.getString("CODE"),
                                RecoveryScenarios.valueOf(resultSet.getString("SCENARIO")),
                                RecoverySteps.valueOf(resultSet.getString("STEP")));
                        if (StringUtils.isNotBlank(resultSet.getString("REMAINING_SETS"))) {
                            userRecoveryData.setRemainingSetIds(resultSet.getString("REMAINING_SETS"));
                        }

                        return userRecoveryData;

                    }, namedPreparedStatement -> {
                        namedPreparedStatement.setString(USER_UNIQUE_ID, user.getUniqueUserId());
                    });
        } catch (DataAccessException e) {
            //todo need to handle ex
            e.printStackTrace();
        }

        return userRecoveryDataObject;
    }

    @Override
    public void invalidate(User user) throws IdentityRecoveryException {
        final String INVALIDATE_USER_CODES = "DELETE FROM IDN_RECOVERY_DATA WHERE USER_UNIQUE_ID = :" +
                USER_UNIQUE_ID + ";";
        try {
            jdbcTemplate.executeUpdate(INVALIDATE_USER_CODES, namedPreparedStatement -> {
                namedPreparedStatement.setString(USER_UNIQUE_ID, user.getUniqueUserId());
            });
        } catch (DataAccessException e) {
            //todo need to handle exception
            e.printStackTrace();
        }
    }
}

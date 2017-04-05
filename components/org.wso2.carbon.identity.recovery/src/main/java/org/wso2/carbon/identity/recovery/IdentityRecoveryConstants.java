/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.recovery;

/**
 * Identity management related constants.
 */
public class IdentityRecoveryConstants {


    public static final String IDENTITY_MANAGEMENT_PATH = "/identity";
    public static final String IDENTITY_MANAGEMENT_QUESTIONS = IDENTITY_MANAGEMENT_PATH + "/questionCollection";
    public static final String IDENTITY_MANAGEMENT_I18N_PATH = "/repository/components/identity";
    public static final String IDENTITY_I18N_QUESTIONS =
            IDENTITY_MANAGEMENT_I18N_PATH + "/questionCollection";
    public static final String LINE_SEPARATOR = "!";
    public static final String CHALLENGE_QUESTION_URI = "http://wso2.org/claims/challengeQuestionUris";
    public static final String NOTIFICATION_TYPE_PASSWORD_RESET = "passwordReset";
    public static final String NOTIFICATION_TYPE_ADMIN_FORCED_PASSWORD_RESET = "adminforcedpasswordreset";
    public static final String NOTIFICATION_TYPE_ADMIN_FORCED_PASSWORD_RESET_WITH_OTP =
            "adminforcedpasswordresetwithotp";
    public static final String NOTIFICATION_TYPE_ACCOUNT_CONFIRM = "accountConfirmation";
    public static final String NOTIFICATION_TYPE_RESEND_ACCOUNT_CONFIRM = "resendAccountConfirmation";
    public static final String NOTIFICATION_TYPE_EMAIL_CONFIRM = "emailconfirm";
    public static final String NOTIFICATION_TYPE_ASK_PASSWORD = "askPassword";
    public static final String NOTIFICATION_TYPE_PASSWORD_RESET_SUCCESS = "passwordresetsucess";
    public static final String NOTIFICATION_TYPE_PASSWORD_RESET_INITIATE = "initiateRecovery";
    public static final String NOTIFICATION_ACCOUNT_ID_RECOVERY = "accountIdRecovery";
    public static final String RECOVERY_STATUS_INCOMPLETE = "INCOMPLETE";
    public static final String RECOVERY_STATUS_COMPLETE = "COMPLETE";
    public static final String TEMPLATE_TYPE = "TEMPLATE_TYPE";
    public static final String CONFIRMATION_CODE = "confirmation-code";
    public static final String WSO2CARBON_CLAIM_DIALECT = "http://wso2.org/claims";
    public static final String ACCOUNT_LOCKED_CLAIM = "http://wso2.org/claims/accountLocked";
    public static final String ACCOUNT_DISABLED_CLAIM = "http://wso2.org/claims/accountDisabled";
    public static final String VERIFY_EMAIL_CLIAM = "http://wso2.org/claims/verifyEmail";
    public static final String EMAIL_VERIFIED_CLAIM = "http://wso2.org/claims/emailVerified";
    public static final String ASK_PASSWORD_CLAIM = "http://wso2.org/claims/askPassword";
    public static final String ADMIN_FORCED_PASSWORD_RESET_CLAIM =
            "http://wso2.org/claims/adminForcedPasswordReset";
    public static final String OTP_PASSWORD_CLAIM = "http://wso2.org/claims/oneTimePassword";
    public static final String DEFAULT_CHALLENGE_QUESTION_SEPARATOR = "!";
    public static final String SELF_SIGN_UP_EVENT = "SELF_SIGN_UP_EVENT";

    public static final String PASSWORD_RESET_FAIL_ATTEMPTS_CLAIM = "http://wso2" +
            ".org/claims/identity/failedPasswordRecoveryAttempts";
    public static final String SIGN_UP_ROLE_SEPARATOR = ",";
    public static final String SELF_SIGN_UP_PROPERTIES = "SELF_SIGN_UP_PROPERTIES";


    public static final String LOCALE_EN_US = "en_US";
    public static final String LOCALE_LK_LK = "lk_lk";
    public static final String SELF_SIGNUP_ROLE = "Internal/selfsignup";
    public static final String EXECUTE_ACTION = "ui.execute";
    public static final String CHALLAENGE_QUESTION_FOLDER_LOCATION = "/conf/identity/challenge-questions";
    public static final String RECOVERY_CONFIG_LOCATION = "/conf/identity/identity-management.yaml";
    public static final String CARBON_HOME = "carbon.home";

    private IdentityRecoveryConstants() {
    }

    /**
     * Error Messages.
     */
    public enum ErrorCodes {

        INVALID_CODE("18001", "Invalid Code '%s.'"),
        EXPIRED_CODE("18002", "Expired Code '%s.'"),
        INVALID_USER("18003", "Invalid User '%s.'"),
        UNEXPECTED("18013", "Unexpected error"),
        RECOVERY_NOTIFICATION_FAILURE("18015", "Error sending recovery notification"),
        INVALID_TENANT("18016", "Invalid tenant'%s.'"),
        CHALLENGE_QUESTION_NOT_FOUND("18017", "No challenge question found"),
        INVALID_CREDENTIALS("17002", "Invalid Credentials"),
        LOCKED_ACCOUNT("17003", "User account is locked - '%s.'"),
        DISABLED_ACCOUNT("17004", "user account is disabled '%s.'"),
        GET_CHALLENGE_QUESTIONS("20001", "Registry exception while getting challenge question"),
        SET_CHALLENGE_QUESTIONS("20002", "Registry exception while setting challenge question"),
        GET_CHALLENGE_URIS("20003", "Error while getting challenge question URIs '%s.'"),
        GET_USER_CHALLENGE_QUESTIONS("20004", "Error while getting challenge questions '%s.'"),
        GET_USER_CHALLENGE_QUESTION("20005", "Error while getting challenge question '%s.'"),
        SET_USER_CHALLENGE_QUESTION("20006", "Error setting challenge quesitons of user '%s.'"),
        NO_HASHING_ALGO("20007", "Error while hashing the security answer"),
        INVALID_ANSWER_FOR_SECURITY_QUESTION("20008", "Invalid answer"),
        STORING_RECOVERY_DATA("20009", "Invalid answer for security question"),
        NEED_TO_ANSWER_MORE_SECURITY_QUESTION("20010", "Need to answer more security questions"),
        TRIGGER_NOTIFICATION("20011", "Error while trigger notification for user '%s.'"),
        NEED_TO_ANSWER_TO_REQUESTED_QUESTIONS("20012", "Need to answer to all requested security questions"),
        NO_VALID_USERNAME("20013", "No Valid username found for recovery"),
        NO_FIELD_FOUND_FOR_USER_RECOVERY("20014", "No fileds found for username recovery"),
        NO_USER_FOUND_FOR_RECOVERY("20015", "No valid user found"),
        ISSUE_IN_LOADING_RECOVERY_CONFIGS("20016", "Error loading recovery configs"),
        NOTIFICATION_BASED_PASSWORD_RECOVERY_NOT_ENABLE("20017", "Notification based password recovery is not enabled"),
        QUESTION_BASED_RECOVERY_NOT_ENABLE("20018", "Security questions based recovery is not enabled"),
        DD_SELF_USER("20019", "Error while adding self signup user"),
        LOCK_USER_USER("20020", "Error while lock user"),
        DISABLE_SELF_SIGN_UP("20021", "Self sign up feature is disabled."),
        LOCK_USER_ACCOUNT("20022", "Error while lock user account"),
        UNLOCK_USER("20023", "Error while unlock user account."),
        OLD_CODE_NOT_FOUND("20024", "Given confirmation code cannot be not found."),
        FAILED_TO_LOAD_REALM_SERVICE("20025", "Failed to retrieve user realm from tenant id : %s"),
        FAILED_TO_LOAD_USER_STORE_MANAGER("20026", "Failed to retrieve user store manager."),
        FAILED_TO_LOAD_USER_CLAIMS("20027", "Error occurred while retrieving user claims."),
        FAILED_TO_LOAD_GOV_CONFIGS("20028", "Error occurred while retrieving account lock connector configuration"),
        HISTORY_VIOLATE("22001", "This password has been used in recent history. Please choose a different password"),
        MULTIPLE_QUESTION_NOT_ALLOWED("20029", "Multiple challenge question not allowed for this operation"),
        USER_ALREADY_EXISTS("20030", "User %s already exists in the system. Please use a different username."),
        USERNAME_RECOVERY_NOT_ENABLE("20031", "Username recovery is not enabled"),
        MULTIPLE_USERS_MATCHING("20032", "Multiple users found"),
        ISSUE_IN_LOADING_SIGNUP_CONFIGS("20033", "Error loading sign-up configs."),
        FAILED_TO_UPDATE_USER_CLAIMS("20034", "Error occurred while updating user claims for user ID: %s."),
        POLICY_VIOLATION("20035", "Password Policy Violate"),
        NEED_TO_ANSWER_TO_ASKED_SECURITY_QUESTION("20036", "Need to answer to asked security question"),
        SELF_SIGN_UP_STORE_ERROR("20037", "Identity store error occurred while user sign-up."),
        FAILED_USER_SEARCH("20038", "Error occurred while searching for the user."),
        NON_UNIQUE_USER("20039", "Multiple users exist with the given user name: %s."),
        IDENTITY_STORE_ERROR("20040", "Error while obtaining the identity store."),
        MISSING_EVENT_PROPERTY("20041", "The event property %s is not available in the event properties."),
        FAILED_LIFECYCLE_EVENT("20042", "Error occurred while executing lifecycle event for user ID: %s."),
        USER_NOT_FOUND("20043", "A user cannot be found for the given ID: %s"),
        FAILED_USER_STATE_UPDATE("20045", "Error occurred while updating user life cycle state."),
        FAILED_ACCOUNT_LOCK("20046", "Error occurred during account lock for user: %s"),
        FAILED_SSU_GROUP_SEARCH("20047", "Error occurred while searching for self sig-up role: %s."),
        FAILED_SSU_GROUP_ADD("20048", "Error occurred while creating self sign-up group %s."),
        ACCOUNT_UNVERIFIED("20049", "User account is unverified for user ID: %s."),
        INVALID_USER_ID("20050", "Invalid unique user id: %s."),
        INVALID_USER_ID_FORMAT("20051", "Invalid user ID for mat for ID : %s. User ID should be in the form of " +
                                        "<domain ID>.<UUID>."),
        USER_REGISTRATION_INFORMATION_NOT_FOUND("20052", "Cannot find user registration information in the request.");


        private final String code;
        private final String message;

        ErrorCodes(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return code + " - " + message;
        }

    }

    /**
     * Connector Config.
     */
    public static class ConnectorConfig {
        public static final String NOTIFICATION_INTERNALLY_MANAGE = "Recovery.Notification.InternallyManage";
        public static final String NOTIFICATION_SEND_RECOVERY_NOTIFICATION_SUCCESS = "Recovery.NotifySuccess";
        public static final String NOTIFICATION_SEND_RECOVERY_SECURITY_START = "Recovery.Question.Password.NotifyStart";
        public static final String NOTIFICATION_BASED_PW_RECOVERY = "Recovery.Notification.Password.Enable";
        public static final String QUESTION_BASED_PW_RECOVERY = "Recovery.Question.Password.Enable";
        public static final String USERNAME_RECOVERY_ENABLE = "Recovery.Notification.Username.Enable";
        public static final String QUESTION_CHALLENGE_SEPARATOR = "Recovery.Question.Password.Separator";
        public static final String QUESTION_MIN_NO_ANSWER = "Recovery.Question.Password.MinAnswers";
        public static final String EXPIRY_TIME = "Recovery.ExpiryTime";
        public static final String RECOVERY_QUESTION_PASSWORD_RECAPTCHA_ENABLE = "Recovery.Question.Password" +
                ".ReCaptcha.Enable";
        public static final String RECOVERY_QUESTION_PASSWORD_RECAPTCHA_MAX_FAILED_ATTEMPTS = "Recovery.Question" +
                ".Password.ReCaptcha.MaxFailedAttempts";
        public static final String ENABLE_SELF_SIGNUP = "SelfRegistration.Enable";
        public static final String ACCOUNT_LOCK_ON_CREATION = "SelfRegistration.LockOnCreation";
        public static final String SIGN_UP_NOTIFICATION_INTERNALLY_MANAGE = "SelfRegistration.Notification" +
                ".InternallyManage";
        public static final String SELF_REGISTRATION_RE_CAPTCHA = "SelfRegistration.ReCaptcha";

        public static final String ENABLE_EMIL_VERIFICATION = "EmailVerification.Enable";
        public static final String EMAIL_ACCOUNT_LOCK_ON_CREATION = "EmailVerification.LockOnCreation";
        public static final String EMAIL_VERIFICATION_NOTIFICATION_INTERNALLY_MANAGE =
                "EmailVerification.Notification.InternallyManage";

        public static final String ENABLE_ADMIN_PASSWORD_RESET_OFFLINE = "Recovery.AdminPasswordReset.Offline";
        public static final String ENABLE_ADMIN_PASSWORD_RESET_WITH_OTP = "Recovery.AdminPasswordReset.OTP";
        public static final String ENABLE_ADMIN_PASSWORD_RESET_WITH_RECOVERY_LINK =
                "Recovery.AdminPasswordReset.RecoveryLink";
    }

    /**
     * SQL Queries.
     */
    public static class SQLQueries {

        public static final String STORE_RECOVERY_DATA = "INSERT INTO IDN_RECOVERY_DATA "
                + "(USER_NAME, USER_DOMAIN, TENANT_ID, CODE, SCENARIO,STEP, TIME_CREATED, REMAINING_SETS)"
                + "VALUES (?,?,?,?,?,?,?,?)";
        public static final String LOAD_RECOVERY_DATA = "SELECT "
                + "* FROM IDN_RECOVERY_DATA WHERE USER_NAME = ? AND USER_DOMAIN = ? AND TENANT_ID = ? AND CODE = ? " +
                "AND " + "SCENARIO = ? AND STEP = ?";

        public static final String LOAD_RECOVERY_DATA_CASE_INSENSITIVE = "SELECT * FROM IDN_RECOVERY_DATA WHERE" +
                " LOWER(USER_NAME)=LOWER(?) AND USER_DOMAIN = ? AND TENANT_ID = ? AND CODE= ? AND SCENARIO = ? AND " +
                "STEP = ?";

        public static final String LOAD_RECOVERY_DATA_FROM_CODE = "SELECT * FROM IDN_RECOVERY_DATA WHERE CODE = ?";


        public static final String INVALIDATE_CODE = "DELETE FROM IDN_RECOVERY_DATA WHERE CODE = ?";

        public static final String INVALIDATE_USER_CODES = "DELETE FROM IDN_RECOVERY_DATA WHERE USER_NAME = ? AND " +
                "USER_DOMAIN = ? AND TENANT_ID =?";

        public static final String INVALIDATE_USER_CODES_CASE_INSENSITIVE = "DELETE FROM IDN_RECOVERY_DATA WHERE " +
                "LOWER(USER_NAME)=LOWER(?) AND USER_DOMAIN = ? AND TENANT_ID =?";

        public static final String LOAD_RECOVERY_DATA_OF_USER = "SELECT "
                + "* FROM IDN_RECOVERY_DATA WHERE USER_NAME = ? AND USER_DOMAIN = ? AND TENANT_ID = ?";

        public static final String LOAD_RECOVERY_DATA_OF_USER_CASE_INSENSITIVE = "SELECT "
                + "* FROM IDN_RECOVERY_DATA WHERE LOWER(USER_NAME)=LOWER(?) AND USER_DOMAIN = ? AND TENANT_ID = ?";

    }

    /**
     * Questions.
     */
    public static class Questions {

        public static final String LOCALE_CLAIM = "http://wso2.org/claims/locality";
        public static final String BLACKLIST_REGEX = ".*[/\\\\].*";

        public static final String CHALLENGE_QUESTION_SET_ID = "questionSetId";
        public static final String CHALLENGE_QUESTION_ID = "questionId";
        public static final String CHALLENGE_QUESTION_LOCALE = "locale";

        // TODO remove this
        private static String[] secretQuestionsSet01 = new String[]{"City where you were born ?",
                "Father's middle name ?", "Favorite food ?", "Favorite vacation location ?"};

        // TODO remove this
        private static String[] secretQuestionsSet02 = new String[]{"Model of your first car ?",
                "Name of the hospital where you were born ?", "Name of your first pet ?", "Favorite sport ?"};

        public static String[] getSecretQuestionsSet01() {
            return secretQuestionsSet01.clone();
        }

        public static String[] getSecretQuestionsSet02() {
            return secretQuestionsSet02.clone();
        }
    }
}

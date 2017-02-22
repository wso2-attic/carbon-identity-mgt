/*
 *
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.recovery.password;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.common.base.event.EventContext;
import org.wso2.carbon.identity.common.base.event.model.Event;
import org.wso2.carbon.identity.common.base.exception.IdentityException;
import org.wso2.carbon.identity.event.EventConstants;
import org.wso2.carbon.identity.mgt.User;
import org.wso2.carbon.identity.recovery.ChallengeQuestionManager;
import org.wso2.carbon.identity.recovery.IdentityRecoveryClientException;
import org.wso2.carbon.identity.recovery.IdentityRecoveryConstants;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.RecoveryScenarios;
import org.wso2.carbon.identity.recovery.RecoverySteps;
import org.wso2.carbon.identity.recovery.bean.ChallengeQuestionsResponse;
import org.wso2.carbon.identity.recovery.internal.IdentityRecoveryServiceDataHolder;
import org.wso2.carbon.identity.recovery.mapping.SecurityQuestionsConfig;
import org.wso2.carbon.identity.recovery.model.ChallengeQuestion;
import org.wso2.carbon.identity.recovery.model.UserChallengeAnswer;
import org.wso2.carbon.identity.recovery.model.UserRecoveryData;
import org.wso2.carbon.identity.recovery.store.UserRecoveryDataStore;
import org.wso2.carbon.identity.recovery.util.Utils;
import org.wso2.carbon.kernel.utils.LambdaExceptionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;


import java.util.stream.Collectors;

/**
 * Security Question Password Recovery Manager
 */
public class SecurityQuestionPasswordRecoveryManager {

    private static final Logger log = LoggerFactory.getLogger(SecurityQuestionPasswordRecoveryManager.class);

//    private static final String PROPERTY_ACCOUNT_LOCK_ON_FAILURE = "account.lock.handler.enable";
//
//    private static final String PROPERTY_ACCOUNT_LOCK_ON_FAILURE_MAX = "account.lock.handler.On.Failure.Max.Attempts";


//    private static SecurityQuestionPasswordRecoveryManager instance = new SecurityQuestionPasswordRecoveryManager();

    private static SecurityQuestionsConfig securityQuestionsConfig = new SecurityQuestionsConfig();
    ChallengeQuestionManager challengeQuestionManager;
    UserRecoveryDataStore userRecoveryDataStore;

    public SecurityQuestionPasswordRecoveryManager(UserRecoveryDataStore userRecoveryDataStore,
                                                   ChallengeQuestionManager challengeQuestionManager) {
        this.userRecoveryDataStore = userRecoveryDataStore;
        this.challengeQuestionManager = challengeQuestionManager;
    }

//    public static SecurityQuestionPasswordRecoveryManager getInstance() {
//        securityQuestionsConfig = new SecurityQuestionsConfig();
//        return instance;
//    }

    /**
     * To initiate challenge question based password recovery, answer questions one by one
     *
     * @param user User object
     * @return ChallengeQuestionsResponse, with security question to be asked and recovery code
     * @throws IdentityRecoveryException
     */
    public ChallengeQuestionsResponse initiateUserChallengeQuestion(User user) throws IdentityRecoveryException {

        String uniqueUserId = user.getUniqueUserId();
        userRecoveryDataStore.invalidateByUserUniqueId(uniqueUserId);

        String challengeQuestionSeparator = securityQuestionsConfig.getQuestionSeparator();

        // check account disable/lock
        handleAccountState(uniqueUserId);

        //TODO notification sending
        handleNotification(uniqueUserId);

        int minNoOfQuestionsToAnswer = securityQuestionsConfig.getMinAnswers(); //TODO get from config bean

        List<String> ids = challengeQuestionManager.getUserChallengeQuestionIds(user);
        //TODO change to list

        if (ids.isEmpty()) {
            return new ChallengeQuestionsResponse(Collections.EMPTY_LIST);
        }

        //when user has more than required number of security questions answered
        if (ids.size() > minNoOfQuestionsToAnswer) {
            ids = getRandomQuestionIds(ids, minNoOfQuestionsToAnswer);
        }

        //generate selected list of security question
        String metaData = String.join(challengeQuestionSeparator, ids);

        //get first question
        ChallengeQuestion userChallengeQuestion = challengeQuestionManager.getUserChallengeQuestion(uniqueUserId,
                ids.get(0));
        List<ChallengeQuestion> questions = new ArrayList<>();
        questions.add(userChallengeQuestion);
        ChallengeQuestionsResponse challengeQuestionsResponse = new ChallengeQuestionsResponse(questions);

        //get recovery code
        String secretKey = UUID.randomUUID().toString();
        challengeQuestionsResponse.setCode(secretKey);

        //construct and store user recovery data
        UserRecoveryData recoveryData = new UserRecoveryData(uniqueUserId, secretKey, RecoveryScenarios
                .QUESTION_BASED_PW_RECOVERY, RecoverySteps.VALIDATE_CHALLENGE_QUESTION);
        recoveryData.setRemainingSetIds(metaData);
        userRecoveryDataStore.store(recoveryData);

        challengeQuestionsResponse.setStatus(IdentityRecoveryConstants.RECOVERY_STATUS_INCOMPLETE);

        return challengeQuestionsResponse;
    }


    /**
     * To initiate challenge question based password recovery, answer questions at once
     *
     * @param user User object
     * @return ChallengeQuestionsResponse, with security questions to be asked and recovery code
     * @throws IdentityRecoveryException
     */
    public ChallengeQuestionsResponse initiateUserChallengeQuestionAtOnce(User user) throws IdentityRecoveryException {
        String challengeQuestionSeparator = securityQuestionsConfig.getQuestionSeparator();
        String uniqueUserID = user.getUniqueUserId();

        userRecoveryDataStore.invalidateByUserUniqueId(uniqueUserID);

        //check account disable/lock
        handleAccountState(uniqueUserID);

        //TODO notification sending
        handleNotification(uniqueUserID);

        int minNoOfQuestionsToAnswer = securityQuestionsConfig.getMinAnswers();


        List<String> ids = challengeQuestionManager.getUserChallengeQuestionIds(user);

        if (ids.isEmpty()) {
            //When no security questions are answered by the user
            return new ChallengeQuestionsResponse(new ArrayList<>());
        }

        //when user has more than required number of security questions answered
        if (ids.size() > minNoOfQuestionsToAnswer) {
            ids = getRandomQuestionIds(ids, minNoOfQuestionsToAnswer);
        }

        List<ChallengeQuestion> randomQuestions;

        //select random set of security questions to be answered
        String allChallengeQuestions = String.join(challengeQuestionSeparator, ids);

        randomQuestions = ids.stream().map(LambdaExceptionUtils.rethrowFunction(id -> challengeQuestionManager
                .getUserChallengeQuestion(user
                        .getUniqueUserId(), id))).collect(Collectors.toList());

        ChallengeQuestionsResponse challengeQuestionResponse = new ChallengeQuestionsResponse(randomQuestions);

        //recovery code
        String secretKey = UUID.randomUUID().toString();
        challengeQuestionResponse.setCode(secretKey);

        //construct and store user recovery data
        UserRecoveryData recoveryData = new UserRecoveryData(uniqueUserID, secretKey, RecoveryScenarios
                .QUESTION_BASED_PW_RECOVERY, RecoverySteps.VALIDATE_ALL_CHALLENGE_QUESTIONS);
        recoveryData.setRemainingSetIds(allChallengeQuestions);
        userRecoveryDataStore.store(recoveryData);

        return challengeQuestionResponse;
    }

    private void handleAccountState(String uniqueUserId) throws IdentityRecoveryException {
        if (Utils.isAccountDisabled(uniqueUserId)) {
            throw Utils.handleClientException(
                    IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_DISABLED_ACCOUNT, null);
        } else if (Utils.isAccountLocked(uniqueUserId)) {
            throw Utils.handleClientException(
                    IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_LOCKED_ACCOUNT, null);
        }
    }

    private void handleNotification(String uniqueUserID) {
        boolean isNotificationInternallyManaged = securityQuestionsConfig.isNotificationInternallyManaged();
        boolean isNotificationSendWhenInitiatingPWRecovery = securityQuestionsConfig.isNotifyWhenStartRecoveryFlow();

        if (isNotificationInternallyManaged && isNotificationSendWhenInitiatingPWRecovery) {
            try {
                triggerNotification(uniqueUserID, IdentityRecoveryConstants.NOTIFICATION_TYPE_PASSWORD_RESET_INITIATE,
                        null);
            } catch (Exception e) {
                log.warn("Error while sending password reset initiating notification to userID:" + uniqueUserID);
            }
        }
    }

    /**
     * Validate user answers for the security question(s) asked for recovery
     *
     * @param userChallengeAnswer List of UserChallengeAnswers
     * @param code                recovery code sent in previous step
     * @return ChallengeQuestionsResponse with recovery status,
     * previously asked question(s) will be sent again in error scenarios
     * if answer is valid, next question will be sent when answer questions one by one
     * @throws IdentityRecoveryException
     */
    public ChallengeQuestionsResponse validateUserChallengeQuestions(List<UserChallengeAnswer> userChallengeAnswer,
                                                                     String code) throws
            IdentityRecoveryException {

        UserRecoveryData userRecoveryData;

        List<ChallengeQuestion> questions = new ArrayList<>();
        ChallengeQuestionsResponse challengeQuestionResponse = new ChallengeQuestionsResponse(questions);

        String challengeQuestionSeparator = securityQuestionsConfig.getQuestionSeparator();

        //load recovery data using provided code
        //if return data from load, it means the code is validated. Otherwise it returns exceptions.
        try {
            userRecoveryData = userRecoveryDataStore.loadByCode(code);
        } catch (IdentityRecoveryException e) {
            log.error("Error while loading recovery data with code: " + code, e);
            String errorCode = !StringUtils.isEmpty(e.getErrorCode()) ? e.getErrorCode() : IdentityRecoveryConstants
                    .ErrorMessages.ERROR_CODE_UNEXPECTED.getCode();
            challengeQuestionResponse.setCode(code);
            challengeQuestionResponse.setStatus(errorCode);
            return challengeQuestionResponse;
        }

        String secretKey = userRecoveryData.getSecret();
        challengeQuestionResponse.setCode(secretKey);

        try {
            if (userChallengeAnswer == null) {
                throw Utils.handleClientException(IdentityRecoveryConstants.ErrorMessages
                        .ERROR_CODE_CHALLENGE_QUESTION_NOT_FOUND, null);
            }

            //if validate security questions one by one
            if (RecoverySteps.VALIDATE_CHALLENGE_QUESTION.equals(userRecoveryData.getRecoveryStep())) {

                if (userChallengeAnswer.size() > 1) {
                    throw Utils.handleClientException(IdentityRecoveryConstants.ErrorMessages
                            .ERROR_CODE_MULTIPLE_QUESTION_NOT_ALLOWED, null);
                }

                //match first question of remaining questions to be answered(asked question), with answered question
                //exception will be thrown if answered to other question
                String[] remainingQuestionIds = userRecoveryData.getRemainingSetIds().split(challengeQuestionSeparator);
                questions.add(getValidatedQuestion(remainingQuestionIds[0], userChallengeAnswer.get(0),
                        userRecoveryData.getUserUniqueId(), challengeQuestionManager));

                challengeQuestionResponse.setQuestions(questions);

                //verify users answer
                boolean verified = challengeQuestionManager.verifyUserChallengeAnswer(userRecoveryData
                        .getUserUniqueId(), userChallengeAnswer.get(0));

                if (verified) {
                    userRecoveryDataStore.invalidateByCode(code);
                    secretKey = UUID.randomUUID().toString();
                    challengeQuestionResponse.setCode(secretKey);

                    //construct new user recovery data
                    UserRecoveryData recoveryData = new UserRecoveryData(userRecoveryData.getUserUniqueId(),
                            secretKey, RecoveryScenarios.QUESTION_BASED_PW_RECOVERY);

                    String remainingSetIds;
                    List<String> ids = new ArrayList<>(Arrays.asList(remainingQuestionIds));

                    //if there are more questions to be answered
                    if (ids.size() > 1) {
                        //update remaining questions list
                        ids.remove(0);
                        remainingSetIds = String.join(challengeQuestionSeparator, ids);
                        //next question to be asked
                        ChallengeQuestion challengeQuestion = challengeQuestionManager.getUserChallengeQuestion
                                (userRecoveryData.getUserUniqueId(), remainingQuestionIds[1]);
                        questions.remove(0);
                        questions.add(challengeQuestion);
                        challengeQuestionResponse.setQuestions(questions);

                        //update user recovery data
                        recoveryData.setRecoveryStep(RecoverySteps.VALIDATE_CHALLENGE_QUESTION);
                        challengeQuestionResponse.setStatus(IdentityRecoveryConstants.RECOVERY_STATUS_INCOMPLETE);
                        recoveryData.setRemainingSetIds(remainingSetIds);
                    } else {
                        questions.remove(0);
                        recoveryData.setRemainingSetIds("");
                        recoveryData.setRecoveryStep(RecoverySteps.UPDATE_PASSWORD);
                        challengeQuestionResponse.setStatus(IdentityRecoveryConstants.RECOVERY_STATUS_COMPLETE);
                    }

                    //store user recovery data
                    userRecoveryDataStore.store(recoveryData);

                    //TODO Reset password recovery failed attempts
                    //resetRecoveryPasswordFailedAttempts(userRecoveryData.getUser());

                    return challengeQuestionResponse;
                } else {
                    //TODO handle recovery failed attempts
                    //handleAnswerVerificationFail(userRecoveryData.getUser());
                    challengeQuestionResponse.setStatus(IdentityRecoveryConstants.ErrorMessages
                            .ERROR_CODE_INVALID_ANSWER_FOR_SECURITY_QUESTION.getCode());
                    return challengeQuestionResponse;
                }

            } else if (RecoverySteps.VALIDATE_ALL_CHALLENGE_QUESTIONS.equals(userRecoveryData.getRecoveryStep())) {
                //validate all the questions at once

                String allChallengeQuestions = userRecoveryData.getRemainingSetIds();

                //validate asked questions with answered questions
                if (StringUtils.isNotBlank(allChallengeQuestions)) {
                    String[] requestedQuestions = allChallengeQuestions.split(challengeQuestionSeparator);

                    if (requestedQuestions.length != userChallengeAnswer.size()) {
                        throw Utils.handleClientException(IdentityRecoveryConstants.ErrorMessages
                                .ERROR_CODE_NEED_TO_ANSWER_TO_REQUESTED_QUESTIONS, null);
                    }

                    questions = getValidatedQuestions(requestedQuestions, userChallengeAnswer,
                            userRecoveryData.getUserUniqueId(), challengeQuestionManager);
                    challengeQuestionResponse.setQuestions(questions);
                    //Validate whether user answered all the requested questions

                } else {
                    throw Utils.handleClientException(IdentityRecoveryConstants.ErrorMessages
                            .ERROR_CODE_CHALLENGE_QUESTION_NOT_FOUND, null);
                }
                //verify user answered for the questions
                for (int i = 0; i < userChallengeAnswer.size(); i++) {
                    boolean verified = challengeQuestionManager.verifyUserChallengeAnswer(userRecoveryData
                            .getUserUniqueId(), userChallengeAnswer.get(i));
                    if (!verified) {
                        //TODO
                        //handleAnswerVerificationFail(userRecoveryData.getUser());
                        challengeQuestionResponse.setStatus(IdentityRecoveryConstants.ErrorMessages
                                .ERROR_CODE_INVALID_ANSWER_FOR_SECURITY_QUESTION.getCode());
                        return challengeQuestionResponse;
                    }
                }

                //TODO Reset password recovery failed attempts
                //resetRecoveryPasswordFailedAttempts(userRecoveryData.getUser());

                userRecoveryDataStore.invalidateByCode(code);
                secretKey = UUID.randomUUID().toString();
                challengeQuestionResponse.setCode(secretKey);
                challengeQuestionResponse.setStatus(IdentityRecoveryConstants.RECOVERY_STATUS_COMPLETE);
                UserRecoveryData recoveryData = new UserRecoveryData(userRecoveryData.getUserUniqueId(), secretKey,
                        RecoveryScenarios.QUESTION_BASED_PW_RECOVERY);

                recoveryData.setRecoveryStep(RecoverySteps.UPDATE_PASSWORD);

                userRecoveryDataStore.store(recoveryData);

                return challengeQuestionResponse;
            } else {
                throw Utils.handleClientException(IdentityRecoveryConstants.ErrorMessages
                        .ERROR_CODE_INVALID_CODE, null);
            }
        } catch (IdentityRecoveryClientException e) {
            //handleAnswerVerificationFail(userRecoveryData.getUser());
            throw e;
        }
    }

//    private void getValidatedQuestion(String[] requestedQuestions, List<UserChallengeAnswer> userChallengeAnswers)
//            throws IdentityRecoveryException {
//        List<String> userChallengeIds = new ArrayList<>();
////        for (int i = 0; i < userChallengeAnswer.length; i++) {
////            userChallengeIds.add(userChallengeAnswer[i].getQuestion().getQuestionSetId().toLowerCase());
////        }
//        userChallengeIds.addAll(userChallengeAnswers.stream().map(answer -> answer.getQuestion().getQuestionSetId()
//                .toLowerCase()).collect(Collectors.toList()));
//
//        for (int i = 0; i < requestedQuestions.length; i++) {
//            if (!userChallengeIds.contains(StringUtils.lowerCase(requestedQuestions[i]))) {
//                throw Utils.handleClientException(IdentityRecoveryConstants.ErrorMessages
//                        .ERROR_CODE_NEED_TO_ANSWER_TO_REQUESTED_QUESTIONS, null);
//            }
//        }
//    }

    /**
     * Validate requested questions with answered questions
     *
     * @param requestedQuestions       list of questions asked, setIDs
     * @param userChallengeAnswers     list of questions answered
     * @param userUniqueID             unique ID of user
     * @param challengeQuestionManager ChallengeQuestionManager instance
     * @return List of asked questions
     * @throws IdentityRecoveryException
     */
    private List<ChallengeQuestion> getValidatedQuestions(String[] requestedQuestions,
                                                          List<UserChallengeAnswer> userChallengeAnswers,
                                                          String userUniqueID,
                                                          ChallengeQuestionManager challengeQuestionManager)
            throws IdentityRecoveryException {

        List<String> userChallengeIds = new ArrayList<>();
        List<ChallengeQuestion> questions = new ArrayList<>();

        userChallengeIds.addAll(userChallengeAnswers.stream().map(answer -> answer.getQuestion().getQuestionSetId()
                .toLowerCase()).collect(Collectors.toList()));

        for (int i = 0; i < requestedQuestions.length; i++) {
            //check whether answered question is available in asked question
            if (!userChallengeIds.contains(StringUtils.lowerCase(requestedQuestions[i]))) {
                throw Utils.handleClientException(IdentityRecoveryConstants.ErrorMessages
                        .ERROR_CODE_NEED_TO_ANSWER_TO_REQUESTED_QUESTIONS, null);
            } else {
                //if answered question is in asked questions
                String q = challengeQuestionManager.getUserChallengeQuestion(userUniqueID,
                        requestedQuestions[i]).getQuestion();
                ChallengeQuestion question = new ChallengeQuestion(requestedQuestions[i], q);
                questions.add(question);
            }
        }
        //list of asked questions
        return questions;
    }

    /**
     * Validate requested questions with answered question
     *
     * @param requestedQuestionSetId   asked question, setID
     * @param userChallengeAnswer      question answered
     * @param userUniqueID             unique ID of user
     * @param challengeQuestionManager ChallengeQuestionManager instance
     * @return asked question
     * @throws IdentityRecoveryException
     */
    private ChallengeQuestion getValidatedQuestion(String requestedQuestionSetId,
                                                   UserChallengeAnswer userChallengeAnswer, String userUniqueID,
                                                   ChallengeQuestionManager challengeQuestionManager)
            throws IdentityRecoveryException {
        if (!requestedQuestionSetId.equals(userChallengeAnswer.getQuestion().getQuestionSetId())) {
            throw Utils.handleClientException(IdentityRecoveryConstants.ErrorMessages
                    .ERROR_CODE_NEED_TO_ANSWER_TO_ASKED_SECURITY_QUESTION, null);
        }
        String question = challengeQuestionManager.getUserChallengeQuestion(userUniqueID,
                userChallengeAnswer.getQuestion().getQuestionSetId()).getQuestion();
        return new ChallengeQuestion(userChallengeAnswer.getQuestion().getQuestionSetId(), question);
    }

//    private void getValidatedQuestion(String[] requestedQuestions, UserChallengeAnswer[] userChallengeAnswer)
//            throws IdentityRecoveryException {
//        List<String> userChallengeIds = new ArrayList<>();
//        for (int i = 0; i < userChallengeAnswer.length; i++) {
//            userChallengeIds.add(userChallengeAnswer[i].getQuestion().getQuestionSetId().toLowerCase());
//        }
//
//        for (int i = 0; i < requestedQuestions.length; i++) {
//            if (!userChallengeIds.contains(requestedQuestions[i].toLowerCase())) {
//                throw Utils.handleClientException(IdentityRecoveryConstants.ErrorMessages
//                        .ERROR_CODE_NEED_TO_ANSWER_TO_REQUESTED_QUESTIONS, null);
//            }
//        }
//    }

    /**
     * Select random list from provided list
     *
     * @param challengeQuestions       all the questions user has answered
     * @param minNoOfQuestionsToAnswer number of questions to be selected
     * @return selected list of question setIDs
     */
    private List<String> getRandomQuestionIds(List<String> challengeQuestions, int minNoOfQuestionsToAnswer) {
        List<String> selectedQuestions = new ArrayList<>();
        List<String> remainingQuestions = new ArrayList<>(challengeQuestions);

        for (int i = 0; i < minNoOfQuestionsToAnswer; i++) {
            int random = new Random().nextInt(challengeQuestions.size());
            selectedQuestions.add(i, remainingQuestions.get(random));
            remainingQuestions.remove(random);
        }
        return selectedQuestions;
    }

//    private void triggerNotification(User user, String type, String code) throws IdentityRecoveryException {
//
//        String eventName = IdentityEventConstants.Event.TRIGGER_NOTIFICATION;
//
//        HashMap<String, Object> properties = new HashMap<>();
//        properties.put(IdentityEventConstants.EventProperty.USER_NAME, user.getUserName());
//        properties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, user.getTenantDomain());
//        properties.put(IdentityEventConstants.EventProperty.USER_STORE_DOMAIN, user.getUserStoreDomain());
//
//        if (StringUtils.isNotBlank(code)) {
//            properties.put(IdentityRecoveryConstants.CONFIRMATION_CODE, code);
//        }
//        properties.put(IdentityRecoveryConstants.TEMPLATE_TYPE, type);
//        Event identityMgtEvent = new Event(eventName, properties);
//        try {
//            IdentityRecoveryServiceDataHolder.getInstance().getIdentityEventService().handleEvent(identityMgtEvent);
//        } catch (IdentityEventException e) {
//            throw Utils.handleServerException(IdentityRecoveryConstants.ErrorMessages
//.ERROR_CODE_TRIGGER_NOTIFICATION, user
//                    .getUserName(), e);
//        }
//    }

    private void triggerNotification(String userUniqueId, String type, String code)
            throws IdentityRecoveryException {
        String eventName = EventConstants.Event.TRIGGER_NOTIFICATION;
        HashMap<String, Object> properties = new HashMap<>();
        properties.put(EventConstants.EventProperty.USER_UNIQUE_ID, userUniqueId);

        if (StringUtils.isNotBlank(code)) {
            properties.put(IdentityRecoveryConstants.CONFIRMATION_CODE, code);
        }

        properties.put(IdentityRecoveryConstants.TEMPLATE_TYPE, type);
        Event identityMgtEvent = new Event(eventName, properties);
        EventContext eventContext = new EventContext();

        try {
            IdentityRecoveryServiceDataHolder.getInstance().getIdentityEventService().pushEvent(identityMgtEvent,
                    eventContext);
        } catch (IdentityException e) {
            throw Utils.handleServerException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_TRIGGER_NOTIFICATION,
                    userUniqueId, e);
        }
    }

//    private Property[] getConnectorConfigs(String tenantDomain) throws IdentityRecoveryException {
//
//        Property[] connectorConfigs;
//        try {
//            connectorConfigs = IdentityRecoveryServiceDataHolder.getInstance()
//                    .getIdentityGovernanceService()
//                    .getConfiguration(
//                            new String[]{PROPERTY_ACCOUNT_LOCK_ON_FAILURE, PROPERTY_ACCOUNT_LOCK_ON_FAILURE_MAX},
//                            tenantDomain);
//        } catch (Exception e) {
//            throw Utils.handleServerException(IdentityRecoveryConstants.ErrorMessages
//                    .ERROR_CODE_FAILED_TO_LOAD_GOV_CONFIGS, null, e);
//        }
//        return connectorConfigs;
//    }

//    private void resetRecoveryPasswordFailedAttempts(User user) throws IdentityRecoveryException {

//        Property[] connectorConfigs = getConnectorConfigs(user.getTenantDomain());
//
//        for (Property connectorConfig : connectorConfigs) {
//            if ((PROPERTY_ACCOUNT_LOCK_ON_FAILURE.equals(connectorConfig.getName())) &&
//                    !Boolean.parseBoolean(connectorConfig.getValue())) {
//                return;
//            }
//        }
//
//        int tenantId = IdentityTenantUtil.getTenantId(user.getTenantDomain());
//
//        RealmService realmService = IdentityRecoveryServiceDataHolder.getInstance().getRealmService();
//        UserRealm userRealm;
//        try {
//            userRealm = (UserRealm) realmService.getTenantUserRealm(tenantId);
//        } catch (UserStoreException e) {
//            throw Utils.handleServerException(IdentityRecoveryConstants.ErrorMessages
//                    .ERROR_CODE_FAILED_TO_LOAD_REALM_SERVICE, user.getTenantDomain(), e);
//        }
//
//        org.wso2.carbon.user.core.UserStoreManager userStoreManager;
//        try {
//            userStoreManager = userRealm.getUserStoreManager();
//        } catch (UserStoreException e) {
//            throw Utils.handleServerException(IdentityRecoveryConstants.ErrorMessages
//                    .ERROR_CODE_FAILED_TO_LOAD_USER_STORE_MANAGER, null, e);
//        }
//
//        Map<String, String> updatedClaims = new HashMap<>();
//        updatedClaims.put(IdentityRecoveryConstants.PASSWORD_RESET_FAIL_ATTEMPTS_CLAIM, "0");
//        try {
//            userStoreManager.setUserClaimValues(IdentityUtil.addDomainToName(user.getUserName(),
//                    user.getUserStoreDomain()), updatedClaims, UserCoreConstants.DEFAULT_PROFILE);
//        } catch (org.wso2.carbon.user.core.UserStoreException e) {
//            throw Utils.handleServerException(IdentityRecoveryConstants.ErrorMessages
//                    .ERROR_CODE_FAILED_TO_UPDATE_USER_CLAIMS, null, e);
//        }
//    }

//    private void handleAnswerVerificationFail(User user) throws IdentityRecoveryException {

//        Property[] connectorConfigs = getConnectorConfigs(user.getTenantDomain());
//
//        int maxAttempts = 0;
//        for (Property connectorConfig : connectorConfigs) {
//            if ((PROPERTY_ACCOUNT_LOCK_ON_FAILURE.equals(connectorConfig.getName())) &&
//                    !Boolean.parseBoolean(connectorConfig.getValue())) {
//                return;
//            } else if (PROPERTY_ACCOUNT_LOCK_ON_FAILURE_MAX.equals(connectorConfig.getName())
//                    && NumberUtils.isNumber(connectorConfig.getValue())) {
//                maxAttempts = Integer.parseInt(connectorConfig.getValue());
//            }
//        }
//
//        int tenantId = IdentityTenantUtil.getTenantId(user.getTenantDomain());
//
//        RealmService realmService = IdentityRecoveryServiceDataHolder.getInstance().getRealmService();
//        UserRealm userRealm;
//        try {
//            userRealm = (UserRealm) realmService.getTenantUserRealm(tenantId);
//        } catch (UserStoreException e) {
//            throw Utils.handleServerException(IdentityRecoveryConstants.ErrorMessages
//                    .ERROR_CODE_FAILED_TO_LOAD_REALM_SERVICE, user.getTenantDomain(), e);
//        }
//
//        org.wso2.carbon.user.core.UserStoreManager userStoreManager;
//        try {
//            userStoreManager = userRealm.getUserStoreManager();
//        } catch (UserStoreException e) {
//            throw Utils.handleServerException(IdentityRecoveryConstants.ErrorMessages
//                    .ERROR_CODE_FAILED_TO_LOAD_USER_STORE_MANAGER, null, e);
//        }
//
//        Map<String, String> claimValues = null;
//        try {
//            claimValues = userStoreManager.getUserClaimValues(IdentityUtil.addDomainToName(user.getUserName(),
//                            user.getUserStoreDomain()),
//                    new String[]{IdentityRecoveryConstants.ACCOUNT_LOCKED_CLAIM,
//                            IdentityRecoveryConstants.PASSWORD_RESET_FAIL_ATTEMPTS_CLAIM},
//                    UserCoreConstants.DEFAULT_PROFILE);
//        } catch (org.wso2.carbon.user.core.UserStoreException e) {
//            throw Utils.handleServerException(IdentityRecoveryConstants.ErrorMessages
//                    .ERROR_CODE_FAILED_TO_LOAD_USER_CLAIMS, null, e);
//        }
//
//        if (Boolean.parseBoolean(claimValues.get(IdentityRecoveryConstants.ACCOUNT_LOCKED_CLAIM))) {
//            return;
//        }
//
//        int currentAttempts = 0;
//        if (NumberUtils.isNumber(claimValues.get(IdentityRecoveryConstants.PASSWORD_RESET_FAIL_ATTEMPTS_CLAIM))) {
//            currentAttempts = Integer.parseInt(claimValues.get(IdentityRecoveryConstants
//                    .PASSWORD_RESET_FAIL_ATTEMPTS_CLAIM));
//        }
//
//        Map<String, String> updatedClaims = new HashMap<>();
//        if ((currentAttempts + 1) >= maxAttempts) {
//            updatedClaims.put(IdentityRecoveryConstants.ACCOUNT_LOCKED_CLAIM, Boolean.TRUE.toString());
//            updatedClaims.put(IdentityRecoveryConstants.PASSWORD_RESET_FAIL_ATTEMPTS_CLAIM, "0");
//            try {
//                userStoreManager.setUserClaimValues(IdentityUtil.addDomainToName(user.getUserName(),
//                        user.getUserStoreDomain()), updatedClaims, UserCoreConstants.DEFAULT_PROFILE);
//                throw Utils.handleClientException(IdentityRecoveryConstants.ErrorMessages
//                        .ERROR_CODE_LOCKED_ACCOUNT, IdentityUtil.addDomainToName(user.getUserName(),
//                        user.getUserStoreDomain()));
//            } catch (org.wso2.carbon.user.core.UserStoreException e) {
//                throw Utils.handleServerException(IdentityRecoveryConstants.ErrorMessages
//                        .ERROR_CODE_FAILED_TO_UPDATE_USER_CLAIMS, null, e);
//            }
//        } else {
//            updatedClaims.put(IdentityRecoveryConstants.PASSWORD_RESET_FAIL_ATTEMPTS_CLAIM,
//                    String.valueOf(currentAttempts + 1));
//            try {
//                userStoreManager.setUserClaimValues(IdentityUtil.addDomainToName(user.getUserName(),
//                        user.getUserStoreDomain()), updatedClaims, UserCoreConstants.DEFAULT_PROFILE);
//            } catch (org.wso2.carbon.user.core.UserStoreException e) {
//                throw Utils.handleServerException(IdentityRecoveryConstants.ErrorMessages
//                        .ERROR_CODE_FAILED_TO_UPDATE_USER_CLAIMS, null, e);
//            }
//        }
//    }
}

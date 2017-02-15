/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.recovery;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.mgt.User;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.recovery.mapping.SecurityQuestionsConfig;
import org.wso2.carbon.identity.recovery.model.ChallengeQuestion;
import org.wso2.carbon.identity.recovery.model.UserChallengeAnswer;
import org.wso2.carbon.identity.recovery.util.Utils;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.wso2.carbon.identity.recovery.IdentityRecoveryConstants.LOCALE_EN_US;

/**
 * OSGi Service to handle functionality related to challenge question management and verification.
 */
public class ChallengeQuestionManager {

    private static final Logger log = LoggerFactory.getLogger(ChallengeQuestionManager.class);
    private static ChallengeQuestionManager instance = new ChallengeQuestionManager();

    private static SecurityQuestionsConfig recoveryConfig;

    private ChallengeQuestionManager() {
    }

    public static ChallengeQuestionManager getInstance() {
        recoveryConfig = new SecurityQuestionsConfig();
        return instance;
    }

    /**
     * Get all challenge questions registered.
     *
     * @return
     * @throws IdentityRecoveryException
     */
    public List<ChallengeQuestion> getAllChallengeQuestions() throws IdentityRecoveryException {

        try {
            return Utils.readChallengeQuestionsFromYAML();
        } catch (IdentityRecoveryException e) {
            throw Utils.handleServerException(
                    IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_EXCEPTION_GET_CHALLENGE_QUESTIONS, null, e);
        }

    }


    /**
     * Get registered challenge questions based on a locale.
     *
     * @param locale
     * @return
     * @throws IdentityRecoveryException
     */
    public List<ChallengeQuestion> getAllChallengeQuestions(String locale) throws IdentityRecoveryException {

        // check the value and set defaults if empty or null
        locale = validateLocale(locale);

        try {
            return Utils.readChallengeQuestionsFromYAML(locale);
        } catch (IdentityRecoveryException e) {
            throw Utils.handleServerException(
                    IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_EXCEPTION_GET_CHALLENGE_QUESTIONS, null, e);
        }
    }


    /**
     * Get challenge questions available for a user.
     *
     * @param user         User object
     * @return List of available challenge questions in user's locale. If no challenge questions
     * are available we return challenge questions from the default en_US locale.
     * @throws IdentityRecoveryException
     */
    public List<ChallengeQuestion> getAllChallengeQuestionsForUser(User user) throws IdentityRecoveryException {

        // Identify the locale of the user
        String locale = getLocaleOfUser(user);
        // get challenge questions in the given for give locale.
        List<ChallengeQuestion> challengeQuestions = getAllChallengeQuestions(locale);

        /*
            If there are no challenge questions found in the locale of the user and the locale is not the default one.
             we return challenge questions from default en_US locale.
         */
        if (challengeQuestions.isEmpty() && !StringUtils.equalsIgnoreCase(LOCALE_EN_US, locale)) {
            String error = "No challenge questions available in '%s' locale. Sending questions of " +
                    "default '%s' locale";
            log.error(String.format(error, locale, LOCALE_EN_US));
            challengeQuestions = getAllChallengeQuestions(LOCALE_EN_US);
        }

        return challengeQuestions;
    }


    /**
     * Set default challenge questions. (This is done during startup)
     *
     * @throws IdentityRecoveryException
     */
    public void setDefaultChallengeQuestions() throws IdentityRecoveryException {

        // check whether we already have default questions.
        boolean isDefaultAvailable = !getAllChallengeQuestions().isEmpty();
        if (isDefaultAvailable) {
            if (log.isDebugEnabled()) {
                log.debug("Default Challenge Questions already available.");
            }
            return;
        }

        ChallengeQuestion[] questions = Utils.getDefaultChallengeQuestions();
        addChallengeQuestions(questions);

        if (log.isDebugEnabled()) {
            String errorMsg = "%d default challenge questions added.";
            log.debug(String.format(errorMsg, questions.length));
        }
    }

    /**
     * Add new challenge questions.
     *
     * @param questions
     * @throws IdentityRecoveryException
     */
    public void addChallengeQuestions(ChallengeQuestion[] questions) throws IdentityRecoveryException {

        try {
            Utils.updateChallengeQuestionsYAML(Arrays.asList(questions));
        } catch (IdentityRecoveryException e) {
            throw Utils.handleServerException(
                    IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_EXCEPTION_SET_CHALLENGE_QUESTIONS, null, e);
        }

    }


    /**
     * Delete challenge questions.
     *
     * @param challengeQuestions
     * @throws IdentityRecoveryException
     */
    public void deleteChallengeQuestions(ChallengeQuestion[] challengeQuestions) throws IdentityRecoveryException {

        try {
            Utils.deleteChallengeQuestions(Arrays.asList(challengeQuestions));
        } catch (IdentityRecoveryException e) {
            throw new IdentityRecoveryServerException("Error when deleting challenge questions.", e);
        }
    }

    /**
     * Get challenge questions answered by a user.
     *
     * @param uniqueUserID unique ID of the user
     * @return List of User Challengeanswer
     */
    public List<UserChallengeAnswer> getChallengeAnswersOfUser(String uniqueUserID) throws IdentityRecoveryException {

        List<UserChallengeAnswer> userChallengeAnswers = new ArrayList<>();
        if (log.isDebugEnabled()) {
            log.debug("Retrieving Challenge question from the user profile.");
        }

        List<String> challengesUris = getChallengeQuestionUris(uniqueUserID);
        List<ChallengeQuestion> challengeQuestions = getAllChallengeQuestions();

        for (String challengesUri1 : challengesUris) {
            String challengesUri = challengesUri1.trim();
            String challengeValue;
            try {
                challengeValue = Utils.getClaimFromIdentityStore(uniqueUserID, challengesUri);
            } catch (IdentityStoreException e) {
                throw Utils.handleServerException(
                        IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_GETTING_CHALLENGE_QUESTIONS,
                        uniqueUserID, e);
            } catch (UserNotFoundException e) {
                throw Utils.handleServerException(
                        IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_GETTING_CHALLENGE_QUESTIONS,
                        uniqueUserID, e);
            }

            // TODO: Add the correct separator.
            String challengeQuestionSeparator = recoveryConfig.getQuestionSeparator();

            String[] challengeValues = challengeValue.split(challengeQuestionSeparator);
            if (challengeValues.length == 2) {
                ChallengeQuestion userChallengeQuestion = new ChallengeQuestion(challengesUri,
                        challengeValues[0].trim());
                String questionId = challengeQuestions
                        .stream()
                        .filter(challengeQuestion -> StringUtils.equals(challengeQuestion.getQuestion(),
                                userChallengeQuestion.getQuestion()))
                        .findFirst()
                        .get()
                        .getQuestionId();
                userChallengeQuestion.setQuestionId(questionId);

                UserChallengeAnswer userChallengeAnswer = new UserChallengeAnswer(userChallengeQuestion,
                        challengeValues[1].trim());
                userChallengeAnswers.add(userChallengeAnswer);
            }
        }

        if (!userChallengeAnswers.isEmpty()) {
            return userChallengeAnswers;
        } else {
            return new ArrayList<>();
        }
    }


    /**
     * Retrieve the challenge question answered from a particular challenge question set.
     *
     * @param uniqueUserID unique ID of the user
     * @param challengesUri claim uri of the challenge set
     * @return ChallengeQuestion of the requested claim
     * @throws IdentityRecoveryException
     */
    public ChallengeQuestion getUserChallengeQuestion(String uniqueUserID, String challengesUri)
            throws IdentityRecoveryException {

        ChallengeQuestion userChallengeQuestion = null;
        if (log.isDebugEnabled()) {
            log.debug("Retrieving Challenge question from the user profile.");
        }

        String challengeValue = null;
        try {
            challengeValue = Utils.getClaimFromIdentityStore(uniqueUserID, challengesUri);
        } catch (IdentityStoreException e) {
            throw Utils.handleServerException(
                    IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_GETTING_CHALLENGE_QUESTION,
                   uniqueUserID, e);
        } catch (UserNotFoundException e) {
            throw Utils.handleServerException(
                    IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_GETTING_CHALLENGE_QUESTION,
                    uniqueUserID, e);
        }

        if (challengeValue != null) {

            //TODO might want to get rid of separator
            String challengeQuestionSeparator = recoveryConfig.getQuestionSeparator();

            String[] challengeValues = challengeValue.split(challengeQuestionSeparator);
            if (challengeValues.length == 2) {
                userChallengeQuestion = new ChallengeQuestion(challengesUri, challengeValues[0].trim());
            }
        }
        return userChallengeQuestion;

    }


    public List<String> getUserChallengeQuestionIds(User user) throws IdentityRecoveryException {

        validateUser(user);

        if (log.isDebugEnabled()) {
            log.debug("Retrieving answered Challenge question set ids from the user profile.");
        }
        List<String> challengesUris = getChallengeQuestionUris(user.getUniqueUserId());

        if (challengesUris.isEmpty()) {
            String msg = "No associated challenge question found for the user : " + user.getUniqueUserId();
            if (log.isDebugEnabled()) {
                log.debug(msg);
            }
        }
        return challengesUris;

    }

    /**
     * Get the claims URIs of the challenge sets answered by the user.
     *
     * @param uniqueUserID unique ID of the user
     * @return String list of challenge question URIs
     */
    public List<String> getChallengeQuestionUris(String uniqueUserID)
            throws IdentityRecoveryException {

        //validateUser(userID);

        if (log.isDebugEnabled()) {
            String msg = String.format("Getting answered challenge question uris from %s's profile.",
                    uniqueUserID);
            log.debug(msg);
        }

        List<String> challenges = new ArrayList<String>();
        String claimValue = null;
        String[] challengesUris;

        try {
            claimValue = Utils.getClaimFromIdentityStore(uniqueUserID,
                    IdentityRecoveryConstants.CHALLENGE_QUESTION_URI);
        } catch (IdentityStoreException e) {
            throw Utils.handleServerException(
                    IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_GETTING_CHALLENGE_URIS, uniqueUserID, e);
        } catch (UserNotFoundException e) {
            throw Utils.handleServerException(
                    IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_GETTING_CHALLENGE_URIS, uniqueUserID, e);
        }

        if (claimValue != null) {

            // TODO: Get the correct challenge question separator.
            String challengeQuestionSeparator = "";
            // IdentityRecoveryConstants.ConnectorConfig.QUESTION_CHALLENGE_SEPARATOR;
//            String challengeQuestionSeparator = IdentityUtil.getProperty(IdentityRecoveryConstants.ConnectorConfig
//                    .QUESTION_CHALLENGE_SEPARATOR);

            if (StringUtils.isEmpty(challengeQuestionSeparator)) {
                challengeQuestionSeparator = IdentityRecoveryConstants.DEFAULT_CHALLENGE_QUESTION_SEPARATOR;
            }

            if (claimValue.contains(challengeQuestionSeparator)) {
                challengesUris = claimValue.split(challengeQuestionSeparator);
            } else {
                challengesUris = new String[]{claimValue.trim()};
            }

            for (String challengesUri : challengesUris) {
                if (StringUtils.isNotBlank(challengesUri)) {
                    challenges.add(challengesUri.trim());
                }
            }
        }

        return challenges;
    }


    /**
     * @param user
     * @param userChallengeAnswers
     * @throws IdentityRecoveryServerException
     */
    public void setChallengesOfUser(User user, List<UserChallengeAnswer> userChallengeAnswers)
            throws IdentityRecoveryException {

        validateUser(user);

        if (log.isDebugEnabled()) {
            log.debug(String.format("Setting user challenge question answers in %s's profile.", user.toString()));
        }

        try {
            // validate whether two questions from the same set has been answered.
            validateSecurityQuestionDuplicate(userChallengeAnswers);

            // check whether the answered questions exists
            checkChallengeQuestionExists(userChallengeAnswers);

            List<String> challengesUris = new ArrayList<String>();
            String challengesUrisValue = "";

            // TODO: Get the correct challenge question separator.
            String separator = ""; // IdentityRecoveryConstants.ConnectorConfig.QUESTION_CHALLENGE_SEPARATOR;
//            String separator = IdentityUtil.getProperty(IdentityRecoveryConstants.ConnectorConfig
//                    .QUESTION_CHALLENGE_SEPARATOR);

            if (StringUtils.isEmpty(separator)) {
                separator = IdentityRecoveryConstants.DEFAULT_CHALLENGE_QUESTION_SEPARATOR;
            }

            if (!userChallengeAnswers.isEmpty()) {

                for (UserChallengeAnswer userChallengeAnswer : userChallengeAnswers) {

                    if (userChallengeAnswer.getQuestion().getQuestionSetId() != null && userChallengeAnswer
                            .getQuestion().getQuestion() != null && userChallengeAnswer.getAnswer() != null) {

                        String oldValue = Utils.getClaimFromIdentityStore(user.getUniqueUserId(),
                                userChallengeAnswer.getQuestion().getQuestionSetId().trim());

                        if (oldValue != null && oldValue.contains(separator)) {
                            String oldAnswer = oldValue.split(separator)[1];
                            if (!oldAnswer.trim().equals(userChallengeAnswer.getAnswer().trim())) {
                                String claimValue = userChallengeAnswer.getQuestion().getQuestion().trim() +
                                        separator + Utils.doHash(userChallengeAnswer.getAnswer().trim()
                                        .toLowerCase(Locale.ENGLISH));
                                Utils.setClaimInIdentityStore(user, userChallengeAnswer.getQuestion().getQuestionSetId()
                                                .trim(), claimValue);
                            }
                        } else {
                            String claimValue = userChallengeAnswer.getQuestion().getQuestion().trim() + separator +
                                    Utils.doHash(userChallengeAnswer.getAnswer().trim().toLowerCase(Locale.ENGLISH));
                            Utils.setClaimInIdentityStore(user, userChallengeAnswer.getQuestion().getQuestionSetId()
                                            .trim(), claimValue);
                        }
                        challengesUris.add(userChallengeAnswer.getQuestion().getQuestionSetId().trim());
                    }
                }

                for (String challengesUri : challengesUris) {
                    if ("".equals(challengesUrisValue)) {
                        challengesUrisValue = challengesUri;
                    } else {
                        challengesUrisValue = challengesUrisValue +
                                separator + challengesUri;
                    }
                }
                Utils.setClaimInIdentityStore(user, IdentityRecoveryConstants.CHALLENGE_QUESTION_URI,
                        challengesUrisValue);
            }
        } catch (NoSuchAlgorithmException | UserNotFoundException | IdentityStoreException e) {
            throw Utils.handleServerException(
                    IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_QUESTION_OF_USER, user.getUniqueUserId(), e);
        }
    }

    /**  Verify whether the provided user challenge answers for the challenge questions are correct
     * @param uniqueUserID unique ID of the user
     * @param userChallengeAnswers List of user ChallenegeAnswer
     * @return true if user answers are correct, false otherwise
     */
    public boolean verifyChallengeAnswer(String uniqueUserID, List<UserChallengeAnswer> userChallengeAnswers)
            throws IdentityRecoveryException {

        boolean verification = false;
        if (log.isDebugEnabled()) {
            log.debug(String.format("Verifying challenge question answers for userID %s.", uniqueUserID));
        }

        List<UserChallengeAnswer> storedAnswers = getChallengeAnswersOfUser(uniqueUserID);

        for (UserChallengeAnswer userChallengeAnswer : userChallengeAnswers) {
            if (StringUtils.isBlank(userChallengeAnswer.getAnswer())) {
                return false;
            }

            for (UserChallengeAnswer storedAnswer : storedAnswers) {
                if ((userChallengeAnswer.getQuestion().getQuestionSetId() == null ||
                        !userChallengeAnswer.getQuestion().getQuestionSetId()
                        .trim().equals(storedAnswer.getQuestion().getQuestionSetId())) &&
                        (userChallengeAnswer.getQuestion().getQuestion() == null ||
                                !userChallengeAnswer.getQuestion().getQuestion().
                                trim().equals(storedAnswer.getQuestion().getQuestion()))) {
                    continue;

                }

                String hashedAnswer = null;
                try {
                    hashedAnswer = Utils.doHash(userChallengeAnswer.getAnswer().trim().toLowerCase(Locale.ENGLISH));
                } catch (NoSuchAlgorithmException e) {
                    throw Utils.handleServerException(IdentityRecoveryConstants.ErrorMessages
                            .ERROR_CODE_NO_HASHING_ALGO, null, e);
                }

                if (hashedAnswer.equals(storedAnswer.getAnswer())) {
                    verification = true;
                } else {
                    return false;
                }
            }
        }

        return verification;
    }

    /**
     * Verify whether the provided user challenge answer for the challenge question is correct
     * @param uniqueUserID unique ID of the user
     * @param userChallengeAnswer user's ChallenegeAnswer
     * @return true if user answer is correct, false otherwise
     */
    public boolean verifyUserChallengeAnswer(String uniqueUserID, UserChallengeAnswer userChallengeAnswer)
            throws IdentityRecoveryException {

        // check whether user data are valid.
//        validateUser(user);

        boolean verification = false;
        if (log.isDebugEnabled()) {
            log.debug(String.format("Verifying challenge question answer for %s.", uniqueUserID));
        }

        List<UserChallengeAnswer> storedDto = getChallengeAnswersOfUser(uniqueUserID);
        if (StringUtils.isBlank(userChallengeAnswer.getAnswer())) {
            log.error("Invalid. Empty answer provided for the challenge question.");
            return false;
        }

        for (UserChallengeAnswer dto : storedDto) {
            if (dto.getQuestion().getQuestionSetId().equals(userChallengeAnswer.getQuestion().getQuestionSetId())) {
                String hashedAnswer = null;
                try {
                    hashedAnswer = Utils.doHash(userChallengeAnswer.getAnswer().trim().toLowerCase(Locale.ENGLISH));
                } catch (NoSuchAlgorithmException e) {
                    throw Utils.handleServerException(IdentityRecoveryConstants.ErrorMessages
                            .ERROR_CODE_NO_HASHING_ALGO, null, e);
                }
                if (hashedAnswer.equals(dto.getAnswer())) {
                    verification = true;
                    if (log.isDebugEnabled()) {
                        log.debug("Challenge question answer verified successfully.");
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Challenge question answer verification failed.");
                    }
                    return false;
                }
            }
        }

        return verification;
    }


    /**
     * Validate whether two questions from the same question set have been answered (ie. we only allow a maximum of
     * one question from each set)
     *
     * @param userChallengeAnswers
     * @throws IdentityRecoveryClientException
     */
    private void validateSecurityQuestionDuplicate(List<UserChallengeAnswer> userChallengeAnswers)
            throws IdentityRecoveryException {

        Set<String> tmpMap = new HashSet<>();
        UserChallengeAnswer challengeAnswer;
        ChallengeQuestion challengeQuestion;

        for (UserChallengeAnswer userChallengeAnswer : userChallengeAnswers) {
            challengeAnswer = userChallengeAnswer;
            challengeQuestion = challengeAnswer.getQuestion();
            // if there's no challenge question details we throw a client exception
            if (challengeQuestion == null) {
                String errorMsg = "Challenge question details not provided with the challenge answers.";
                throw Utils.handleClientException(
                        IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_CHALLENGE_QUESTION_NOT_FOUND, errorMsg);
            }

            if (tmpMap.contains(challengeQuestion.getQuestionSetId())) {
                String errMsg = "Validation Error. Cannot answer two questions from the same question set claim uri";
                log.error(errMsg);
                throw new IdentityRecoveryClientException(errMsg);
            }
            tmpMap.add(challengeQuestion.getQuestionSetId());
        }
    }


    /**
     * Check whether an answered challenge question actually exists.
     *
     * @param userChallengeAnswers
     * @throws IdentityRecoveryException
     */
    private void checkChallengeQuestionExists(List<UserChallengeAnswer> userChallengeAnswers)
            throws IdentityRecoveryException {

        for (UserChallengeAnswer challengeAnswer : userChallengeAnswers) {
            ChallengeQuestion challengeQuestion = challengeAnswer.getQuestion();
            // if challenge question details are missing in the challenge answer we can't proceed further
            if (challengeQuestion == null) {
                String errorMsg = "Challenge question missing in the user challenge answer.";
                throw new IdentityRecoveryClientException(errorMsg);
            }

            if (StringUtils.isBlank(challengeQuestion.getQuestion())) {
                String errorMsg = "Invalid. Empty Challenge question provided.";
                throw new IdentityRecoveryClientException(errorMsg);
            }

            String locale = validateLocale(challengeQuestion.getLocale());

            List<ChallengeQuestion> challengeQuestions = getAllChallengeQuestions(locale);
            boolean isQuestionAvailable = false;
            for (ChallengeQuestion availableQuestion : challengeQuestions) {
                if (StringUtils.equals(availableQuestion.getQuestion(), challengeQuestion.getQuestion())) {
                    isQuestionAvailable = true;
                }
            }

            if (!isQuestionAvailable) {
                String error = "Error persisting user challenge answers for user. " +
                        "Challenge question answered is not registered with.";
                throw Utils.handleClientException(
                        IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_CHALLENGE_QUESTION_NOT_FOUND,
                        String.format(error));
            }
        }
    }

    private String validateLocale(String locale) throws IdentityRecoveryException {
        // if the locale is blank, we go with the default locale
        if (StringUtils.isBlank(locale)) {
            locale = LOCALE_EN_US;
        }
        // validate locale input string
        if (locale.matches(IdentityRecoveryConstants.Questions.BLACKLIST_REGEX)) {
            log.error("Invalid locale value provided : " + locale);
            throw new IdentityRecoveryClientException("Invalid Locale value provided : " + locale);
        }

        return locale;

    }

    private void validateUser(User user) throws IdentityRecoveryException {
        if (user == null || StringUtils.isBlank(user.getUniqueUserId())) {
            throw Utils.handleClientException(
                    IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_INVALID_USER, "Invalid User Data provided.");
        }
    }

    private String getLocaleOfUser(User user) throws IdentityRecoveryException {

        String locale = IdentityRecoveryConstants.LOCALE_EN_US;
        try {
            String userLocale =
                    Utils.getClaimFromIdentityStore(user.getUniqueUserId(), IdentityRecoveryConstants.Questions
                            .LOCALE_CLAIM);
            if (StringUtils.isNotBlank(userLocale)) {
                locale = userLocale;
            }
        } catch (IdentityStoreException e) {
            String errorMsg = String.format("Error when retrieving the locale claim of user '%s'.",
                                            user.getUniqueUserId());
            log.error(errorMsg);
            throw new IdentityRecoveryServerException(errorMsg, e);
        } catch (UserNotFoundException e) {
            String errorMsg = String.format("Error when retrieving the locale claim of user '%s'.",
                                            user.getUniqueUserId());
            log.error(errorMsg);
            throw new IdentityRecoveryServerException(errorMsg, e);
        }

        return locale;
    }

    /**
     * Get Minimum no of challenge questions user has to answer.
     *
     * @return No of questions needs to answer.
     * @throws IdentityRecoveryException
     */
    public int getMinimumNoOfChallengeQuestionsToAnswer() throws IdentityRecoveryException {
        return recoveryConfig.getMinAnswers();
    }

    /**
     * Check whether security question based password recovery enabled
     *
     * @return true if security question based password recovery enabled, false otherwise.
     * @throws IdentityRecoveryException
     */
    public boolean isQuestionBasedPwdRecoveryEnabledInPortal() throws IdentityRecoveryException {
        return recoveryConfig.isEnablePortal();
    }
}

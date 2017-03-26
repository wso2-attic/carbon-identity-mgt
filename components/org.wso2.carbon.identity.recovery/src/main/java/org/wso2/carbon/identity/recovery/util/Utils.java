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

package org.wso2.carbon.identity.recovery.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.common.base.event.model.Event;
import org.wso2.carbon.identity.common.base.exception.IdentityException;
import org.wso2.carbon.identity.event.EventConstants;
import org.wso2.carbon.identity.mgt.IdentityStore;
import org.wso2.carbon.identity.mgt.RealmService;
import org.wso2.carbon.identity.mgt.User;
import org.wso2.carbon.identity.mgt.UserState;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.event.IdentityMgtMessageContext;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.mgt.impl.util.IdentityMgtConstants;
import org.wso2.carbon.identity.recovery.IdentityRecoveryClientException;
import org.wso2.carbon.identity.recovery.IdentityRecoveryConstants;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.IdentityRecoveryRuntimeException;
import org.wso2.carbon.identity.recovery.IdentityRecoveryServerException;
import org.wso2.carbon.identity.recovery.internal.IdentityRecoveryServiceDataHolder;
import org.wso2.carbon.identity.recovery.mapping.ChallengeQuestionsFile;
import org.wso2.carbon.identity.recovery.model.ChallengeQuestion;
import org.wso2.carbon.identity.recovery.model.Property;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import static org.wso2.carbon.identity.recovery.IdentityRecoveryConstants.CONFIRMATION_CODE;
import static org.wso2.carbon.identity.recovery.IdentityRecoveryConstants.TEMPLATE_TYPE;

/**
 * Utility to provide recovery functionality.
 */
public class Utils {
    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    //This is used to pass the arbitrary properties from self user manager to self handler
    private static ThreadLocal<org.wso2.carbon.identity.recovery.model.Property[]> arbitraryProperties = new
            ThreadLocal<>();

    private static final String CHALLENGE_QUESTIONS_FOLDER_PATH =
            System.getProperty(IdentityRecoveryConstants.CARBON_HOME) +
            IdentityRecoveryConstants.CHALLAENGE_QUESTION_FOLDER_LOCATION;

    /**
     * Get user claim value from identity store manager
     *
     * @param uniqueUserID
     * @param claimuri
     * @return
     * @throws IdentityStoreException
     * @throws UserNotFoundException
     */
    public static String getClaimFromIdentityStore(String uniqueUserID, String claimuri)
            throws IdentityStoreException, UserNotFoundException {

        RealmService realmService = IdentityRecoveryServiceDataHolder.getInstance().getRealmService();
        IdentityStore identityStore = realmService.getIdentityStore();
        String claimValue = "";

        if (identityStore != null) {
            List<Claim> claimsList = identityStore.getClaimsOfUser(uniqueUserID);
            if (claimsList != null && !claimsList.isEmpty()) {
                for (Claim claim : claimsList) {
                    if (claim.getClaimUri().equals(claimuri)) {
                        claimValue = claim.getValue();
                        break;
                    }
                }
            }
        }
        return claimValue;

    }

    public static IdentityRecoveryServerException handleServerException(IdentityRecoveryConstants.ErrorMessages error,
                                                                String data) throws IdentityRecoveryServerException {

        String errorDescription;
        if (StringUtils.isNotBlank(data)) {
            errorDescription = String.format(error.getMessage(), data);
        } else {
            errorDescription = error.getMessage();
        }

        return new IdentityRecoveryServerException(error.getCode(), errorDescription);
    }

    public static IdentityRecoveryServerException handleServerException(IdentityRecoveryConstants.ErrorMessages
                                                                                error, String data, Throwable e) {

        String errorDescription;
        if (StringUtils.isNotBlank(data)) {
            errorDescription = String.format(error.getMessage(), data);
        } else {
            errorDescription = error.getMessage();
        }

        return new IdentityRecoveryServerException(error.getCode(), errorDescription, e);
    }

    public static IdentityRecoveryClientException handleClientException(IdentityRecoveryConstants.ErrorMessages error,
                                                                        String data) {
        String errorDescription;
        if (StringUtils.isNotBlank(data)) {
            errorDescription = String.format(error.getMessage(), data);
        } else {
            errorDescription = error.getMessage();
        }
        return new IdentityRecoveryClientException(error.getCode(), errorDescription);
    }

    public static IdentityRecoveryClientException handleClientException(IdentityRecoveryConstants.ErrorMessages error,
                                                                        String data, Throwable e) {

        String errorDescription;
        if (StringUtils.isNotBlank(data)) {
            errorDescription = String.format(error.getMessage(), data);
        } else {
            errorDescription = error.getMessage();
        }
        return new IdentityRecoveryClientException(error.getCode(), errorDescription, e);
    }

    public static IdentityRecoveryRuntimeException handleRuntimeException(IdentityRecoveryConstants.ErrorMessages error,
                                                                          String data, Throwable e) {

        String errorDescription;
        if (StringUtils.isNotBlank(data)) {
            errorDescription = String.format(error.getMessage(), data);
        } else {
            errorDescription = error.getMessage();
        }
        return new IdentityRecoveryRuntimeException(error.getCode(), errorDescription, e);

    }

    public static IdentityRecoveryRuntimeException handleRuntimeException(IdentityRecoveryConstants.ErrorMessages error,
                                                                          String data) {

        String errorDescription;
        if (StringUtils.isNotBlank(data)) {
            errorDescription = String.format(error.getMessage(), data);
        } else {
            errorDescription = error.getMessage();
        }
        return new IdentityRecoveryRuntimeException(error.getCode(), errorDescription);

    }

    /**
     * Hash and encode a string.
     *
     * @param value
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String doHash(String value) throws NoSuchAlgorithmException {
        String digsestFunction = "SHA-256";
        MessageDigest dgst = MessageDigest.getInstance(digsestFunction);
        byte[] byteValue = dgst.digest(value.getBytes(StandardCharsets.UTF_8));
        return new String(Base64.getEncoder().encode(byteValue), StandardCharsets.UTF_8);
    }

    /**
     * Add/Update a claim of a user.
     *
     * @param uniqueUserId Unique ID of the user.
     * @param claimUri Claim URI to be added or updated.
     * @param value Value of the claim.
     * @param claimDialect Claim dialect of the claim URI. If dialect is null, root dialect will be used.
     * @throws IdentityStoreException If error occurs while updating the claim.
     * @throws UserNotFoundException If the user does not exist.
     */

    public static void setClaimInIdentityStore(String uniqueUserId, String claimUri, String value, @Nullable String
            claimDialect) throws IdentityStoreException, UserNotFoundException {

        RealmService realmService = IdentityRecoveryServiceDataHolder.getInstance().getRealmService();
        IdentityStore identityStore = realmService.getIdentityStore();

        if (identityStore == null) {
            throw handleRuntimeException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_IDENTITY_STORE_ERROR, null);
        }

        if (claimDialect == null) {
            claimDialect = IdentityMgtConstants.CLAIM_ROOT_DIALECT;
        }

        List<Claim> claimsList = new ArrayList<>(1);
        claimsList.add(new Claim(claimDialect, claimUri, value));

        identityStore.updateUserClaims(uniqueUserId, claimsList, null);
    }

    /**
     * Add/Update multiple claims of a user.
     *
     * @param uniqueUserId Unique ID of the user.
     * @param claims Map of claim URIs and values to be updated
     * @param claimDialect Claim dialect of the claim URI. If dialect is null, root dialect will be used.
     * @throws IdentityStoreException If error occurs while updating the claim.
     * @throws UserNotFoundException If the user does not exist.
     */
    public static void setClaimsInIdentityStore(String uniqueUserId, Map<String, String> claims, @Nullable String
            claimDialect) throws IdentityStoreException, UserNotFoundException {

        RealmService realmService = IdentityRecoveryServiceDataHolder.getInstance().getRealmService();
        IdentityStore identityStore = realmService.getIdentityStore();

        if (identityStore == null) {
            throw handleRuntimeException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_IDENTITY_STORE_ERROR, null);

        }

        if (claimDialect == null) {
            claimDialect = IdentityMgtConstants.CLAIM_ROOT_DIALECT;
        }
        String dialect = claimDialect;
        List<Claim> claimsList = claims.entrySet().stream()
                                       .map((x) -> new Claim(dialect, x.getKey(), x.getValue()))
                                       .collect(Collectors.toList());

        identityStore.updateUserClaims(uniqueUserId, claimsList, null);
    }

    public static String getUniqueUserId(Claim claim, String domainName) throws IdentityRecoveryException {

        RealmService realmService = IdentityRecoveryServiceDataHolder.getInstance().getRealmService();
        IdentityStore identityStore = realmService.getIdentityStore();

        if (identityStore == null) {
            throw handleRuntimeException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_IDENTITY_STORE_ERROR, null);
        }

        if (StringUtils.isNotBlank(domainName)) {
            try {
                User user = identityStore.getUser(claim, domainName);
                return user.getUniqueUserId();
            } catch (IdentityStoreException e) {
                throw handleServerException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_FAILED_USER_SEARCH,
                                            null, e);
            } catch (UserNotFoundException e) {
                throw handleClientException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_INVALID_USER,
                                            claim.getValue() + " in domain: " + domainName, e);
            }
        } else {
            List<Claim> claimList = new ArrayList<>();
            claimList.add(claim);

            try {
                List<String> userExist = identityStore.isUserExist(claimList);

                if (userExist.isEmpty()) {
                    throw handleClientException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_INVALID_USER, "");
                } else if (userExist.size() > 1) {
                    throw handleClientException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_NON_UNIQUE_USER,
                                                claim.getValue());
                } else {
                    return userExist.get(0);
                }
            } catch (IdentityStoreException e) {
                throw handleServerException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_FAILED_USER_SEARCH,
                                            null, e);
            }
        }
    }

    public static List<ChallengeQuestion> getDefaultChallengeQuestions() {
        List<ChallengeQuestion> challengeQuestions = new ArrayList<>();
        // locale en_US, challengeSet1
        int count = 0;
        for (String question : IdentityRecoveryConstants.Questions.getSecretQuestionsSet01()) {
            String setId = IdentityRecoveryConstants.WSO2CARBON_CLAIM_DIALECT + "/" + "challengeQuestion1";
            String questionId = "question" + (++count);
            challengeQuestions.add(
                    new ChallengeQuestion(setId, questionId, question, IdentityRecoveryConstants.LOCALE_EN_US));
        }

        count = 0;
        for (String question : IdentityRecoveryConstants.Questions.getSecretQuestionsSet02()) {
            String setId = IdentityRecoveryConstants.WSO2CARBON_CLAIM_DIALECT + "/" + "challengeQuestion2";
            String questionId = "question" + (++count);
            challengeQuestions.add(
                    new ChallengeQuestion(setId, questionId, question, IdentityRecoveryConstants.LOCALE_EN_US));
        }

        return challengeQuestions;
    }

    public static boolean isAccountLocked(String uniqueUserId) throws IdentityRecoveryException {
        return isAccountInState(uniqueUserId, UserState.Group.LOCKED);
    }


    public static boolean isAccountDisabled(String uniqueUserId) throws IdentityRecoveryException {
        return isAccountInState(uniqueUserId, UserState.Group.DISABLED);
    }

    private static boolean isAccountInState(String uniqueUserId, UserState.Group group) throws 
            IdentityRecoveryException {
        RealmService realmService = IdentityRecoveryServiceDataHolder.getInstance().getRealmService();
        IdentityStore identityStore = realmService.getIdentityStore();
        String state;

        try {
            state = identityStore.getUser(uniqueUserId).getState();
        } catch (IdentityStoreException e) {
            throw Utils.handleServerException(
                    IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_FAILED_TO_LOAD_USER_CLAIMS, null, e);
        } catch (UserNotFoundException e) {
            throw Utils.handleServerException(
                    IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_FAILED_TO_LOAD_USER_CLAIMS, null, e);
        }
        return UserState.valueOf(state).isInGroup(group);

    }

    public static void updateChallengeQuestionsYAML(List<ChallengeQuestion> challengeQuestions)
            throws IdentityRecoveryException {

        final boolean[] error = { false };
        Map<String, List<ChallengeQuestion>> groupedByLocale =
                challengeQuestions.stream().collect(
                        Collectors.groupingBy(
                                challengeQuestion -> challengeQuestion.getLocale()
                        )
                );
        groupedByLocale.forEach((key, value) -> {
            try {
                updateChallengeQuestionsYAML(value, key);
            } catch (IdentityRecoveryException e) {
                log.error(String.format("Error while updating challenge questions from locale file %s", key));
                error[0] = true;
            }
        });

        if (error[0]) {
            throw new IdentityRecoveryException("Error while updating challenge questions");
        }

    }


    public static void updateChallengeQuestionsYAML(List<ChallengeQuestion> challengeQuestions, String locale)
            throws IdentityRecoveryException {

        ChallengeQuestionsFile challengeQuestionFile = new ChallengeQuestionsFile();
        challengeQuestionFile.setChallengeQuestions(challengeQuestions);

        FileUtil.writeConfigFiles(Paths.get(CHALLENGE_QUESTIONS_FOLDER_PATH + File.separator + locale + ".yaml"),
                                  challengeQuestionFile);

    }

    public static List<ChallengeQuestion> readChallengeQuestionsFromYAML() throws IdentityRecoveryException {

        List<ChallengeQuestion> challengeQuestionsInAllLocales = new ArrayList<>();
        final boolean[] error = { false };
        try {
            Files.list(Paths.get(CHALLENGE_QUESTIONS_FOLDER_PATH))
                 .forEach((path) -> {
                     try {
                         String locale = FilenameUtils.removeExtension(path.toAbsolutePath().toString());
                         ChallengeQuestionsFile challengeQuestionFile =
                                 FileUtil.readConfigFile(path, ChallengeQuestionsFile.class);
                         challengeQuestionFile.getChallengeQuestions().forEach(challengeQuestion -> {
                             challengeQuestion.setLocale(locale);
                         });
                         challengeQuestionsInAllLocales.addAll(challengeQuestionFile.getChallengeQuestions());
                     } catch (IdentityRecoveryException e) {
                         log.error(String.format("Error while reading challenge questions from locale file %s", path));
                         error[0] = true;
                     }
                 });
        } catch (IOException e) {
            throw new IdentityRecoveryException("Error while reading challenge questions", e);
        }

        if (error[0]) {
            throw new IdentityRecoveryException("Error while updating challenge questions");
        }

        return challengeQuestionsInAllLocales;
    }

    public static List<ChallengeQuestion> readChallengeQuestionsFromYAML(String locale)
            throws IdentityRecoveryException {

        ChallengeQuestionsFile challengeQuestionFile =
                FileUtil.readConfigFile(Paths.get(CHALLENGE_QUESTIONS_FOLDER_PATH + File.separator + locale + ".yaml"),
                                        ChallengeQuestionsFile.class);
        challengeQuestionFile.getChallengeQuestions().forEach(challengeQuestion -> {
            challengeQuestion.setLocale(locale);
        });

        return challengeQuestionFile.getChallengeQuestions();
    }

    /**
     * Generates a UUID.
     *
     * @return Generated UUID.
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * Trigger notification event.
     *
     * @param uniqueUserId Unique ID of the user.
     * @param type Notification type.
     * @param code Confirmation code.
     * @param props Additional properties to be sent with the event.
     * @throws IdentityRecoveryException If error occurs during the firing the event.
     */
    public static void triggerNotification(String uniqueUserId, String type, String code, Property[] props) throws
            IdentityRecoveryException {

        String eventName = EventConstants.Event.TRIGGER_NOTIFICATION;

        Map<String, Object> properties = new HashMap<>();
        properties.put(EventConstants.EventProperty.USER_UNIQUE_ID, uniqueUserId);

        if (props != null && props.length > 0) {
            for (Property prop : props) {
                properties.put(prop.getKey(), prop.getValue());
            }
        }
        if (StringUtils.isNotBlank(code)) {
            properties.put(CONFIRMATION_CODE, code);
        }
        properties.put(TEMPLATE_TYPE, type);
        Event event = new Event(eventName, properties);
        IdentityMgtMessageContext messageContext = new IdentityMgtMessageContext();
        try {
            IdentityRecoveryServiceDataHolder.getInstance().getIdentityEventService().pushEvent(event, messageContext);
        } catch (IdentityException e) {
            throw Utils.handleServerException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_TRIGGER_NOTIFICATION,
                                              uniqueUserId, e);
        }
    }
}

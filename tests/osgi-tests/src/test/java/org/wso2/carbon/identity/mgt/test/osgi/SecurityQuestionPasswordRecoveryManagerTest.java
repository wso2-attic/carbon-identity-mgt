package org.wso2.carbon.identity.mgt.test.osgi;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.mgt.RealmService;
import org.wso2.carbon.identity.mgt.User;
import org.wso2.carbon.identity.mgt.UserState;
import org.wso2.carbon.identity.mgt.bean.UserBean;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.mgt.test.osgi.util.IdentityMgtOSGiTestUtils;
import org.wso2.carbon.identity.recovery.ChallengeQuestionManager;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.bean.ChallengeQuestionsResponse;
import org.wso2.carbon.identity.recovery.model.ChallengeQuestion;
import org.wso2.carbon.identity.recovery.model.UserChallengeAnswer;
import org.wso2.carbon.identity.recovery.password.SecurityQuestionPasswordRecoveryManager;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import static org.ops4j.pax.exam.CoreOptions.systemProperty;

/**
 * Test Class for SecurityQuestionPasswordRecoveryManager
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class SecurityQuestionPasswordRecoveryManagerTest {

    private static List<User> users = new ArrayList<>();
    private static List<ChallengeQuestion> challengeQuestions = new ArrayList<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityQuestionPasswordRecoveryManagerTest.class);
    private static ChallengeQuestionsResponse challengeQuestionsResponse;
    private static String answer1 = "Answer1";

    @Inject
    private BundleContext bundleContext;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Inject
    private SecurityQuestionPasswordRecoveryManager securityQuestionPasswordRecoveryManager;

//    @Inject
//    private IdentityStoreClientService identityStoreClientService;

    @Configuration
    public Option[] createConfiguration() {

        List<Option> optionList = IdentityMgtOSGiTestUtils.getDefaultSecurityPAXOptions();

        optionList.add(systemProperty("java.security.auth.login.config")
                .value(Paths.get(IdentityMgtOSGiTestUtils.getCarbonHome(), "conf", "security", "carbon-jaas.config")
                        .toString()));

        return optionList.toArray(new Option[optionList.size()]);
    }

    @Test(groups = "getRecoveryQuestions")
    public void getUserChallengeQuestion() throws IdentityRecoveryException, IdentityStoreException {

        addUser();
        getAllChallangeQuestionsForUser();
        ChallengeQuestion question = challengeQuestions.get(0);
        List<UserChallengeAnswer> answers = new ArrayList<>();
        UserChallengeAnswer answer = new UserChallengeAnswer(question, "Answer1");
        answers.add(answer);
        addChallengeQuestionForUser(answers);

        challengeQuestionsResponse = securityQuestionPasswordRecoveryManager.initiateUserChallengeQuestion
                (users.get(0));
        Assert.assertNotNull(challengeQuestionsResponse, "Failed to start challenge question based password recovery " +
                "for the user");
        Assert.assertNotNull(challengeQuestionsResponse.getCode(), "ChallengeQuestions Response does not have a " +
                "confirmation code");
        Assert.assertTrue(challengeQuestionsResponse.getQuestions().size() > 0, "ChallengeQuestions Response does not" +
                " contain questions to be answered");
        Assert.assertEquals(challengeQuestionsResponse.getQuestions().get(0).getQuestion(),
                question.getQuestion(), "Asked question is not from answered question");

    }

    @Test(groups = "answerRecoveryQuestions", dependsOnGroups = {"getRecoveryQuestions"})
    public void answerChallengeQuestion() throws IdentityRecoveryException {

        challengeQuestionsResponse = startQuestionBasedPasswordRecovery(users.get(0));

        List<UserChallengeAnswer> answers = new ArrayList<>();
        ChallengeQuestion question = new ChallengeQuestion(challengeQuestionsResponse.getQuestions().get(0)
                .getQuestionSetId(), "");
        answers.add(new UserChallengeAnswer(question, answer1));

        challengeQuestionsResponse = securityQuestionPasswordRecoveryManager.validateUserChallengeQuestions
                (answers, challengeQuestionsResponse.getCode());
        Assert.assertNotNull(challengeQuestionsResponse, "Failed to answer challenge question for password recovery");
        Assert.assertNotNull(challengeQuestionsResponse.getCode(), "ChallengeQuestions Response does not have a " +
                "confirmation code");
        Assert.assertNotNull(challengeQuestionsResponse.getStatus(), "ChallengeQuestions Response does not" +
                " contain status");
        Assert.assertEquals(challengeQuestionsResponse.getStatus(), "COMPLETE", "Challenge Question answer not " +
                "validated");

    }

    @Test(groups = "answerRecoveryQuestions", dependsOnGroups = {"getRecoveryQuestions"})
    public void answerChallengeQuestionWrongAnswer() throws IdentityRecoveryException {

        ChallengeQuestionsResponse challengeQuestionsResponse1 = startQuestionBasedPasswordRecovery(users.get(0));
        List<UserChallengeAnswer> answers = new ArrayList<>();
        ChallengeQuestion question = new ChallengeQuestion(challengeQuestionsResponse1.getQuestions().get(0)
                .getQuestionSetId(), "");
        answers.add(new UserChallengeAnswer(question, answer1 + "ASDF"));

        challengeQuestionsResponse = securityQuestionPasswordRecoveryManager.validateUserChallengeQuestions
                (answers, challengeQuestionsResponse1.getCode());

        Assert.assertNotNull(challengeQuestionsResponse, "Failed to answer challenge question for password recovery");
        Assert.assertNotNull(challengeQuestionsResponse.getCode(), "ChallengeQuestions Response does not have a " +
                "confirmation code");
        Assert.assertNotNull(challengeQuestionsResponse.getStatus(), "ChallengeQuestions Response does not" +
                " contain status");
        Assert.assertEquals(challengeQuestionsResponse.getStatus(), "20008", "Challenge Question answer hasn't been " +
                "invalid");
        Assert.assertEquals(challengeQuestionsResponse1.getQuestions().get(0).getQuestion(),
                challengeQuestionsResponse.getQuestions().get(0).getQuestion(), "Question after failed attempt " +
                        "is not same as the question in previous attempt ");
        Assert.assertEquals(challengeQuestionsResponse1.getCode(),
                challengeQuestionsResponse.getCode(), "Recovery code after failed attempt " +
                        "is not same as the code in previous attempt ");

    }

    @Test(groups = "answerRecoveryQuestionsAtOnce", dependsOnGroups = {"answerRecoveryQuestions"})
    public void startChallengeQuestionAtOnce() throws IdentityRecoveryException {

        ChallengeQuestion question1 = challengeQuestions.get(0);
        ChallengeQuestion question2 = challengeQuestions.get(7);
        List<UserChallengeAnswer> answers = new ArrayList<>();
        UserChallengeAnswer answer1 = new UserChallengeAnswer(question1, "Answer1");
        answers.add(answer1);
        UserChallengeAnswer answer2 = new UserChallengeAnswer(question2, "Answer2");
        answers.add(answer2);
        addChallengeQuestionForUser(answers);
        challengeQuestionsResponse = securityQuestionPasswordRecoveryManager.initiateUserChallengeQuestionAtOnce
                (users.get(0));
        Assert.assertNotNull(challengeQuestionsResponse, "Failed to start challenge question based password recovery " +
                "for the user");
        Assert.assertNotNull(challengeQuestionsResponse.getCode(), "ChallengeQuestions Response does not have a " +
                "confirmation code");
        Assert.assertTrue(challengeQuestionsResponse.getQuestions().size() > 0, "ChallengeQuestions Response does not" +
                " contain questions to be answered");
        Assert.assertTrue(challengeQuestionsResponse.getQuestions().size() == 2, "ChallengeQuestions Response does " +
                "not contain multiple questions to be answered");

    }

    @Test(groups = "startChallengeQuestionWhenAccountLocked", dependsOnGroups = {"answerRecoveryQuestionsAtOnce"})
    public void startChallengeQuestionWhenAccountLocked() throws IdentityRecoveryException, UserNotFoundException,
            IdentityStoreException {

        lockAccount(users.get(0).getUniqueUserId());
        boolean accountLockDetetcted = false;
        try {
            ChallengeQuestionsResponse challengeQuestionsResponse = securityQuestionPasswordRecoveryManager
                    .initiateUserChallengeQuestion(users.get(0));
        } catch (IdentityRecoveryException e) {
            Assert.assertEquals(e.getErrorCode(), "17003", "Account lock status is not returned");
            if (e.getErrorCode().equals("17003")) {
                accountLockDetetcted = true;
            }
        }
        Assert.assertTrue(accountLockDetetcted, "Account Locked exception is not thrown.");
    }

    @Test(groups = "startChallengeQuestionWhenAccountDisabled",
            dependsOnGroups = {"startChallengeQuestionWhenAccountLocked"})
    public void startChallengeQuestionWhenAccountDisabled() throws IdentityRecoveryException, UserNotFoundException,
            IdentityStoreException {

        disableAccount(users.get(0).getUniqueUserId());
        boolean accountLockDetetcted = false;
        try {
            ChallengeQuestionsResponse challengeQuestionsResponse = securityQuestionPasswordRecoveryManager
                    .initiateUserChallengeQuestion(users.get(0));
        } catch (IdentityRecoveryException e) {
            Assert.assertEquals(e.getErrorCode(), "17004", "Account lock status is not returned");
            if (e.getErrorCode().equals("17004")) {
                accountLockDetetcted = true;
            }
        }
        Assert.assertTrue(accountLockDetetcted, "Account Locked exception is not thrown.");
    }

    private void addUser() throws IdentityStoreException {
        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        UserBean userBean = new UserBean();
        List<Claim> claims = Arrays
                .asList(new Claim("http://wso2.org/claims", "http://wso2.org/claims/username", "Ayesha "),
                        new Claim("http://wso2.org/claims", "http://wso2.org/claims/firstName", "Ayesha"),
                        new Claim("http://wso2.org/claims", "http://wso2.org/claims/lastName", "Dissanayaka"),
                        new Claim("http://wso2.org/claims", "http://wso2.org/claims/email", "ayesha@wso2.com"));
        userBean.setClaims(claims);
        User user = realmService.getIdentityStore().addUser(userBean);

        Assert.assertNotNull(user, "Failed to receive the user.");
        Assert.assertNotNull(user.getUniqueUserId(), "Invalid user unique id.");

        users.add(user);
    }

    private void getAllChallangeQuestionsForUser() throws IdentityRecoveryException {
        ChallengeQuestionManager challengeQuestionManager =
                bundleContext.getService(bundleContext.getServiceReference
                        (ChallengeQuestionManager.class));
        Assert.assertNotNull(challengeQuestionManager,
                "Failed to get ChallengeQuestionManagerClientService instance");

        List<ChallengeQuestion> challengeQuestions = challengeQuestionManager.getAllChallengeQuestionsForUser(users
                .get(0));
        Assert.assertNotNull(challengeQuestions, "Failed to retrieve the challenge question list.");

        this.challengeQuestions = challengeQuestions;
    }

    private void addChallengeQuestionForUser(List<UserChallengeAnswer> answers) throws IdentityRecoveryException {
        ChallengeQuestionManager challengeQuestionManager = bundleContext.getService(bundleContext.getServiceReference
                (ChallengeQuestionManager.class));

        Assert.assertNotNull(challengeQuestionManager,
                "Failed to get ChallengeQuestionManagerClientService instance");

        try {
            challengeQuestionManager.setChallengesOfUser(users.get(0), answers);
        } catch (IdentityRecoveryException e) {
            throw new IdentityRecoveryException("Test Failure. Error when setting challenge questions for the user.");
        }
    }

    private ChallengeQuestionsResponse startQuestionBasedPasswordRecovery(User user) throws IdentityRecoveryException {
        return securityQuestionPasswordRecoveryManager.initiateUserChallengeQuestion(user);
    }

    private void updateUserState(String uniqueUserId, String state) throws UserNotFoundException,
            IdentityStoreException {
        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));

        try {
            realmService.getIdentityStore().setUserState(uniqueUserId, state);
        } catch (IdentityStoreException e) {
            Assert.fail("Failed to update user claims.");
        }

    }

    private void lockAccount(String uniqueUserID) throws UserNotFoundException, IdentityStoreException {
        updateUserState(uniqueUserID, UserState.LOCKED__UNVERIFIED.toString());
    }

    private void disableAccount(String uniqueUserID) throws UserNotFoundException, IdentityStoreException {
        updateUserState(uniqueUserID, UserState.DISABLED.toString());
    }

}

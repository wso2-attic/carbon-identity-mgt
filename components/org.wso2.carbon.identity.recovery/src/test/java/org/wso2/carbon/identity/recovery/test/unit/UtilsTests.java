package org.wso2.carbon.identity.recovery.test.unit;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.recovery.IdentityRecoveryConstants;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.IdentityRecoveryServerException;
import org.wso2.carbon.identity.recovery.model.ChallengeQuestion;
import org.wso2.carbon.identity.recovery.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Unit tests for org.wso2.carbon.identity.recovery.util.Utils
 */
@PrepareForTest(Utils.class)
public class UtilsTests {

    @Test
    public void testGetDefaultChallengeQuestions() {
        List<ChallengeQuestion> questions = Utils.getDefaultChallengeQuestions();
        Assert.assertNotNull(questions, "Default Challenge Questions are not returned.");
        Assert.assertEquals(questions.size(), 8 , "Two Challenge Questions should be returned.");
    }

    @Test
    public void testReadChallengeQuestionsFromYAML() {
        try {
            Utils.readChallengeQuestionsFromYAML();
        } catch (IdentityRecoveryException e) {
            return;
        }
        Assert.fail("Exception Should be thrown when no challenge-question resource is found.");
    }

    @Test
    public void testUpdateChallengeQuestionsYAML() {
        ChallengeQuestion question = new ChallengeQuestion();
        question.setQuestionSetId("http://wso2.org/claims/challengeQuestion10");
        question.setQuestionId("10");
        question.setQuestion("This is a test question?");
        question.setLocale("en_US");

        List<ChallengeQuestion> questions = new ArrayList<>();
        questions.add(question);
        try {
            Utils.updateChallengeQuestionsYAML(questions);
        } catch (IdentityRecoveryException e) {
            return;
        }
        Assert.fail("Exception Should be thrown when no challenge-question resource is found.");
    }

    @Test
    public void testUpdateChallengeQuestionsYAMLWithLocale() {
        ChallengeQuestion question = new ChallengeQuestion();
        question.setQuestionSetId("http://wso2.org/claims/challengeQuestion10");
        question.setQuestionId("10");
        question.setQuestion("This is a test question?");
        question.setLocale("en_US");

        List<ChallengeQuestion> questions = new ArrayList<>();
        questions.add(question);
        try {
            Utils.updateChallengeQuestionsYAML(questions, "en_US");
        } catch (IdentityRecoveryException e) {
            return;
        }
        Assert.fail("Exception Should be thrown when no challenge-question resource is found.");
    }

    @Test
    public void testHandleServerException() {
        IdentityRecoveryConstants.ErrorMessages message = IdentityRecoveryConstants.ErrorMessages
                .ERROR_CODE_INVALID_CODE;
        String code = UUID.randomUUID().toString();

        IdentityRecoveryServerException exception = Utils.handleServerException(message, code, new Throwable());
        Assert.assertEquals(exception.getErrorCode(), message.getCode(), "Incorrect Error code");
        Assert.assertEquals(exception.getMessage(), String.format(message.getMessage(), code),
                "Incorrect Error Message");
    }
}

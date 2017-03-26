package org.wso2.carbon.identity.recovery.test.unit;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.base.event.EventContext;
import org.wso2.carbon.identity.common.base.event.model.Event;
import org.wso2.carbon.identity.common.base.exception.IdentityException;
import org.wso2.carbon.identity.common.base.handler.IdentityEventHandler;
import org.wso2.carbon.identity.event.EventConstants;
import org.wso2.carbon.identity.event.EventService;
import org.wso2.carbon.identity.recovery.IdentityRecoveryConstants;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.IdentityRecoveryServerException;
import org.wso2.carbon.identity.recovery.internal.IdentityRecoveryServiceDataHolder;
import org.wso2.carbon.identity.recovery.model.ChallengeQuestion;
import org.wso2.carbon.identity.recovery.model.Property;
import org.wso2.carbon.identity.recovery.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Unit tests for org.wso2.carbon.identity.recovery.util.Utils.
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

    @Test
    public void testTriggerNotification() throws IdentityException {

        EventService eventService = new EventService() {
            @Override
            public void pushEvent(Event event, EventContext eventContext) throws IdentityException {

                if (event == null) {
                    throw new IdentityException("Event cannot be null.");
                }

                if (!EventConstants.Event.TRIGGER_NOTIFICATION.equals(event.getEventName())) {
                    throw new IdentityException("Expected event: " + EventConstants.Event.TRIGGER_NOTIFICATION +
                                                " found: " + event.getEventName());
                } else {

                    if (event.getEventProperties().get("fail") != null) {
                        throw new IdentityException("Test exception.");
                    }
                }
            }

            @Override
            public void pushEvent(Event event, EventContext eventContext, IdentityEventHandler identityEventHandler)
                    throws IdentityException {

            }
        };

        IdentityRecoveryServiceDataHolder.getInstance().setIdentityEventService(eventService);

        String userId = "testID";
        String type = "templateType";
        String code = "testCode";
        Property property[] = new Property[]{ new Property("fail", "true")};

        try {
            Utils.triggerNotification(userId, type, null, null);
            Assert.assertTrue(true);
        } catch (IdentityRecoveryException e) {
            Assert.fail("Failed notification event trigger.", e);
        }

        try {
            Utils.triggerNotification(userId, type, code, property);
            Assert.fail("Expected a failed notification trigger.");
        } catch (IdentityRecoveryException e) {
            Assert.assertTrue(true);
        }

    }
}

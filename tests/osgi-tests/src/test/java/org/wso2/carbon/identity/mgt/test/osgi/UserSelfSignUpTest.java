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

package org.wso2.carbon.identity.mgt.test.osgi;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.BundleContext;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.mgt.RealmService;
import org.wso2.carbon.identity.mgt.User;
import org.wso2.carbon.identity.mgt.bean.UserBean;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.exception.AuthenticationFailure;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.mgt.impl.util.IdentityMgtConstants;
import org.wso2.carbon.identity.mgt.test.osgi.util.IdentityMgtOSGiTestUtils;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.bean.NotificationResponseBean;
import org.wso2.carbon.identity.recovery.signup.UserSelfSignUpManager;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;

import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.wso2.carbon.identity.recovery.IdentityRecoveryConstants.ErrorCodes.ACCOUNT_UNVERIFIED;

/**
 * User self sign-up related OSGi tests.
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class UserSelfSignUpTest {

    @Inject
    private BundleContext bundleContext;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Configuration
    public Option[] createConfiguration() {

        List<Option> optionList = IdentityMgtOSGiTestUtils.getDefaultSecurityPAXOptions();

        optionList.add(systemProperty("java.security.auth.login.config")
                               .value(Paths.get(IdentityMgtOSGiTestUtils.getCarbonHome(), "conf", "security",
                                                "carbon-jaas.config").toString()));
        return optionList.toArray(new Option[optionList.size()]);
    }

    @Test
    public void testUserSelfRegistration()
            throws IdentityRecoveryException, IdentityStoreException, UserNotFoundException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance.");

        UserSelfSignUpManager selfSignUpManager = bundleContext.getService(bundleContext.getServiceReference
                (UserSelfSignUpManager.class));
        Assert.assertNotNull(selfSignUpManager, "Failed to get user self sign-up service instance.");

        Claim claim = new Claim(IdentityMgtConstants.CLAIM_ROOT_DIALECT, IdentityMgtConstants.USERNAME_CLAIM,
                                "omindu");

        List<Claim> claims = new ArrayList<>();
        claims.add(claim);

        PasswordCallback passwordCallback = new PasswordCallback("pass", false);
        passwordCallback.setPassword(new char[] {'p', 'a', 's', 's'});
        List<Callback> credentials = new ArrayList<>(1);
        credentials.add(passwordCallback);

        UserBean userBean = new UserBean();
        userBean.setClaims(claims);
        userBean.setCredentials(credentials);

        NotificationResponseBean responseBean = selfSignUpManager.registerUser(userBean, null, null);
        Assert.assertNotNull(responseBean.getUserUniqueId(), "Unique user ID is null.");
        Assert.assertNotNull(responseBean.getCode(), "Confirmation code is null.");

        // Check isUserConfirmed.
        Assert.assertFalse(selfSignUpManager.isUserConfirmed(responseBean.getUserUniqueId()), "User should not be" +
                                                                                              "confirmed.");

        User user = realmService.getIdentityStore().getUser(responseBean.getUserUniqueId());
        Assert.assertNotNull(user, String.format("User: %s not found.", responseBean.getUserUniqueId()));

        // Try to authenticate locked user.
        try {
            realmService.getIdentityStore().authenticate(claim, new Callback[] {passwordCallback}, null);
            Assert.fail("Authenticated unconfirmed user.");
        } catch (AuthenticationFailure authenticationFailure) {
            Assert.fail("Error occurred while authenticating user.", authenticationFailure);
        } catch (IdentityStoreException e) {
            if (ACCOUNT_UNVERIFIED.getCode().equals("" + e.getErrorCode())) {
                Assert.assertTrue(true);
            } else {
                Assert.fail("Expected error code: " + ACCOUNT_UNVERIFIED.getCode() + " found: " + e
                        .getErrorCode());
            }
        }

        // Resend confirmation code test.
        NotificationResponseBean response = selfSignUpManager.resendConfirmationCode(claim, null, null);

        Assert.assertNotNull(response.getUserUniqueId(), "Unique user ID is null for resend confirmation code " +
                                                         "response.");
        Assert.assertNotNull(response.getCode(), "Confirmation code is null for resend confirmation code " +
                                                 "response.");
        // Confirm user.
        try {
            selfSignUpManager.confirmUserSelfSignUp(response.getCode());

            Assert.assertTrue(true);
        } catch (IdentityRecoveryException e) {
            Assert.fail("Error occurred while self sign-up confirmation.");
            throw e;
        }
    }

    @Test
    public void testSelfSinUpForInvalidUser() {

        UserSelfSignUpManager selfSignUpManager = bundleContext.getService(bundleContext.getServiceReference
                (UserSelfSignUpManager.class));
        Assert.assertNotNull(selfSignUpManager, "Failed to get user self sign-up service instance.");

        try {
            selfSignUpManager.registerUser(null, null, null);
            Assert.fail();
        } catch (IdentityRecoveryException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testResendConfirmationCodeForInvalidUser() {
        UserSelfSignUpManager selfSignUpManager = bundleContext.getService(bundleContext.getServiceReference
                (UserSelfSignUpManager.class));
        Assert.assertNotNull(selfSignUpManager, "Failed to get user self sign-up service instance.");

        Claim claim = new Claim(IdentityMgtConstants.CLAIM_ROOT_DIALECT, IdentityMgtConstants.USERNAME_CLAIM,
                                "invalidUser");
        try {
            NotificationResponseBean response = selfSignUpManager.resendConfirmationCode(claim, null, null);
            Assert.assertNull(response, "Cannot have a valid response for invalid user.");
        } catch (IdentityRecoveryException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testConfirmInvalidCode() {
        UserSelfSignUpManager selfSignUpManager = bundleContext.getService(bundleContext.getServiceReference
                (UserSelfSignUpManager.class));
        Assert.assertNotNull(selfSignUpManager, "Failed to get user self sign-up service instance.");
        try {
            selfSignUpManager.confirmUserSelfSignUp("invalid");
            Assert.assertTrue(false, "Invalid code code confirmed.");
        } catch (IdentityRecoveryException e) {
            Assert.assertTrue(true);
        }
    }
}

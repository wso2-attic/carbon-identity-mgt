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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.wso2.carbon.identity.mgt.test.osgi.util.IdentityMgtOSGITestConstants;
import org.wso2.carbon.identity.mgt.test.osgi.util.IdentityMgtOSGiTestUtils;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;

import static org.ops4j.pax.exam.CoreOptions.systemProperty;

/**
 * Test class for handling Account Locked
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class PasswordHistoryTests {

    private static List<User> users = new ArrayList<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordHistoryTests.class);
    private static final String PASSWORD_PREFIX = "admin";

    @Inject
    private BundleContext bundleContext;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Configuration
    public Option[] createConfiguration() {

        List<Option> optionList = IdentityMgtOSGiTestUtils.getDefaultSecurityPAXOptions();

        optionList.add(systemProperty(IdentityMgtOSGITestConstants.JAVA_SEC_SYSTEM_PROPERTY)
                .value(Paths.get(IdentityMgtOSGiTestUtils.getCarbonHome(),
                        IdentityMgtOSGITestConstants.CARBON_DIRECTORY_CONF,
                        IdentityMgtOSGITestConstants.CARBON_DIRECTORY_SECURITY,
                        IdentityMgtOSGITestConstants.JAAS_CONFIG_FILE).toString()));

        return optionList.toArray(new Option[optionList.size()]);
    }

    @Test(groups = "passwordHistoryValidation", description = "Password History Validation with respect to last N " +
            "number of attemps")
    public void passwordHistoryValidationByCount() throws IdentityStoreException, AuthenticationFailure,
            UserNotFoundException {

        String uniqueUserId = addUser("testUser1");
        RealmService realmService = null;
        try {
            realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
            Assert.assertNotNull(realmService, "Failed to get realm service instance");

            realmService.getIdentityStore().updateUserCredentials(uniqueUserId, generateCredentials(PASSWORD_PREFIX));

            for (int i = 0; i < 4; i++) {
                realmService.getIdentityStore().updateUserCredentials(uniqueUserId, generateCredentials
                        (PASSWORD_PREFIX + i));
            }
            try {
                realmService.getIdentityStore().updateUserCredentials(uniqueUserId,
                        generateCredentials(PASSWORD_PREFIX));
            } catch (Exception e) {
                Assert.assertTrue((e instanceof IdentityStoreException));
            }
        } finally {
            realmService.getIdentityStore().deleteUser(uniqueUserId);
        }
    }

    @Test(groups = "passwordHistoryValidation", description = "Password History Validation by using sub string of " +
            "previous password")
    public void passwordHistoryValidationByCountWithPwSubString() throws IdentityStoreException, AuthenticationFailure,
            UserNotFoundException {

        String uniqueUserId = addUser("testUser1");
        RealmService realmService = null;
        try {
            realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
            Assert.assertNotNull(realmService, "Failed to get realm service instance");

            realmService.getIdentityStore().updateUserCredentials(uniqueUserId, generateCredentials(PASSWORD_PREFIX));

            for (int i = 0; i < 4; i++) {
                realmService.getIdentityStore().updateUserCredentials(uniqueUserId, generateCredentials
                        (PASSWORD_PREFIX + i));
            }

            realmService.getIdentityStore().updateUserCredentials(uniqueUserId, generateCredentials(PASSWORD_PREFIX.
                    substring(PASSWORD_PREFIX.length() - 2)));
        } finally {
            realmService.getIdentityStore().deleteUser(uniqueUserId);
        }
    }

    @Test(groups = "passwordHistoryValidation", description = "Password History Validation by using different cases")
    public void passwordHistoryValidationByCountWithCaseSensitivity() throws IdentityStoreException,
            AuthenticationFailure, UserNotFoundException {

        String uniqueUserId = addUser("testUser1");
        RealmService realmService = null;

        try {
            realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
            Assert.assertNotNull(realmService, "Failed to get realm service instance");

            realmService.getIdentityStore().updateUserCredentials(uniqueUserId, generateCredentials(PASSWORD_PREFIX));

            for (int i = 0; i < 4; i++) {
                realmService.getIdentityStore().updateUserCredentials(uniqueUserId, generateCredentials
                        (PASSWORD_PREFIX + i));
            }

            realmService.getIdentityStore().updateUserCredentials(uniqueUserId, generateCredentials(PASSWORD_PREFIX.
                    toUpperCase()));
        } finally {
            realmService.getIdentityStore().deleteUser(uniqueUserId);
        }
    }

    @Test(groups = "passwordHistoryValidation", description = "Password History Validation by using last N number of " +
            "passwords")
    public void tryAllLastGivenPasswordsRespectToCount() throws IdentityStoreException,
            AuthenticationFailure, UserNotFoundException {

        String uniqueUserId = addUser("testUser1");
        RealmService realmService = null;
        try {
            realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
            Assert.assertNotNull(realmService, "Failed to get realm service instance");

            realmService.getIdentityStore().updateUserCredentials(uniqueUserId, generateCredentials(PASSWORD_PREFIX));

            for (int i = 0; i < 5; i++) {
                realmService.getIdentityStore().updateUserCredentials(uniqueUserId, generateCredentials
                        (PASSWORD_PREFIX + i));
            }

            for (int i = 0; i < 5; i++) {
                try {
                    realmService.getIdentityStore().updateUserCredentials(uniqueUserId, generateCredentials(
                            PASSWORD_PREFIX + i));
                } catch (Exception e) {
                    Assert.assertTrue((e instanceof IdentityStoreException));
                }
            }
        } finally {
            realmService.getIdentityStore().deleteUser(uniqueUserId);
        }
    }

    @Test(groups = "passwordHistoryValidation",
            description = "Password History Validation with respect to given time duration")
    public void passwordHistoryValidationByTime() throws IdentityStoreException, AuthenticationFailure,
            UserNotFoundException {

        String uniqueUserId = addUser("testUser1");
        RealmService realmService = null;

        try {
            realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
            Assert.assertNotNull(realmService, "Failed to get realm service instance");

            realmService.getIdentityStore().updateUserCredentials(uniqueUserId, generateCredentials(PASSWORD_PREFIX));

            for (int i = 0; i < 5; i++) {
                realmService.getIdentityStore().updateUserCredentials(uniqueUserId, generateCredentials
                        (PASSWORD_PREFIX + i));
            }

            try {
                realmService.getIdentityStore().updateUserCredentials(uniqueUserId,
                        generateCredentials(PASSWORD_PREFIX));
            } catch (Exception e) {
                Assert.assertTrue((e instanceof IdentityStoreException));
            }
        } finally {
            realmService.getIdentityStore().deleteUser(uniqueUserId);
        }

    }

    @Test(groups = "passwordHistoryValidation", description = "Password History Validation with respect to given " +
            "time duration by using sub string of previous password")
    public void passwordHistoryValidationByTimeWithPwSubString() throws IdentityStoreException, AuthenticationFailure,
            UserNotFoundException {

        String uniqueUserId = addUser("testUser1");
        RealmService realmService = null;
        try {
            realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
            Assert.assertNotNull(realmService, "Failed to get realm service instance");

            realmService.getIdentityStore().updateUserCredentials(uniqueUserId, generateCredentials(PASSWORD_PREFIX));

            for (int i = 0; i < 5; i++) {
                realmService.getIdentityStore().updateUserCredentials(uniqueUserId, generateCredentials
                        (PASSWORD_PREFIX + i));
            }

            realmService.getIdentityStore().updateUserCredentials(uniqueUserId,
                    generateCredentials(PASSWORD_PREFIX.substring(PASSWORD_PREFIX.length() - 2)));
        } finally {
            realmService.getIdentityStore().deleteUser(uniqueUserId);
        }
    }

    @Test(groups = "passwordHistoryValidation", description = "Password History Validation with respect to given " +
            "time duration by using different cases of previous password")
    public void passwordHistoryValidationByTimeWithCaseSensitivity() throws IdentityStoreException,
            AuthenticationFailure, UserNotFoundException {

        String uniqueUserId = addUser("testUser1");
        RealmService realmService = null;
        try {

            realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
            Assert.assertNotNull(realmService, "Failed to get realm service instance");

            realmService.getIdentityStore().updateUserCredentials(uniqueUserId, generateCredentials(PASSWORD_PREFIX));

            for (int i = 0; i < 5; i++) {
                realmService.getIdentityStore().updateUserCredentials(uniqueUserId, generateCredentials
                        (PASSWORD_PREFIX + i));
            }

            realmService.getIdentityStore().updateUserCredentials(uniqueUserId, generateCredentials(PASSWORD_PREFIX.
                    toUpperCase()));
        } finally {
            realmService.getIdentityStore().deleteUser(uniqueUserId);
        }
    }

    @Test(groups = "passwordHistoryValidation", description = "Password History Validation with respect to " +
            "count for Deleted user")
    public void passwordHistoryValidationByCountForDeletedUser() throws IdentityStoreException, AuthenticationFailure,
            UserNotFoundException {

        String uniqueUserId = addUser("testUser1");
        RealmService realmService = null;
        try {
            realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
            Assert.assertNotNull(realmService, "Failed to get realm service instance");

            realmService.getIdentityStore().updateUserCredentials(uniqueUserId, generateCredentials(PASSWORD_PREFIX));

            for (int i = 0; i < 4; i++) {
                realmService.getIdentityStore().updateUserCredentials(uniqueUserId, generateCredentials
                        (PASSWORD_PREFIX + i));
            }
            uniqueUserId = addUser("testUser1");
            realmService.getIdentityStore().updateUserCredentials(uniqueUserId, generateCredentials(PASSWORD_PREFIX));
        } finally {
            realmService.getIdentityStore().deleteUser(uniqueUserId);
        }
    }

    @Test(groups = "passwordHistoryValidation", description = "Password History Validation with respect to " +
            "Time for Deleted user")
    public void passwordHistoryValidationByTimeForDeletedUser() throws IdentityStoreException, AuthenticationFailure,
            UserNotFoundException {

        String uniqueUserId = addUser("testUser1");
        RealmService realmService = null;
        try {
            realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
            Assert.assertNotNull(realmService, "Failed to get realm service instance");

            realmService.getIdentityStore().updateUserCredentials(uniqueUserId, generateCredentials(PASSWORD_PREFIX));

            for (int i = 0; i < 5; i++) {
                realmService.getIdentityStore().updateUserCredentials(uniqueUserId, generateCredentials
                        (PASSWORD_PREFIX + i));
            }

            realmService.getIdentityStore().deleteUser(uniqueUserId);
            uniqueUserId = addUser("testUser1");
            realmService.getIdentityStore().updateUserCredentials(uniqueUserId, generateCredentials(PASSWORD_PREFIX));
        } finally {
            realmService.getIdentityStore().deleteUser(uniqueUserId);
        }
    }


    private String addUser(String username) throws IdentityStoreException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        UserBean userBean = new UserBean();
        List<Claim> claims = Arrays
                .asList(new Claim(IdentityMgtOSGITestConstants.ClaimURIs.WSO2_DIALECT_URI,
                                IdentityMgtOSGITestConstants.ClaimURIs.USERNAME_CLAIM_URI, username),
                        new Claim(IdentityMgtOSGITestConstants.ClaimURIs.WSO2_DIALECT_URI,
                                IdentityMgtOSGITestConstants.ClaimURIs.FIRST_NAME_CLAIM_URI, "user_firstName"),
                        new Claim(IdentityMgtOSGITestConstants.ClaimURIs.WSO2_DIALECT_URI,
                                IdentityMgtOSGITestConstants.ClaimURIs.LAST_NAME_CLAIM_URI, "user_lastName"),
                        new Claim(IdentityMgtOSGITestConstants.ClaimURIs.WSO2_DIALECT_URI,
                                IdentityMgtOSGITestConstants.ClaimURIs.EMAIL_CLAIM_URI, "john@wso2.com"));

        List<Callback> credentialsList = new ArrayList<>();
        PasswordCallback passwordCallback = new PasswordCallback(IdentityMgtOSGITestConstants.PASSWORD_CALLBACK, false);
        passwordCallback.setPassword("admin".toCharArray());
        credentialsList.add(passwordCallback);

        userBean.setCredentials(credentialsList);
        userBean.setClaims(claims);
        User user = realmService.getIdentityStore().addUser(userBean);

        return user.getUniqueUserId();
    }

    private List<Callback> generateCredentials(String password) {
        List<Callback> credentialsList = new ArrayList<>();
        PasswordCallback passwordCallback =
                new PasswordCallback(IdentityMgtOSGITestConstants.PASSWORD_CALLBACK, false);
        passwordCallback.setPassword(password.toCharArray());
        credentialsList.add(passwordCallback);
        return credentialsList;
    }
}

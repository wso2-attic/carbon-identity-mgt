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
import org.wso2.carbon.identity.mgt.AuthenticationContext;
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
public class AccountLockHandlerTest {

    private static List<User> users = new ArrayList<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountDisabledHandlerTest.class);

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

    @Test(groups = "accountLockWithInvalidCredential")
    public void testLockUserInvalidCredential() throws IdentityStoreException, AuthenticationFailure {

        addUser("testLockUser");
        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        Claim usernameClaim = new Claim(IdentityMgtOSGITestConstants.ClaimURIs.WSO2_DIALECT_URI,
                IdentityMgtOSGITestConstants.ClaimURIs.USERNAME_CLAIM_URI, "testLockUser");

        AuthenticationContext context;

        //Success Attempt
        Callback[] credentialsList = new Callback[1];
        PasswordCallback passwordCallback =
                new PasswordCallback(IdentityMgtOSGITestConstants.PASSWORD_CALLBACK, false);
        passwordCallback.setPassword("admin".toCharArray());
        credentialsList[0] = passwordCallback;
        context = realmService.getIdentityStore().authenticate(usernameClaim, credentialsList,
                IdentityMgtOSGITestConstants.PRIMARY_DOMAIN);

        Assert.assertNotNull(context, "Test failed. User authentication failed");
        Assert.assertTrue(context.isAuthenticated(), "Test failed. User authentication failed");


        Callback[] wrongCredentialsList = new Callback[1];
        PasswordCallback wrongPasswordCallback =
                new PasswordCallback(IdentityMgtOSGITestConstants.PASSWORD_CALLBACK, false);
        wrongPasswordCallback.setPassword("admin2".toCharArray());
        wrongCredentialsList[0] = wrongPasswordCallback;

        //Failed attempt 1
        context = realmService.getIdentityStore().authenticate(usernameClaim, wrongCredentialsList,
                IdentityMgtOSGITestConstants.PRIMARY_DOMAIN);

        Assert.assertFalse(context.isAuthenticated(), "Test Failure." +
                "Successfully authenticated the user with invalid password credential.");

        //Failed attempt 2
        context = realmService.getIdentityStore().authenticate(usernameClaim, wrongCredentialsList,
                IdentityMgtOSGITestConstants.PRIMARY_DOMAIN);

        Assert.assertFalse(context.isAuthenticated(), "Test Failure." +
                "Successfully authenticated the user with invalid password credential.");

        //Failed attempt 3
        context = realmService.getIdentityStore().authenticate(usernameClaim, wrongCredentialsList,
                IdentityMgtOSGITestConstants.PRIMARY_DOMAIN);

        Assert.assertFalse(context.isAuthenticated(), "Test Failure." +
                "Successfully authenticated the user with invalid password credential.");

        //Try with correct credentials. Now the account should be locked.
        context = null;
        try {
            context = realmService.getIdentityStore().authenticate(usernameClaim, credentialsList,
                    IdentityMgtOSGITestConstants.PRIMARY_DOMAIN);
        } catch (IdentityStoreException e) {
            LOGGER.info("Test passed. Authentication failure for the locked user");
        }

        Assert.assertNull(context, "Test Failure. User account is not locked");
    }

    @Test(groups = "adminUnlockUserAccount", dependsOnGroups = {"accountLockWithInvalidCredential"})
    public void testUnlockUserByAdmin() throws AuthenticationFailure, IdentityStoreException, UserNotFoundException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));

        Claim usernameClaim = new Claim(IdentityMgtOSGITestConstants.ClaimURIs.WSO2_DIALECT_URI,
                IdentityMgtOSGITestConstants.ClaimURIs.USERNAME_CLAIM_URI, "testLockUser");

        Callback[] credentialsList = new Callback[1];
        PasswordCallback passwordCallback =
                new PasswordCallback(IdentityMgtOSGITestConstants.PASSWORD_CALLBACK, false);
        passwordCallback.setPassword("admin".toCharArray());
        credentialsList[0] = passwordCallback;

        //Success attempt, but the account is locked, So, user should not be able to authenticate
        try {
            realmService.getIdentityStore().authenticate(usernameClaim, credentialsList,
                    IdentityMgtOSGITestConstants.PRIMARY_DOMAIN);
        } catch (IdentityStoreException e) {
            LOGGER.info("Test passed. Authentication failure for the locked user");
        }

        List<Claim> claims = Arrays.asList(usernameClaim,
        new Claim(IdentityMgtOSGITestConstants.ClaimURIs.WSO2_DIALECT_URI,
                IdentityMgtOSGITestConstants.ClaimURIs.ACCOUNT_LOCKED_CLAIM_URI, "false"));

        try {
            realmService.getIdentityStore().updateUserClaims(users.get(0).getUniqueUserId(), claims);
        } catch (IdentityStoreException e) {
            Assert.fail("Failed to update user claims.");
        }

        //Success attempt
        AuthenticationContext context = realmService.getIdentityStore().authenticate(usernameClaim, credentialsList,
                IdentityMgtOSGITestConstants.PRIMARY_DOMAIN);

        Assert.assertTrue(context.isAuthenticated(), "Test failed. User authentication failed");

    }


    @Test(groups = "adminLockUserAccount", dependsOnGroups = {"adminUnlockUserAccount"})
    public void testLockUserByAdmin() throws AuthenticationFailure, IdentityStoreException, UserNotFoundException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Claim usernameClaim = new Claim(IdentityMgtOSGITestConstants.ClaimURIs.WSO2_DIALECT_URI,
                IdentityMgtOSGITestConstants.ClaimURIs.USERNAME_CLAIM_URI, "testLockUser");

        //Success Attempt
        Callback[] credentialsList = new Callback[1];
        PasswordCallback passwordCallback =
                new PasswordCallback(IdentityMgtOSGITestConstants.PASSWORD_CALLBACK, false);
        passwordCallback.setPassword("admin".toCharArray());
        credentialsList[0] = passwordCallback;

        AuthenticationContext context = realmService.getIdentityStore().authenticate(usernameClaim, credentialsList,
                IdentityMgtOSGITestConstants.PRIMARY_DOMAIN);

        Assert.assertNotNull(context, "Test failed. User authentication failed");
        Assert.assertTrue(context.isAuthenticated(), "Test failed. User authentication failed");

        List<Claim> claims = Arrays.asList(usernameClaim,
                new Claim(IdentityMgtOSGITestConstants.ClaimURIs.WSO2_DIALECT_URI,
                        IdentityMgtOSGITestConstants.ClaimURIs.ACCOUNT_LOCKED_CLAIM_URI, "true"));

        try {
            realmService.getIdentityStore().updateUserClaims(users.get(0).getUniqueUserId(), claims);
        } catch (IdentityStoreException e) {
            Assert.fail("Failed to update user claims.");
        }

        //Success attempt, but the account is locked, So, user should not be able to authenticate
        try {
            context = null;
            context = realmService.getIdentityStore().authenticate(usernameClaim, credentialsList,
                    IdentityMgtOSGITestConstants.PRIMARY_DOMAIN);
        } catch (IdentityStoreException e) {
            LOGGER.info("Test passed. Authentication failure for the locked user");
        }

        Assert.assertNull(context, "Test Failure. User account is not locked");
        removeUsers();
    }

    private void addUser(String username) throws IdentityStoreException {
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
                                IdentityMgtOSGITestConstants.ClaimURIs.EMAIL_CLAIM_URI, "nuwandiw@wso2.com"));

        List<Callback> credentialsList = new ArrayList<>();
        PasswordCallback passwordCallback = new PasswordCallback(IdentityMgtOSGITestConstants.PASSWORD_CALLBACK, false);
        passwordCallback.setPassword("admin".toCharArray());
        credentialsList.add(passwordCallback);

        userBean.setCredentials(credentialsList);
        userBean.setClaims(claims);
        User user = realmService.getIdentityStore().addUser(userBean);

        Assert.assertNotNull(user, "Failed to receive the user.");
        Assert.assertNotNull(user.getUniqueUserId(), "Invalid user unique id.");

        users.add(user);
    }

    private void removeUsers() throws IdentityStoreException, UserNotFoundException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        for (User user : users) {
            realmService.getIdentityStore().deleteUser(user.getUniqueUserId());
        }
    }

}

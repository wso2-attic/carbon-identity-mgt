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
import org.wso2.carbon.identity.mgt.Group;
import org.wso2.carbon.identity.mgt.RealmService;
import org.wso2.carbon.identity.mgt.User;
import org.wso2.carbon.identity.mgt.bean.GroupBean;
import org.wso2.carbon.identity.mgt.bean.UserBean;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.exception.AuthenticationFailure;
import org.wso2.carbon.identity.mgt.exception.CredentialStoreConnectorException;
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
 * Test class for handling Account Disabled
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class AccountDisabledHandlerTest {

    private static List<User> users = new ArrayList<>();
    private static List<Group> groups = new ArrayList<>();
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

    @Test(groups = "adminDisabledUserAccount")
    public void testDisableUserByAdmin() throws IdentityStoreException, UserNotFoundException,
            CredentialStoreConnectorException, AuthenticationFailure {

        addUser("testUser");
        addGroups();
        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        Claim usernameClaim = new Claim(IdentityMgtOSGITestConstants.ClaimURIs.WSO2_DIALECT_URI,
                IdentityMgtOSGITestConstants.ClaimURIs.USERNAME_CLAIM_URI, "testUser");
        List<Claim> claims = Arrays
                .asList(usernameClaim,
                        new Claim(IdentityMgtOSGITestConstants.ClaimURIs.WSO2_DIALECT_URI,
                                IdentityMgtOSGITestConstants.ClaimURIs.FIRST_NAME_CLAIM_URI, "user_firstName"),
                        new Claim(IdentityMgtOSGITestConstants.ClaimURIs.WSO2_DIALECT_URI,
                                IdentityMgtOSGITestConstants.ClaimURIs.LAST_NAME_CLAIM_URI, "user_lastName"),
                        new Claim(IdentityMgtOSGITestConstants.ClaimURIs.WSO2_DIALECT_URI,
                                IdentityMgtOSGITestConstants.ClaimURIs.EMAIL_CLAIM_URI, "nuwandiw@wso2.com"),
                        new Claim(IdentityMgtOSGITestConstants.ClaimURIs.WSO2_DIALECT_URI,
                                IdentityMgtOSGITestConstants.ClaimURIs.ACCOUNT_DISABLED_CLAIM_URI, "true"));

        try {
            realmService.getIdentityStore().updateUserClaims(users.get(0).getUniqueUserId(), claims);
        } catch (IdentityStoreException e) {
            Assert.fail("Failed to update user claims.");
        }

        Callback[] credentialsList = new Callback[1];
        PasswordCallback passwordCallback = new PasswordCallback(IdentityMgtOSGITestConstants.PASSWORD_CALLBACK, false);
        passwordCallback.setPassword("admin".toCharArray());
        credentialsList[0] = passwordCallback;

        AuthenticationContext context = null;

        try {
            context = realmService.getIdentityStore().authenticate(usernameClaim, credentialsList,
                    IdentityMgtOSGITestConstants.PRIMARY_DOMAIN);
        } catch (IdentityStoreException e) {
            LOGGER.info("Test passed. Authentication failure for the user with invalid credentials.");
        }

        Assert.assertNull(context, "Test Failure. User account is not disabled.");

    }

    @Test(groups = "disabledUserAccount", dependsOnGroups = {"adminDisabledUserAccount"})
    public void testUpdateClaimsDisabledUserPUT() throws IdentityStoreException, UserNotFoundException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        List<Claim> claims = Arrays
                .asList(new Claim(IdentityMgtOSGITestConstants.ClaimURIs.WSO2_DIALECT_URI,
                                IdentityMgtOSGITestConstants.ClaimURIs.USERNAME_CLAIM_URI, "testUser.put"),
                        new Claim(IdentityMgtOSGITestConstants.ClaimURIs.WSO2_DIALECT_URI,
                                IdentityMgtOSGITestConstants.ClaimURIs.FIRST_NAME_CLAIM_URI, "user_firstName.put"),
                        new Claim(IdentityMgtOSGITestConstants.ClaimURIs.WSO2_DIALECT_URI,
                                IdentityMgtOSGITestConstants.ClaimURIs.LAST_NAME_CLAIM_URI, "put.user_lastName.put"),
                        new Claim(IdentityMgtOSGITestConstants.ClaimURIs.WSO2_DIALECT_URI,
                                IdentityMgtOSGITestConstants.ClaimURIs.EMAIL_CLAIM_URI, "put.nuwandiw@wso2.com"));

        try {
            realmService.getIdentityStore().updateUserClaims(users.get(0).getUniqueUserId(), claims);
        } catch (IdentityStoreException e) {
            LOGGER.info("Test passed. Cannot update user profile of disabled user.");
        }

    }

    @Test(groups = "disabledUserAccount", dependsOnGroups = {"adminDisabledUserAccount"})
    public void testUpdateClaimsDisabledUserPATCH() throws IdentityStoreException, UserNotFoundException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        List<Claim> claimsToAdd = Arrays
                .asList(new Claim(IdentityMgtOSGITestConstants.ClaimURIs.WSO2_DIALECT_URI,
                                IdentityMgtOSGITestConstants.ClaimURIs.USERNAME_CLAIM_URI, "testUser.patch"),
                        new Claim(IdentityMgtOSGITestConstants.ClaimURIs.WSO2_DIALECT_URI,
                                IdentityMgtOSGITestConstants.ClaimURIs.FIRST_NAME_CLAIM_URI, "user_firstName.patch"));

        List<Claim> claimsToRemove = Arrays
                .asList(new Claim(IdentityMgtOSGITestConstants.ClaimURIs.WSO2_DIALECT_URI,
                        IdentityMgtOSGITestConstants.ClaimURIs.LAST_NAME_CLAIM_URI, "user_lastName"));

        try {
            realmService.getIdentityStore().updateUserClaims(users.get(0).getUniqueUserId(),
                    claimsToAdd, claimsToRemove);
        } catch (IdentityStoreException e) {
            LOGGER.info("Test passed. Cannot update user profile of disabled user.");
        }

    }

    @Test(groups = "disabledUserAccount", dependsOnGroups = {"adminDisabledUserAccount"})
    public void testUpdateCredentialDisabledUserPUT() throws UserNotFoundException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        List<Callback> credentialsList = new ArrayList<>();
        PasswordCallback passwordCallback = new PasswordCallback(IdentityMgtOSGITestConstants.PASSWORD_CALLBACK, false);
        passwordCallback.setPassword("admin2".toCharArray());
        credentialsList.add(passwordCallback);

        try {
            realmService.getIdentityStore().updateUserCredentials(users.get(0).getUniqueUserId(), credentialsList);
        } catch (IdentityStoreException e) {
            LOGGER.info("Test passed. Cannot update credentials of disabled user.");
        }

    }

    @Test(groups = "disabledUserAccount", dependsOnGroups = {"adminDisabledUserAccount"})
    public void testUpdateCredentialDisabledUserPATCH() throws UserNotFoundException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        List<Callback> credentialsToAdd = new ArrayList<>();
        PasswordCallback passwordCallback1 = new PasswordCallback(
                IdentityMgtOSGITestConstants.PASSWORD_CALLBACK, false);
        passwordCallback1.setPassword("admin".toCharArray());
        credentialsToAdd.add(passwordCallback1);

        List<Callback> credentialsToRemove = new ArrayList<>();
        PasswordCallback passwordCallback2 = new PasswordCallback(
                IdentityMgtOSGITestConstants.PASSWORD_CALLBACK, false);
        passwordCallback2.setPassword("admin2".toCharArray());
        credentialsToRemove.add(passwordCallback2);

        try {
            realmService.getIdentityStore().updateUserCredentials(users.get(0).getUniqueUserId(),
                    credentialsToAdd, credentialsToRemove);
        } catch (IdentityStoreException e) {
            LOGGER.info("Test passed. Cannot update credentials of disabled user.");
        }

    }

    @Test(groups = "disabledUserAccount", dependsOnGroups = {"adminDisabledUserAccount"})
    public void testDeleteDisabledUser() throws UserNotFoundException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        try {
            realmService.getIdentityStore().deleteUser(users.get(0).getUniqueUserId());
        } catch (IdentityStoreException e) {
            LOGGER.info("Test passed. Cannot delete disabled user.");
        }

    }

    @Test(groups = "disabledUserAccount", dependsOnGroups = {"adminDisabledUserAccount"})
    public void testUpdateGroupsForDisabledUserPUT() throws UserNotFoundException, IdentityStoreException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        try {
            realmService.getIdentityStore().updateGroupsOfUser(users.get(0).getUniqueUserId(),
                    Arrays.asList(groups.get(0).getUniqueGroupId(), groups.get(1).getUniqueGroupId()));
        } catch (IdentityStoreException e) {
            LOGGER.info("Test passed. Cannot update groups of disabled user.");
        }

    }

    @Test(groups = "disabledUserAccount", dependsOnGroups = {"adminDisabledUserAccount"})
    public void testUpdateGroupsForDisabledUserPATCH() throws UserNotFoundException, IdentityStoreException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        try {
            realmService.getIdentityStore().updateGroupsOfUser(users.get(0).getUniqueUserId(),
                    Arrays.asList(groups.get(2).getUniqueGroupId()),
                    Arrays.asList(groups.get(0).getUniqueGroupId(), groups.get(1).getUniqueGroupId()));
        } catch (IdentityStoreException e) {
            LOGGER.info("Test passed. Cannot update groups of disabled user.");
        }

    }

    @Test(groups = "disabledUserAccount", dependsOnGroups = {"adminDisabledUserAccount"})
    public void testUpdateUsersOfGroupPUT() throws UserNotFoundException, IdentityStoreException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        List<String> userIds = new ArrayList<>();
        userIds.add(users.get(0).getUniqueUserId());

        try {
            realmService.getIdentityStore().updateUsersOfGroup(groups.get(3).getUniqueGroupId(), userIds);
        } catch (IdentityStoreException e) {
            LOGGER.info("Test passed. Cannot update groups with disabled user.");
        }

    }

    @Test(groups = "disabledUserAccount", dependsOnGroups = {"adminDisabledUserAccount"})
    public void testUpdateUsersOfGroupPATCH() throws UserNotFoundException, IdentityStoreException {

        addUser("user2");
        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        List<String> userIdsToAdd = new ArrayList<>();
        userIdsToAdd.add(users.get(0).getUniqueUserId());

        List<String> userIdsToRemove = new ArrayList<>();
        userIdsToRemove.add(users.get(1).getUniqueUserId());

        try {
            realmService.getIdentityStore().updateUsersOfGroup(groups.get(3).getUniqueGroupId(),
                    userIdsToAdd, userIdsToRemove);
        } catch (IdentityStoreException e) {
            LOGGER.info("Test passed. Cannot update groups with disabled user.");
        }

    }

    @Test(groups = "enabledUserAccount", dependsOnGroups = {"adminDisabledUserAccount"})
    public void testEnableUserByAdmin() throws AuthenticationFailure, UserNotFoundException, IdentityStoreException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        Claim usernameClaim = new Claim(IdentityMgtOSGITestConstants.ClaimURIs.WSO2_DIALECT_URI,
                IdentityMgtOSGITestConstants.ClaimURIs.USERNAME_CLAIM_URI, "testUser");

        Callback[] credentialsList = new Callback[1];
        PasswordCallback passwordCallback = new PasswordCallback(
                IdentityMgtOSGITestConstants.PASSWORD_CALLBACK, false);
        passwordCallback.setPassword("admin".toCharArray());
        credentialsList[0] = passwordCallback;

        AuthenticationContext context = null;

        try {
            context = realmService.getIdentityStore().authenticate(usernameClaim, credentialsList,
                    IdentityMgtOSGITestConstants.PRIMARY_DOMAIN);
        } catch (IdentityStoreException e) {
            LOGGER.info("Test passed. Authentication failure disabled user.");
        }

        Assert.assertNull(context, "Test Failure. User account is not disabled.");

        List<Claim> claims = Arrays
                .asList(usernameClaim,
                        new Claim(IdentityMgtOSGITestConstants.ClaimURIs.WSO2_DIALECT_URI,
                                IdentityMgtOSGITestConstants.ClaimURIs.FIRST_NAME_CLAIM_URI, "user_firstName"),
                        new Claim(IdentityMgtOSGITestConstants.ClaimURIs.WSO2_DIALECT_URI,
                                IdentityMgtOSGITestConstants.ClaimURIs.LAST_NAME_CLAIM_URI, "user_lastName"),
                        new Claim(IdentityMgtOSGITestConstants.ClaimURIs.WSO2_DIALECT_URI,
                                IdentityMgtOSGITestConstants.ClaimURIs.EMAIL_CLAIM_URI, "nuwandiw@wso2.com"),
                        new Claim(IdentityMgtOSGITestConstants.ClaimURIs.WSO2_DIALECT_URI,
                                IdentityMgtOSGITestConstants.ClaimURIs.ACCOUNT_DISABLED_CLAIM_URI, "false"));

        try {
            realmService.getIdentityStore().updateUserClaims(users.get(0).getUniqueUserId(), claims);
        } catch (IdentityStoreException e) {
            Assert.fail("Failed to update user claims.");
        }

        try {
            context = realmService.getIdentityStore().authenticate(usernameClaim, credentialsList,
                    IdentityMgtOSGITestConstants.PRIMARY_DOMAIN);
        } catch (IdentityStoreException e) {
            Assert.fail("Failed to authenticate enabled user");
        }

        Assert.assertNotNull(context, "Test Failed. User not authenticated");
        Assert.assertTrue(context.isAuthenticated(), "Test Passed. User enabled and authenticated");
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

    private void addGroups() throws IdentityStoreException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        GroupBean groupBean;
        List<Claim> claims;

        for (int i = 0; i < 4; i++) {
            groupBean = new GroupBean();
            claims = Arrays.asList(
                    new Claim(IdentityMgtOSGITestConstants.ClaimURIs.WSO2_DIALECT_URI,
                            IdentityMgtOSGITestConstants.ClaimURIs.GROUP_NAME_CLAIM_URI, "Angels" + i),
                    new Claim(IdentityMgtOSGITestConstants.ClaimURIs.WSO2_DIALECT_URI,
                            IdentityMgtOSGITestConstants.ClaimURIs.ORGANIZATION_CLAIM_URI, "Heaven" + i));

            groupBean.setClaims(claims);
            Group group = realmService.getIdentityStore().addGroup(groupBean);
            Assert.assertNotNull(group, "Failed to receive the group.");
            Assert.assertNotNull(group.getUniqueGroupId(), "Invalid group unique id.");
            groups.add(group);
        }
    }

    private void removeUsers() throws IdentityStoreException, UserNotFoundException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        for (User user : users) {
            realmService.getIdentityStore().deleteUser(user.getUniqueUserId());
        }
    }
}

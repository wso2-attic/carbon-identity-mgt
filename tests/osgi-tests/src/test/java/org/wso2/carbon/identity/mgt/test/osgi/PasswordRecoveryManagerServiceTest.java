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
import org.wso2.carbon.identity.mgt.AuthenticationContext;
import org.wso2.carbon.identity.mgt.RealmService;
import org.wso2.carbon.identity.mgt.bean.UserBean;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.exception.AuthenticationFailure;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.mgt.test.osgi.util.IdentityMgtOSGITestConstants;
import org.wso2.carbon.identity.mgt.test.osgi.util.IdentityMgtOSGiTestUtils;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.bean.NotificationResponseBean;
import org.wso2.carbon.identity.recovery.model.UserRecoveryData;
import org.wso2.carbon.identity.recovery.password.NotificationPasswordRecoveryManager;
import org.wso2.carbon.identity.recovery.store.JDBCRecoveryDataStore;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;

import static org.ops4j.pax.exam.CoreOptions.systemProperty;

/**
 * Test class for password recovery.
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class PasswordRecoveryManagerServiceTest {

    @Inject
    private BundleContext bundleContext;

    @Inject
    private RealmService realmService;

    @Configuration
    public Option[] createConfiguration() {

        List<Option> optionList = IdentityMgtOSGiTestUtils.getDefaultSecurityPAXOptions();

        optionList.add(systemProperty("java.security.auth.login.config")
                .value(Paths.get(IdentityMgtOSGiTestUtils.getCarbonHome(), "conf", "security",
                        "carbon-jaas.config").toString()));

        return optionList.toArray(new Option[optionList.size()]);
    }

    @Test(groups = "passwordRecovery")
    public void testSendPasswordRecoveryNotification()
            throws IdentityStoreException, UserNotFoundException, IdentityRecoveryException, AuthenticationFailure {
        addUser();
        NotificationPasswordRecoveryManager notificationPasswordRecoveryManager = bundleContext
                .getService(bundleContext.getServiceReference(NotificationPasswordRecoveryManager.class));
        Assert.assertNotNull(notificationPasswordRecoveryManager,
                "Failed to get NotificationPasswordRecoveryManager instance");

        Claim claim = new Claim(IdentityMgtOSGITestConstants.ClaimURIs.WSO2_DIALECT_URI,
                IdentityMgtOSGITestConstants.ClaimURIs.USERNAME_CLAIM_URI, "johnw");
        String userUniqueId = realmService.getIdentityStore().getUser(claim).getUniqueUserId();
        NotificationResponseBean notificationResponseBean = notificationPasswordRecoveryManager
                .sendRecoveryNotification(userUniqueId, false);
        Assert.assertNotNull(notificationResponseBean, "Failed to retrieve the notification bean");
        Assert.assertNotNull(notificationResponseBean.getKey(), "Failed to retrieve the notification code");
    }

    @Test(groups = "passwordRecovery")
    public void testUpdatePassword()
            throws IdentityRecoveryException, IdentityStoreException, AuthenticationFailure, UserNotFoundException {
        NotificationPasswordRecoveryManager notificationPasswordRecoveryManager = bundleContext
                .getService(bundleContext.getServiceReference(NotificationPasswordRecoveryManager.class));
        Assert.assertNotNull(notificationPasswordRecoveryManager,
                "Failed to get NotificationPasswordRecoveryManager instance");

        Claim usernameClaim = new Claim(IdentityMgtOSGITestConstants.ClaimURIs.WSO2_DIALECT_URI,
                IdentityMgtOSGITestConstants.ClaimURIs.USERNAME_CLAIM_URI, "johnw");
        String userUniqueId = realmService.getIdentityStore().getUser(usernameClaim).getUniqueUserId();
        UserRecoveryData userRecoveryData = JDBCRecoveryDataStore.getInstance().loadByUserUniqueId(userUniqueId);
        char[] password = "testpasschange".toCharArray();
        notificationPasswordRecoveryManager.updatePassword(userRecoveryData.getSecret(), password);

        PasswordCallback passwordCallback = new PasswordCallback("password", false);
        passwordCallback.setPassword(password);
        AuthenticationContext authenticationContext = realmService.getIdentityStore()
                .authenticate(usernameClaim, new Callback[] { passwordCallback },
                        IdentityMgtOSGITestConstants.PRIMARY_DOMAIN);

        Assert.assertNotNull(authenticationContext.getUser(), "Failed to authenticate the user with updated password.");
        Assert.assertNotNull(authenticationContext.getUser().getUniqueUserId(), "Invalid user unique id.");
        deleteUser();
    }

    private void addUser() throws IdentityStoreException, AuthenticationFailure {
        UserBean userBean = new UserBean();
        List<Claim> claims = Arrays.asList(new Claim(IdentityMgtOSGITestConstants.ClaimURIs.WSO2_DIALECT_URI,
                        IdentityMgtOSGITestConstants.ClaimURIs.USERNAME_CLAIM_URI, "johnw"),
                new Claim(IdentityMgtOSGITestConstants.ClaimURIs.WSO2_DIALECT_URI,
                        IdentityMgtOSGITestConstants.ClaimURIs.FIRST_NAME_CLAIM_URI, "John"),
                new Claim(IdentityMgtOSGITestConstants.ClaimURIs.WSO2_DIALECT_URI,
                        IdentityMgtOSGITestConstants.ClaimURIs.LAST_NAME_CLAIM_URI, "Wick"),
                new Claim(IdentityMgtOSGITestConstants.ClaimURIs.WSO2_DIALECT_URI,
                        IdentityMgtOSGITestConstants.ClaimURIs.EMAIL_CLAIM_URI, "johnw@wso2.com"));
        userBean.setClaims(claims);

        List<Callback> credentialsList = new ArrayList<>();
        PasswordCallback passwordCallback = new PasswordCallback("password", false);
        passwordCallback.setPassword("testpass".toCharArray());
        credentialsList.add(passwordCallback);
        userBean.setCredentials(credentialsList);

        realmService.getIdentityStore().addUser(userBean);
    }

    private void deleteUser() throws IdentityStoreException, UserNotFoundException {
        Claim claim = new Claim(IdentityMgtOSGITestConstants.ClaimURIs.WSO2_DIALECT_URI,
                IdentityMgtOSGITestConstants.ClaimURIs.USERNAME_CLAIM_URI, "johnw");
        String userUniqueId = realmService.getIdentityStore().getUser(claim).getUniqueUserId();
        realmService.getIdentityStore().deleteUser(userUniqueId);
    }

}

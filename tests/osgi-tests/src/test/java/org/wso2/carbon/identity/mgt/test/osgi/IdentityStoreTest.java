/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.mgt.test.osgi;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.BundleContext;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.mgt.bean.User;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.mgt.model.UserModel;
import org.wso2.carbon.identity.mgt.service.RealmService;
import org.wso2.carbon.identity.mgt.test.osgi.util.IdentityMgtOSGiTestUtils;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;

import static org.ops4j.pax.exam.CoreOptions.systemProperty;

/**
 * JAAS OSGI Tests.
 */

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class IdentityStoreTest {

    private static List<User> users = new ArrayList<>();

    @Inject
    private BundleContext bundleContext;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Configuration
    public Option[] createConfiguration() {

        List<Option> optionList = IdentityMgtOSGiTestUtils.getDefaultSecurityPAXOptions();

        optionList.add(systemProperty("java.security.auth.login.config").value(Paths.get(
                IdentityMgtOSGiTestUtils.getCarbonHome(), "conf", "security", "carbon-jaas.config").toString()));

        return optionList.toArray(new Option[optionList.size()]);
    }

    @Test
    public void testAddUser() throws IdentityStoreException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        UserModel userModel = new UserModel();
        List<Claim> claims = Arrays.asList(
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/username", "lucifer"),
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/firstName", "Lucifer"),
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/lastName", "Morningstar"),
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/email", "lucifer@wso2.com"));
        userModel.setClaims(claims);
        User user = realmService.getIdentityStore().addUser(userModel);

        Assert.assertNotNull(user, "Failed to receive the user.");
        Assert.assertNotNull(user.getUniqueUserId(), "Invalid user unique id.");

        users.add(user);
    }

    @Test
    public void testAddUserByDomain() throws IdentityStoreException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        UserModel userModel = new UserModel();
        List<Claim> claims = Arrays.asList(
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/username", "chloe"),
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/firstName", "chloe"),
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/lastName", "Decker"),
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/email", "chloe@wso2.com"));
        userModel.setClaims(claims);
        User user = realmService.getIdentityStore().addUser(userModel, "PRIMARY");

        Assert.assertNotNull(user, "Failed to receive the user.");
        Assert.assertNotNull(user.getUniqueUserId(), "Invalid user unique id.");

        users.add(user);
    }

    @Test(dependsOnMethods = {"testAddUser"})
    public void testGetUserByUniqueUserId() throws IdentityStoreException, UserNotFoundException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        User user = realmService.getIdentityStore().getUser(users.get(0).getUniqueUserId());

        Assert.assertNotNull(user, "Failed to receive the user.");
    }

    @Test(dependsOnMethods = {"testAddUser"})
    public void testGetUserByUniqueUserIdAndDomain() throws IdentityStoreException, UserNotFoundException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        User user = realmService.getIdentityStore().getUser(users.get(0).getUniqueUserId(), "PRIMARY");

        Assert.assertNotNull(user, "Failed to receive the user.");
    }

    @Test(dependsOnMethods = {"testAddUser"})
    public void testGetUserByClaim() throws IdentityStoreException, UserNotFoundException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        User user = realmService.getIdentityStore().getUser(new Claim("http://wso2.org/claims", "http://wso2" +
                ".org/claims/username", "lucifer"));

        Assert.assertNotNull(user, "Failed to receive the user.");

        Assert.assertNotNull(user.getUniqueUserId(), "Invalid user unique id.");
    }

    @Test(dependsOnMethods = {"testAddUser"})
    public void testGetUserByClaimAndDomain() throws IdentityStoreException, UserNotFoundException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        User user = realmService.getIdentityStore().getUser(new Claim("http://wso2.org/claims", "http://wso2" +
                ".org/claims/username", "lucifer"), "PRIMARY");

        Assert.assertNotNull(user, "Failed to receive the user.");

        Assert.assertNotNull(user.getUniqueUserId(), "Invalid user unique id.");
    }
}

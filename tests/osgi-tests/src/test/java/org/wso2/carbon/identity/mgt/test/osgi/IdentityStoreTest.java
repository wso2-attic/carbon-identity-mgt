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
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.BundleContext;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.mgt.bean.Group;
import org.wso2.carbon.identity.mgt.bean.User;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.claim.MetaClaim;
import org.wso2.carbon.identity.mgt.exception.GroupNotFoundException;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.mgt.model.GroupModel;
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
@ExamReactorStrategy(PerSuite.class)
public class IdentityStoreTest {

    private static List<User> users = new ArrayList<>();
    private static List<Group> groups = new ArrayList<>();

    @Inject
    private BundleContext bundleContext;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Configuration
    public Option[] createConfiguration() {

        List<Option> optionList = IdentityMgtOSGiTestUtils.getDefaultSecurityPAXOptions();

        optionList.add(systemProperty("java.security.auth.login.config")
                .value(Paths.get(IdentityMgtOSGiTestUtils.getCarbonHome(), "conf", "security", "carbon-jaas.config")
                        .toString()));

        return optionList.toArray(new Option[optionList.size()]);
    }

    @Test(groups = "addUsers")
    public void testAddUser() throws IdentityStoreException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        UserModel userModel = new UserModel();
        List<Claim> claims = Arrays
                .asList(new Claim("http://wso2.org/claims", "http://wso2.org/claims/username", "lucifer"),
                        new Claim("http://wso2.org/claims", "http://wso2.org/claims/firstName", "Lucifer"),
                        new Claim("http://wso2.org/claims", "http://wso2.org/claims/lastName", "Morningstar"),
                        new Claim("http://wso2.org/claims", "http://wso2.org/claims/email", "lucifer@wso2.com"));
        userModel.setClaims(claims);
        User user = realmService.getIdentityStore().addUser(userModel);

        Assert.assertNotNull(user, "Failed to receive the user.");
        Assert.assertNotNull(user.getUniqueUserId(), "Invalid user unique id.");

        users.add(user);
    }

    @Test(groups = "addUsers")
    public void testAddUserByDomain() throws IdentityStoreException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        UserModel userModel = new UserModel();
        List<Claim> claims = Arrays.asList(
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/username", "chloe"),
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/firstName", "Chloe"),
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/lastName", "Decker"),
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/email", "chloe@wso2.com"));
        userModel.setClaims(claims);
        User user = realmService.getIdentityStore().addUser(userModel, "PRIMARY");

        Assert.assertNotNull(user, "Failed to receive the user.");
        Assert.assertNotNull(user.getUniqueUserId(), "Invalid user unique id.");

        users.add(user);
    }

    @Test(groups = "addUsers")
    public void testAddUsers() throws IdentityStoreException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        UserModel userModel1 = new UserModel();
        List<Claim> claims1 = Arrays.asList(
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/username", "dan"),
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/firstName", "Dan"),
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/lastName", "Espinoza"),
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/email", "dan@wso2.com"));
        userModel1.setClaims(claims1);

        UserModel userModel2 = new UserModel();
        List<Claim> claims2 = Arrays.asList(
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/username", "linda"),
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/firstName", "Linda"),
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/lastName", "Martin"),
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/email", "linda@wso2.com"));
        userModel2.setClaims(claims2);

        List<User> addedUsers = realmService.getIdentityStore().addUsers(Arrays.asList(userModel1, userModel2));

        Assert.assertNotNull(addedUsers, "Failed to receive the users.");
        Assert.assertTrue(!addedUsers.isEmpty() && addedUsers.size() == 2, "Number of users received in the response " +
                "is invalid.");

        users.addAll(addedUsers);
    }

    @Test(groups = "addUsers")
    public void testAddUsersByDomain() throws IdentityStoreException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        UserModel userModel1 = new UserModel();
        List<Claim> claims1 = Arrays.asList(
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/username", "ella"),
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/firstName", "Ella"),
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/lastName", "Lopez"),
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/email", "ella@wso2.com"));
        userModel1.setClaims(claims1);

        UserModel userModel2 = new UserModel();
        List<Claim> claims2 = Arrays.asList(
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/username", "trixie"),
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/firstName", "Trixie"),
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/lastName", "Decker"),
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/email", "trixie@wso2.com"));
        userModel2.setClaims(claims2);

        List<User> addedUsers = realmService.getIdentityStore().addUsers(Arrays.asList(userModel1, userModel2),
                "PRIMARY");

        Assert.assertNotNull(addedUsers, "Failed to receive the users.");
        Assert.assertTrue(!addedUsers.isEmpty() && addedUsers.size() == 2, "Number of users received in the response " +
                "is invalid.");

        users.addAll(addedUsers);
    }

    @Test(dependsOnGroups = {"addUsers"})
    public void testGetUserByUniqueUserId() throws IdentityStoreException, UserNotFoundException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        User user = realmService.getIdentityStore().getUser(users.get(0).getUniqueUserId());

        Assert.assertNotNull(user, "Failed to receive the user.");
    }

    @Test(dependsOnGroups = {"addUsers"})
    public void testGetUserByUniqueUserIdAndDomain() throws IdentityStoreException, UserNotFoundException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        User user = realmService.getIdentityStore().getUser(users.get(0).getUniqueUserId(), "PRIMARY");

        Assert.assertNotNull(user, "Failed to receive the user.");
    }

    @Test(dependsOnGroups = {"addUsers"})
    public void testGetUserByClaim() throws IdentityStoreException, UserNotFoundException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        User user = realmService.getIdentityStore()
                .getUser(new Claim("http://wso2.org/claims", "http://wso2" + ".org/claims/username", "lucifer"));

        Assert.assertNotNull(user, "Failed to receive the user.");

        Assert.assertNotNull(user.getUniqueUserId(), "Invalid user unique id.");
    }

    @Test(dependsOnGroups = {"addUsers"})
    public void testGetUserByClaimAndDomain() throws IdentityStoreException, UserNotFoundException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        User user = realmService.getIdentityStore().getUser(new Claim("http://wso2.org/claims", "http://wso2" +
                ".org/claims/username", "chloe"), "PRIMARY");

        Assert.assertNotNull(user, "Failed to receive the user.");

        Assert.assertNotNull(user.getUniqueUserId(), "Invalid user unique id.");
    }

    @Test(dependsOnGroups = {"addUsers"})
    public void testListUsersByOffsetAndLength() throws IdentityStoreException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        List<User> users = realmService.getIdentityStore().listUsers(2, 3);

        Assert.assertNotNull(users, "Failed to list the users.");
        Assert.assertTrue(!users.isEmpty() && users.size() == 3, "Number of users received in the response " +
                "is invalid.");
    }

    @Test(dependsOnGroups = {"addUsers"})
    public void testListUsersByOffsetAndLengthInADomain() throws IdentityStoreException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        List<User> users = realmService.getIdentityStore().listUsers(2, 3, "PRIMARY");

        Assert.assertNotNull(users, "Failed to list the users.");
        Assert.assertTrue(!users.isEmpty() && users.size() == 3, "Number of users received in the response " +
                "is invalid.");
    }

    @Test(dependsOnGroups = {"addUsers"})
    public void testListUsersByClaimOffsetAndLength() throws IdentityStoreException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        Claim claim = new Claim("http://wso2.org/claims", "http://wso2.org/claims/lastName", "Decker");
        List<User> users = realmService.getIdentityStore().listUsers(claim, 2, 3);

        Assert.assertNotNull(users, "Failed to list the users.");
        Assert.assertTrue(!users.isEmpty() && users.size() == 2, "Number of users received in the response " +
                "is invalid.");
    }

    @Test(dependsOnGroups = {"addUsers"})
    public void testListUsersByClaimOffsetAndLengthInADomain() throws IdentityStoreException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        Claim claim = new Claim("http://wso2.org/claims", "http://wso2.org/claims/lastName", "Decker");
        List<User> users = realmService.getIdentityStore().listUsers(claim, 2, 3, "PRIMARY");

        Assert.assertNotNull(users, "Failed to list the users.");
        Assert.assertTrue(!users.isEmpty() && users.size() == 2, "Number of users received in the response " +
                "is invalid.");
    }

    @Test(dependsOnGroups = {"addUsers"})
    public void testListUsersByMetaClaimFilterPatternOffsetAndLength() throws IdentityStoreException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        MetaClaim metaClaim = new MetaClaim("http://wso2.org/claims", "http://wso2.org/claims/lastName");
        List<User> users = realmService.getIdentityStore().listUsers(metaClaim, "(?i).*cke.*", 2, 3);

        Assert.assertNotNull(users, "Failed to list the users.");
        Assert.assertTrue(!users.isEmpty() && users.size() == 2, "Number of users received in the response " +
                "is invalid.");
    }

    @Test(dependsOnGroups = {"addUsers"})
    public void testListUsersByMetaClaimFilterPatternOffsetAndLengthInDomain() throws IdentityStoreException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        MetaClaim metaClaim = new MetaClaim("http://wso2.org/claims", "http://wso2.org/claims/lastName");
        List<User> users = realmService.getIdentityStore().listUsers(metaClaim, "(?i).*cke.*", 2, 3, "PRIMARY");

        Assert.assertNotNull(users, "Failed to list the users.");
        Assert.assertTrue(!users.isEmpty() && users.size() == 2, "Number of users received in the response " +
                "is invalid.");
    }

    @Test(groups = "addGroups")
    public void testAddGroup() throws IdentityStoreException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        GroupModel groupModel = new GroupModel();
        List<Claim> claims = Arrays.asList(
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/groupName", "Angels"),
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/organization", "Heaven"));
        groupModel.setClaims(claims);
        Group group = realmService.getIdentityStore().addGroup(groupModel);

        Assert.assertNotNull(group, "Failed to receive the group.");
        Assert.assertNotNull(group.getUniqueGroupId(), "Invalid group unique id.");

        groups.add(group);
    }

    @Test(groups = "addGroups")
    public void testAddGroupByDomain() throws IdentityStoreException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        GroupModel groupModel = new GroupModel();
        List<Claim> claims = Arrays.asList(
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/groupName", "Demons"),
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/organization", "Hell"));
        groupModel.setClaims(claims);
        Group group = realmService.getIdentityStore().addGroup(groupModel, "PRIMARY");

        Assert.assertNotNull(group, "Failed to receive the group.");
        Assert.assertNotNull(group.getUniqueGroupId(), "Invalid group unique id.");

        groups.add(group);
    }

    @Test(groups = "addGroups")
    public void testAddGroups() throws IdentityStoreException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        GroupModel groupModel1 = new GroupModel();
        List<Claim> claims1 = Arrays.asList(
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/groupName", "humans"),
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/organization", "Society"));
        groupModel1.setClaims(claims1);

        GroupModel groupModel2 = new GroupModel();
        List<Claim> claims2 = Arrays.asList(
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/groupName", "children"),
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/organization", "Society"));
        groupModel2.setClaims(claims2);

        List<Group> addedGroups = realmService.getIdentityStore().addGroups(Arrays.asList(groupModel1, groupModel2));

        Assert.assertNotNull(addedGroups, "Failed to receive the groups.");
        Assert.assertTrue(!addedGroups.isEmpty() && addedGroups.size() == 2, "Number of groups received in the " +
                "response is invalid.");

        groups.addAll(addedGroups);
    }

    @Test(groups = "addGroups")
    public void testAddGroupsByDomain() throws IdentityStoreException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        GroupModel groupModel1 = new GroupModel();
        List<Claim> claims1 = Arrays.asList(
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/groupName", "SuperAngels"),
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/organization", "SuperHeaven"));
        groupModel1.setClaims(claims1);

        GroupModel groupModel2 = new GroupModel();
        List<Claim> claims2 = Arrays.asList(
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/groupName", "SuperDemons"),
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/organization", "SupperHell"));
        groupModel2.setClaims(claims2);

        List<Group> addedGroups = realmService.getIdentityStore().addGroups(Arrays.asList(groupModel1, groupModel2),
                "PRIMARY");

        Assert.assertNotNull(addedGroups, "Failed to receive the groups.");
        Assert.assertTrue(!addedGroups.isEmpty() && addedGroups.size() == 2, "Number of groups received in the " +
                "response is invalid.");

        groups.addAll(addedGroups);
    }

    @Test(dependsOnGroups = {"addGroups"})
    public void testGetGroupByUniqueGroupId() throws IdentityStoreException, GroupNotFoundException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        Group group = realmService.getIdentityStore().getGroup(groups.get(0).getUniqueGroupId());

        Assert.assertNotNull(group, "Failed to receive the group.");
    }

    @Test(dependsOnGroups = {"addGroups"})
    public void testGetGroupByUniqueGroupIdAndDomain() throws IdentityStoreException, GroupNotFoundException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        Group user = realmService.getIdentityStore().getGroup(groups.get(0).getUniqueGroupId(), "PRIMARY");

        Assert.assertNotNull(user, "Failed to receive the group.");
    }

    @Test(dependsOnGroups = {"addGroups"})
    public void testGetGroupByClaim() throws IdentityStoreException, GroupNotFoundException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        Group group = realmService.getIdentityStore().getGroup(new Claim("http://wso2.org/claims", "http://wso2" +
                ".org/claims/groupName", "Angels"));

        Assert.assertNotNull(group, "Failed to receive the group.");

        Assert.assertNotNull(group.getUniqueGroupId(), "Invalid group unique id.");
    }

    @Test(dependsOnGroups = {"addGroups"})
    public void testGetGroupByClaimAndDomain() throws IdentityStoreException, GroupNotFoundException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        Group group = realmService.getIdentityStore().getGroup(new Claim("http://wso2.org/claims", "http://wso2" +
                ".org/claims/groupName", "Demons"), "PRIMARY");

        Assert.assertNotNull(group, "Failed to receive the group.");

        Assert.assertNotNull(group.getUniqueGroupId(), "Invalid group unique id.");
    }

    @Test(dependsOnGroups = {"addGroups"})
    public void testListGroupsByOffsetAndLength() throws IdentityStoreException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        List<Group> groups = realmService.getIdentityStore().listGroups(2, 3);

        Assert.assertNotNull(groups, "Failed to list the users.");
        Assert.assertTrue(!groups.isEmpty() && groups.size() == 3, "Number of users received in the response " +
                "is invalid.");
    }

    @Test(dependsOnGroups = {"addGroups"})
    public void testListGroupsByOffsetAndLengthInADomain() throws IdentityStoreException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        List<Group> groups = realmService.getIdentityStore().listGroups(2, 3, "PRIMARY");

        Assert.assertNotNull(groups, "Failed to list the groups.");
        Assert.assertTrue(!groups.isEmpty() && groups.size() == 3, "Number of groups received in the response " +
                "is invalid.");
    }

    @Test(dependsOnGroups = {"addGroups"})
    public void testListGroupsByClaimOffsetAndLength() throws IdentityStoreException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        Claim claim = new Claim("http://wso2.org/claims", "http://wso2.org/claims/organization", "Society");
        List<Group> groups = realmService.getIdentityStore().listGroups(claim, 2, 3);

        Assert.assertNotNull(groups, "Failed to list the groups.");
        Assert.assertTrue(!groups.isEmpty() && groups.size() == 2, "Number of groups received in the response " +
                "is invalid.");
    }

    @Test(dependsOnGroups = {"addGroups"})
    public void testListGroupsByClaimOffsetAndLengthInADomain() throws IdentityStoreException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        Claim claim = new Claim("http://wso2.org/claims", "http://wso2.org/claims/organization", "Society");
        List<Group> groups = realmService.getIdentityStore().listGroups(claim, 2, 3, "PRIMARY");

        Assert.assertNotNull(groups, "Failed to list the groups.");
        Assert.assertTrue(!groups.isEmpty() && groups.size() == 2, "Number of groups received in the response " +
                "is invalid.");
    }

    @Test(dependsOnGroups = {"addGroups"})
    public void testListGroupsByMetaClaimFilterPatternOffsetAndLength() throws IdentityStoreException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        MetaClaim metaClaim = new MetaClaim("http://wso2.org/claims", "http://wso2.org/claims/organization");
        List<Group> groups = realmService.getIdentityStore().listGroups(metaClaim, "(?i).*cie.*", 2, 3);

        Assert.assertNotNull(groups, "Failed to list the groups.");
        Assert.assertTrue(!groups.isEmpty() && groups.size() == 2, "Number of groups received in the response " +
                "is invalid.");
    }

    @Test(dependsOnGroups = {"addGroups"})
    public void testListGroupsByMetaClaimFilterPatternOffsetAndLengthInDomain() throws IdentityStoreException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        MetaClaim metaClaim = new MetaClaim("http://wso2.org/claims", "http://wso2.org/claims/organization");
        List<Group> groups = realmService.getIdentityStore().listGroups(metaClaim, "(?i).*cie.*", 2, 3, "PRIMARY");

        Assert.assertNotNull(groups, "Failed to list the groups.");
        Assert.assertTrue(!groups.isEmpty() && groups.size() == 2, "Number of groups received in the response " +
                "is invalid.");
    }

    @Test(dependsOnGroups = {"addUsers", "addGroups"}, groups = "addGroupsToUser")
    public void testUpdateGroupsOfUser() {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        try {
            realmService.getIdentityStore().updateGroupsOfUser(users.get(0).getUniqueUserId(), Arrays.asList(groups.get
                    (0).getUniqueGroupId(), groups.get(1).getUniqueGroupId()));
        } catch (IdentityStoreException e) {
            Assert.fail("Failed to update groups of user.");
        }
    }

    @Test(dependsOnGroups = {"addUsers", "addGroups"}, groups = "addGroupsToUser")
    public void testUpdateGroupsOfUserByDomain() {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        try {
            realmService.getIdentityStore().updateGroupsOfUser(users.get(1).getUniqueUserId(), Arrays.asList(groups.get
                    (0).getUniqueGroupId(), groups.get(1).getUniqueGroupId()), "PRIMARY");
        } catch (IdentityStoreException e) {
            Assert.fail("Failed to update groups of user.");
        }
    }

    @Test(dependsOnGroups = {"addUsers", "addGroups"}, groups = "addUsersToGroup")
    public void testUpdateUsersOfGroup() {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        try {
            realmService.getIdentityStore().updateUsersOfGroup(groups.get(3).getUniqueGroupId(), Arrays.asList(users.get
                    (2).getUniqueUserId(), users.get(3).getUniqueUserId()));
        } catch (IdentityStoreException e) {
            Assert.fail("Failed to update groups of user.");
        }
    }

    @Test(dependsOnGroups = {"addUsers", "addGroups"}, groups = "addUsersToGroup")
    public void testUpdateUsersOfGroupByDomain() {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        try {
            realmService.getIdentityStore().updateUsersOfGroup(groups.get(4).getUniqueGroupId(), Arrays.asList(users.get
                    (4).getUniqueUserId(), users.get(5).getUniqueUserId()), "PRIMARY");
        } catch (IdentityStoreException e) {
            Assert.fail("Failed to update groups of user.");
        }
    }

    @Test(dependsOnGroups = {"addGroupsToUser", "addUsersToGroup"})
    public void testGetGroupsOfUser() throws IdentityStoreException, UserNotFoundException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        List<Group> groupsOfUser = realmService.getIdentityStore().getGroupsOfUser(users.get(0).getUniqueUserId());
        Assert.assertNotNull(groupsOfUser, "Failed to get the groups.");
        Assert.assertTrue(!groupsOfUser.isEmpty() && groupsOfUser.size() > 0, "Number of groups received in the " +
                "response is invalid.");
    }

    @Test(dependsOnGroups = {"addGroupsToUser", "addUsersToGroup"})
    public void testGetGroupsOfUserByDomain() throws IdentityStoreException, UserNotFoundException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        List<Group> groupsOfUser = realmService.getIdentityStore().getGroupsOfUser(users.get(1).getUniqueUserId(),
                "PRIMARY");
        Assert.assertNotNull(groupsOfUser, "Failed to get the groups.");
        Assert.assertTrue(!groupsOfUser.isEmpty() && groupsOfUser.size() > 0, "Number of groups received in the " +
                "response is invalid.");
    }

    @Test(dependsOnGroups = {"addGroupsToUser", "addUsersToGroup"})
    public void testGetUsersOfGroup() throws IdentityStoreException, GroupNotFoundException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        List<User> usersOfGroup = realmService.getIdentityStore().getUsersOfGroup(groups.get(3).getUniqueGroupId());
        Assert.assertNotNull(usersOfGroup, "Failed to get the users.");
        Assert.assertTrue(!usersOfGroup.isEmpty() && usersOfGroup.size() > 0, "Number of users received in the " +
                "response is invalid.");
    }

    @Test(dependsOnGroups = {"addGroupsToUser", "addUsersToGroup"})
    public void testGetUsersOfGroupByDomain() throws IdentityStoreException, GroupNotFoundException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        List<User> usersOfGroup = realmService.getIdentityStore().getUsersOfGroup(groups.get(4).getUniqueGroupId(),
                "PRIMARY");
        Assert.assertNotNull(usersOfGroup, "Failed to get the users.");
        Assert.assertTrue(!usersOfGroup.isEmpty() && usersOfGroup.size() > 0, "Number of users received in the " +
                "response is invalid.");
    }

    @Test(dependsOnGroups = {"addGroupsToUser", "addUsersToGroup"})
    public void testIsUserInGroup() throws IdentityStoreException, UserNotFoundException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        boolean isUserInGroup = realmService.getIdentityStore().isUserInGroup(users.get(0).getUniqueUserId(), groups
                .get(0).getUniqueGroupId());

        Assert.assertTrue(isUserInGroup, "Is user exists in group failed.");
    }

    @Test(dependsOnGroups = {"addGroupsToUser", "addUsersToGroup"})
    public void testIsUserInGroupByDomain() throws IdentityStoreException, UserNotFoundException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        boolean isUserInGroup = realmService.getIdentityStore().isUserInGroup(users.get(1).getUniqueUserId(), groups
                .get(1).getUniqueGroupId(), "PRIMARY");

        Assert.assertTrue(isUserInGroup, "Is user exists in group failed.");
    }

    @Test(dependsOnGroups = {"addUsers"})
    public void testGetClaims() throws IdentityStoreException, UserNotFoundException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        List<Claim> claims = realmService.getIdentityStore().getClaimsOfUser(users.get(0).getUniqueUserId());
        Assert.assertNotNull(claims, "Failed to get the claims.");
        Assert.assertTrue(!claims.isEmpty() && claims.size() > 0, "Number of claims received in the " +
                "response is invalid.");
    }

    @Test(dependsOnGroups = {"addUsers"})
    public void testGetClaimsByDomain() throws IdentityStoreException, UserNotFoundException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        List<Claim> claims = realmService.getIdentityStore().getClaimsOfUser(users.get(1).getUniqueUserId(), "PRIMARY");
        Assert.assertNotNull(claims, "Failed to get the claims.");
        Assert.assertTrue(!claims.isEmpty() && claims.size() > 0, "Number of claims received in the " +
                "response is invalid.");
    }

    @Test(dependsOnGroups = {"addUsers"})
    public void testGetClaimFromMetaClaims() throws IdentityStoreException, UserNotFoundException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        List<MetaClaim> metaClaims = Arrays.asList(
                new MetaClaim("http://wso2.org/claims", "http://wso2.org/claims/username"),
                new MetaClaim("http://wso2.org/claims", "http://wso2.org/claims/email"));

        List<Claim> claims = realmService.getIdentityStore().getClaimsOfUser(users.get(0).getUniqueUserId(),
                metaClaims);
        Assert.assertNotNull(claims, "Failed to get the claims.");
        Assert.assertTrue(!claims.isEmpty() && claims.size() == 2, "Number of claims received in the " +
                "response is invalid.");
    }

    @Test(dependsOnGroups = {"addUsers"})
    public void testGetClaimFromMetaClaimsByDomain() throws IdentityStoreException, UserNotFoundException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        List<MetaClaim> metaClaims = Arrays.asList(
                new MetaClaim("http://wso2.org/claims", "http://wso2.org/claims/username"),
                new MetaClaim("http://wso2.org/claims", "http://wso2.org/claims/email"));

        List<Claim> claims = realmService.getIdentityStore().getClaimsOfUser(users.get(1).getUniqueUserId(), metaClaims,
                "PRIMARY");
        Assert.assertNotNull(claims, "Failed to get the claims.");
        Assert.assertTrue(!claims.isEmpty() && claims.size() == 2, "Number of claims received in the " +
                "response is invalid.");
    }

    @Test(dependsOnGroups = {"addUsers"})
    public void testUpdateUserClaims() throws UserNotFoundException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        List<Claim> claims = Arrays
                .asList(new Claim("http://wso2.org/claims", "http://wso2.org/claims/username", "lucifer"),
                        new Claim("http://wso2.org/claims", "http://wso2.org/claims/firstName", "UpdatedLucifer"),
                        new Claim("http://wso2.org/claims", "http://wso2.org/claims/email", "up.lucifer@wso2.com"));

        try {
            realmService.getIdentityStore().updateUserClaims(users.get(0).getUniqueUserId(), claims);
        } catch (IdentityStoreException e) {
            Assert.fail("Failed to update user claims.");
        }
    }

    @Test(dependsOnGroups = {"addUsers"})
    public void testUpdateUserClaimsByDomain() throws UserNotFoundException {

        RealmService realmService = bundleContext.getService(bundleContext.getServiceReference(RealmService.class));
        Assert.assertNotNull(realmService, "Failed to get realm service instance");

        List<Claim> claims = Arrays.asList(
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/username", "chloe"),
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/firstName", "UpdatedChloe"),
                new Claim("http://wso2.org/claims", "http://wso2.org/claims/email", "up.chloe@wso2.com"));

        try {
            realmService.getIdentityStore().updateUserClaims(users.get(0).getUniqueUserId(), claims, "PRIMARY");
        } catch (IdentityStoreException e) {
            Assert.fail("Failed to update user claims.");
        }
    }
}

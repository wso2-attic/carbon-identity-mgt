package org.wso2.carbon.identity.mgt.test.osgi;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.BundleContext;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.claim.ProfileMgtService;
import org.wso2.carbon.identity.claim.exception.ProfileMgtServiceException;
import org.wso2.carbon.identity.claim.impl.internal.config.profile.ClaimConfigEntry;
import org.wso2.carbon.identity.mgt.test.osgi.util.IdentityMgtOSGiTestUtils;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;

/**
 * Tests the ClaimResolvingService.
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class ProfileMgtServiceTest {

    private List<ClaimConfigEntry> claimConfigEntriesRegistration = new ArrayList<>();
    private ClaimConfigEntry claimConfigEntryUserName = new ClaimConfigEntry();
    private ClaimConfigEntry claimConfigEntryEmployeeNumber = new ClaimConfigEntry();

    private static final String DEFAULT = "default";
    private static final String EMPLOYEE = "employee";

    //    profileName: "default"
    //    claims:
    //            -
    //    claimURI: "http://wso2.org/claims/username"
    //    displayName: "User Name"
    //    required: true
    //    readonly: true
    //    verifier: "claim verifying extension"
    //    validator: "claim validating extension"
    //    transformer: "transform extension"
    //    regex: "*"
    //    defaultValue: "user1"
    //    dataType: "text"
    //    properties:
    //    customproperty: "custom value"
    //            -
    //    claimURI: "http://wso2.org/claims/email"
    //    displayName: "Email"
    //    required: true
    //    readonly: true
    //    regex: "*"
    //            -
    //    claimURI: "http://wso2.org/claims/mobile"
    //    displayName: "Mobile"
    //    required: true
    //    readonly: true
    //    regex: "*"
    //            -
    //    claimURI: "http://wso2.org/claims/organization"
    //    displayName: "Organization"
    //    required: true
    //    readonly: true
    //    regex: "*"

    @Inject
    private BundleContext bundleContext;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Configuration
    public Option[] createConfiguration() {

        List<Option> optionList = IdentityMgtOSGiTestUtils.getDefaultSecurityPAXOptions();

        optionList.add(CoreOptions.systemProperty("java.security.auth.login.config")
                .value(Paths.get(IdentityMgtOSGiTestUtils.getCarbonHome(), "conf", "security", "carbon-jaas.config")
                        .toString()));

        return optionList.toArray(new Option[optionList.size()]);
    }

    @Test
    public void testGetProfileMappings() throws ProfileMgtServiceException {

        ProfileMgtService profileMgtService = bundleContext
                .getService(bundleContext.getServiceReference(ProfileMgtService.class));
        Assert.assertNotNull(profileMgtService, "Failed to get profile mgt service instance");

        Assert.assertNotNull(profileMgtService.getProfiles().get(DEFAULT),
                "Registration profile mappings not read correctly.");
        Assert.assertNotNull(profileMgtService.getProfiles().get(EMPLOYEE),
                "Employee profile mappings not read correctly");
        Assert.assertEquals(DEFAULT, profileMgtService.getProfile(DEFAULT).getProfileName(),
                "Registration profile mappings not read correctly.");
        Assert.assertNotNull(profileMgtService.getProfiles().get(EMPLOYEE).getClaims(),
                "Claims are not read correctly from profile");

    }

    @Test
    public void testGetProfile() throws ProfileMgtServiceException {
        ProfileMgtService profileMgtService = bundleContext
                .getService(bundleContext.getServiceReference(ProfileMgtService.class));
        Assert.assertNotNull(profileMgtService, "Failed to get profile mgt service instance");

        Assert.assertNotNull(profileMgtService.getProfile(DEFAULT),
                "Registration profile mappings not read correctly.");
        Assert.assertNotNull(profileMgtService.getProfile(EMPLOYEE), "Employee profile mappings not read correctly.");

        Assert.assertEquals("custom value",
                profileMgtService.getProfile(EMPLOYEE).getClaims().get(0).getProperties().get("customproperty"),
                "Claim entries not read correct");

    }

    @Test
    public void testGetRequiredClaims() throws ProfileMgtServiceException {
        ProfileMgtService profileMgtService = bundleContext
                .getService(bundleContext.getServiceReference(ProfileMgtService.class));
        Assert.assertNotNull(profileMgtService, "Failed to get profile mgt service instance");

        Assert.assertTrue(profileMgtService.getRequiredClaims(DEFAULT).contains("http://wso2.org/claims/username"),
                "Required claim not read correctly");
    }

    @Test
    public void testGetReadOnlyClaims() throws ProfileMgtServiceException {
        ProfileMgtService profileMgtService = bundleContext
                .getService(bundleContext.getServiceReference(ProfileMgtService.class));
        Assert.assertNotNull(profileMgtService, "Failed to get profile mgt service instance");

        Assert.assertTrue(profileMgtService.getReadOnlyClaims(DEFAULT).contains("http://wso2.org/claims/email"),
                "ReadOnly claim not read correctly");
    }

    @Test
    public void testGetTransformingClaims() throws ProfileMgtServiceException {
        ProfileMgtService profileMgtService = bundleContext
                .getService(bundleContext.getServiceReference(ProfileMgtService.class));
        Assert.assertNotNull(profileMgtService, "Failed to get profile mgt service instance");

        Assert.assertTrue(profileMgtService.getTransformingClaims(DEFAULT).contains("http://wso2.org/claims/username"),
                "Transforming claims not read correctly");
        Assert.assertFalse(
                profileMgtService.getTransformingClaims(EMPLOYEE).contains("http://wso2.org/claims/username"),
                "Transforming claims not read correctly");
    }

    @Test
    public void testGetValidatingClaims() throws ProfileMgtServiceException {
        ProfileMgtService profileMgtService = bundleContext
                .getService(bundleContext.getServiceReference(ProfileMgtService.class));
        Assert.assertNotNull(profileMgtService, "Failed to get profile mgt service instance");

        Assert.assertTrue(profileMgtService.getValidatingClaims(DEFAULT).contains("http://wso2.org/claims/username"),
                "Transforming claims not read correctly");
        Assert.assertFalse(profileMgtService.getValidatingClaims(EMPLOYEE).contains("http://wso2.org/claims/username"),
                "Transforming claims not read correctly");
    }

    @Test
    public void testGetVerifyingClaims() throws ProfileMgtServiceException {
        ProfileMgtService profileMgtService = bundleContext
                .getService(bundleContext.getServiceReference(ProfileMgtService.class));
        Assert.assertNotNull(profileMgtService, "Failed to get profile mgt service instance");

        Assert.assertTrue(profileMgtService.getVerifyingClaims(DEFAULT).contains("http://wso2.org/claims/username"),
                "Transforming claims not read correctly");
        Assert.assertFalse(profileMgtService.getVerifyingClaims(EMPLOYEE).contains("http://wso2.org/claims/username"),
                "Transforming claims not read correctly");
    }

    @Test
    public void testGetClaimAttributes() throws ProfileMgtServiceException {
        Map<String, String> properties = new HashMap<>();

        properties.put("customproperty", "custom value");

        claimConfigEntryUserName.setClaimURI("http://wso2.org/claims/username");
        claimConfigEntryUserName.setRequired(true);
        claimConfigEntryUserName.setReadonly(true);
        claimConfigEntryUserName.setVerifier("claim verifying extension");
        claimConfigEntryUserName.setValidator("claim validating extension");
        claimConfigEntryUserName.setTransformer("transform extension");
        claimConfigEntryUserName.setRegex("*");
        claimConfigEntryUserName.setDefaultValue("user1");
        claimConfigEntryUserName.setDataType("text");
        claimConfigEntryUserName.setProperties(properties);

        claimConfigEntryEmployeeNumber.setClaimURI("http://wso2.org/claims/employeeNumber");
        claimConfigEntryEmployeeNumber.setRequired(true);
        claimConfigEntryEmployeeNumber.setReadonly(true);
        claimConfigEntryEmployeeNumber.setRegex("*");
        claimConfigEntryEmployeeNumber.setProperties(properties);

        claimConfigEntriesRegistration.add(claimConfigEntryUserName);
        claimConfigEntriesRegistration.add(claimConfigEntryEmployeeNumber);

        ProfileMgtService profileMgtService = bundleContext
                .getService(bundleContext.getServiceReference(ProfileMgtService.class));
        Assert.assertNotNull(profileMgtService, "Failed to get profile mgt service instance");

        Assert.assertEquals(claimConfigEntryUserName.getTransformer(),
                profileMgtService.getClaimAttributes(DEFAULT, "http://wso2.org/claims/username").getTransformer(),
                "Failed to get claim attributes.");
        Assert.assertEquals(claimConfigEntryEmployeeNumber.getProperties(),
                profileMgtService.getClaimAttributes(EMPLOYEE, "http://wso2.org/claims/employeeNumber").getProperties(),
                "Failed to get claim attributes.");
    }

    @Test
    public void testGetProfileNames() throws ProfileMgtServiceException {
        Set<String> profileNames = new HashSet<>();
        profileNames.add(DEFAULT);
        profileNames.add(EMPLOYEE);

        ProfileMgtService profileMgtService = bundleContext
                .getService(bundleContext.getServiceReference(ProfileMgtService.class));
        Assert.assertEquals(profileNames, profileMgtService.getProfileNames(),
                "Error reading the available profile names");

    }

}

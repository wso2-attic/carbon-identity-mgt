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
import org.wso2.carbon.identity.mgt.exception.CarbonIdentityMgtConfigException;
import org.wso2.carbon.identity.mgt.service.ClaimResolvingService;
import org.wso2.carbon.identity.mgt.test.osgi.util.IdentityMgtOSGiTestUtils;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import static org.ops4j.pax.exam.CoreOptions.systemProperty;

/**
 * Tests the ClaimResolvingService.
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class ClaimResolvingServiceTest {

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

    @Test
    public void testGetApplicationClaimMapping() throws CarbonIdentityMgtConfigException {
        Map<String, String> applicationMappings = new HashMap<>();
        applicationMappings.put("http://application1.com/name", "http://wso2.org/claims/username");
        applicationMappings.put("http://application1.com/role", "http://wso2.org/claims/role");
        applicationMappings.put("http://application1.com/mobile", "http://wso2.org/claims/mobile");
        applicationMappings.put("http://application1.com/email", "http://wso2.org/claims/email");

        ClaimResolvingService claimResolvingService = bundleContext
                .getService(bundleContext.getServiceReference(ClaimResolvingService.class));
        Assert.assertNotNull(claimResolvingService, "Failed to get claim resolving service instance");

        Assert.assertEquals(applicationMappings, claimResolvingService.getApplicationClaimMapping("Application1"),
                "Claim mappings not read correctly");

    }

    @Test
    public void testIdpClaimMapping() throws CarbonIdentityMgtConfigException {
        Map<String, String> idpMappings = new HashMap<>();
        idpMappings.put("http://identityprovider1.com/idp-name", "http://wso2.org/claims/username");
        idpMappings.put("http://identityprovider1.com/idp-role", "http://wso2.org/claims/role");
        idpMappings.put("http://identityprovider1.com/idp-mobile", "http://wso2.org/claims/mobile");
        idpMappings.put("http://identityprovider1.com/idp-email", "http://wso2.org/claims/email");

        ClaimResolvingService claimResolvingService = bundleContext
                .getService(bundleContext.getServiceReference(ClaimResolvingService.class));
        Assert.assertNotNull(claimResolvingService, "Failed to get claim resolving service instance");

        Assert.assertEquals(idpMappings, claimResolvingService.getIdpClaimMapping("IDP1"));
    }

    @Test
    public void testStandardClaimMapping() throws CarbonIdentityMgtConfigException {
        Map<String, String> standardMappings = new HashMap<>();
        standardMappings.put("username", "http://wso2.org/claims/username");
        standardMappings.put("role", "http://wso2.org/claims/role");
        standardMappings.put("mobile", "http://wso2.org/claims/mobile");
        standardMappings.put("email", "http://wso2.org/claims/email");

        ClaimResolvingService claimResolvingService = bundleContext
                .getService(bundleContext.getServiceReference(ClaimResolvingService.class));
        Assert.assertNotNull(claimResolvingService, "Failed to get claim resolving service instance");

        Assert.assertEquals(standardMappings, claimResolvingService.getStandardClaimMapping("SCIM"));
    }

}

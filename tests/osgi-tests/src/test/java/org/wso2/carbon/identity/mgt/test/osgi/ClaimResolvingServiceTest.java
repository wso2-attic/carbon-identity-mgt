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
import org.wso2.carbon.identity.meta.claim.mgt.exception.ClaimResolvingServiceException;
import org.wso2.carbon.identity.meta.claim.mgt.service.ClaimResolvingService;
import org.wso2.carbon.identity.mgt.test.osgi.util.IdentityMgtOSGiTestUtils;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

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

        optionList.add(CoreOptions.systemProperty("java.security.auth.login.config")
                .value(Paths.get(IdentityMgtOSGiTestUtils.getCarbonHome(), "conf", "security", "carbon-jaas.config")
                        .toString()));

        return optionList.toArray(new Option[optionList.size()]);
    }

    @Test
    public void testGetClaimMapping() throws ClaimResolvingServiceException {
        Map<String, String> applicationMappings = new HashMap<>();
        applicationMappings.put("http://application1.com/name", "http://wso2.org/claims/username");
        applicationMappings.put("http://application1.com/role", "http://wso2.org/claims/role");
        applicationMappings.put("http://application1.com/mobile", "http://wso2.org/claims/mobile");
        applicationMappings.put("http://application1.com/email", "http://wso2.org/claims/email");

        ClaimResolvingService claimResolvingService = bundleContext
                .getService(bundleContext.getServiceReference(ClaimResolvingService.class));
        Assert.assertNotNull(claimResolvingService, "Failed to get claim resolving service instance");

        Assert.assertEquals(applicationMappings, claimResolvingService.getClaimMapping("http://application1.com/"),
                "Claim mappings not read correctly");

    }

}

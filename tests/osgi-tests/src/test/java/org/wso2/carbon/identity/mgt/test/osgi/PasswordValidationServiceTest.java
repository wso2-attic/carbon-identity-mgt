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
import org.wso2.carbon.identity.mgt.test.osgi.util.IdentityMgtOSGITestConstants;
import org.wso2.carbon.identity.mgt.test.osgi.util.IdentityMgtOSGiTestUtils;
import org.wso2.carbon.identity.policy.password.validation.PasswordValidationService;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;

import java.nio.file.Paths;
import java.util.List;
import javax.inject.Inject;

import static org.ops4j.pax.exam.CoreOptions.systemProperty;

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class PasswordValidationServiceTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordValidationServiceTest.class);
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

    @Test(groups = "passwordValidation", description = "Validate a password which is in compliance " +
            "with the password policy")
    public void correctPasswordValidation() {

        PasswordValidationService passwordValidationService = bundleContext.getService(bundleContext.
                getServiceReference(PasswordValidationService.class));
        Assert.assertNotNull(passwordValidationService, "Failed to get password validation service instance");
        String password = "ABCabc01$";
        char[] passwordAsCharArray = password.toCharArray();
        boolean isValidPassword = passwordValidationService.validatePassword(passwordAsCharArray);
        Assert.assertTrue(isValidPassword, "Password is not valid");
    }

    @Test(groups = "passwordValidation", description = "Validate a password which is not in compliance with " +
            "the password policy")
    public void incorrectPasswordValidation() {

        PasswordValidationService passwordValidationService = bundleContext.getService(bundleContext.
                getServiceReference(PasswordValidationService.class));
        Assert.assertNotNull(passwordValidationService, "Failed to get password validation service instance");
        String password = "ABC";
        char[] passwordAsCharArray = password.toCharArray();
        boolean isValidPassword = passwordValidationService.validatePassword(passwordAsCharArray);
        Assert.assertFalse(isValidPassword, "Password is a valid password");
    }
}


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
 * limitations und
 */

package org.wso2.carbon.identity.policy.password.pattern.internal;


import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.event.AbstractEventHandler;
import org.wso2.carbon.identity.policy.password.pattern.PasswordPolicyPatternHandler;
import org.wso2.carbon.identity.policy.password.pattern.registry.PolicyEnforcer;
import org.wso2.carbon.identity.policy.password.pattern.registry.PolicyRegistry;
import org.wso2.carbon.identity.policy.password.pattern.registry.impl.DefaultPasswordPatternPolicy;
import org.wso2.carbon.identity.policy.password.pattern.validation.PasswordValidationService;
import org.wso2.carbon.identity.policy.password.pattern.validation.impl.PasswordValidationServiceImpl;

import java.util.List;
import java.util.Map;


/**
 * PasswordPolicyPatternServiceComponent class.
 */
@Component(
        name = "PasswordPolicyPatternServiceComponent",
        immediate = true,
        property = {
                "componentName=handler.policy.password.pattern"
        }
)
public class PasswordPolicyPatternServiceComponent {

    private static Logger log = LoggerFactory.getLogger(PasswordPolicyPatternServiceComponent.class);
    private PolicyRegistry policyRegistry = new PolicyRegistry();

    @Activate
    protected void activate(ComponentContext context) {

        List<PolicyEnforcer> policyList = policyRegistry.getPolicyRegistry();
        if (policyList.isEmpty()) {
            policyRegistry.addPolicy(new DefaultPasswordPatternPolicy());
        }

        context.getBundleContext().registerService(AbstractEventHandler.class.getName(),
                new PasswordPolicyPatternHandler(policyRegistry), null);
        if (log.isDebugEnabled()) {
            log.debug("PasswordPatternHandler is registered");
        }

        context.getBundleContext().registerService(PasswordValidationService.class,
                new PasswordValidationServiceImpl(policyRegistry), null);
        if (log.isDebugEnabled()) {
            log.debug("PasswordValidationHandler is registered");
        }

    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("PasswordPolicyPatternHandler is unregistered");
        }
    }


    @Reference(
            name = "org.wso2.carbon.identity.policy.password.pattern.registry.PolicyEnforcer",
            service = PolicyEnforcer.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterPolicyEnforcer"
    )
    protected void registerPolicyEnforcer(PolicyEnforcer service, Map<String, String> properties) {

        if (service == null) {
            log.error("Policy enforcer service is null. Registering policy enforce service is unsuccessful.");
            return;
        }

        policyRegistry.addPolicy(service);

        if (log.isDebugEnabled()) {
            log.debug("Policy enforcer service registered successfully.");
        }
    }

    protected void unregisterPolicyEnforcer(PolicyEnforcer service) {

        if (log.isDebugEnabled()) {
            log.debug("Data source service unregistered.");
        }

        policyRegistry.removePolicy(service);

    }
}

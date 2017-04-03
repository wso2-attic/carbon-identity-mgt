package org.wso2.carbon.identity.policy.password.pattern.validation.impl;


import org.wso2.carbon.identity.policy.password.pattern.registry.PolicyRegistry;
import org.wso2.carbon.identity.policy.password.pattern.validation.PasswordValidationService;

/**
 * This is the implementation class of PasswordValidationService
 */
public class PasswordValidationServiceImpl implements PasswordValidationService {

    private PolicyRegistry policyRegistry;

    public PasswordValidationServiceImpl(PolicyRegistry policyRegistry) {
        this.policyRegistry = policyRegistry;
    }

    @Override
    public boolean validatePassword(char[] password) {

        return policyRegistry.enforcePasswordPolicies(password).isSuccess();

    }
}

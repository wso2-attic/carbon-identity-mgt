package org.wso2.carbon.identity.policy.password.validation.impl;


import org.wso2.carbon.identity.policy.password.history.util.PasswordValidation;
import org.wso2.carbon.identity.policy.password.validation.PasswordValidationService;

/**
 * This is the implementation class of PasswordValidationService
 */
public class PasswordValidationServiceImpl implements PasswordValidationService {

    @Override
    public boolean validatePassword(String password) {
        return PasswordValidation.validatePassword(password);
    }
}

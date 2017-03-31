/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.policy.password.pattern.registry.impl;

import org.wso2.carbon.identity.policy.password.pattern.bean.PasswordPolicyBean;
import org.wso2.carbon.identity.policy.password.pattern.bean.ValidationResult;
import org.wso2.carbon.identity.policy.password.pattern.registry.PolicyEnforcer;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Default password policy check.
 */
public class DefaultPasswordPatternPolicy implements PolicyEnforcer {

    abstract static class BaseRule implements PasswordRule {

        private final String message;

        BaseRule(String message) {
            this.message = message;
        }

        public String failMessage() {
            return message;
        }
    }

    private final PasswordRule[] availableRules = {

            new BaseRule("Password is too short. Needs to have at least: " + new PasswordPolicyBean().getMinLength() +
                    " characters") {

                @Override
                public boolean passRule(char[] password) {
                    return password.length >= new PasswordPolicyBean().getMinLength();
                }

            },

            new BaseRule("Password is too long. Needs to have less than " + new PasswordPolicyBean().getMaxLength() +
                    " characters") {

                @Override
                public boolean passRule(char[] password) {
                    return password.length <= new PasswordPolicyBean().getMaxLength();
                }

            },

            new BaseRule("Password needs at least one upper case character") {

                private final Pattern pattern = Pattern.compile(".*[A-Z].*");

                @Override
                public boolean passRule(char[] password) {
                    return pattern.matcher(CharBuffer.wrap(password)).matches();
                }
            },

            new BaseRule("Password needs at least one lower case character") {

                private final Pattern pattern = Pattern.compile(".*[a-z].*");

                @Override
                public boolean passRule(char[] password) {
                    return pattern.matcher(CharBuffer.wrap(password)).matches();

                }
            },

            new BaseRule("Password needs at least one number") {

                private final Pattern pattern = Pattern.compile(".*[0-9].*");

                @Override
                public boolean passRule(char[] password) {
                    return pattern.matcher(CharBuffer.wrap(password)).matches();
                }
            },

            new BaseRule("Password needs at least one special character") {

                private final Pattern pattern = Pattern.compile(".*[-+_!@#$%^&*.,?=].*");

                @Override
                public boolean passRule(char[] password) {
                    return pattern.matcher(CharBuffer.wrap(password)).matches();
                }
            },
    };

    List<PasswordRule> effectiveRules = new ArrayList<>();

    public DefaultPasswordPatternPolicy() {

        PasswordPolicyBean passwordPolicyBean = new PasswordPolicyBean();

        if (passwordPolicyBean.getMinLength() > 0) {
            effectiveRules.add(availableRules[0]);
        }

        if (passwordPolicyBean.getMaxLength() > 0) {
            effectiveRules.add(availableRules[1]);
        }

        if (passwordPolicyBean.isIncludeUpperCase()) {
            effectiveRules.add(availableRules[2]);
        }

        if (passwordPolicyBean.isIncludeLowerCase()) {
            effectiveRules.add(availableRules[3]);
        }

        if (passwordPolicyBean.isIncludeNumbers()) {
            effectiveRules.add(availableRules[4]);
        }

        if (passwordPolicyBean.isIncludeSymbols()) {
            effectiveRules.add(availableRules[5]);
        }
    }

    @Override
    public ValidationResult enforce(Object... args) {

        char[] password = (char[]) args[0];
        StringBuilder errorMessage = new StringBuilder();
        ValidationResult validationResult = new ValidationResult();

        //Give first priority to regex pattern
        PasswordPolicyBean passwordPolicyBean = new PasswordPolicyBean();
        if (!passwordPolicyBean.getRegex().isEmpty()) {
            Pattern pattern = Pattern.compile(passwordPolicyBean.getRegex());
            if (pattern.matcher(CharBuffer.wrap(password)).matches()) {
                validationResult.setSuccess(true);
                return validationResult;
            }
            validationResult.setSuccess(false);
            validationResult.setMessage("Entered password does not adhere to password policy");
            return validationResult;
        }

        boolean pass = true;
        for (PasswordRule rule : effectiveRules) {
            if (!rule.passRule(password)) {
                errorMessage.append(rule.failMessage()).append(" <br>");
                pass = false;
            }
        }

        validationResult.setSuccess(pass);
        if (!pass) {
            validationResult.setMessage(errorMessage.toString());
        }

        return validationResult;
    }
}

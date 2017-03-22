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
package org.wso2.carbon.identity.policy.password.history.util;


import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.identity.policy.password.history.bean.PasswordPolicyBean;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * This is the util class which contains utility methods to handle passwords.
 */
public class PasswordValidation {

    /**
     * Method to validate the password.
     * If a regex is defined in the policy bean, regex will be given priority.
     * If regex field is empty validate the password against other specified properties in the mean (
     * ex:uppercase,lowercase)
     *
     * @param password to be validated
     * @return a boolean indication the password is valid or not
     */
    public static boolean validatePassword(String password) {
        boolean isValid = true;
        PasswordPolicyBean passwordPolicyBean = new PasswordPolicyBean();
        Pattern pattern;
        if (password != null) {
            if (!passwordPolicyBean.getRegex().isEmpty()) {
                pattern = Pattern.compile(passwordPolicyBean.getRegex());
                if (pattern.matcher(password).matches()) {
                    return isValid;
                } else {
                    isValid = false;
                    return isValid;
                }
            } else {
                if (password.length() > passwordPolicyBean.getMaxLength() ||
                        password.length() <= passwordPolicyBean.getMinLength()) {
                    isValid = false;
                }
                if (passwordPolicyBean.isIncludeUpperCase()) {
                    if (!hasUpperCase(password)) {
                        isValid = false;
                    }
                } else {
                    if (hasUpperCase(password)) {
                        isValid = false;
                    }
                }
                if (passwordPolicyBean.isIncludeLowerCase()) {
                    if (!hasLowerCase(password)) {
                        isValid = false;
                    }
                } else {
                    if (hasLowerCase(password)) {
                        isValid = false;
                    }
                }
                String numbers = "(.*[0-9].*)";
                if (passwordPolicyBean.isIncludeNumbers()) {
                    if (!password.matches(numbers)) {
                        isValid = false;
                    }
                } else {
                    if (password.matches(numbers)) {
                        isValid = false;
                    }
                }
                if (passwordPolicyBean.isIncludeSymbols() && !passwordPolicyBean.getSymbols().isEmpty()) {
                    if (!hasSymbol(password)) {
                        isValid = false;
                    }
                } else {
                    if (hasSymbol(password)) {
                        isValid = false;
                    }
                }
            }
        }
        return isValid;
    }

    /**
     * Method to check wether the password contains the specified special characters.
     *
     * @param password passowrd to be validated
     * @return boolean
     */
    private static boolean hasSymbol(String password) {
        boolean isAlphaNumeric = StringUtils.isAlphanumeric(password);
        if (!isAlphaNumeric) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Method to check wether the password contains any uppercase characters
     *
     * @param password passowrd to be validated
     * @return boolean
     */
    private static boolean hasUpperCase(String password) {
        boolean hasUppercase = !password.equals(password.toLowerCase(Locale.getDefault()));
        return hasUppercase;
    }

    /**
     * Method to check wether the password contains any lowercase characters
     *
     * @param password passowrd to be validated
     * @return boolean
     */
    private static boolean hasLowerCase(String password) {
        boolean hasLowercase = !password.equals(password.toUpperCase(Locale.getDefault()));
        return hasLowercase;
    }

}

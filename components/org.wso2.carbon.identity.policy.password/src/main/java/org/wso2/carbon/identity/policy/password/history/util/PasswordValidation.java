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

import org.wso2.carbon.identity.policy.password.history.bean.PasswordPolicyBean;

import java.nio.CharBuffer;
import java.util.regex.Pattern;

/**
 * This is the util class which contains utility methods to handle passwords.
 */
public class PasswordValidation {

    /**
     * Method to validate the password.
     * If a regex is defined in the policy bean, regex will be given priority.
     * If regex field is empty validate the password against other specified properties in the bean (
     * ex:uppercase,lowercase)
     *
     * @param password to be validated
     * @return a boolean indication the password is valid or not
     */
    public static boolean validatePassword(char[] password) {
        boolean isValid = false;
        PasswordPolicyBean passwordPolicyBean = new PasswordPolicyBean();
        Pattern pattern;
        if (password != null) {
            if (!passwordPolicyBean.getRegex().isEmpty()) {
                if (isRegexMatch(passwordPolicyBean.getRegex(), password)) {
                    isValid = true;
                } else {
                    isValid = false;
                }
                return isValid;
            } else {
                if (password.length <= passwordPolicyBean.getMaxLength() &&
                        password.length >= passwordPolicyBean.getMinLength()) {
                    isValid = true;
                } else {
                    isValid = false;
                    return isValid;
                }
                String startOfRegex = "^";
                String endOfRegex = "$";
                String regexContent = startOfRegex;
                boolean upper = passwordPolicyBean.isIncludeUpperCase();
                boolean lower = passwordPolicyBean.isIncludeLowerCase();
                boolean digit = passwordPolicyBean.isIncludeNumbers();
                boolean symbol = passwordPolicyBean.isIncludeSymbols();
                if (digit) {
                    regexContent = regexContent + "(?=.*[0-9])";
                }
                if (lower) {
                    regexContent = regexContent + "(?=.*[a-z])";
                }
                if (upper) {
                    regexContent = regexContent + "(?=.*[A-Z])";
                }
                if (symbol && !passwordPolicyBean.getSymbols().isEmpty()) {
                    regexContent = regexContent + "(?=.*[" + passwordPolicyBean.getSymbols() + "])";
                }
                regexContent = regexContent + ".*" + endOfRegex;
                if (isRegexMatch(regexContent, password)) {
                    isValid = true;
                } else {
                    isValid = false;
                }
            }
        }
        return isValid;
    }

    private static boolean isRegexMatch(String regex, char[] inputArray) {
        Pattern pattern;
        pattern = Pattern.compile(regex);
        return pattern.matcher(CharBuffer.wrap(inputArray)).matches();
    }
}

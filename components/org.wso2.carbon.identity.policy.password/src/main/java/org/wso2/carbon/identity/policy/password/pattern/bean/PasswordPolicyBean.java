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
package org.wso2.carbon.identity.policy.password.pattern.bean;

import java.io.Serializable;

/**
 * Bean that encapsulates the password policy configuration info.
 * TODO:This should be used as a bean w.r.t C5 cinfiguration bean model
 */
public class PasswordPolicyBean implements Serializable {

    private static final long serialVersionUID = -2913500114444797062L;
    private boolean includeUpperCase = true;
    private boolean includeLowerCase = true;
    private boolean includeNumbers = true;
    private boolean includeSymbols = true;
    private int minLength = 6;
    private int maxLength = 12;
    private String symbols = "-+_!@#$%^&*.,?=";
    private String regex = "";
    private boolean isEnabled = true;

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    @Override
    public String toString() {
        return "PasswordPolicyBean{" +
                "includeUpperCase=" + includeUpperCase +
                ", includeLowerCase=" + includeLowerCase +
                ", includeNumbers=" + includeNumbers +
                ", includeSymbols=" + includeSymbols +
                ", minLength=" + minLength +
                ", maxLength=" + maxLength +
                ", symbols='" + symbols + '\'' +
                ", regex='" + regex + '\'' +
                '}';
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public boolean isIncludeUpperCase() {
        return includeUpperCase;
    }

    public void setIncludeUpperCase(boolean includeUpperCase) {
        this.includeUpperCase = includeUpperCase;
    }

    public boolean isIncludeLowerCase() {
        return includeLowerCase;
    }

    public void setIncludeLowerCase(boolean includeLowerCase) {
        this.includeLowerCase = includeLowerCase;
    }

    public boolean isIncludeNumbers() {
        return includeNumbers;
    }

    public void setIncludeNumbers(boolean includeNumbers) {
        this.includeNumbers = includeNumbers;
    }

    public boolean isIncludeSymbols() {
        return includeSymbols;
    }

    public void setIncludeSymbols(boolean includeSymbols) {
        this.includeSymbols = includeSymbols;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getSymbols() {
        return symbols;
    }

    public void setSymbols(String symbols) {
        this.symbols = symbols;
    }
}

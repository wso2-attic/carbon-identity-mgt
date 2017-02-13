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
package org.wso2.carbon.identity.recovery.mapping;

/**
 * Class to represent security question based password recovery config
 */
public class SecurityQuestionsConfig {

    private boolean notifyStart = false;
    private int minAnswers = 2;
    private boolean enableAPI = false;
    private boolean enablePortal = true;
    private String questionSeparator = "!";
    private boolean validateOneByOne = true;

    public boolean isNotifyStart() {
        return notifyStart;
    }

    public void setNotifyStart(boolean notifyStart) {
        this.notifyStart = notifyStart;
    }

    public int getMinAnswers() {
        return minAnswers;
    }

    public void setMinAnswers(int minAnswers) {
        this.minAnswers = minAnswers;
    }

    public boolean isEnableAPI() {
        return enableAPI;
    }

    public void setEnableAPI(boolean enableAPI) {
        this.enableAPI = enableAPI;
    }

    public boolean isEnablePortal() {
        return enablePortal;
    }

    public void setEnablePortal(boolean enablePortal) {
        this.enablePortal = enablePortal;
    }

    public String getQuestionSeparator() {
        return questionSeparator;
    }

    public void setQuestionSeparator(String questionSeparator) {
        this.questionSeparator = questionSeparator;
    }

    public boolean isValidateOneByOne() {
        return validateOneByOne;
    }

    public void setValidateOneByOne(boolean validateOneByOne) {
        this.validateOneByOne = validateOneByOne;
    }
}

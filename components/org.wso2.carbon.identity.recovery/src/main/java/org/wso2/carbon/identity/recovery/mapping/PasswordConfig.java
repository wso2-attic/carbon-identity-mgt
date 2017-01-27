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
public class PasswordConfig {

    private boolean enable;
    private boolean notifyStart;
    private String separator;
    private int minAnswers;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isNotifyStart() {
        return notifyStart;
    }

    public void setNotifyStart(boolean notifyStart) {
        this.notifyStart = notifyStart;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public int getMinAnswers() {
        return minAnswers;
    }

    public void setMinAnswers(int minAnswers) {
        this.minAnswers = minAnswers;
    }
}

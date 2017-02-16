/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.mgt.resolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Unique User.
 */
public class DomainUser {

    private String domainUserId;

    private List<UserPartition> userPartitions;

    private String state;

    public DomainUser(String domainUserId, List<UserPartition> userPartitions) {

        this.domainUserId = domainUserId;
        this.userPartitions = userPartitions;
    }

    public DomainUser(String domainUserId, List<UserPartition> userPartitions, String state) {

        this.domainUserId = domainUserId;
        this.userPartitions = userPartitions;
        this.state = state;
    }

    public DomainUser() {

    }

    public String getDomainUserId() {
        return domainUserId;
    }

    public void setDomainUserId(String domainUserId) {
        this.domainUserId = domainUserId;
    }

    public List<UserPartition> getUserPartitions() {

        if (userPartitions == null) {
            return Collections.emptyList();
        }
        return userPartitions;
    }

    public void setUserPartitions(List<UserPartition> userPartitions) {
        this.userPartitions = userPartitions;
    }

    public void addUserPartition(UserPartition userPartition) {
        if (this.userPartitions == null) {
            this.userPartitions = new ArrayList<>();
        }
        this.userPartitions.add(userPartition);
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }
}

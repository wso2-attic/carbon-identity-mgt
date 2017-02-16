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

package org.wso2.carbon.identity.mgt.bean;

import org.wso2.carbon.identity.mgt.claim.Claim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.security.auth.callback.Callback;

/**
 * Model for User
 */
public class UserBean {

    private List<Claim> claims = new ArrayList<>();

    private List<Callback> credentials = new ArrayList<>();

    private String state;

    public List<Claim> getClaims() {

        if (claims == null) {
            return Collections.emptyList();
        }
        return claims;
    }

    public void setClaims(List<Claim> claims) {
        this.claims = claims;
    }

    public List<Callback> getCredentials() {

        if (claims == null) {
            return Collections.emptyList();
        }
        return credentials;
    }

    public void setCredentials(List<Callback> credentials) {
        this.credentials = credentials;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}

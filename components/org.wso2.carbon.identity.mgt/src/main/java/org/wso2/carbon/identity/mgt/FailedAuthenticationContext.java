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

package org.wso2.carbon.identity.mgt;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents AuthenticationContext which holds the information related to the authentication failed user.
 */
public class FailedAuthenticationContext extends AuthenticationContext {

    List<User> failedUsers = new ArrayList<>();

    public FailedAuthenticationContext() {
        super(null, false);
    }

    public FailedAuthenticationContext(List<User> failedUsers) {
        super(null, false);
        this.failedUsers = failedUsers;
    }

    public void addFailedUserToList(User failedUser) {
        failedUsers.add(failedUser);
    }

    public List<User> getFailedUsers() {
        return failedUsers;
    }
}

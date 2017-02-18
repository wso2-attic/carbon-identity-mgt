/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.identity.common.base.exception.IdentityRuntimeException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents AuthenticationContext which holds the information related to the authenticated user.
 * @param <T1> key
 * @param <T2> value
 */
public class AuthenticationContext<T1 extends Object, T2 extends Object> {

    private User user;
    private boolean authenticated;
    protected Map<T1, T2> parameters = new HashMap();

    public AuthenticationContext() {
        this.authenticated = true;
    }

    public AuthenticationContext(User user) {
        this.user = user;
        this.authenticated = true;
    }

    protected AuthenticationContext(User user, boolean authenticated) {
        this.user = user;
        this.authenticated = authenticated;
    }

    public void addParameter(T1 key, T2 value) {
        if (this.parameters.containsKey(key)) {
            throw new IdentityRuntimeException("Parameters map trying to override existing key " +
                    key);
        }
        parameters.put(key, value);
    }

    public void addParameters(Map<T1, T2> parameters) {
        for (Map.Entry<T1, T2> parameter : parameters.entrySet()) {
            if (this.parameters.containsKey(parameter.getKey())) {
                throw new IdentityRuntimeException("Parameters map trying to override existing key " + parameter
                        .getKey());
            }
            parameters.put(parameter.getKey(), parameter.getValue());
        }
    }

    public Map<T1, T2> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    public T2 getParameter(T1 key) {
        return parameters.get(key);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}

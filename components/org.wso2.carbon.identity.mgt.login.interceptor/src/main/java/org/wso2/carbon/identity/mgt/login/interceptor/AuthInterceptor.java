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

package org.wso2.carbon.identity.mgt.login.interceptor;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.mgt.AuthenticationContext;
import org.wso2.carbon.identity.mgt.RealmService;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.exception.AuthenticationFailure;
import org.wso2.carbon.identity.mgt.impl.util.IdentityMgtConstants;
import org.wso2.carbon.security.caas.api.util.CarbonSecurityConstants;
import org.wso2.msf4j.Interceptor;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.ServiceMethodInfo;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;
import javax.ws.rs.HttpMethod;

/**
 * Authentication and Authorization interceptor for Carbon Admin Services
 */
@Component(
        name = "org.wso2.carbon.security.interceptor.AuthInterceptor",
        service = Interceptor.class,
        immediate = true
)
public class AuthInterceptor implements Interceptor {

    RealmService realmService;

    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);

    @Override
    public boolean preCall(Request request, Response response, ServiceMethodInfo serviceMethodInfo) throws Exception {

        if (request.getUri().startsWith("/scim/v2")){
            if (("/scim/v2/Me".equals(request.getUri()) && HttpMethod.POST.equals(request.getHttpMethod()))
                    || "/scim/v2/ServiceProviderConfig".equals(request.getUri())
                    || "/scim/v2/ResourceType".equals(request.getUri())) {
                return true;
            } else {
                if (request.getHeader("Authorization") == null) {
                    sendUnauthorized(response);
                    return false;
                }

                String authorizationHeader = request.getHeader("Authorization").trim();

                if (authorizationHeader.startsWith(CarbonSecurityConstants.HTTP_AUTHORIZATION_PREFIX_BASIC)) {

                    String credentials = authorizationHeader.split("\\s+")[1];
                    byte[] decodedByte = credentials.getBytes(Charset.forName(StandardCharsets.UTF_8.name()));
                    String authDecoded = new String(Base64.getDecoder().decode(decodedByte),
                            Charset.forName(StandardCharsets.UTF_8.name()));
                    String[] authParts = authDecoded.split(":");
                    if (authParts.length == 2) {
                        String domain = null;
                        String username = authParts[0];
                        if (username.contains("/")) {
                            domain = username.substring(0, username.indexOf("/"));
                            username = username.substring(username.indexOf("/") + 1);
                        }
                        char[] password = authParts[1].toCharArray();

                        PasswordCallback passwordCallback = new PasswordCallback("password", false);
                        passwordCallback.setPassword(password);
                        Callback[] callbacks = {passwordCallback};

                        Claim claim = new Claim(IdentityMgtConstants.CLAIM_ROOT_DIALECT, IdentityMgtConstants.USERNAME_CLAIM,
                                username);

                        try {
                            AuthenticationContext authenticateContext = realmService.getIdentityStore().authenticate(claim,
                                    callbacks, domain);
                            request.setProperty("authzUser", authenticateContext.getUser().getUniqueUserId());
                            return true;
                        } catch (AuthenticationFailure authenticationFailure) {
                            sendUnauthorized(response);
                            return false;
                        }
                    }
                }
                sendUnauthorized(response);
                return false;
            }
        }
        return true;
    }

    @Override
    public void postCall(Request request, int i, ServiceMethodInfo serviceMethodInfo) throws Exception {

    }

    private void sendUnauthorized(Response response) {
        response.setStatus(401);
        response.send();
    }

    @Reference(
            name = "org.wso2.carbon.security.interceptor.RealmService",
            service = RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterRealmService"
    )
    protected void registerRealmService(RealmService realmService, Map<String, String> properties) {

        this.realmService = realmService;
    }


    protected void unregisterRealmService(RealmService realmService) {
    }

}

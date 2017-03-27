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

package org.wso2.carbon.identity.mgt.test.osgi;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.BundleContext;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.mgt.test.osgi.util.IdentityMgtOSGITestConstants;
import org.wso2.carbon.identity.mgt.test.osgi.util.IdentityMgtOSGiTestUtils;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.ops4j.pax.exam.CoreOptions.systemProperty;

/**
 * User micro-service related OSGi tests.
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class UserEndpointTest {

    private static final String BASE_URL = "http://localhost:8080/api/identity/user/v1";
    private static String userUniqueID;
    private static String confirmationCode;
    private static final String USERNAME = "testuser";
    private static final String INVALID_USERNAME = "invaliduser";
    private static final String PASSWORD = "password";
    private static final String DOMAIN = "PRIMARY";
    private static final String INVALID_DOMAIN = "INVALID";

    @Inject
    private BundleContext bundleContext;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Configuration
    public Option[] createConfiguration() {

        List<Option> optionList = IdentityMgtOSGiTestUtils.getDefaultSecurityPAXOptions();

        optionList.add(systemProperty("java.security.auth.login.config")
                               .value(Paths.get(IdentityMgtOSGiTestUtils.getCarbonHome(), "conf", "security",
                                                "carbon-jaas.config").toString()));
        return optionList.toArray(new Option[optionList.size()]);
    }

    @Test
    public void testMeAPI() throws IOException, JSONException {

        HttpURLConnection post = getConnection("/me", HttpMethod.POST, false, "admin:admin", MediaType
                .APPLICATION_JSON);

        JSONObject user = new JSONObject();
        user.put("username", USERNAME);
        user.put("domain", DOMAIN);
        user.put("password", PASSWORD);

        JSONArray claims = new JSONArray();

        JSONObject claim = new JSONObject();
        claim.put("uri", IdentityMgtOSGITestConstants.ClaimURIs.FIRST_NAME_CLAIM_URI);
        claim.put("value", "user");

        claims.put(claim);

        user.put("claims", claims);

        JSONArray properties = new JSONArray();

        properties.put(getProperty());

        JSONObject payload = new JSONObject();
        payload.put("user", user);
        payload.put("properties", properties);

        post.getOutputStream().write(payload.toString().getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals(post.getResponseCode(), Response.Status.CREATED.getStatusCode());

        String content = getContent(post);
        JSONObject result = new JSONObject(content);

        userUniqueID = result.getString("userID");
        confirmationCode = result.getString("code");

        post.disconnect();
    }

    @Test
    public void testMeAPIForInvalidDomain() throws IOException, JSONException {

        HttpURLConnection post = getConnection("/me", HttpMethod.POST, false, "admin:admin", MediaType
                .APPLICATION_JSON);

        JSONObject user = new JSONObject();
        user.put("username", USERNAME);
        user.put("domain", INVALID_DOMAIN);
        user.put("password", PASSWORD);

        JSONArray claims = new JSONArray();

        JSONObject claim = new JSONObject();
        claim.put("uri", IdentityMgtOSGITestConstants.ClaimURIs.FIRST_NAME_CLAIM_URI);
        claim.put("value", "user");

        claims.put(claim);

        user.put("claims", claims);

        JSONArray properties = new JSONArray();

        properties.put(getProperty());

        JSONObject payload = new JSONObject();
        payload.put("user", user);
        payload.put("properties", properties);

        post.getOutputStream().write(payload.toString().getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals(post.getResponseCode(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

        post.disconnect();
    }

    @Test(dependsOnMethods = {"testMeAPI"})
    public void testResendCodeAPI() throws JSONException, IOException {

        HttpURLConnection post = getConnection("/resend-code", HttpMethod.POST, false, "admin:admin", MediaType
                .APPLICATION_JSON);

        JSONObject user = new JSONObject();
        user.put("username", USERNAME);
        user.put("domain", DOMAIN);

        JSONArray properties = new JSONArray();

        properties.put(getProperty());

        JSONObject payload = new JSONObject();
        payload.put("user", user);
        payload.put("properties", properties);

        post.getOutputStream().write(payload.toString().getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals(post.getResponseCode(), Response.Status.OK.getStatusCode());

        String content = getContent(post);
        JSONObject result = new JSONObject(content);

        userUniqueID = result.getString("userID");
        confirmationCode = result.getString("code");

        post.disconnect();
    }

    @Test(dependsOnMethods = {"testMeAPI"})
    public void testResendCodeAPIForInvalidUser() throws JSONException, IOException {

        HttpURLConnection post = getConnection("/resend-code", HttpMethod.POST, false, "admin:admin", MediaType
                .APPLICATION_JSON);

        JSONObject user = new JSONObject();
        user.put("username", INVALID_USERNAME);
        user.put("domain", DOMAIN);

        JSONArray properties = new JSONArray();

        properties.put(getProperty());

        JSONObject payload = new JSONObject();
        payload.put("user", user);
        payload.put("properties", properties);

        post.getOutputStream().write(payload.toString().getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals(post.getResponseCode(), Response.Status.BAD_REQUEST.getStatusCode());

        post.disconnect();
    }

    @Test(dependsOnMethods = {"testMeAPI"})
    public void testResendCodeAPIForInvalidDomain() throws JSONException, IOException {

        HttpURLConnection post = getConnection("/resend-code", HttpMethod.POST, false, "admin:admin", MediaType
                .APPLICATION_JSON);

        JSONObject user = new JSONObject();
        user.put("username", USERNAME);
        user.put("domain", INVALID_DOMAIN);

        JSONArray properties = new JSONArray();

        properties.put(getProperty());

        JSONObject payload = new JSONObject();
        payload.put("user", user);
        payload.put("properties", properties);

        post.getOutputStream().write(payload.toString().getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals(post.getResponseCode(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

        post.disconnect();
    }

    @Test(dependsOnMethods = {"testResendCodeAPI"})
    public void testValidateCodeAPI() throws IOException, JSONException {
        HttpURLConnection post = getConnection("/validate-code", HttpMethod.POST, false, "admin:admin", MediaType
                    .APPLICATION_JSON);

        JSONArray properties = new JSONArray();

        properties.put(getProperty());

        JSONObject payload = new JSONObject();
        payload.put("code", confirmationCode);
        payload.put("properties", properties);

        post.getOutputStream().write(payload.toString().getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals(post.getResponseCode(), Response.Status.ACCEPTED.getStatusCode(),
                            "Invalid response code.");
        post.disconnect();
    }

    @Test(dependsOnMethods = {"testResendCodeAPI"})
    public void testValidateCodeAPIForInvalidCode() throws IOException, JSONException {
        HttpURLConnection post = getConnection("/validate-code", HttpMethod.POST, false, "admin:admin", MediaType
                .APPLICATION_JSON);

        JSONArray properties = new JSONArray();

        properties.put(getProperty());

        JSONObject payload = new JSONObject();
        payload.put("code", "invalid-code");
        payload.put("properties", properties);

        post.getOutputStream().write(payload.toString().getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals(post.getResponseCode(), Response.Status.BAD_REQUEST.getStatusCode());

        post.disconnect();
    }

    private static HttpURLConnection getConnection(String path, String method, boolean keepAlive,
                                                   String authorizationHeader, String contentTypeHeader)
            throws IOException {

        URL url = new URL(BASE_URL + path);

        HttpURLConnection httpURLConnection = null;

        if (BASE_URL.contains("https")) {
            httpURLConnection = (HttpsURLConnection) url.openConnection();
        } else {
            httpURLConnection = (HttpURLConnection) url.openConnection();
        }

        if (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT)) {
            httpURLConnection.setDoOutput(true);
        }
        httpURLConnection.setRequestMethod(method);
        if (!keepAlive) {
            httpURLConnection.setRequestProperty("CONNECTION", "CLOSE");
        }

        if (authorizationHeader != null) {
            String temp = new String(Base64.getEncoder().encode(authorizationHeader.getBytes(StandardCharsets.UTF_8)),
                                     StandardCharsets.UTF_8);

            authorizationHeader = "Basic " + temp;
            httpURLConnection.setRequestProperty(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }
        if (contentTypeHeader != null) {
            httpURLConnection.setRequestProperty(HttpHeaders.CONTENT_TYPE, contentTypeHeader);
        }
        return httpURLConnection;
    }

    private String getContent(HttpURLConnection urlConn) throws IOException {
        return new String(IOUtils.toByteArray(urlConn.getInputStream()), StandardCharsets.UTF_8);
    }

    private JSONObject getProperty() throws JSONException {

        JSONObject property = new JSONObject();
        property.put("key", "K1");
        property.put("value", "V1");

        return property;
    }
}

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

package org.wso2.carbon.identity.user.endpoint.impl;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.recovery.IdentityRecoveryClientException;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.signup.UserSelfSignUpManager;
import org.wso2.carbon.identity.user.endpoint.dto.SelfRegistrationUserDTO;
import org.wso2.carbon.identity.user.endpoint.dto.SelfUserRegistrationRequestDTO;
import org.wso2.carbon.identity.user.endpoint.internal.UserEndpointServiceDataHolder;

import java.util.ArrayList;
import javax.ws.rs.core.Response;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for me API.
 */
public class MeApiServiceImplTest {
    @Test
    public void testMePost() throws Exception {

        UserSelfSignUpManager selfSignUpManager = mock(UserSelfSignUpManager.class);
        doThrow(new IdentityRecoveryException("0001", "Test IdentityRecoveryException."))
                .doThrow(new IdentityRecoveryClientException("0002", "Test IdentityRecoveryClientException"))
                .when(selfSignUpManager).registerUser(any(), anyString(), any());

        UserEndpointServiceDataHolder.getInstance().setUserSelfSignUpManager(selfSignUpManager);

        SelfUserRegistrationRequestDTO dto = new SelfUserRegistrationRequestDTO();

        SelfRegistrationUserDTO userDTO = new SelfRegistrationUserDTO();
        userDTO.setUsername("testUser");
        userDTO.setClaims(new ArrayList<>(1));
        userDTO.setPassword("testPW");
        userDTO.setDomain("testDomain");

        dto.setUser(userDTO);
        dto.setProperties(null);

        Response response = new MeApiServiceImpl().mePost(dto);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

        response = new MeApiServiceImpl().mePost(dto);
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

    }

    @Test
    public void testEmptyUser() throws Exception {

        UserSelfSignUpManager selfSignUpManager = mock(UserSelfSignUpManager.class);
        doThrow(new IdentityRecoveryException("0001", "Test IdentityRecoveryException."))
                .doThrow(new IdentityRecoveryClientException("0002", "Test IdentityRecoveryClientException"))
                .when(selfSignUpManager).registerUser(any(), anyString(), any());

        UserEndpointServiceDataHolder.getInstance().setUserSelfSignUpManager(selfSignUpManager);

        Response response = new MeApiServiceImpl().mePost(null);
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
    }
}

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
import org.wso2.carbon.identity.recovery.IdentityRecoveryRuntimeException;
import org.wso2.carbon.identity.recovery.IdentityRecoveryServerException;
import org.wso2.carbon.identity.recovery.signup.UserSelfSignUpManager;
import org.wso2.carbon.identity.user.endpoint.dto.ResendCodeRequestDTO;
import org.wso2.carbon.identity.user.endpoint.dto.UserDTO;
import org.wso2.carbon.identity.user.endpoint.internal.UserEndpointServiceDataHolder;

import javax.ws.rs.core.Response;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for resend code API.
 */
public class ResendCodeApiServiceImplTest {

    @Test
    public void testResendCodePost() throws Exception {
        UserSelfSignUpManager selfSignUpManager = mock(UserSelfSignUpManager.class);
        doThrow(new RuntimeException())
                .doThrow(new IdentityRecoveryRuntimeException("0001", "Test IdentityRecoveryRuntimeException."))
                .doThrow(new IdentityRecoveryServerException("0002", "Test IdentityRecoveryServerException."))
                .doThrow(new IdentityRecoveryClientException("0003", "Test IdentityRecoveryClientException"))
                .when(selfSignUpManager).resendConfirmationCode(any(), anyString(), any());

        UserEndpointServiceDataHolder.getInstance().setUserSelfSignUpManager(selfSignUpManager);

        ResendCodeRequestDTO dto = new ResendCodeRequestDTO();

        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testUser");
        userDTO.setDomain("testDomain");

        dto.setUser(userDTO);
        dto.setProperties(null);

        Response response = new ResendCodeApiServiceImpl().resendCodePost(dto);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

        response = new ResendCodeApiServiceImpl().resendCodePost(dto);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

        response = new ResendCodeApiServiceImpl().resendCodePost(dto);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

        response = new ResendCodeApiServiceImpl().resendCodePost(dto);
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
    }
}

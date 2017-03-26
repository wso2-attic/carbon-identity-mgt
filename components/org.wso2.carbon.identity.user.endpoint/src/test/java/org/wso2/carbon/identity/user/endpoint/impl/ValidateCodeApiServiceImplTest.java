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
import org.wso2.carbon.identity.user.endpoint.dto.CodeValidationRequestDTO;
import org.wso2.carbon.identity.user.endpoint.internal.UserEndpointServiceDataHolder;

import javax.ws.rs.core.Response;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for validate code API.
 */
public class ValidateCodeApiServiceImplTest {

    @Test
    public void testValidateCodePost() throws Exception {

        UserSelfSignUpManager selfSignUpManager = mock(UserSelfSignUpManager.class);
        doThrow(new RuntimeException())
                .doThrow(new IdentityRecoveryException("0001", "Test IdentityRecoveryException."))
                .doThrow(new IdentityRecoveryClientException("0002", "Test IdentityRecoveryClientException"))
                .when(selfSignUpManager).confirmUserSelfSignUp(any());

        UserEndpointServiceDataHolder.getInstance().setUserSelfSignUpManager(selfSignUpManager);

        CodeValidationRequestDTO dto = new CodeValidationRequestDTO();
        dto.setCode("testCode");

        Response response = new ValidateCodeApiServiceImpl().validateCodePost(dto);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

        response = new ValidateCodeApiServiceImpl().validateCodePost(dto);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

        response = new ValidateCodeApiServiceImpl().validateCodePost(dto);
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
    }
}

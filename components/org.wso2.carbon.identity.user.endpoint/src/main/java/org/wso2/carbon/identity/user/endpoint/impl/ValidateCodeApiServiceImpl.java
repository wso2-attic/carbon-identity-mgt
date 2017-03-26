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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.recovery.IdentityRecoveryClientException;
import org.wso2.carbon.identity.recovery.IdentityRecoveryConstants.ErrorMessages;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.signup.UserSelfSignUpManager;
import org.wso2.carbon.identity.user.endpoint.NotFoundException;
import org.wso2.carbon.identity.user.endpoint.ValidateCodeApiService;
import org.wso2.carbon.identity.user.endpoint.dto.CodeValidationRequestDTO;
import org.wso2.carbon.identity.user.endpoint.dto.ErrorDTO;
import org.wso2.carbon.identity.user.endpoint.util.Utils;

import javax.ws.rs.core.Response;

/**
 * Micro service implementation of /validatecode endpoint.
 */
public class ValidateCodeApiServiceImpl extends ValidateCodeApiService {

    private static final Logger log = LoggerFactory.getLogger(ValidateCodeApiServiceImpl.class);

    @Override
    public Response validateCodePost(CodeValidationRequestDTO code) throws NotFoundException {

        UserSelfSignUpManager userSelfSignUpManager = Utils.getUserSelfSignUpManager();

        try {
            userSelfSignUpManager.confirmUserSelfSignUp(code.getCode());
        } catch (IdentityRecoveryClientException e) {

            if (log.isDebugEnabled()) {
                log.debug("Client error while user confirmation.", e);
            }

            ErrorDTO errorDTO = Utils.buildBadRequestErrorDTO(e.getErrorCode(), e.getErrorDescription());
            return Response.status(Response.Status.BAD_REQUEST).entity(errorDTO).build();
        } catch (IdentityRecoveryException e) {

            log.error("Server error while user confirmation.", e);
            ErrorDTO errorDTO = Utils.buildInternalServerErrorDTO(e.getErrorCode(), e.getErrorDescription());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorDTO).build();
        } catch (Throwable e) {

            log.error("Server error while user confirmation.", e);
            ErrorDTO errorDTO = Utils.buildInternalServerErrorDTO(ErrorMessages.ERROR_CODE_UNEXPECTED.getCode(),
                                                                  e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorDTO).build();
        }
        return Response.accepted().build();
    }
}

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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.recovery.IdentityRecoveryClientException;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.bean.NotificationResponseBean;
import org.wso2.carbon.identity.recovery.signup.UserSelfSignUpManager;
import org.wso2.carbon.identity.user.endpoint.MeApiService;
import org.wso2.carbon.identity.user.endpoint.NotFoundException;
import org.wso2.carbon.identity.user.endpoint.dto.ErrorDTO;
import org.wso2.carbon.identity.user.endpoint.dto.SelfUserRegistrationRequestDTO;
import org.wso2.carbon.identity.user.endpoint.dto.SelfUserRegistrationResponseDTO;
import org.wso2.carbon.identity.user.endpoint.util.Utils;

import javax.ws.rs.core.Response;

import static org.wso2.carbon.identity.recovery.IdentityRecoveryConstants.ErrorCodes;

/**
 * Micro service implementation of /me endpoint.
 */
public class MeApiServiceImpl extends MeApiService {

    private static final Logger log = LoggerFactory.getLogger(MeApiServiceImpl.class);

    @Override
    public Response mePost(SelfUserRegistrationRequestDTO user) throws NotFoundException {

        UserSelfSignUpManager userSelfSignUpManager = Utils.getUserSelfSignUpManager();

        NotificationResponseBean notificationResponseBean;
        try {

            notificationResponseBean = userSelfSignUpManager.registerUser(Utils.getUserBean
                    (user.getUser()), user.getUser().getDomain(), Utils.getProperties(user.getProperties()));
        } catch (IdentityRecoveryClientException e) {

            if (log.isDebugEnabled()) {
                log.debug("Client error while registering self sign-up user: " + user.getUser().getUsername(), e);
            }
            ErrorDTO errorDTO = Utils.buildBadRequestErrorDTO(e.getErrorCode(), e.getErrorDescription());
            return Response.status(Response.Status.BAD_REQUEST).entity(errorDTO).build();
        } catch (IdentityRecoveryException e) {

            log.error("Server error while registering self sign-up user: " + user.getUser().getUsername(), e);
            ErrorDTO errorDTO = Utils.buildInternalServerErrorDTO(e.getErrorCode(), e.getErrorDescription());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorDTO).build();
        } catch (Throwable e) {

            log.error("Server error while registering self sign-up user: " + user.getUser().getUsername(), e);
            ErrorDTO errorDTO = Utils.buildInternalServerErrorDTO(ErrorCodes.UNEXPECTED.getCode(), e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorDTO).build();
        }

        if (StringUtils.isBlank(notificationResponseBean.getCode())) {
            return Response.status(Response.Status.CREATED).build();
        }

        SelfUserRegistrationResponseDTO responseDTO = new SelfUserRegistrationResponseDTO();
        responseDTO.setCode(notificationResponseBean.getCode());
        responseDTO.setUserID(notificationResponseBean.getUserUniqueId());

        return Response.status(Response.Status.CREATED).entity(responseDTO).build();
    }
}

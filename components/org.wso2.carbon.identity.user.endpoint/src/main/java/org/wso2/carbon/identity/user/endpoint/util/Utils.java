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

package org.wso2.carbon.identity.user.endpoint.util;

import org.wso2.carbon.identity.mgt.bean.UserBean;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.impl.util.IdentityMgtConstants;
import org.wso2.carbon.identity.recovery.model.Property;
import org.wso2.carbon.identity.recovery.signup.UserSelfSignUpManager;
import org.wso2.carbon.identity.user.endpoint.dto.ErrorDTO;
import org.wso2.carbon.identity.user.endpoint.dto.PropertyDTO;
import org.wso2.carbon.identity.user.endpoint.dto.SelfRegistrationUserDTO;
import org.wso2.carbon.identity.user.endpoint.internal.UserEndpointServiceDataHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.security.auth.callback.PasswordCallback;
import javax.ws.rs.core.Response;

/**
 * Utility class for user endpoint.
 */
public class Utils {

    public static UserSelfSignUpManager getUserSelfSignUpManager() {
        //FrameworkUtil.getBundle()
        return UserEndpointServiceDataHolder.getInstance().getUserSelfSignUpManager();
    }

    public static UserBean getUserBean(SelfRegistrationUserDTO dto) {

        Claim usernameClaim = getUsernameClaim(dto.getUsername());
        List<Claim> claims = new ArrayList<>();
        claims.add(usernameClaim);

        dto.getClaims().stream().forEach(claim -> claims.add(new Claim(IdentityMgtConstants.CLAIM_ROOT_DIALECT, claim
                .getUri(), claim.getValue())));

        PasswordCallback passwordCallback = new PasswordCallback("Password", false);
        passwordCallback.setPassword(dto.getPassword().toCharArray());

        UserBean userBean = new UserBean();
        userBean.setClaims(claims);
        userBean.setCredentials(Collections.singletonList(passwordCallback));

        return userBean;
    }

    public static Property[] getProperties(List<PropertyDTO> propertyDTOs) {

        if (propertyDTOs == null) {
            return new Property[0];
        }

        Property[] properties = new Property[propertyDTOs.size()];
        for (int i = 0; i < propertyDTOs.size(); i++) {
            Property property = new Property(propertyDTOs.get(i).getKey(), propertyDTOs.get(i).getValue());
            properties[i] = property;
        }
        return properties;
    }

    /**
     * Returns a generic errorDTO
     *
     * @param message specifies the error message
     * @return A generic errorDTO with the specified details
     */
    public static ErrorDTO getErrorDTO(String message, String code, String description) {

        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setCode(code);
        errorDTO.setMessage(message);
        errorDTO.setDescription(description);
        return errorDTO;
    }

    public static ErrorDTO buildBadRequestErrorDTO(String code, String description) {

        return getErrorDTO(Response.Status.BAD_REQUEST.getReasonPhrase(), code, description);
    }

    public static ErrorDTO buildInternalServerErrorDTO(String code, String description) {

        return getErrorDTO(Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase(), code, description);
    }

    public static Claim getUsernameClaim(String username) {

        Claim claim = new Claim(IdentityMgtConstants.CLAIM_ROOT_DIALECT, IdentityMgtConstants.USERNAME_CLAIM, username);
        return claim;
    }
}

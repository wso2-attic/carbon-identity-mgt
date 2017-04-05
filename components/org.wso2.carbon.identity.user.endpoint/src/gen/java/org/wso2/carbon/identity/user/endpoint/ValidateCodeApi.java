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

package org.wso2.carbon.identity.user.endpoint;

import io.swagger.annotations.ApiParam;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.identity.user.endpoint.dto.CodeValidationRequestDTO;
import org.wso2.carbon.identity.user.endpoint.factories.ValidateCodeApiServiceFactory;
import org.wso2.msf4j.Microservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Component(
    name = "org.wso2.carbon.identity.user.endpoint.ValidateCodeApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/identity/user/v1/validate-code")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the validate-code API")
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-03-01T19:30:42.380+05:30")
public class ValidateCodeApi implements Microservice  {
   private final ValidateCodeApiService delegate = ValidateCodeApiServiceFactory.getValidateCodeApi();

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Validate Code ", notes = "This API is used to validate code of self reigstered users ", response = void.class, tags={ "Self Register", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 202, message = "Accepted", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error", response = void.class) })
    public Response validateCodePost(@ApiParam(value = "Code retried after user self registration and optional property parameters" ,required=true) CodeValidationRequestDTO code
)
    throws NotFoundException {
        return delegate.validateCodePost(code);
    }
}

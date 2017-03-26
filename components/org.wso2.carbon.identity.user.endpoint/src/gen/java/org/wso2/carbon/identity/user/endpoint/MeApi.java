package org.wso2.carbon.identity.user.endpoint;

import org.wso2.carbon.identity.user.endpoint.factories.MeApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.identity.user.endpoint.dto.ErrorDTO;
import org.wso2.carbon.identity.user.endpoint.dto.SelfUserRegistrationRequestDTO;
import org.wso2.carbon.identity.user.endpoint.dto.SelfUserRegistrationResponseDTO;

import org.wso2.msf4j.Microservice;
import org.osgi.service.component.annotations.Component;

import java.io.InputStream;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Component(
    name = "org.wso2.carbon.identity.user.endpoint.MeApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/identity/user/v1/me")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the me API")
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-03-01T19:30:42.380+05:30")
public class MeApi implements Microservice  {
   private final MeApiService delegate = MeApiServiceFactory.getMeApi();

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Register User ", notes = "This API is used to user self registration. ", response = SelfUserRegistrationResponseDTO.class, tags={ "Self Register", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Successfully created", response = SelfUserRegistrationResponseDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request", response = SelfUserRegistrationResponseDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 409, message = "Conflict", response = SelfUserRegistrationResponseDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error", response = SelfUserRegistrationResponseDTO.class) })
    public Response mePost(@ApiParam(value = "It can be sent optional property parameters over email based on email" +
                                               " template." ,required=true) SelfUserRegistrationRequestDTO user
)
    throws NotFoundException {
        return delegate.mePost(user);
    }
}

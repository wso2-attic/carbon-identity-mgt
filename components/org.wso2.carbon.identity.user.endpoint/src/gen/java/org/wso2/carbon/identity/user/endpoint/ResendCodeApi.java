package org.wso2.carbon.identity.user.endpoint;

import org.wso2.carbon.identity.user.endpoint.factories.ResendCodeApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.identity.user.endpoint.dto.ErrorDTO;
import org.wso2.carbon.identity.user.endpoint.dto.ResendCodeRequestDTO;
import org.wso2.carbon.identity.user.endpoint.dto.ResendConfirmationCodeResponseDTO;

import org.wso2.msf4j.Microservice;
import org.osgi.service.component.annotations.Component;

import java.io.InputStream;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Component(
    name = "org.wso2.carbon.identity.user.endpoint.ResendCodeApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/identity/user/v1/resend-code")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the resend-code API")
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-03-01T19:30:42.380+05:30")
public class ResendCodeApi implements Microservice  {
   private final ResendCodeApiService delegate = ResendCodeApiServiceFactory.getResendCodeApi();

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Resend Code ", notes = "This API is used to resend confirmation code, if it is missing. ", response = ResendConfirmationCodeResponseDTO.class, tags={ "Self Register", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Successful", response = ResendConfirmationCodeResponseDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request", response = ResendConfirmationCodeResponseDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error", response = ResendConfirmationCodeResponseDTO.class) })
    public Response resendCodePost(@ApiParam(value = "It can be sent optional property parameters over email based on email template." ,required=true) ResendCodeRequestDTO user
)
    throws NotFoundException {
        return delegate.resendCodePost(user);
    }
}

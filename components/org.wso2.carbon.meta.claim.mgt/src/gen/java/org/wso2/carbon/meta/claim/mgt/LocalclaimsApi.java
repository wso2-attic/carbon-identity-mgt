package org.wso2.carbon.meta.claim.mgt;

import org.wso2.carbon.meta.claim.mgt.factories.LocalclaimsApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.meta.claim.mgt.dto.ErrorDTO;
import org.wso2.carbon.meta.claim.mgt.dto.LocalClaimDTO;

import org.wso2.msf4j.Microservice;
import org.osgi.service.component.annotations.Component;

import java.io.InputStream;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Component(
    name = "org.wso2.carbon.meta.claim.mgt.LocalclaimsApi",
    service = Microservice.class,
    immediate = true
)
@Path("/localclaims")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the localclaims API")
@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-17T23:15:30.678+05:30")
public class LocalclaimsApi implements Microservice  {
   private final LocalclaimsApiService delegate = LocalclaimsApiServiceFactory.getLocalclaimsApi();

    @DELETE
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Remove an existing local claim. ", response = String.class, tags={ "Meta Claim", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Successfully Removed.", response = String.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Resource not found.", response = String.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error", response = String.class) })
    public Response localclaimsDelete(@ApiParam(value = "claim dialect that is to be removed.",required=true) @QueryParam("claim") String claim
)
    throws NotFoundException {
        return delegate.localclaimsDelete(claim);
    }
    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get all the available local claims ", response = LocalClaimDTO.class, responseContainer = "List", tags={ "Meta Claim", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "An array of available local claims with their meta details", response = LocalClaimDTO.class, responseContainer = "List"),
        
        @io.swagger.annotations.ApiResponse(code = 200, message = "Unexpected error", response = LocalClaimDTO.class, responseContainer = "List") })
    public Response localclaimsGet()
    throws NotFoundException {
        return delegate.localclaimsGet();
    }
    @PATCH
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Add a new local claim. ", response = String.class, tags={ "Meta Claim", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Successfully Created.", response = String.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request", response = String.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error", response = String.class) })
    public Response localclaimsPatch(@ApiParam(value = "new local claim with it's meta details" ,required=true) LocalClaimDTO localclaim
)
    throws NotFoundException {
        return delegate.localclaimsPatch(localclaim);
    }
    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Add a new local claim. ", response = String.class, tags={ "Meta Claim", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Successfully Created.", response = String.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request", response = String.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error", response = String.class) })
    public Response localclaimsPost(@ApiParam(value = "new local claim with it's meta details" ,required=true) LocalClaimDTO localclaim
)
    throws NotFoundException {
        return delegate.localclaimsPost(localclaim);
    }
}

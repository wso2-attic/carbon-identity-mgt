package org.wso2.carbon.meta.claim.mgt;

import io.swagger.annotations.ApiParam;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.meta.claim.mgt.dto.ExternalClaimDTO;
import org.wso2.carbon.meta.claim.mgt.factories.ExternalclaimsApiServiceFactory;
import org.wso2.msf4j.Microservice;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Component(
        name = "org.wso2.carbon.meta.claim.mgt.ExternalclaimsApi",
        service = Microservice.class,
        immediate = true
)
@Path("/externalclaims")
@Consumes({"application/json"})
@Produces({"application/json"})
@io.swagger.annotations.Api(description = "the externalclaims API")
@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-17T23:15:30.678+05:30")
public class ExternalclaimsApi implements Microservice {
    private final ExternalclaimsApiService delegate = ExternalclaimsApiServiceFactory.getExternalclaimsApi();

    @DELETE

    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "", notes = "Remove an existing external claim. ", response = String.class, tags = {"Meta Claim",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Successfully Removed.", response = String.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Resource not found.", response = String.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error", response = String.class)})
    public Response externalclaimsDelete(@ApiParam(value = "claim dialect URI of the claim URI, that is to be removed.", required = true) @QueryParam("dialectURI") String dialectURI
            , @ApiParam(value = "claim URI that is to be removed.", required = true) @QueryParam("claimURI") String claimURI
    )
            throws NotFoundException {
        return delegate.externalclaimsDelete(dialectURI, claimURI);
    }

    @GET

    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get all the available external claims for the given dialect ", response = ExternalClaimDTO.class, responseContainer = "List", tags = {"Meta Claim",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "An array of available external claims with their meta details", response = ExternalClaimDTO.class, responseContainer = "List"),

            @io.swagger.annotations.ApiResponse(code = 200, message = "Unexpected error", response = ExternalClaimDTO.class, responseContainer = "List")})
    public Response externalclaimsGet(@ApiParam(value = "claim dialect URI for the external claims to be retrieved.", required = true) @QueryParam("dialect") String dialect
    )
            throws NotFoundException {
        return delegate.externalclaimsGet(dialect);
    }

    @PATCH

    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "", notes = "Update an external claim ", response = String.class, tags = {"Meta Claim",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 201, message = "Successfully Created.", response = String.class),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request", response = String.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error", response = String.class)})
    public Response externalclaimsPatch(@ApiParam(value = "External claim with updated meta details", required = true) ExternalClaimDTO externalclaim
    )
            throws NotFoundException {
        return delegate.externalclaimsPatch(externalclaim);
    }

    @POST

    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "", notes = "Add a new external claim. ", response = String.class, tags = {"Meta Claim",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 201, message = "Successfully Created.", response = String.class),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request", response = String.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error", response = String.class)})
    public Response externalclaimsPost(@ApiParam(value = "new extrenal claim with it's meta details", required = true) ExternalClaimDTO externalclaim
    )
            throws NotFoundException {
        return delegate.externalclaimsPost(externalclaim);
    }
}

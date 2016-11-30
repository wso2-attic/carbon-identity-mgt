package org.wso2.carbon.meta.claim.mgt;

import io.swagger.annotations.ApiParam;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.meta.claim.mgt.dto.ClaimDialectDTO;
import org.wso2.carbon.meta.claim.mgt.dto.PairOfClaimsDTO;
import org.wso2.carbon.meta.claim.mgt.factories.DialectsApiServiceFactory;
import org.wso2.msf4j.Microservice;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Component(
        name = "org.wso2.carbon.meta.claim.mgt.DialectsApi",
        service = Microservice.class,
        immediate = true
)
@Path("/dialects")
@Consumes({"application/json"})
@Produces({"application/json"})
@io.swagger.annotations.Api(description = "the dialects API")
@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-17T23:15:30.678+05:30")
public class DialectsApi implements Microservice {
    private final DialectsApiService delegate = DialectsApiServiceFactory.getDialectsApi();

    @DELETE

    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "", notes = "Remove an existing claim dialect. ", response = String.class, tags = {"Meta Claim",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Successfully Removed.", response = String.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Resource not found.", response = String.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error", response = String.class)})
    public Response dialectsDelete(@ApiParam(value = "claim dialect that is to be removed.", required = true) @QueryParam("claim") String claim
    )
            throws NotFoundException {
        return delegate.dialectsDelete(claim);
    }

    @GET

    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get all the available claim dialects ", response = ClaimDialectDTO.class, responseContainer = "List", tags = {"Meta Claim",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "An array of available claim dialects with the dialect URI", response = ClaimDialectDTO.class, responseContainer = "List"),

            @io.swagger.annotations.ApiResponse(code = 200, message = "Unexpected error", response = ClaimDialectDTO.class, responseContainer = "List")})
    public Response dialectsGet()
            throws NotFoundException {
        return delegate.dialectsGet();
    }

    @POST

    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "", notes = "Add a new claim dialect. ", response = String.class, tags = {"Meta Claim",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 201, message = "Successfully Created.", response = String.class),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request", response = String.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error", response = String.class)})
    public Response dialectsPost(@ApiParam(value = "new dialect with it's dialect URI.", required = true) ClaimDialectDTO claimDialect
    )
            throws NotFoundException {
        return delegate.dialectsPost(claimDialect);
    }

    @PUT

    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "", notes = "Rename an existing claim dialect. ", response = String.class, tags = {"Meta Claim",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 201, message = "Successfully Created.", response = String.class),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request", response = String.class),

            @io.swagger.annotations.ApiResponse(code = 401, message = "Resource not found.", response = String.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error", response = String.class)})
    public Response dialectsPut(@ApiParam(value = "old claim dialect that is to be renamed.", required = true) PairOfClaimsDTO claimPair
    )
            throws NotFoundException {
        return delegate.dialectsPut(claimPair);
    }
}

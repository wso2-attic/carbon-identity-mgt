package org.wso2.carbon.meta.claim.mgt.impl;

import org.wso2.carbon.meta.claim.mgt.ApiResponseMessage;
import org.wso2.carbon.meta.claim.mgt.ExternalclaimsApiService;
import org.wso2.carbon.meta.claim.mgt.NotFoundException;
import org.wso2.carbon.meta.claim.mgt.dto.ExternalClaimDTO;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-17T23:15:30.678+05:30")
public class ExternalclaimsApiServiceImpl extends ExternalclaimsApiService {
    @Override
    public Response externalclaimsDelete(String dialectURI
, String claimURI
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response externalclaimsGet(String dialect
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response externalclaimsPatch(ExternalClaimDTO externalclaim
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response externalclaimsPost(ExternalClaimDTO externalclaim
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}

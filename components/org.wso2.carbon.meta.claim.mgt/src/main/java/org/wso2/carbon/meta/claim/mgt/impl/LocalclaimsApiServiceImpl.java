package org.wso2.carbon.meta.claim.mgt.impl;

import org.wso2.carbon.meta.claim.mgt.ApiResponseMessage;
import org.wso2.carbon.meta.claim.mgt.LocalclaimsApiService;
import org.wso2.carbon.meta.claim.mgt.NotFoundException;
import org.wso2.carbon.meta.claim.mgt.dto.LocalClaimDTO;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-17T23:15:30.678+05:30")
public class LocalclaimsApiServiceImpl extends LocalclaimsApiService {
    @Override
    public Response localclaimsDelete(String claim
    ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response localclaimsGet() throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response localclaimsPatch(LocalClaimDTO localclaim
    ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response localclaimsPost(LocalClaimDTO localclaim
    ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}

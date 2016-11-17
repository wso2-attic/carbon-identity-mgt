package org.wso2.carbon.meta.claim.mgt.impl;

import org.wso2.carbon.meta.claim.mgt.ApiResponseMessage;
import org.wso2.carbon.meta.claim.mgt.DialectsApiService;
import org.wso2.carbon.meta.claim.mgt.NotFoundException;
import org.wso2.carbon.meta.claim.mgt.dto.ClaimDialectDTO;
import org.wso2.carbon.meta.claim.mgt.dto.PairOfClaimsDTO;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-17T23:15:30.678+05:30")
public class DialectsApiServiceImpl extends DialectsApiService {
    @Override
    public Response dialectsDelete(String claim
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response dialectsGet() throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response dialectsPost(ClaimDialectDTO claimDialect
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response dialectsPut(PairOfClaimsDTO claimPair
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}

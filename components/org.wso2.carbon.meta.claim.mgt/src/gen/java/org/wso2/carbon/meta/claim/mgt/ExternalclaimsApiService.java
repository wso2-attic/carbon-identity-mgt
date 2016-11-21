package org.wso2.carbon.meta.claim.mgt;

import org.wso2.carbon.meta.claim.mgt.*;
import org.wso2.carbon.meta.claim.mgt.NotFoundException;
import org.wso2.carbon.meta.claim.mgt.dto.*;
import org.wso2.carbon.meta.claim.mgt.dto.ExternalClaimDTO;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-17T23:15:30.678+05:30")
public abstract class ExternalclaimsApiService {
    public abstract Response externalclaimsDelete(String dialectURI
            , String claimURI
    ) throws NotFoundException;

    public abstract Response externalclaimsGet(String dialect
    ) throws NotFoundException;

    public abstract Response externalclaimsPatch(ExternalClaimDTO externalclaim
    ) throws NotFoundException;

    public abstract Response externalclaimsPost(ExternalClaimDTO externalclaim
    ) throws NotFoundException;
}

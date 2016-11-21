package org.wso2.carbon.meta.claim.mgt;

import org.wso2.carbon.meta.claim.mgt.*;
import org.wso2.carbon.meta.claim.mgt.NotFoundException;
import org.wso2.carbon.meta.claim.mgt.dto.*;
import org.wso2.carbon.meta.claim.mgt.dto.LocalClaimDTO;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-17T23:15:30.678+05:30")
public abstract class LocalclaimsApiService {
    public abstract Response localclaimsDelete(String claim
    ) throws NotFoundException;

    public abstract Response localclaimsGet() throws NotFoundException;

    public abstract Response localclaimsPatch(LocalClaimDTO localclaim
    ) throws NotFoundException;

    public abstract Response localclaimsPost(LocalClaimDTO localclaim
    ) throws NotFoundException;
}

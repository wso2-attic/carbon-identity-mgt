package org.wso2.carbon.meta.claim.mgt;

import org.wso2.carbon.meta.claim.mgt.*;
import org.wso2.carbon.meta.claim.mgt.dto.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;

import org.wso2.carbon.meta.claim.mgt.dto.ErrorDTO;
import org.wso2.carbon.meta.claim.mgt.dto.LocalClaimDTO;

import java.util.List;
import org.wso2.carbon.meta.claim.mgt.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

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

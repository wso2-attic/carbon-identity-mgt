package org.wso2.carbon.meta.claim.mgt;

import org.wso2.carbon.meta.claim.mgt.*;
import org.wso2.carbon.meta.claim.mgt.dto.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;

import org.wso2.carbon.meta.claim.mgt.dto.ErrorDTO;
import org.wso2.carbon.meta.claim.mgt.dto.ExternalClaimDTO;

import java.util.List;
import org.wso2.carbon.meta.claim.mgt.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-17T23:15:30.678+05:30")
public abstract class ExternalclaimsApiService {
    public abstract Response externalclaimsDelete(String dialectURI
 ,String claimURI
 ) throws NotFoundException;
    public abstract Response externalclaimsGet(String dialect
 ) throws NotFoundException;
    public abstract Response externalclaimsPatch(ExternalClaimDTO externalclaim
 ) throws NotFoundException;
    public abstract Response externalclaimsPost(ExternalClaimDTO externalclaim
 ) throws NotFoundException;
}

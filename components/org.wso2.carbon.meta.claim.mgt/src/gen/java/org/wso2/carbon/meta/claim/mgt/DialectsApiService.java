package org.wso2.carbon.meta.claim.mgt;

import org.wso2.carbon.meta.claim.mgt.dto.ClaimDialectDTO;
import org.wso2.carbon.meta.claim.mgt.dto.PairOfClaimsDTO;

import javax.ws.rs.core.Response;

import static com.sun.xml.internal.ws.api.message.Packet.Status.Response;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen",
                            date = "2016-11-17T23:15:30.678+05:30")
public abstract class DialectsApiService {
    public abstract Response dialectsDelete(String claim) throws NotFoundException;

    public abstract Response dialectsGet() throws NotFoundException;

    public abstract Response dialectsPost(ClaimDialectDTO claimDialect) throws NotFoundException;

    public abstract Response dialectsPut(PairOfClaimsDTO claimPair) throws NotFoundException;
}

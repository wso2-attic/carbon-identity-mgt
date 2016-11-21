package org.wso2.carbon.identity.mgt.claim;

import java.util.List;

/**
 * MetaClaimStore file object used for ClaimConfigBuilder initialization.
 */
public class MetaClaimStoreFile {

    private List<MetaClaim> claims;

    public List<MetaClaim> getClaims() {
        return claims;
    }

    public void setClaims(List<MetaClaim> claims) {
        this.claims = claims;
    }
}

package org.wso2.carbon.identity.mgt.service.impl;

import org.wso2.carbon.identity.mgt.exception.CarbonIdentityMgtConfigException;
import org.wso2.carbon.identity.mgt.internal.config.claim.mapping.ClaimMappingBuilder;
import org.wso2.carbon.identity.mgt.service.ClaimResolvingService;

import java.util.Map;

/**
 *
 */
public class ClaimResolvingServiceImpl implements ClaimResolvingService {
    /**
     * Provides claim mappings for applications.
     *
     * @param applicationName : Uniquely identifying name for application.
     * @return Map(application claims : root claim)
     * @throws CarbonIdentityMgtConfigException
     */
    @Override
    public Map<String, String> getApplicationClaimMapping(String applicationName) throws
            CarbonIdentityMgtConfigException {
        return ClaimMappingBuilder.getInstance().getApplicationClaimMapping(applicationName);
    }

    /**
     * Provides claim mappings for IDPs.
     *
     * @param idpName : Uniquely identifying name for IDPs.
     * @return Map(Idp claim : root claim)
     * @throws CarbonIdentityMgtConfigException
     */
    @Override
    public Map<String, String> getIdpClaimMapping(String idpName) throws CarbonIdentityMgtConfigException {
        return ClaimMappingBuilder.getInstance().getIdpClaimMapping(idpName);
    }

    /**
     * Provides claim mappings for standards.
     *
     * @param standardName : Uniquely identifying name for standards.
     * @return Map(Standard claim : root claim)
     * @throws CarbonIdentityMgtConfigException
     */
    @Override
    public Map<String, String> getStandardClaimMapping(String standardName) throws CarbonIdentityMgtConfigException {
        return ClaimMappingBuilder.getInstance().getStandardClaimMapping(standardName);
    }
}

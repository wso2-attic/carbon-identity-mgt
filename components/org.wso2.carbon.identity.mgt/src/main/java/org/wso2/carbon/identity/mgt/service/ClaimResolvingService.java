package org.wso2.carbon.identity.mgt.service;

import org.wso2.carbon.identity.mgt.exception.CarbonSecurityConfigException;

import java.util.Map;

/**
 * Provides the claim mappings for applications, idps and standards.
 */
public interface ClaimResolvingService {

    /**
     * Provides claim mappings for applications.
     *
     * @param applicationName : Uniquely identifying name for application.
     * @return Map(application claims : root claim)
     * @throws CarbonSecurityConfigException
     */
    Map<String, String> getApplicationClaimMapping(String applicationName) throws CarbonSecurityConfigException;

    /**
     * Provides claim mappings for IDPs.
     *
     * @param idpName : Uniquely identifying name for IDPs.
     * @return Map(Idp claim : root claim)
     * @throws CarbonSecurityConfigException
     */
    Map<String, String> getIdpClaimMapping(String idpName) throws CarbonSecurityConfigException;

    /**
     * Provides claim mappings for standards.
     *
     * @param standardName : Uniquely identifying name for standards.
     * @return Map(Standard claim : root claim)
     * @throws CarbonSecurityConfigException
     */
    Map<String, String> getStandardClaimMapping(String standardName) throws CarbonSecurityConfigException;
}

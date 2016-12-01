package org.wso2.carbon.identity.meta.claim.mgt.service;

import org.wso2.carbon.identity.meta.claim.mgt.exception.ClaimResolvingServiceException;

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
     * @throws ClaimResolvingServiceException : Error in getting the claim mapping for application.
     */
    Map<String, String> getApplicationClaimMapping(String applicationName) throws ClaimResolvingServiceException;

    /**
     * Provides claim mappings for IDPs.
     *
     * @param idpName : Uniquely identifying name for IDPs.
     * @return Map(Idp claim : root claim)
     * @throws ClaimResolvingServiceException : Error in getting the claim mapping for IDP.
     */
    Map<String, String> getIdpClaimMapping(String idpName) throws ClaimResolvingServiceException;

    /**
     * Provides claim mappings for standards.
     *
     * @param standardName : Uniquely identifying name for standards.
     * @return Map(Standard claim : root claim)
     * @throws ClaimResolvingServiceException : Error in getting the claim mapping for standard.
     */
    Map<String, String> getStandardClaimMapping(String standardName) throws ClaimResolvingServiceException;
}

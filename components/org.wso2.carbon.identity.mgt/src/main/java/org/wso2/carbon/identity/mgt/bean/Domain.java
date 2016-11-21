/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.mgt.bean;

import org.wso2.carbon.identity.mgt.claim.MetaClaimMapping;
import org.wso2.carbon.identity.mgt.exception.DomainException;
import org.wso2.carbon.identity.mgt.store.connector.CredentialStoreConnector;
import org.wso2.carbon.identity.mgt.store.connector.IdentityStoreConnector;
import org.wso2.carbon.identity.mgt.user.UniqueIdResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a domain.
 */
public class Domain {

    /**
     * Mapping between IdentityStoreConnector ID and IdentityStoreConnector
     */
    private Map<String, IdentityStoreConnector> identityStoreConnectorsMap = new HashMap<>();

    /**
     * Mapping between CredentialStoreConnector ID and CredentialStoreConnector
     */
    private Map<String, CredentialStoreConnector> credentialStoreConnectorsMap = new HashMap<>();

    /**
     * Set of IdentityStoreConnectors for this domain sorted by their priority.
     */
    private List<IdentityStoreConnector> identityStoreConnectors = new ArrayList<>();

    /**
     * Set of CredentialStoreConnectors for this domain sorted by their priority.
     */
    private List<CredentialStoreConnector> credentialStoreConnectors = new ArrayList<>();

    /**
     * Mapping between IdentityStoreConnector ID and MetaClaimMapping
     */
    private Map<String, List<MetaClaimMapping>> connectorIdToMetaClaimMappings = new HashMap<>();

    private List<MetaClaimMapping> metaClaimMappings = new ArrayList<>();

    private Map<String, MetaClaimMapping> claimUriToMetaClaimMappings = new HashMap<>();

    /**
     * Name of the domain.
     */
    private String domainName;

    /**
     * Priority of the domain.
     * Highest priority for domain is 1
     * Domain priority value should be greater than 0
     */
    private int domainPriority;

    private UniqueIdResolver uniqueIdResolver;

    public Domain(String domainName, int domainPriority, UniqueIdResolver uniqueIdResolver) throws DomainException {

        if (domainPriority < 1) {
            throw new DomainException("Domain priority value should be greater than 0");
        }

        this.domainName = domainName;
        this.domainPriority = domainPriority;
        this.uniqueIdResolver = uniqueIdResolver;
    }

    /**
     * Get the domain name.
     *
     * @return String - domain name
     */
    public String getDomainName() {
        return domainName;
    }

    /**
     * Get the priority of the domain.
     *
     * @return integer - domain priority
     */
    public int getDomainPriority() {

        return domainPriority;
    }

    /**
     * Add an identity store connector to the map.
     *
     * @param identityStoreConnector Identity Store connector.
     * @throws DomainException domain exception.
     */
    public void addIdentityStoreConnector(IdentityStoreConnector identityStoreConnector) throws DomainException {

        identityStoreConnectorsMap.put(identityStoreConnector.getIdentityStoreConfig().getConnectorId(),
                identityStoreConnector);
        identityStoreConnectors.add(identityStoreConnector);
    }

    /**
     * Get IdentityStoreConnector from identity store connector id.
     *
     * @param identityStoreConnectorId identity store connector id.
     * @return IdentityStoreConnector instance.
     */
    public IdentityStoreConnector getIdentityStoreConnectorFromId(String identityStoreConnectorId) {

        return identityStoreConnectorsMap.get(identityStoreConnectorId);
    }

    /**
     * Add an credential store connector to the map.
     *
     * @param credentialStoreConnector Credential Store connector
     * @throws DomainException domain exception
     */
    public void addCredentialStoreConnector(CredentialStoreConnector credentialStoreConnector) throws DomainException {

        credentialStoreConnectorsMap.put(credentialStoreConnector.getCredentialStoreConfig().getConnectorId(),
                credentialStoreConnector);
        credentialStoreConnectors.add(credentialStoreConnector);
    }

    /**
     * Get CredentialStoreConnector from credential store connector id.
     *
     * @param credentialStoreConnectorId String - CredentialStoreConnector ID
     * @return credentialStoreConnector
     */
    public CredentialStoreConnector getCredentialStoreConnectorFromId(String credentialStoreConnectorId) {

        return credentialStoreConnectorsMap.get(credentialStoreConnectorId);
    }

    /**
     * Checks weather a certain claim URI exists in the domain claim mappings.
     *
     * @param claimURI Claim
     * @return is claim belong to domain
     */
    public boolean isClaimSupported(String claimURI) {

        return connectorIdToMetaClaimMappings.values().stream()
                .anyMatch(list -> list.stream().filter(metaClaimMapping ->
                        claimURI.equals(metaClaimMapping.getMetaClaim().getClaimURI()))
                        .findFirst().isPresent());
    }

    /**
     * Get claim mappings for an identity store id.
     *
     * @return Map of connector Id to List of MetaClaimMapping
     */
    public Map<String, List<MetaClaimMapping>> getConnectorIdToMetaClaimMappings() {

        return connectorIdToMetaClaimMappings;
    }

    public MetaClaimMapping getMetaClaimMapping(String claimURI) throws DomainException {

        if (claimUriToMetaClaimMappings.isEmpty()) {
            throw new DomainException("Invalid domain configuration found. No meta claim mappings.");
        }

        return claimUriToMetaClaimMappings.get(claimURI);
    }

    public List<MetaClaimMapping> getMetaClaimMappings() throws DomainException {

        if (metaClaimMappings.isEmpty()) {
            throw new DomainException("Invalid domain configuration found. No meta claim mappings.");
        }

        return metaClaimMappings;
    }

    public void setMetaClaimMappings(List<MetaClaimMapping> metaClaimMappings) {

        if (metaClaimMappings == null || metaClaimMappings.isEmpty()) {
            this.metaClaimMappings.clear();
            this.claimUriToMetaClaimMappings.clear();
            return;
        }

        this.metaClaimMappings = metaClaimMappings;
        this.claimUriToMetaClaimMappings = this.metaClaimMappings.stream()
                .collect(Collectors.toMap(metaClaimMapping -> metaClaimMapping.getMetaClaim().getClaimURI(),
                        metaClaimMapping -> metaClaimMapping));
    }

    /**
     * Set claim mappings for an identity store id.
     *
     * @param connectorIdToMetaClaimMappings Map&lt;String, List&lt;MetaClaimMapping&gt;&gt; claim mappings
     */
    public void setConnectorIdToMetaClaimMappings(Map<String, List<MetaClaimMapping>> connectorIdToMetaClaimMappings) {

        this.connectorIdToMetaClaimMappings = connectorIdToMetaClaimMappings;
    }

    /**
     * Get set of IdentityStoreConnectors for this domain sorted by their priority.
     *
     * @return Sorted IdentityStoreConnectors set
     */
    public List<IdentityStoreConnector> getIdentityStoreConnectors() {

        if (identityStoreConnectors == null) {
            return Collections.emptyList();
        }
        return identityStoreConnectors;
    }

    /**
     * Get set of CredentialStoreConnectors for this domain sorted by their priority.
     *
     * @return Sorted CredentialStoreConnectors set
     */
    public List<CredentialStoreConnector> getCredentialStoreConnectors() {

        if (credentialStoreConnectors == null) {
            return Collections.emptyList();
        }
        return credentialStoreConnectors;
    }

    public UniqueIdResolver getUniqueIdResolver() {
        return uniqueIdResolver;
    }

    public void setUniqueIdResolver(UniqueIdResolver uniqueIdResolver) {
        this.uniqueIdResolver = uniqueIdResolver;
    }
}

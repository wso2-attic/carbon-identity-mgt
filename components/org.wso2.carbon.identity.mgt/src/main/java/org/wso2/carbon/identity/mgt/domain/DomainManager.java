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

package org.wso2.carbon.identity.mgt.domain;

import org.wso2.carbon.identity.mgt.bean.Domain;
import org.wso2.carbon.identity.mgt.exception.DomainException;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Domain manager.
 */
public class DomainManager {

    /**
     * Mapping between domain priority and domain name to domain.
     * Map<String, Domain> maps between domain map to domain instance
     * Retrieval and insertion - O(log n)
     */
    private TreeMap<Integer, Map<String, Domain>> domainPriorityToDomainMap = new TreeMap<>();

    /**
     * Mapping between all domain names and domain instances.
     */
    private Map<String, Domain> allDomainNameToDomainMap = new HashMap<>();

    /**
     * Set of domains sorted by their priority highest to lowest.
     */
    private SortedSet<Domain> sortedDomains = new TreeSet<>((d1, d2) -> {

        int d1Priority = d1.getDomainPriority();
        int d2Priority = d2.getDomainPriority();

        // Allow having multiple domains with the same priority
        if (d1Priority == d2Priority) {
            d2Priority++;
        }

        return Integer.compare(d1Priority, d2Priority);
    });

    public Domain getPrimaryDomain() throws DomainException {

        Domain domain = sortedDomains.first();

        if (domain == null) {
            throw new DomainException("No domains registered.");
        }

        return domain;
    }

    /**
     * Get the domain from the name.
     *
     * @param domainName Name of the domain.
     * @return Domain.
     * @throws DomainException domain exception
     */
    public Domain getDomainFromDomainName(String domainName) throws DomainException {

        Domain domain = allDomainNameToDomainMap.get(domainName);

        if (domain == null) {
            throw new DomainException(String.format("Domain %s was not found", domainName));
        }

        return domain;
    }

    /**
     * Get the list of domains which belongs to a certain priority.
     *
     * @param priority domain priority.
     * @return Map of domain name to Domain.
     * @throws DomainException domain exception.
     */
    public Map<String, Domain> getDomainsFromPriority(int priority) throws DomainException {

        Map<String, Domain> domainNameToDomainMap = domainPriorityToDomainMap.get(priority);

        if (domainNameToDomainMap == null) {
            throw new DomainException(String.format("Domain for priority %d not found", priority));
        }

        return domainNameToDomainMap;
    }

    /**
     * Add a domain to the mapping
     *
     * @param domain Domain object.
     * @throws DomainException domain exception.
     */
    public void addDomain(Domain domain) throws DomainException {

        String domainName = domain.getDomainName();
        int domainPriority = domain.getDomainPriority();

        if (allDomainNameToDomainMap.containsKey(domainName)) {
            throw new DomainException(String
                    .format("Domain %s already exists in the domain map", domainName));
        }

        if (!domainPriorityToDomainMap.containsKey(domainPriority)) {
            domainPriorityToDomainMap.put(domainPriority, new HashMap<>());
        }

        // Add to domain priority list and domain name list
        domainPriorityToDomainMap.get(domain.getDomainPriority()).put(domainName, domain);
        allDomainNameToDomainMap.put(domainName, domain);

        sortedDomains.add(domain);

    }

    /**
     * Get all available domains.
     * Domains are returned as a list ordered by their priority highest to lowest.
     *
     * @return A list of domains ordered by their priority
     * @throws DomainException DomainException
     */
    public SortedSet<Domain> getSortedDomains() throws DomainException {

        if (sortedDomains.isEmpty()) {
            throw new DomainException("No domains registered.");
        }
        return sortedDomains;
    }

}

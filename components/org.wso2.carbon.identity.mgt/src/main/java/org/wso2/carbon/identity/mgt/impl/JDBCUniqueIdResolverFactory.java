package org.wso2.carbon.identity.mgt.impl;

import org.wso2.carbon.identity.mgt.resolver.UniqueIdResolver;
import org.wso2.carbon.identity.mgt.resolver.UniqueIdResolverFactory;

/**
 * JDBC Unique Id Resolver Factory.
 */
public class JDBCUniqueIdResolverFactory implements UniqueIdResolverFactory {

    @Override
    public UniqueIdResolver getInstance() {
        return new JDBCUniqueIdResolver();
    }
}

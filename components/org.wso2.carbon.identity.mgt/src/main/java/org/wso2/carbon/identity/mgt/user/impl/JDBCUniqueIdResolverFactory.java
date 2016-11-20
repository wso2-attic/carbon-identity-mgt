package org.wso2.carbon.identity.mgt.user.impl;

import org.wso2.carbon.identity.mgt.user.UniqueIdResolver;
import org.wso2.carbon.identity.mgt.user.UniqueIdResolverFactory;

/**
 * JDBC Unique Id Resolver Factory.
 */
public class JDBCUniqueIdResolverFactory implements UniqueIdResolverFactory {

    @Override
    public UniqueIdResolver getInstance() {
        return new JDBCUniqueIdResolver();
    }
}

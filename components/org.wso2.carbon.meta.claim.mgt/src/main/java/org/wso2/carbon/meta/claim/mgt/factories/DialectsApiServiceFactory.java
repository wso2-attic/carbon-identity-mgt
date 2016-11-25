package org.wso2.carbon.meta.claim.mgt.factories;

import org.wso2.carbon.meta.claim.mgt.DialectsApiService;
import org.wso2.carbon.meta.claim.mgt.impl.DialectsApiServiceImpl;

/**
 * Provides instances of the available service implementation, for claim dialect management.
 */
public class DialectsApiServiceFactory {
    private static final DialectsApiService service = new DialectsApiServiceImpl();

    public static DialectsApiService getDialectsApi() {
        return service;
    }
}

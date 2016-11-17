package org.wso2.carbon.meta.claim.mgt.factories;

import org.wso2.carbon.meta.claim.mgt.DialectsApiService;
import org.wso2.carbon.meta.claim.mgt.impl.DialectsApiServiceImpl;

public class DialectsApiServiceFactory {
    private final static DialectsApiService service = new DialectsApiServiceImpl();

    public static DialectsApiService getDialectsApi() {
        return service;
    }
}

package org.wso2.carbon.meta.claim.mgt.factories;

import org.wso2.carbon.meta.claim.mgt.LocalclaimsApiService;
import org.wso2.carbon.meta.claim.mgt.impl.LocalclaimsApiServiceImpl;

public class LocalclaimsApiServiceFactory {
    private final static LocalclaimsApiService service = new LocalclaimsApiServiceImpl();

    public static LocalclaimsApiService getLocalclaimsApi() {
        return service;
    }
}

package org.wso2.carbon.meta.claim.mgt.factories;

import org.wso2.carbon.meta.claim.mgt.ExternalclaimsApiService;
import org.wso2.carbon.meta.claim.mgt.impl.ExternalclaimsApiServiceImpl;

public class ExternalclaimsApiServiceFactory {
    private final static ExternalclaimsApiService service = new ExternalclaimsApiServiceImpl();

    public static ExternalclaimsApiService getExternalclaimsApi() {
        return service;
    }
}

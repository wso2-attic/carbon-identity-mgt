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
 * limitations und
 */

package org.wso2.carbon.identity.handler.event.account.lock.internal;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
//import org.wso2.carbon.identity.handler.event.account.lock.AccountDisableHandler;
import org.wso2.carbon.identity.handler.event.account.lock.AccountLockHandler;
import org.wso2.carbon.identity.event.AbstractEventHandler;
import org.wso2.carbon.identity.event.EventService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
        name = "org.wso2.carbon.identity.handler.event.account.lock.internal.AccountServiceComponent",
        immediate = true,
        property = {
                "componentName=handler.event.account.lock"
        }
)
public class AccountServiceComponent {

//    private static Log log = LogFactory.getLog(AccountServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        AccountServiceDataHolder.getInstance().setBundleContext(context.getBundleContext());
        AccountLockHandler accountLockHandler = new AccountLockHandler();
        context.getBundleContext().registerService(AbstractEventHandler.class.getName(), accountLockHandler, null);
//        if (log.isDebugEnabled()) {
//            log.debug("AccountLockHandler is registered");
//        }
//        AccountDisableHandler accountDisableHandler = new AccountDisableHandler();
//        context.getBundleContext().registerService(AbstractEventHandler.class.getName(), accountDisableHandler, null);
//        if (log.isDebugEnabled()) {
//            log.debug("AccountDisableHandler is registered");
//        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
//        if (log.isDebugEnabled()) {
//            log.debug("AccountLock bundle is de-activated");
//        }
    }

    protected void unsetIdentityEventService(EventService eventService) {
        AccountServiceDataHolder.getInstance().setIdentityEventService(null);
    }

    @Reference(
            name = "EventService",
            service = EventService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetIdentityEventService"
    )
    protected void setIdentityEventService(EventService eventService) {
        AccountServiceDataHolder.getInstance().setIdentityEventService(eventService);
    }

}

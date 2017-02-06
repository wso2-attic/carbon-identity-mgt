/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.recovery.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.mgt.RealmService;
import org.wso2.carbon.identity.recovery.ChallengeQuestionManager;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.username.NotificationUsernameRecoveryManager;

/**
 * @scr.reference name="realm.service"
 * interface="org.wso2.carbon.identity.mgt.RealmService"cardinality="1..1"
 * policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 * @scr.reference name="IdentityGovernanceService"
 * interface="org.wso2.carbon.identity.governance.IdentityGovernanceService" cardinality="1..1"
 * policy="dynamic" bind="setIdentityGovernanceService" unbind="unsetIdentityGovernanceService"
 * @scr.reference name="IdentityEventService"
 * interface="org.wso2.carbon.identity.event.services.IdentityEventService" cardinality="1..1"
 * policy="dynamic" bind="setIdentityEventService" unbind="unsetIdentityEventService"
 */

/**
 * Identity Recovery Service Component.
 */
@Component(
        name = "org.wso2.carbon.identity.recovery.internal.IdentityRecoveryServiceComponent",
        service = IdentityRecoveryServiceComponent.class,
        immediate = true
)
public class IdentityRecoveryServiceComponent {

    private static Logger log = LoggerFactory.getLogger(IdentityRecoveryServiceComponent.class);
    private IdentityRecoveryServiceDataHolder dataHolder = IdentityRecoveryServiceDataHolder.getInstance();

    @Activate
    protected void activate(BundleContext bundleContext) {

        try {
//            bundleContext.registerService(NotificationPasswordRecoveryManager.class.getName(),
//                    NotificationPasswordRecoveryManager.getInstance(), null);
//            bundleContext.registerService(SecurityQuestionPasswordRecoveryManager.class.getName(),
//                    SecurityQuestionPasswordRecoveryManager.getInstance(), null);
            bundleContext.registerService(NotificationUsernameRecoveryManager.class.getName(),
                    NotificationUsernameRecoveryManager.getInstance(), null);
//            bundleContext.registerService(UserSelfRegistrationManager.class.getName(),
//                    UserSelfRegistrationManager.getInstance(), null);
            bundleContext.registerService(ChallengeQuestionManager.class.getName(),
                    ChallengeQuestionManager.getInstance(), null);
//            bundleContext.registerService(AbstractEventHandler.class.getName(),
//                    new AccountConfirmationValidationHandler(), null);
//            bundleContext.registerService(AbstractEventHandler.class.getName(),
//                    new UserSelfRegistrationHandler(), null);
//            bundleContext.registerService(AbstractEventHandler.class.getName(),
//                    new UserEmailVerificationHandler(), null);
//            bundleContext.registerService(AbstractEventHandler.class.getName(),
//                    new AdminForcedPasswordResetHandler(), null);
//            bundleContext.registerService(IdentityConnectorConfig.class.getName(),
//                    new SelfRegistrationConfigImpl(), null);
//            bundleContext.registerService(IdentityConnectorConfig.class.getName(),
//                    new UserEmailVerificationConfigImpl(), null);
//            bundleContext.registerService(IdentityConnectorConfig.class.getName(),
//                    new AdminForcedPasswordResetConfigImpl(), null);


        } catch (Exception e) {
            log.error("Error while activating identity governance component.", e);
        }

        // register default challenge questions
        try {
            if (log.isDebugEnabled()) {
                log.debug("Loading default challenge questions for super tenant.");
            }
            loadDefaultChallengeQuestions();
            //   new ChallengeQuestionManager().getAllChallengeQuestions("carbon.super", "lk_LK");
        } catch (IdentityRecoveryException e) {
            log.error("Error persisting challenge question for super tenant.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Identity Management bundle is de-activated");
        }
    }

    @Reference(
            name = "realmService",
            service = RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the Realm Service");
        }
        dataHolder.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        log.debug("UnSetting the Realm Service");
        dataHolder.setRealmService(null);
    }
//
//    protected void unsetIdentityEventService(EventService identityEventService) {
//        IdentityRecoveryServiceDataHolder.getInstance().setIdentityEventService(null);
//    }
//
//    protected void setIdentityEventService(EventService identityEventService) {
//        IdentityRecoveryServiceDataHolder.getInstance().setIdentityEventService(identityEventService);
//    }
//

    private void loadDefaultChallengeQuestions() throws IdentityRecoveryException {
        ChallengeQuestionManager.getInstance().setDefaultChallengeQuestions();
    }
}

///*
// * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations und
// */
//
//package org.wso2.carbon.identity.handler.event.account.lock;
//
//import org.apache.commons.lang.StringUtils;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.wso2.carbon.identity.core.model.IdentityErrorMsgContext;
//import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
//import org.wso2.carbon.identity.core.util.IdentityUtil;
//import org.wso2.carbon.identity.event.IdentityEventConstants;
//import org.wso2.carbon.identity.event.IdentityEventException;
//import org.wso2.carbon.identity.event.event.Event;
//import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
//import org.wso2.carbon.identity.handler.event.account.lock.constants.AccountConstants;
//import org.wso2.carbon.identity.handler.event.account.lock.exception.AccountLockException;
//import org.wso2.carbon.identity.handler.event.account.lock.internal.AccountServiceDataHolder;
//import org.wso2.carbon.identity.handler.event.account.lock.util.AccountUtil;
//import org.wso2.carbon.user.api.UserStoreException;
//import org.wso2.carbon.user.api.UserStoreManager;
//import org.wso2.carbon.user.core.UserCoreConstants;
//import org.wso2.carbon.user.core.util.UserCoreUtil;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class AccountDisableHandler extends AbstractEventHandler {
//
//    private static final Log log = LogFactory.getLog(AccountDisableHandler.class);
//
//    private static ThreadLocal<String> disabledState = new ThreadLocal<>();
//
//    private enum disabledStates {DISABLED_UNMODIFIED, DISABLED_MODIFIED, ENABLED_UNMODIFIED, ENABLED_MODIFIED}
//
//    public String getName() {
//        return "account.disable.handler";
//    }
//
//    @Override
//    public void handleEvent(Event event) throws IdentityEventException {
//
//        Map<String, Object> eventProperties = event.getEventProperties();
//        String userName = (String) eventProperties.get(IdentityEventConstants.EventProperty.USER_NAME);
//        UserStoreManager userStoreManager = (UserStoreManager) eventProperties.get(IdentityEventConstants.EventProperty
//                .USER_STORE_MANAGER);
//        String userStoreDomainName = AccountUtil.getUserStoreDomainName(userStoreManager);
//        String tenantDomain = (String) eventProperties.get(IdentityEventConstants.EventProperty.TENANT_DOMAIN);
//
//        String usernameWithDomain = UserCoreUtil.addDomainToName(userName, userStoreDomainName);
//        boolean userExists;
//        try {
//            userExists = userStoreManager.isExistingUser(usernameWithDomain);
//        } catch (UserStoreException e) {
//            throw new IdentityEventException("Error in accessing user store", e);
//        }
//        if (!userExists) {
//            return;
//        }
//
//        if (IdentityEventConstants.Event.PRE_AUTHENTICATION.equals(event.getEventName())) {
//            handlePreAuthentication(event, userName, userStoreManager, userStoreDomainName, tenantDomain);
//        } else if (IdentityEventConstants.Event.PRE_SET_USER_CLAIMS.equals(event.getEventName())) {
//            handlePreSetUserClaimValues(event, userName, userStoreManager, userStoreDomainName, tenantDomain);
//        } else if (IdentityEventConstants.Event.POST_SET_USER_CLAIMS.equals(event.getEventName())) {
//            handlePostSetUserClaimValues(event, userName, userStoreManager, userStoreDomainName, tenantDomain);
//        }
//    }
//
//    protected boolean handlePreAuthentication(Event event, String userName, UserStoreManager userStoreManager,
//                                              String userStoreDomainName,
//                                              String tenantDomain) throws AccountLockException {
//
//        String accountDisabledClaim;
//        try {
//            Map<String, String> claimValues = userStoreManager.getUserClaimValues(userName, new String[]{
//                    AccountConstants.ACCOUNT_DISABLED_CLAIM}, UserCoreConstants.DEFAULT_PROFILE);
//            accountDisabledClaim = claimValues.get(AccountConstants.ACCOUNT_DISABLED_CLAIM);
//
//        } catch (UserStoreException e) {
//            throw new AccountLockException("Error occurred while retrieving " + AccountConstants
//                    .ACCOUNT_DISABLED_CLAIM + " claim value", e);
//        }
//        if (Boolean.parseBoolean(accountDisabledClaim)) {
//            String message = null;
//            if (StringUtils.isNotBlank(userStoreDomainName)) {
//                message = "Account is disabled for user " + userName + " in user store "
//                        + userStoreDomainName + " in tenant " + tenantDomain + ". Cannot login until the " +
//                        "account is enabled.";
//            } else {
//                message = "Account is disabled for user " + userName + " in tenant " + tenantDomain + ". Cannot" +
//                        " login until the account is enabled.";
//            }
//
//            IdentityErrorMsgContext customErrorMessageContext = new IdentityErrorMsgContext(
//                    IdentityCoreConstants.USER_ACCOUNT_DISABLED_ERROR_CODE);
//            IdentityUtil.setIdentityErrorMsg(customErrorMessageContext);
//
//            throw new AccountLockException(IdentityCoreConstants.USER_ACCOUNT_DISABLED_ERROR_CODE + " " + message);
//        }
//        return true;
//    }
//
//    protected boolean handlePreSetUserClaimValues(Event event, String userName, UserStoreManager userStoreManager,
//                                                  String userStoreDomainName,
//                                                  String tenantDomain) throws AccountLockException {
//
//
//        if (disabledState.get() != null) {
//            return true;
//        }
//        boolean existingAccountDisabledValue;
//        try {
//            Map<String, String> claimValues = userStoreManager.getUserClaimValues(userName, new String[]{
//                    AccountConstants.ACCOUNT_DISABLED_CLAIM}, UserCoreConstants.DEFAULT_PROFILE);
//            existingAccountDisabledValue = Boolean.parseBoolean(claimValues.get(AccountConstants
//                    .ACCOUNT_DISABLED_CLAIM));
//        } catch (UserStoreException e) {
//            throw new AccountLockException("Error occurred while retrieving " + AccountConstants
//                    .ACCOUNT_DISABLED_CLAIM + " claim value", e);
//        }
//
//        String newAccountDisableString = ((Map<String, String>) event.getEventProperties().get("USER_CLAIMS")).get
//                (AccountConstants.ACCOUNT_DISABLED_CLAIM);
//        if (StringUtils.isNotBlank(newAccountDisableString)) {
//            Boolean newAccountDisabledValue = Boolean.parseBoolean(
//                    ((Map<String, String>) event.getEventProperties().get("USER_CLAIMS"))
//                            .get(AccountConstants.ACCOUNT_DISABLED_CLAIM));
//            if (existingAccountDisabledValue != newAccountDisabledValue) {
//                if (existingAccountDisabledValue) {
//                    disabledState.set(disabledStates.ENABLED_MODIFIED.toString());
//                } else {
//                    disabledState.set(disabledStates.DISABLED_MODIFIED.toString());
//                    IdentityErrorMsgContext customErrorMessageContext = new IdentityErrorMsgContext(
//                            IdentityCoreConstants.USER_ACCOUNT_DISABLED);
//                    IdentityUtil.setIdentityErrorMsg(customErrorMessageContext);
//                    IdentityUtil.threadLocalProperties.get().put(IdentityCoreConstants.USER_ACCOUNT_STATE,
//                            IdentityCoreConstants.USER_ACCOUNT_DISABLED_ERROR_CODE);
//                }
//            } else {
//                if (existingAccountDisabledValue) {
//                    disabledState.set(disabledStates.DISABLED_UNMODIFIED.toString());
//                } else {
//                    disabledState.set(disabledStates.ENABLED_UNMODIFIED.toString());
//                }
//            }
//        } else {
//            if (existingAccountDisabledValue) {
//                disabledState.set(disabledStates.DISABLED_UNMODIFIED.toString());
//            } else {
//                disabledState.set(disabledStates.ENABLED_UNMODIFIED.toString());
//            }
//        }
//        return true;
//    }
//
//    protected boolean handlePostSetUserClaimValues(Event event, String userName, UserStoreManager userStoreManager,
//                                                   String userStoreDomainName,
//                                                   String tenantDomain) throws AccountLockException {
//
//        try {
//            if (disabledStates.ENABLED_MODIFIED.toString().equals(disabledState.get())) {
//                triggerNotification(event, userName, userStoreManager, userStoreDomainName, tenantDomain,
//                        AccountConstants.EMAIL_TEMPLATE_TYPE_ACC_ENABLED);
//            } else if (disabledStates.DISABLED_MODIFIED.toString().equals(disabledState.get())) {
//                triggerNotification(event, userName, userStoreManager, userStoreDomainName, tenantDomain,
//                        AccountConstants.EMAIL_TEMPLATE_TYPE_ACC_DISABLED);
//            }
//        } finally {
//            disabledState.remove();
//        }
//        return true;
//    }
//
//    protected void triggerNotification(Event event, String userName, UserStoreManager userStoreManager,
//                                       String userStoreDomainName, String tenantDomain,
//                                       String notificationEvent) throws AccountLockException {
//
//        String eventName = IdentityEventConstants.Event.TRIGGER_NOTIFICATION;
//
//        HashMap<String, Object> properties = new HashMap<>();
//        properties.put(IdentityEventConstants.EventProperty.USER_NAME, userName);
//        properties.put(IdentityEventConstants.EventProperty.USER_STORE_DOMAIN, userStoreDomainName);
//        properties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
//        properties.put("TEMPLATE_TYPE", notificationEvent);
//        Event identityMgtEvent = new Event(eventName, properties);
//        try {
//            AccountServiceDataHolder.getInstance().getIdentityEventService().handleEvent(identityMgtEvent);
//        } catch (Exception e) {
//            String errorMsg = "Error occurred while calling triggerNotification, detail : " + e.getMessage();
//            //We are not throwing any exception from here, because this event notification should not break the main
//            // flow.
//            log.warn(errorMsg);
//            if (log.isDebugEnabled()) {
//                log.debug(errorMsg, e);
//            }
//        }
//    }
//}
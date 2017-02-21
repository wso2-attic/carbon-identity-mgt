/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.mgt;

/**
 * This is used to define the User States
 */
public enum UserState {
    CREATED(Group.UNLOCKED),
    LOCKED_SELF_SIGN_UP(Group.LOCKED),
    LOCKED__UNVERIFIED(Group.LOCKED),
    LOCKED__VERIFIED(Group.LOCKED),
    LOCKED_INVALID_CREDENTIAL__VERIFIED(Group.LOCKED),
    LOCKED_INVALID_CREDENTIAL__UNVERIFIED(Group.LOCKED),
    UNLOCKED__UNVERIFIED(Group.UNLOCKED),
    UNLOCKED__VERIFIED(Group.UNLOCKED),
    LOCKED_INVALID_ANSWER__UNVERIFIED(Group.LOCKED),
    LOCKED_INVALID_ANSWER__VERIFIED(Group.LOCKED),
    LOCKED_AIPR__VERIFIED(Group.LOCKED),
    LOCKED_AIPR__UNVERIFIED(Group.LOCKED),
    DISABLED(Group.DISABLED);

    private Group group;

    UserState(Group group) {
        this.group = group;
    }

    public boolean isInGroup(Group group) {
        return this.group == group;
    }

    /**
     * This is used to define user state groups
     */
    public enum Group {
        LOCKED,
        UNLOCKED,
        DISABLED;
    }
}


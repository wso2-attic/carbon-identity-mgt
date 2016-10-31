package org.wso2.carbon.identity.mgt.exception;

/**
 * Exception class for UserManager.
 */
public class UserManagerException extends Exception {

    public UserManagerException(String message) {
        super(message);
    }

    public UserManagerException(String message, Throwable cause) {
        super(message, cause);
    }
}

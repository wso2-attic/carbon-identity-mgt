package org.wso2.carbon.identity.mgt.exception;

/**
 * Exception class for UserManager.
 */
public class UniqueIdResolverException extends Exception {

    public UniqueIdResolverException(String message) {
        super(message);
    }

    public UniqueIdResolverException() {
        super();
    }

    public UniqueIdResolverException(String message, Throwable cause) {
        super(message, cause);
    }
}

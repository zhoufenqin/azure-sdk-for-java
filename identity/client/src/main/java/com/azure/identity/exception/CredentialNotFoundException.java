package com.azure.identity.exception;

/**
 * An exception thrown when a credential cannot be found for authentication.
 */
public class CredentialNotFoundException extends RuntimeException {
    /**
     * Creates a CredentialNotFoundException with a message.
     * @param message the error message
     */
    public CredentialNotFoundException(String message) {
        super(message);
    }
}

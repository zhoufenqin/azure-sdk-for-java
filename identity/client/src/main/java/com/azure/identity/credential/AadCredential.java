package com.azure.identity.credential;

/**
 * The base class for credentials that acquires a token from AAD.
 */
public abstract class AadCredential extends AzureCredential {

    private final String clientId;

    private final String tenantId;

    private final String aadEndpoint;

    /**
     * Creates an AadCredential with default AAD endpoint https://login.microsoftonline.com.
     * @param clientId the client ID for authenticating to AAD.
     * @param tenantId the tenant for authenticating to AAD.
     */
    protected AadCredential(String clientId, String tenantId) {
        this(clientId, tenantId, "https://login.microsoftonline.com/");
    }

    /**
     * Creates an AadCredential.
     * @param clientId the client ID for authenticating to AAD.
     * @param tenantId the tenant for authenticating to AAD.
     * @param aadEndpoint the endpoint of the Azure Active Directory
     */
    protected AadCredential(String clientId, String tenantId, String aadEndpoint) {
        this.clientId = clientId;
        this.tenantId = tenantId;
        this.aadEndpoint = aadEndpoint.endsWith("/") ? aadEndpoint : aadEndpoint + "/";
    }

    /**
     * @return the client ID for authenticating to AAD.
     */
    public String clientId() {
        return clientId;
    }

    /**
     * @return the tenant ID for authenticating to AAD.
     */
    public String tenantId() {
        return tenantId;
    }

    /**
     * @return the endpoint for the Azure Active Directory.
     */
    public String aadEndpoint() {
        return aadEndpoint;
    }
}

package com.azure.identity.credential;

import com.azure.common.http.policy.HttpPipelinePolicy;
import com.azure.identity.exception.CredentialNotFoundException;
import com.azure.identity.policy.AzureCredentialPipelinePolicy;
import com.azure.identity.provider.EnvironmentCredentialProvider;
import reactor.core.publisher.Mono;

import java.net.Proxy;
import java.util.Arrays;
import java.util.List;

public abstract class AadCredential extends AzureCredential {

    private String clientId;

    private String tenant;

    private String aadEndpoint;

    private Proxy proxy;

    protected AadCredential(String clientId, String tenant) {
        this(clientId, tenant, "https://login.microsoftonline.com/");
    }

    protected AadCredential(String clientId, String tenant, String aadEndpoint) {
        this.clientId = clientId;
        this.tenant = tenant;
        this.aadEndpoint = aadEndpoint.endsWith("/") ? aadEndpoint : aadEndpoint + "/";
    }

    public String clientId() {
        return clientId;
    }

    public String tenant() {
        return tenant;
    }

    public String aadEndpoint() {
        return aadEndpoint;
    }

    public Proxy proxy() {
        return proxy;
    }

    public AadCredential proxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }
}

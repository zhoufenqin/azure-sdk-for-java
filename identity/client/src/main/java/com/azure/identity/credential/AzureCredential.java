package com.azure.identity.credential;

import com.azure.common.http.policy.HttpPipelinePolicy;
import com.azure.identity.exception.CredentialNotFoundException;
import com.azure.identity.policy.AzureCredentialPipelinePolicy;
import com.azure.identity.provider.EnvironmentCredentialProvider;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

public abstract class AzureCredential extends TokenCredential {

    public static final AzureCredential DEFAULT = createDefault();

    protected AzureCredential() {
        super();
    }

    protected AzureCredential(String scheme) {
        super(scheme);
    }

    private static AzureCredential createDefault() {
        try {
            TokenCredential cred = new EnvironmentCredentialProvider().getCredential();
            return new AzureCredential(cred.scheme()) {
                @Override
                public Mono<String> getTokenAsync(String resource) {
                    return cred.getTokenAsync(resource);
                }
            };
        } catch (CredentialNotFoundException e) {
            return null;
        }
    }

    public List<HttpPipelinePolicy> createDefaultPipelinePolicies() {
        return Arrays.asList(new AzureCredentialPipelinePolicy(this));
    }
}

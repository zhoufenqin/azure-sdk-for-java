package com.azure.identity.policy;

import com.azure.common.http.HttpPipelineCallContext;
import com.azure.common.http.HttpPipelineNextPolicy;
import com.azure.common.http.HttpResponse;
import com.azure.common.http.policy.HttpPipelinePolicy;
import com.azure.identity.credential.AzureCredential;
import reactor.core.publisher.Mono;

/**
 * A default simple implementation of a pipeline policy that authenticates a request
 * with the given AzureCredential instance.
 */
public class AzureCredentialPipelinePolicy implements HttpPipelinePolicy {
    private AzureCredential credential;

    /**
     * Creates an instance of the pipeline policy with a given AzureCredential instance.
     * @param azureCredential the credential to authenticate requests
     */
    public AzureCredentialPipelinePolicy(AzureCredential azureCredential) {
        this.credential = azureCredential;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        String resource = context.httpRequest().url().getHost();

        return credential.getTokenAsync(resource)
                .flatMap(token -> {
                    context.httpRequest().withHeader("Authorization", credential.scheme() + " " + token);
                    return next.process();
                });
    }
}

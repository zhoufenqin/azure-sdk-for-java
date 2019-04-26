package com.azure.identity.policy;

import com.azure.common.http.HttpPipelineCallContext;
import com.azure.common.http.HttpPipelineNextPolicy;
import com.azure.common.http.HttpResponse;
import com.azure.common.http.policy.HttpPipelinePolicy;
import com.azure.identity.credential.AzureCredential;
import reactor.core.publisher.Mono;

public class AzureCredentialPipelinePolicy implements HttpPipelinePolicy {
    private AzureCredential credential;

    public AzureCredentialPipelinePolicy(AzureCredential azureCredential) {
        this.credential = azureCredential;
    }

    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        String resource = context.httpRequest().url().getHost();

        return credential.getTokenAsync(resource)
                .flatMap(token -> {
                    context.httpRequest().withHeader("Authorization", credential.scheme() + " " + token);
                    return next.process();
                });
    }
}

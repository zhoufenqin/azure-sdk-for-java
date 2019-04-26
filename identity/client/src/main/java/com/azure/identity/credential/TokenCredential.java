package com.azure.identity.credential;

import reactor.core.publisher.Mono;

public abstract class TokenCredential {
    private String scheme;

    protected TokenCredential() {
        this("Bearer");
    }

    protected TokenCredential(String scheme) {
        this.scheme = scheme;
    }

    public String scheme() {
        return scheme;
    }

    public TokenCredential scheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    public abstract Mono<String> getTokenAsync(String resource);
}

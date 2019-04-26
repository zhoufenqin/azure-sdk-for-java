package com.azure.identity.provider;

import com.azure.identity.credential.TokenCredential;
import com.azure.identity.exception.CredentialNotFoundException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TokenCredentialProvider {
    private final List<TokenCredentialProvider> providers;

    protected TokenCredentialProvider() {
        this.providers = new ArrayList<>();
    }

    public TokenCredentialProvider(List<TokenCredentialProvider> providers) {
        this.providers = providers;
    }

    public TokenCredentialProvider(TokenCredentialProvider... providers) {
        this.providers = Arrays.asList(providers);
    }

    public TokenCredential getCredential() throws CredentialNotFoundException {
        TokenCredential credential = null;
        for (TokenCredentialProvider provider : providers) {
            try {
                credential = provider.getCredential();
                if (credential != null) {
                    break;
                }
            } catch (CredentialNotFoundException e) { }
        }
        if (credential == null) {
            throw new CredentialNotFoundException("No credential found in the chain");
        }
        return credential;
    }

    public Mono<String> getTokenAsync(String resource) throws CredentialNotFoundException {
        return getCredential().getTokenAsync(resource);
    }
}

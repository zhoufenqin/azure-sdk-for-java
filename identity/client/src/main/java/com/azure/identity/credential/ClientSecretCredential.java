package com.azure.identity.credential;

import com.microsoft.aad.adal4j.AuthenticationCallback;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.net.MalformedURLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ClientSecretCredential extends AadCredential {
    /** A mapping from resource endpoint to its cached access token. */
    private final Map<String, AuthenticationResult> tokens = new HashMap<>();

    private String clientSecret;

    public ClientSecretCredential(String clientId, String clientSecret, String tenant) {
        this(clientId, clientSecret, tenant, "https://login.microsoftonline.com/");
    }

    public ClientSecretCredential(String clientId, String clientSecret, String tenant, String aadEndpoint) {
        super(clientId, tenant, aadEndpoint);
        this.clientSecret = clientSecret;
    }

    @Override
    public Mono<String> getTokenAsync(String resource) {
        AuthenticationResult authenticationResult = tokens.get(resource);
        if (authenticationResult != null && authenticationResult.getExpiresOnDate().after(new Date())) {
            return Mono.just(authenticationResult.getAccessToken());
        } else {
            return acquireAccessToken(resource)
                    .map(ar -> {
                        tokens.put(resource, ar);
                        return ar.getAccessToken();
                    });
        }
    }

    private Mono<AuthenticationResult> acquireAccessToken(String resource) {
        String authorityUrl = this.aadEndpoint() + this.tenant();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        AuthenticationContext context;
        try {
            context = new AuthenticationContext(authorityUrl, false, executor);
        } catch (MalformedURLException mue) {
            executor.shutdown();
            throw Exceptions.propagate(mue);
        }
        if (proxy() != null) {
            context.setProxy(proxy());
        }
        return Mono.create((Consumer<MonoSink<AuthenticationResult>>) callback -> {
            context.acquireToken(
                    resource,
                    new ClientCredential(this.clientId(), this.clientSecret),
                    new AuthenticationCallback() {
                        @Override
                        public void onSuccess(Object o) {
                            callback.success((AuthenticationResult) o);
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            callback.error(throwable);
                        }
                    });
        }).doFinally(s -> executor.shutdown());
    }
}

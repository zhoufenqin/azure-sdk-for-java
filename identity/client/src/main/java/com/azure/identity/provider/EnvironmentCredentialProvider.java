package com.azure.identity.provider;

import com.azure.identity.credential.AzureCredential;
import com.azure.identity.credential.ClientSecretCredential;
import com.azure.identity.credential.TokenCredential;
import com.azure.identity.exception.CredentialNotFoundException;

import java.util.HashMap;
import java.util.Map;

public class EnvironmentCredentialProvider extends TokenCredentialProvider {
    private Map<Variable, String> variables = new HashMap<>();

    public EnvironmentCredentialProvider() {
        for (Variable var : Variable.values()) {
            String value = System.getenv(var.name);
            if (value != null) {
                variables.put(var, value);
            }
        }
    }

    @Override
    public TokenCredential getCredential() throws CredentialNotFoundException {
        if (variables.containsKey(Variable.CLIENT_ID)
                && variables.containsKey(Variable.CLIENT_SECRET)
                && variables.containsKey(Variable.TENANT)) {
            // ClientSecretCredential
            if (!variables.containsKey(Variable.AAD_ENDPOINT)) {
                return new ClientSecretCredential(
                        variables.get(Variable.CLIENT_ID),
                        variables.get(Variable.CLIENT_SECRET),
                        variables.get(Variable.TENANT));
            } else {
                return new ClientSecretCredential(
                        variables.get(Variable.CLIENT_ID),
                        variables.get(Variable.CLIENT_SECRET),
                        variables.get(Variable.TENANT),
                        variables.get(Variable.AAD_ENDPOINT));
            }
        }
        throw new CredentialNotFoundException("Cannot create any credentials with the current environment variables");
    }

    public enum Variable {
        CLIENT_ID("CLIENT_ID"),
        CLIENT_SECRET("CLIENT_SECRET"),
        TENANT("TENANT"),
        AAD_ENDPOINT("AAD_ENDPOINT");
        // here goes other possible environment variables for authentication

        private String name;

        Variable(String name) {
            this.name = name;
        }
    }
}

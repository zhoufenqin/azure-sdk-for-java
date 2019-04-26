package com.azure.keyvault;

import com.azure.identity.credential.AzureCredential;
import com.azure.keyvault.models.Secret;
import org.junit.Test;

public class SecretClientTests {

    @Test
    public void canGetSecret() throws Exception {
        SecretClient client = new SecretClientBuilder()
            .vaultEndpoint("https://todo20181105025332.vault.azure.net")
            .credentials(AzureCredential.DEFAULT)
            .build();

        Secret secret = client.getSecret("kvtest").value();

        System.out.println(secret.value());
    }
}

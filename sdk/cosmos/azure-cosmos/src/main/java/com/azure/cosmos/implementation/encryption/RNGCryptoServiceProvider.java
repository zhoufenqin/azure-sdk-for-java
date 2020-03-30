package com.azure.cosmos.implementation.encryption;

import java.io.Closeable;
import java.io.IOException;
import java.security.SecureRandom;

public class RNGCryptoServiceProvider implements Closeable {
    // TODO: is this thread safe? efficient, etc?
    private SecureRandom random = new SecureRandom();

    public void GetBytes(byte[] randomBytes) {
        random.nextBytes(randomBytes);
    }

    @Override
    public void close() {

    }
}

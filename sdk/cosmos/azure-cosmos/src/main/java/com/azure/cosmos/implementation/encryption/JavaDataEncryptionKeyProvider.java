package com.azure.cosmos.implementation.encryption;

public interface JavaDataEncryptionKeyProvider {
    JavaDataEncryptionKey loadDataEncryptionKey(String id);
}

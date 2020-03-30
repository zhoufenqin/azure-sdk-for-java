package com.azure.cosmos.implementation.encryption;

public interface JavaDataEncryptionKey {

    String getId();

    EncryptionAlgorithm getEncryptionAlgorithm();
}

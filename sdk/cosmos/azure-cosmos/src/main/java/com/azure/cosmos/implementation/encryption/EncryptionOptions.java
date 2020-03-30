package com.azure.cosmos.implementation.encryption;

import java.util.List;

class EncryptionOptions {
    /// <summary>
    /// Reference to encryption key to be used for encryption of data in the request payload.
    /// The key must already be created using Database.CreateDataEncryptionKeyAsync
    /// before using it in encryption options.
    /// </summary>
    public JavaDataEncryptionKey DataEncryptionKey;

    /// <summary>
    /// For the request payload, list of JSON paths to encrypt.
    /// Only top level paths are supported.
    /// Example of a path specification: /sensitive
    /// </summary>
    public List<String> PathsToEncrypt;
}

package com.azure.cosmos.implementation.encryption;

/// <summary>
/// Base class containing raw key bytes for symmetric key algorithms. Some encryption algorithms can use the key directly while others derive sub keys from this.
/// If an algorithm needs to derive more keys, have a derived class from this and use it in the corresponding encryption algorithm.
/// </summary>
class SymmetricKey {
    /// <summary>
    /// The underlying key material
    /// </summary>
    protected final byte[] rootKey;

    /// <summary>
    /// Constructor that initializes the root key.
    /// </summary>
    /// <param name="rootKey">root key</param>
    SymmetricKey(byte[] rootKey) {
        // Key validation
        if (rootKey == null || rootKey.length == 0) {
            throw new NullPointerException("rootKey");
        }

        this.rootKey = rootKey;
    }

    /// <summary>
    /// Returns a copy of the plain text key
    /// This is needed for actual encryption/decryption.
    /// </summary>
    protected byte[] getRootKey() {
        return this.rootKey;
    }

    /// <summary>
    /// Computes SHA256 value of the plain text key bytes
    /// </summary>
    /// <returns>A string containing SHA256 hash of the root key</returns>
    protected String GetKeyHash() {
        return SecurityUtility.GetSHA256Hash(this.getRootKey());
    }

    /// <summary>
    /// Gets the length of the root key
    /// </summary>
    /// <returns>
    /// Returns the length of the root key
    /// </returns>
    int getLength() {
        return this.rootKey.length;
    }
}

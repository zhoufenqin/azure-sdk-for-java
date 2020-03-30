package com.azure.cosmos.implementation.encryption;

/// <summary>
/// Abstract base class for all encryption algorithms.
/// </summary>
interface  EncryptionAlgorithm
{
    String getAlgorithmName();

    /// <summary>
    /// Encrypts the plainText with a data encryption key.
    /// </summary>
    /// <param name="plainText">Plain text value to be encrypted.</param>
    /// <returns>Encrypted value.</returns>
    byte[] encryptData(byte[] plainText);

    /// <summary>
    /// Decrypts the cipherText with a data encryption key.
    /// </summary>
    /// <param name="cipherText">Ciphertext value to be decrypted.</param>
    /// <returns>Plain text.</returns>
    byte[] decryptData(byte[] cipherText);
}

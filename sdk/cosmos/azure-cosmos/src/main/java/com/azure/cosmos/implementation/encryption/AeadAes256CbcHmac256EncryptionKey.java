package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.guava27.Strings;

/// <summary>
/// Encryption key class containing 4 keys. This class is used by AeadAes256CbcHmac256Algorithm
/// 1) root key - Main key that is used to derive the keys used in the encryption algorithm
/// 2) encryption key - A derived key that is used to encrypt the plain text and generate cipher text
/// 3) mac_key - A derived key that is used to compute HMAC of the cipher text
/// 4) iv_key - A derived key that is used to generate a synthetic IV from plain text data.
/// </summary>
public class AeadAes256CbcHmac256EncryptionKey extends SymmetricKey {
    /// <summary>
    /// Key size in bits
    /// </summary>
    static final int KeySize = 256;

    /// <summary>
/// Encryption Key Salt format. This is used to derive the encryption key from the root key.
/// </summary>
    private static final String encryptionKeySaltFormat = "Microsoft Azure Cosmos DB encryption key with encryption algorithm:%s and key length:%s}";

    /// <summary>
/// MAC Key Salt format. This is used to derive the MAC key from the root key.
/// </summary>
    private static final String macKeySaltFormat = "Microsoft Azure Cosmos DB MAC key with encryption algorithm:%s and key length:%s";

    /// <summary>
/// IV Key Salt format. This is used to derive the IV key from the root key. This is only used for Deterministic encryption.
/// </summary>
    private static final String ivKeySaltFormat = "Microsoft Azure Cosmos DB IV key with encryption algorithm:%s and key length:%s";

    /// <summary>
/// Encryption Key
/// </summary>
    private final SymmetricKey encryptionKey;

    /// <summary>
/// MAC key
/// </summary>
    private final SymmetricKey macKey;

    /// <summary>
/// IV Key
/// </summary>
    private final SymmetricKey ivKey;

    /// <summary>
/// The name of the algorithm this key will be used with.
/// </summary>
    private final String algorithmName;

    /// <summary>
    /// Derives all the required keys from the given root key
    /// </summary>
    AeadAes256CbcHmac256EncryptionKey(byte[] rootKey, String algorithmName) {
        super(rootKey);
        this.algorithmName = algorithmName;

        int keySizeInBytes = KeySize / 8;

        // Key validation
        if (rootKey.length != keySizeInBytes) {
            throw EncryptionExceptionFactory.InvalidKeySize(
                this.algorithmName,
                rootKey.length,
                keySizeInBytes);
        }

        // Derive keys from the root key
        //
        // Derive encryption key
        String encryptionKeySalt = Strings.lenientFormat(encryptionKeySaltFormat,
            this.algorithmName,
            KeySize);
        byte[] buff1 = new byte[keySizeInBytes];
        SecurityUtility.GetHMACWithSHA256(Utils.getUTF8Bytes(encryptionKeySalt), this.getRootKey(), buff1);
        this.encryptionKey = new SymmetricKey(buff1);

        // Derive mac key
        String macKeySalt = Strings.lenientFormat(macKeySaltFormat, this.algorithmName, KeySize);
        byte[] buff2 = new byte[keySizeInBytes];
        SecurityUtility.GetHMACWithSHA256(Utils.getUTF8Bytes(macKeySalt), this.getRootKey(), buff2);
        this.macKey = new SymmetricKey(buff2);

        // Derive iv key
        String ivKeySalt = Strings.lenientFormat(ivKeySaltFormat, this.algorithmName, KeySize);
        byte[] buff3 = new byte[keySizeInBytes];
        SecurityUtility.GetHMACWithSHA256(Utils.getUTF8Bytes(ivKeySalt), this.getRootKey(), buff3);
        this.ivKey = new SymmetricKey(buff3);
    }

    /// <summary>
    /// Encryption key should be used for encryption and decryption
    /// </summary>
    byte[] getEncryptionKey() {
        return this.encryptionKey.getRootKey();
    }

    /// <summary>
    /// MAC key should be used to compute and validate HMAC
    /// </summary>
    byte[] getMACKey() {
        return this.macKey.getRootKey();
    }

    /// <summary>
    /// IV key should be used to compute synthetic IV from a given plain text
    /// </summary>
    byte[] getIVKey() {
        return this.ivKey.getRootKey();
    }

}

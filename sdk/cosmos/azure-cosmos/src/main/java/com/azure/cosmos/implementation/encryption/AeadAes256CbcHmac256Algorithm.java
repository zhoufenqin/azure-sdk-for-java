package com.azure.cosmos.implementation.encryption;

import java.util.concurrent.ConcurrentLinkedQueue;

/// <summary>
/// This class implements authenticated encryption algorithm with associated data as described in
/// http://tools.ietf.org/html/draft-mcgrew-aead-aes-cbc-hmac-sha2-05 - specifically this implements
/// AEAD_AES_256_CBC_HMAC_SHA256 algorithm.
/// This (and AeadAes256CbcHmac256EncryptionKey) implementation for Cosmos DB is same as the existing
/// SQL client implementation with StyleCop related changes - also, we restrict to randomized encryption to start with.
/// </summary>
public class AeadAes256CbcHmac256Algorithm implements EncryptionAlgorithm {

    private final static  String AlgorithmNameConstant = "AEAD_AES_256_CBC_HMAC_SHA256";


    /// <summary>
    /// Algorithm Name
    /// </summary>
    @Override
    public String getAlgorithmName() {
        return AlgorithmNameConstant;
    }

    /// <summary>
    /// Key size in bytes
    /// </summary>
    private static final int KeySizeInBytes = AeadAes256CbcHmac256EncryptionKey.KeySize / 8;

    /// <summary>
    /// Block size in bytes. AES uses 16 byte blocks.
    /// </summary>
    private static final int BlockSizeInBytes = 16;

    /// <summary>
    /// Minimum Length of cipherText without authentication tag. This value is 1 (version byte) + 16 (IV) + 16 (minimum of 1 block of cipher Text)
    /// </summary>
    private static final int MinimumCipherTextLengthInBytesNoAuthenticationTag = Bytes.ONE_BYTE_SIZE + BlockSizeInBytes + BlockSizeInBytes;

    /// <summary>
    /// Minimum Length of cipherText. This value is 1 (version byte) + 32 (authentication tag) + 16 (IV) + 16 (minimum of 1 block of cipher Text)
    /// </summary>
    private static final int MinimumCipherTextLengthInBytesWithAuthenticationTag = MinimumCipherTextLengthInBytesNoAuthenticationTag + KeySizeInBytes;

    /// <summary>
    /// Cipher Mode. For this algorithm, we only use CBC mode.
    /// </summary>
    private static final AesCryptoServiceProvider.CipherMode cipherMode = AesCryptoServiceProvider.CipherMode.CBC;

    /// <summary>
    /// Padding mode. This algorithm uses PKCS7.
    /// </summary> // TODO:
    private static final AesCryptoServiceProvider.PaddingMode paddingMode = AesCryptoServiceProvider.PaddingMode.PKCS5;

    /// <summary>
    /// Variable indicating whether this algorithm should work in Deterministic mode or Randomized mode.
    /// For deterministic encryption, we derive an IV from the plaintext data.
    /// For randomized encryption, we generate a cryptographically random IV.
    /// </summary>
    private final boolean isDeterministic;

    /// <summary>
    /// Algorithm Version.
    /// </summary>
    private final byte algorithmVersion;

    /// <summary>
    /// Data Encryption Key. This has a root key and three derived keys.
    /// </summary>
    private final AeadAes256CbcHmac256EncryptionKey dataEncryptionKey;

    /// <summary>
    /// The pool of crypto providers to use for encrypt/decrypt operations.
    /// </summary>
    private final ConcurrentLinkedQueue<AesCryptoServiceProvider> cryptoProviderPool;

    /// <summary>
    /// Byte array with algorithm version used for authentication tag computation.
    /// </summary>
    private static final byte[] version = new byte[] { 0x01 };

    /// <summary>
    /// Byte array with algorithm version size used for authentication tag computation.
    /// </summary>
    private static final byte[] versionSize = new byte[] { Bytes.ONE_BYTE_SIZE };



    /// <summary>
    /// Initializes a new instance of AeadAes256CbcHmac256Algorithm algorithm with a given key and encryption type
    /// </summary>
    /// <param name="encryptionKey">
    /// Root encryption key from which three other keys will be derived
    /// </param>
    /// <param name="encryptionType">Encryption Type, accepted values are Deterministic and Randomized.
    /// For Deterministic encryption, a synthetic IV will be genenrated during encryption
    /// For Randomized encryption, a random IV will be generated during encryption.
    /// </param>
    /// <param name="algorithmVersion">
    /// Algorithm version
    /// </param>
    AeadAes256CbcHmac256Algorithm(AeadAes256CbcHmac256EncryptionKey encryptionKey, EncryptionType encryptionType, byte algorithmVersion)
    {
        this.dataEncryptionKey = encryptionKey;
        this.algorithmVersion = algorithmVersion;

        version[0] = algorithmVersion;

        assert encryptionKey != null: "Null encryption key detected in AeadAes256CbcHmac256 algorithm";
        assert algorithmVersion == 0x01 : "Unknown algorithm version passed to AeadAes256CbcHmac256";

        // Validate encryption type for this algorithm
        // This algorithm can only provide randomized or deterministic encryption types.
        // Right now, we support only randomized encryption for Cosmos DB client side encryption.
        assert encryptionType == EncryptionType.Randomized : "Invalid Encryption Type detected in AeadAes256CbcHmac256Algorithm";
        this.isDeterministic = false;

        this.cryptoProviderPool = new ConcurrentLinkedQueue<AesCryptoServiceProvider>();
    }

    /// <summary>
    /// Encryption Algorithm
    /// cell_iv = HMAC_SHA-2-256(iv_key, cell_data) truncated to 128 bits
    /// cell_ciphertext = AES-CBC-256(enc_key, cell_iv, cell_data) with PKCS7 padding.
    /// cell_tag = HMAC_SHA-2-256(mac_key, versionbyte + cell_iv + cell_ciphertext + versionbyte_length)
    /// cell_blob = versionbyte + cell_tag + cell_iv + cell_ciphertext
    /// </summary>
    /// <param name="plainText">Plaintext data to be encrypted</param>
    /// <returns>Returns the ciphertext corresponding to the plaintext.</returns>
    @Override
    public byte[] encryptData(byte[] plainText)
    {
        return this.EncryptData(plainText, true);
    }


    /// <summary>
    /// Encryption Algorithm
    /// cell_iv = HMAC_SHA-2-256(iv_key, cell_data) truncated to 128 bits
    /// cell_ciphertext = AES-CBC-256(enc_key, cell_iv, cell_data) with PKCS7 padding.
    /// (optional) cell_tag = HMAC_SHA-2-256(mac_key, versionbyte + cell_iv + cell_ciphertext + versionbyte_length)
    /// cell_blob = versionbyte + [cell_tag] + cell_iv + cell_ciphertext
    /// </summary>
    /// <param name="plainText">Plaintext data to be encrypted</param>
    /// <param name="hasAuthenticationTag">Does the algorithm require authentication tag.</param>
    /// <returns>Returns the ciphertext corresponding to the plaintext.</returns>
    private byte[] EncryptData(byte[] plainText, boolean hasAuthenticationTag)
    {
        // Empty values get encrypted and decrypted properly for both Deterministic and Randomized encryptions.
        assert(plainText != null);

        byte[] iv = new byte[BlockSizeInBytes];

        // Prepare IV
        // Should be 1 single block (16 bytes)
        if (this.isDeterministic)
        {
            SecurityUtility.GetHMACWithSHA256(plainText, this.dataEncryptionKey.getIVKey(), iv);
        }
        else
        {
            SecurityUtility.GenerateRandomBytes(iv);
        }

        int numBlocks = (plainText.length / BlockSizeInBytes) + 1;

        // Final blob we return = version + HMAC + iv + cipherText
        final int hmacStartIndex = 1;
        int authenticationTagLen = hasAuthenticationTag ? KeySizeInBytes : 0;
        int ivStartIndex = hmacStartIndex + authenticationTagLen;
        int cipherStartIndex = ivStartIndex + BlockSizeInBytes; // this is where hmac starts.

        // Output buffer size = size of VersionByte + Authentication Tag + IV + cipher Text blocks.
        int outputBufSize = Bytes.ONE_BYTE_SIZE + authenticationTagLen + iv.length + (numBlocks * BlockSizeInBytes);
        byte[] outBuffer = new byte[outputBufSize];

        // Store the version and IV rightaway
        outBuffer[0] = this.algorithmVersion;
        System.arraycopy(iv, 0, outBuffer, ivStartIndex, iv.length);

        AesCryptoServiceProvider aesAlg = this.cryptoProviderPool.poll();

        // Try to get a provider from the pool.
        // If no provider is available, create a new one.
        if (aesAlg == null)
        {
            aesAlg = new AesCryptoServiceProvider(this.dataEncryptionKey.getEncryptionKey(), paddingMode, cipherMode);

//            try
//            {
//                // Set various algorithm properties
//                aesAlg.Key = this.dataEncryptionKey.getEncryptionKey();
//                AesCryptoServiceProvider.CipherMode = cipherMode;
//                aesAlg.Padding = paddingMode;
//            }
//            catch (Exception)
//            {
//                if (aesAlg != null)
//                {
//                    aesAlg.Dispose();
//                }
//
//                throw;
//            }
        }

        try
        {
            // Always set the IV since it changes from cell to cell.
            aesAlg.setIv(iv);

            // Compute CipherText and authentication tag in a single pass
            try (AesCryptoServiceProvider.ICryptoTransform encryptor = aesAlg.CreateEncryptor())
            {
                // TODO: assert encryptor.CanTransformMultipleBlocks : "AES Encryptor can transform multiple blocks";
                int count = 0;
                int cipherIndex = cipherStartIndex; // this is where cipherText starts
                if (numBlocks > 1)
                {
                    count = (numBlocks - 1) * BlockSizeInBytes;
                    cipherIndex += encryptor.TransformBlock(plainText, 0, count, outBuffer, cipherIndex);
                }

                byte[] buffTmp = encryptor.TransformFinalBlock(plainText, count, plainText.length - count); // done encrypting
                System.arraycopy(buffTmp, 0, outBuffer, cipherIndex, buffTmp.length);
                cipherIndex += buffTmp.length;
            }

            if (hasAuthenticationTag)
            {
                try (HMACSHA256 hmac = new HMACSHA256(this.dataEncryptionKey.getMACKey()))
                {
                    // TODO: always true assert(hmac.CanTransformMultipleBlocks, "HMAC can't transform multiple blocks");
                    hmac.TransformBlock(version, 0, version.length, version, 0);
                    hmac.TransformBlock(iv, 0, iv.length, iv, 0);

                    // Compute HMAC on final block
                    hmac.TransformBlock(outBuffer, cipherStartIndex, numBlocks * BlockSizeInBytes, outBuffer, cipherStartIndex);
                    hmac.TransformFinalBlock(versionSize, 0, versionSize.length);
                    byte[] hash = hmac.getHash();
                    assert hash.length >= authenticationTagLen:  "Unexpected hash size";
                    System.arraycopy(hash, 0, outBuffer, hmacStartIndex, authenticationTagLen);
                }
            }
        }
        finally
        {
            // Return the provider to the pool.
            this.cryptoProviderPool.add(aesAlg);
        }

        return outBuffer;
    }


    /// <summary>
    /// Decryption steps
    /// 1. Validate version byte
    /// 2. Validate Authentication tag
    /// 3. Decrypt the message
    /// </summary>
    @Override
    public byte[] decryptData(byte[] cipherText)
    {
        return this.decryptData(cipherText, /** hasAuthenticationTag */ true);
    }

    /// <summary>
    /// Decryption steps
    /// 1. Validate version byte
    /// 2. (optional) Validate Authentication tag
    /// 3. Decrypt the message
    /// </summary>
    private byte[] decryptData(byte[] cipherText, boolean hasAuthenticationTag) {
        assert cipherText != null;

        byte[] iv = new byte[BlockSizeInBytes];

        int minimumCipherTextLength = hasAuthenticationTag ? MinimumCipherTextLengthInBytesWithAuthenticationTag : MinimumCipherTextLengthInBytesNoAuthenticationTag;
        if (cipherText.length < minimumCipherTextLength)
        {
            throw EncryptionExceptionFactory.InvalidCipherTextSize(cipherText.length, minimumCipherTextLength);
        }

        // Validate the version byte
        int startIndex = 0;
        if (cipherText[startIndex] != this.algorithmVersion)
        {
            // Cipher text was computed with a different algorithm version than this.
            throw EncryptionExceptionFactory.InvalidAlgorithmVersion(cipherText[startIndex], this.algorithmVersion);
        }

        startIndex += 1;
        int authenticationTagOffset = 0;

        // Read authentication tag
        if (hasAuthenticationTag)
        {
            authenticationTagOffset = startIndex;
            startIndex += KeySizeInBytes; // authentication tag size is KeySizeInBytes
        }

        // Read cell IV
        System.arraycopy(cipherText, startIndex, iv, 0, iv.length);
        startIndex += iv.length;

        // Read encrypted text
        int cipherTextOffset = startIndex;
        int cipherTextCount = cipherText.length - startIndex;

        if (hasAuthenticationTag)
        {
            // Compute authentication tag
            byte[] authenticationTag = this.PrepareAuthenticationTag(iv, cipherText, cipherTextOffset, cipherTextCount);
            if (!SecurityUtility.CompareBytes(authenticationTag, cipherText, authenticationTagOffset, authenticationTag.length))
            {
                // Potentially tampered data, throw an exception
                throw EncryptionExceptionFactory.InvalidAuthenticationTag();
            }
        }

        // Decrypt the text and return
        return this.DecryptData(iv, cipherText, cipherTextOffset, cipherTextCount);
     }


    /// <summary>
    /// Decrypts plain text data using AES in CBC mode
    /// </summary>
    private byte[] DecryptData(byte[] iv, byte[] cipherText, int offset, int count)
    {
        assert((iv != null) && (cipherText != null));
        assert(offset > -1 && count > -1);
        assert((count + offset) <= cipherText.length);

        byte[] plainText;

        AesCryptoServiceProvider aesAlg = this.cryptoProviderPool.poll();

        // Try to get a provider from the pool.
        // If no provider is available, create a new one.
        if (aesAlg == null)
        {
            aesAlg = new AesCryptoServiceProvider(this.dataEncryptionKey.getEncryptionKey(), paddingMode, cipherMode);

//            try
//            {
//                // Set various algorithm properties
//                aesAlg.Key = this.dataEncryptionKey.EncryptionKey;
//                aesAlg.Mode = cipherMode;
//                aesAlg.Padding = paddingMode;
//            }
//            catch (Exception)
//            {
//                if (aesAlg != null)
//                {
//                    aesAlg.Dispose();
//                }
//
//                throw;
//            }
        }

        try
        {
            // Always set the IV since it changes from cell to cell.
            aesAlg.setIv(iv);

            // Create the streams used for decryption.


            try(AesCryptoServiceProvider.ICryptoTransform decryptor = aesAlg.CreateDecryptor()) {

                plainText = decryptor.TransformFinalBlock(cipherText, offset, count);

            }


//            using (MemoryStream msDecrypt = new MemoryStream())
//            {
//                // Create an encryptor to perform the stream transform.
//                using (ICryptoTransform decryptor = aesAlg.CreateDecryptor())
//                {
//                    using (CryptoStream csDecrypt = new CryptoStream(msDecrypt, decryptor, CryptoStreamMode.Write))
//                    {
//                        // Decrypt the secret message and get the plain text data
//                        csDecrypt.Write(cipherText, offset, count);
//                        csDecrypt.FlushFinalBlock();
//                        plainText = msDecrypt.ToArray();
//                    }
//                }
//            }
        }
        finally
        {
            // Return the provider to the pool.
            this.cryptoProviderPool.add(aesAlg);
        }

        return plainText;
    }



    /// <summary>
    /// Prepares an authentication tag.
    /// Authentication Tag = HMAC_SHA-2-256(mac_key, versionbyte + cell_iv + cell_ciphertext + versionbyte_length)
    /// </summary>
    private byte[] PrepareAuthenticationTag(byte[] iv, byte[] cipherText, int offset, int length)
    {
        assert(cipherText != null);

        byte[] computedHash;
        byte[] authenticationTag = new byte[KeySizeInBytes];

        // Raw Tag Length:
        //              1 for the version byte
        //              1 block for IV (16 bytes)
        //              cipherText.Length
        //              1 byte for version byte length
        try (HMACSHA256 hmac = new HMACSHA256(this.dataEncryptionKey.getMACKey()))
        {
            int retVal = 0;
            retVal = hmac.TransformBlock(version, 0, version.length, version, 0);
            assert(retVal == version.length);
            retVal = hmac.TransformBlock(iv, 0, iv.length, iv, 0);
            assert(retVal == iv.length);
            retVal = hmac.TransformBlock(cipherText, offset, length, cipherText, offset);
            assert(retVal == length);
            hmac.TransformFinalBlock(versionSize, 0, versionSize.length);
            computedHash = hmac.getHash();
        }

        assert(computedHash.length >= authenticationTag.length);
        System.arraycopy(computedHash, 0, authenticationTag, 0, authenticationTag.length);
        return authenticationTag;
    }
}

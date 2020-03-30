package com.azure.cosmos.implementation.encryption;

class SecurityUtility {

    final static int MaxSHA256HashBytes = 32;

    /// <summary>
    /// Computes a keyed hash of a given text and returns. It fills the buffer "hash" with computed hash value.
    /// </summary>
    /// <param name="plainText">Plain text bytes whose hash has to be computed.</param>
    /// <param name="key">key used for the HMAC.</param>
    /// <param name="hash">Output buffer where the computed hash value is stored. If it is less than 32 bytes, the hash is truncated.</param>
    static void GetHMACWithSHA256(byte[] plainText, byte[] key, byte[] hash) {

        assert (key != null && plainText != null);
        assert (hash.length != 0 && hash.length <= MaxSHA256HashBytes);

        try(HMACSHA256 hmac = new HMACSHA256(key))
        {
            byte[] computedHash = hmac.ComputeHash(plainText);

            // Truncate the hash if needed
            System.arraycopy(computedHash, 0, hash, 0, hash.length);
        }
    }

    /// <summary>
    /// Computes SHA256 hash of a given input.
    /// </summary>
    /// <param name="input">input byte array which needs to be hashed.</param>
    /// <returns>Returns SHA256 hash in a string form.</returns>
    static String GetSHA256Hash(byte[] input) {
        assert (input != null);

        try(SHA256 sha256 = SHA256.Create())
        {
            byte[] hashValue = sha256.ComputeHash(input);
            return GetHexString(hashValue);
        }
    }

    /// <summary>
    /// Generates cryptographically random bytes.
    /// </summary>
    /// <param name="randomBytes">Buffer into which cryptographically random bytes are to be generated.</param>
    static void GenerateRandomBytes(byte[] randomBytes) {
        // Generate random bytes cryptographically.
        try(RNGCryptoServiceProvider rngCsp = new RNGCryptoServiceProvider())
        {
            rngCsp.GetBytes(randomBytes);
        }
    }

    /// <summary>
    /// Compares two byte arrays and returns true if all bytes are equal.
    /// </summary>
    /// <param name="buffer1">input buffer</param>
    /// <param name="buffer2">another buffer to be compared against</param>
    /// <param name="buffer2Index"></param>
    /// <param name="lengthToCompare"></param>
    /// <returns>returns true if both the arrays have the same byte values else returns false</returns>
    static boolean CompareBytes(byte[] buffer1, byte[] buffer2, int buffer2Index, int lengthToCompare) {
        if (buffer1 == null || buffer2 == null) {
            return false;
        }

        assert buffer1.length >= lengthToCompare : "invalid lengthToCompare";
        assert buffer2Index > -1 && buffer2Index < buffer2.length : "invalid index";
        if ((buffer2.length - buffer2Index) < lengthToCompare) {
            return false;
        }

        for (int index = 0; index < buffer1.length && index < lengthToCompare; ++index) {
            if (buffer1[index] != buffer2[buffer2Index + index]) {
                return false;
            }
        }

        return true;
    }

    /// <summary>
    /// Gets hex representation of byte array.
    /// <param name="input">input byte array</param>
    /// </summary>
    private static String GetHexString(byte[] input) {
        assert (input != null);

        return Bytes.toHex(input);
    }
}

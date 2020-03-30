package com.azure.cosmos.implementation.encryption;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class JavaKeyProvider {

    Map<String, JavaDataEncryptionKey> keyMap = new HashMap<>();

    public void addKey(JavaDataEncryptionKey  key) {
        keyMap.put(key.getId(), key);
    }

    private static byte[] generateKey() {
        try {

            Random random = new Random();
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            KeySpec spec = new PBEKeySpec("رمز".toCharArray(), salt, 65536, 256); // AES-256
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] key = new byte[0];
            key = secretKeyFactory.generateSecret(spec).getEncoded();

            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            return keySpec.getEncoded();
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }

    }

    public JavaDataEncryptionKey loadKey(String id) {

        return keyMap.get(id);

//        AeadAes256CbcHmac256EncryptionKey aeadAesKey = new AeadAes256CbcHmac256EncryptionKey(generateKey(), "AES");
//        AeadAes256CbcHmac256Algorithm encryptionAlgorithm = new AeadAes256CbcHmac256Algorithm(aeadAesKey, EncryptionType.Randomized, (byte) 0x01);
//
//        return new JavaDataEncryptionKey() {
//            @Override
//            public String getId() {
//                return id;
//            }
//
//            @Override
//            public EncryptionAlgorithm getEncryptionAlgorithm() {
//                return encryptionAlgorithm;
//            }
//        };
    }


}

package com.azure.cosmos.implementation.encryption;

import com.google.common.io.BaseEncoding;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;

public class AesCryptoServiceProviderTest {
    private byte[] key;

    @BeforeClass(groups = "unit")
    public void beforeClass() throws Exception {
        key = generateKey();
    }

    @Test(groups = "unit", dataProvider = "encryptionInput")
    public void aesEncryptThenDecrypt(byte[] input)  {

        AeadAes256CbcHmac256EncryptionKey aeadAesKey = new AeadAes256CbcHmac256EncryptionKey(key, "AES");


        AeadAes256CbcHmac256Algorithm encryptionAlgorithm = new AeadAes256CbcHmac256Algorithm(aeadAesKey, EncryptionType.Randomized, (byte) 0x01);
        byte[] encrypted = encryptionAlgorithm.encryptData(input);


        assertThat(encrypted).isNotEqualTo(input);
        assertThat(encrypted.length).isGreaterThan(input.length);

        byte[] decrypted = encryptionAlgorithm.decryptData(encrypted);



        assertThat(decrypted).isEqualTo(input);
    }

    @DataProvider(name = "encryptionInput")
    public Object[][] encryptionInput() {
        return new Object[][]{
            { new byte[] {} },
            {"secret".getBytes(StandardCharsets.UTF_8) },
            {"محرمانه".getBytes(StandardCharsets.UTF_8) },
            { RandomStringUtils.randomAlphabetic(100_000).getBytes(StandardCharsets.UTF_8) }
        };
    }

    private static byte[] generateKey() throws Exception {
        Random random = new Random();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        KeySpec spec = new PBEKeySpec("رمز".toCharArray(), salt, 65536, 256); // AES-256
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] key = secretKeyFactory.generateSecret(spec).getEncoded();
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        return keySpec.getEncoded();
    }

    private static byte[] hexToByteArray(String hex) {
        return BaseEncoding.base16().decode(hex);
    }
}

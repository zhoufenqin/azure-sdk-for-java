package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.io.BaseEncoding;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class EncryptionProcessorTest {
    private byte[] key;

    @BeforeClass(groups = "unit")
    public void beforeClass() throws Exception {
        key = generateKey();
    }

    public static class Test {
        @JsonProperty
        public String id;
        @JsonProperty
        public String pk;
        @JsonProperty
        public String nonSensitive;
        @JsonProperty
        public String sensitive;
    }

    private Test getTestDate() {

        Test test = new Test();
        test.id = UUID.randomUUID().toString();
        test.pk = UUID.randomUUID().toString();
        test.nonSensitive = UUID.randomUUID().toString();
        test.sensitive = UUID.randomUUID().toString();

        return test;
    }

    @org.testng.annotations.Test(groups = "unit")
    public void aesEncryptThenDecrypt()  {

        AeadAes256CbcHmac256EncryptionKey aeadAesKey = new AeadAes256CbcHmac256EncryptionKey(key, "AES");
        AeadAes256CbcHmac256Algorithm encryptionAlgorithm = new AeadAes256CbcHmac256Algorithm(aeadAesKey, EncryptionType.Randomized, (byte) 0x01);
        JavaDataEncryptionKey javaDataEncryptionKey = new JavaDataEncryptionKey() {
            String keyId = UUID.randomUUID().toString();

            @Override
            public String getId() {
                return keyId;
            }

            @Override
            public EncryptionAlgorithm getEncryptionAlgorithm() {
                return encryptionAlgorithm;
            }
        };

        JavaKeyProvider keyProvider = new JavaKeyProvider();
        keyProvider.addKey(javaDataEncryptionKey);

        EncryptionProcessor encryptionProcessor = new EncryptionProcessor();
        EncryptionOptions encryptionOptions = new EncryptionOptions();
        encryptionOptions.PathsToEncrypt = ImmutableList.of("/sensitive");
        encryptionOptions.DataEncryptionKey = javaDataEncryptionKey;

        Test testDate = getTestDate();
        byte[] inputAsByteArray = toByteArray(testDate);

        byte[] objectAfterEncryptionSensitiveDataAsByteArray = encryptionProcessor.EncryptAsync(inputAsByteArray, encryptionOptions);


        byte[] decryptAsByteArray = encryptionProcessor.DecryptAsync(objectAfterEncryptionSensitiveDataAsByteArray, null, keyProvider);




        assertThat(decryptAsByteArray).isEqualTo(inputAsByteArray);
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

    private static byte[] toByteArray(Object object) {
        return EncryptionUtils.serializeToByteArray(Utils.getSimpleObjectMapper(), object);
    }

    private static byte[] hexToByteArray(String hex) {
        return BaseEncoding.base16().decode(hex);
    }
}

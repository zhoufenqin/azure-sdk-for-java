package com.azure.cosmos.implementation.encryption;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.Closeable;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

public class AesCryptoServiceProvider {

    private final Cipher cipher;
    private final SecretKeySpec secretKeySpec;
    private IvParameterSpec ivspec;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    enum PaddingMode {
        PKCS5("PKCS5Padding"),
        PKCS7("PKCS7Padding");

        String value;
        PaddingMode(String value) {
            this.value = value;
        }
    }

    enum CipherMode {
        CBC("CBC");

        String value;
        CipherMode(String value) {
            this.value = value;
        }
    }

    public AesCryptoServiceProvider(byte[] key, PaddingMode padding, CipherMode mode) {
        try {
            secretKeySpec = new SecretKeySpec(key, "AES");

            cipher = Cipher.getInstance("AES/" + mode.value + "/" + padding.value);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException  e) {
            throw new IllegalStateException(e);
        }
    }

    public class ICryptoTransform implements Closeable {

        public ICryptoTransform(Cipher cipher) {
        }

        @Override
        public void close() {

        }

        public int TransformBlock(
            byte[] inputBuffer,
            int inputOffset,
            int inputCount,
            byte[] outputBuffer,
            int outputOffset) {

            try {
                return cipher.update(inputBuffer, inputOffset, inputCount, outputBuffer, outputOffset);
            } catch (ShortBufferException e) {
                throw new IllegalStateException(e);
            }
        }

        public byte[] TransformFinalBlock(byte[] inputBuffer, int inputOffset, int inputCount)  {
            try {
                return cipher.doFinal(inputBuffer, inputOffset, inputCount);
            } catch (IllegalBlockSizeException |BadPaddingException  e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public ICryptoTransform CreateDecryptor() {
        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivspec);
            return new ICryptoTransform(cipher);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new IllegalStateException(e);
        }
    }

    public ICryptoTransform CreateEncryptor() {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivspec);
            return new ICryptoTransform(cipher);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new IllegalStateException(e);
        }
    }

    public void setIv(byte[] iv) {
        ivspec = new IvParameterSpec(iv);
    }
}

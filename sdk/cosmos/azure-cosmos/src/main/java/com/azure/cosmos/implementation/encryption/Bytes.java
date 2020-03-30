package com.azure.cosmos.implementation.encryption;

public class Bytes {

    public static final int ONE_BYTE_SIZE = 1;

    public static String toHex(byte[] input) {
        StringBuilder str = new StringBuilder();
        for(byte b: input)
        {
            // TODO: is there a faster way to do this?
            str.append(toHex(b));
        }
        return str.toString();
    }

    public static String toHex(byte b) {
        return String.format("%02X", b);
    }
}

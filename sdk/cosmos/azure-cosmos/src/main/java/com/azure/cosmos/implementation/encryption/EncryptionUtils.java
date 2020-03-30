package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.ByteBuffer;

public class EncryptionUtils {

    public static <T> T asObject(ObjectMapper mapper, ObjectNode object, Class<T> classType) {
        try {
            return mapper.treeToValue(object, classType);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to convert JSON to object", e);
        }
    }

    public static byte[] serializeToByteArray(ObjectMapper mapper, Object object) {
        try {
            return Utils.getSimpleObjectMapper().writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to convert JSON to byte[]", e);
        }
    }

    public static byte[] toByteArray(ByteBuffer buf) {
        byte[] bytes = new byte[buf.limit()];
        buf.rewind();
        return bytes;
    }

    public static ObjectNode parseToObjectNode(byte[] item) {
        if (Utils.isEmpty(item)) {
            return null;
        }
        try {
            return (ObjectNode) Utils.getSimpleObjectMapper().readTree(item);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse to ObjectNode.", e);
        }
    }

}

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

public class CosmosJsonSerializer implements CosmosSerializer{
    private final ObjectMapper mapper = Utils.getSimpleObjectMapper();


    @Override
    public <T> T FromStream(byte[] bytes) {


        return null;
//        try {
//          //   return (ObjectNode) mapper.readTree(bytes);
//
//        } catch (IOException e) {
//            throw new IllegalArgumentException(e);
//        }
    }

    @Override
    public <T> byte[] ToStream(T input) {
        try {
            return mapper.writeValueAsBytes(input);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}

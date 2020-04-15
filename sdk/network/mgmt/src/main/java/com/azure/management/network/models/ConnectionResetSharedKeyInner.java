// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.management.network.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The ConnectionResetSharedKey model. */
@Fluent
public final class ConnectionResetSharedKeyInner {
    /*
     * The virtual network connection reset shared key length, should between 1
     * and 128.
     */
    @JsonProperty(value = "keyLength", required = true)
    private int keyLength;

    /**
     * Get the keyLength property: The virtual network connection reset shared key length, should between 1 and 128.
     *
     * @return the keyLength value.
     */
    public int keyLength() {
        return this.keyLength;
    }

    /**
     * Set the keyLength property: The virtual network connection reset shared key length, should between 1 and 128.
     *
     * @param keyLength the keyLength value to set.
     * @return the ConnectionResetSharedKeyInner object itself.
     */
    public ConnectionResetSharedKeyInner withKeyLength(int keyLength) {
        this.keyLength = keyLength;
        return this;
    }
}

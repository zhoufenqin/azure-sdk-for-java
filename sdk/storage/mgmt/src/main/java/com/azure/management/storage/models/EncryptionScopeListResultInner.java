// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.management.storage.models;

import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** The EncryptionScopeListResult model. */
@Immutable
public final class EncryptionScopeListResultInner {
    /*
     * List of encryption scopes requested.
     */
    @JsonProperty(value = "value", access = JsonProperty.Access.WRITE_ONLY)
    private List<EncryptionScopeInner> value;

    /*
     * Request URL that can be used to query next page of encryption scopes.
     * Returned when total number of requested encryption scopes exceeds the
     * maximum page size.
     */
    @JsonProperty(value = "nextLink", access = JsonProperty.Access.WRITE_ONLY)
    private String nextLink;

    /**
     * Get the value property: List of encryption scopes requested.
     *
     * @return the value value.
     */
    public List<EncryptionScopeInner> value() {
        return this.value;
    }

    /**
     * Get the nextLink property: Request URL that can be used to query next page of encryption scopes. Returned when
     * total number of requested encryption scopes exceeds the maximum page size.
     *
     * @return the nextLink value.
     */
    public String nextLink() {
        return this.nextLink;
    }
}

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.implementation.Constants;
import com.fasterxml.jackson.annotation.JsonProperty;

class EncryptionProperties {
    public EncryptionProperties() {}


    @JsonProperty(Constants.Properties.EncryptionFormatVersion)
    public int EncryptionFormatVersion;

    @JsonProperty(Constants.Properties.DataEncryptionKeyRid)
    public String DataEncryptionKeyRid;

    @JsonProperty(Constants.Properties.EncryptedData)
    public byte[] EncryptedData;

    public EncryptionProperties(
        int encryptionFormatVersion,
        String dataEncryptionKeyRid,
        byte[] encryptedData) {
        this.EncryptionFormatVersion = encryptionFormatVersion;
        this.DataEncryptionKeyRid = dataEncryptionKeyRid;
        this.EncryptedData = encryptedData;
    }
}

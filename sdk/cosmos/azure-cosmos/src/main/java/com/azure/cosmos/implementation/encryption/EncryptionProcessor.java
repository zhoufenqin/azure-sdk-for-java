package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.InternalServerErrorException;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.guava27.Strings;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;
import java.util.Map;

class EncryptionProcessor
{
//    private static final CosmosSerializer baseSerializer = new Co

    public byte[] EncryptAsync(
    byte[] input,
    EncryptionOptions encryptionOptions
//    DatabaseCore database,
//    EncryptionKeyWrapProvider encryptionKeyWrapProvider,
//    CosmosDiagnosticsContext diagnosticsContext
) {
        assert (input != null);
        assert (encryptionOptions != null);
//    assert(database != null);
//    assert(diagnosticsContext != null);

        if (encryptionOptions.PathsToEncrypt == null) {
            throw new NullPointerException("encryptionOptions.PathsToEncrypt");
        }

        if (encryptionOptions.PathsToEncrypt.size() == 0) {
            return input;
        }

//    for (String path: encryptionOptions.PathsToEncrypt)
//    {
//        if (StringUtils.isEmpty(path) || path[0] != '/' || path.LastIndexOf('/') != 0)
//        {
//            throw new ArgumentException($"Invalid path {path ?? string.Empty}", nameof(encryptionOptions.PathsToEncrypt));
//        }
//    }

//    if (encryptionOptions.DataEncryptionKey == null)
//    {
//        throw new ArgumentException("Invalid encryption options", nameof(encryptionOptions.DataEncryptionKey));
//    }
//
//    if (encryptionKeyWrapProvider == null)
//    {
//        throw new ArgumentException(ClientResources.EncryptionKeyWrapProviderNotConfigured);
//    }

//    DataEncryptionKey dek = database.GetDataEncryptionKey(encryptionOptions.DataEncryptionKey.Id);



//    DataEncryptionKeyCore dekCore = (DataEncryptionKeyInlineCore)dek;
//    (DataEncryptionKeyProperties dekProperties, InMemoryRawDek inMemoryRawDek) = await dekCore.FetchUnwrappedAsync(
//    diagnosticsContext,
//    cancellationToken);

//    JObject itemJObj = EncryptionProcessor.baseSerializer.FromStream<JObject>(input);

        ObjectNode itemJObj = EncryptionUtils.parseToObjectNode(input);

        ObjectNode toEncryptJObj = Utils.getSimpleObjectMapper().createObjectNode();

        for (String pathToEncrypt : encryptionOptions.PathsToEncrypt) {
            String propertyName = pathToEncrypt.substring(1);
            JsonNode propertyValueHolder = itemJObj.get(propertyName);

            // Even null in the JSON is a JToken with Type Null, this null check is just a sanity check
            if (propertyValueHolder != null) {
                toEncryptJObj.set(propertyName, itemJObj.get(propertyName));
                itemJObj.remove(propertyName);
            }
        }

//    MemoryStream memoryStream = EncryptionProcessor.baseSerializer.ToStream<JObject>(toEncryptJObj) as MemoryStream;
//    assert(memoryStream != null);
//    assert(memoryStream.TryGetBuffer(out _));
        // TODO:
        byte[] plainText = EncryptionUtils.serializeToByteArray(Utils.getSimpleObjectMapper(), toEncryptJObj);
        JavaDataEncryptionKey dataEncryptionKey = encryptionOptions.DataEncryptionKey;
        EncryptionProperties encryptionProperties = new EncryptionProperties(
            /** encryptionFormatVersion: **/1,
            dataEncryptionKey.getId(),
            dataEncryptionKey.getEncryptionAlgorithm().encryptData(plainText));

        itemJObj.set(Constants.Properties.EncryptedInfo, EncryptionUtils.parseToObjectNode(EncryptionUtils.serializeToByteArray(Utils.getSimpleObjectMapper(), encryptionProperties)));

        return EncryptionUtils.serializeToByteArray(Utils.getSimpleObjectMapper(),  itemJObj);
//    return EncryptionProcessor.baseSerializer.ToStream(itemJObj);
//}
    }


    public byte[] DecryptAsync(
    byte[] input,
//    DatabaseCore database,
    EncryptionKeyWrapProvider encryptionKeyWrapProvider,
    JavaKeyProvider keyProvider
//    CosmosDiagnosticsContext diagnosticsContext,
//    CancellationToken cancellationToken)
    ) {
        assert (input != null);
//    assert(database != null);
//    assert(input.CanSeek);
//    assert(diagnosticsContext != null);

//    if (encryptionKeyWrapProvider == null)
//    {
//        return input;
//    }

        ObjectNode itemJObj;
//    using (StreamReader sr = new StreamReader(input, Encoding.UTF8, detectEncodingFromByteOrderMarks: true, bufferSize: 1024, leaveOpen: true))
//    {
//        using (JsonTextReader jsonTextReader = new JsonTextReader(sr))
//        {
//            itemJObj = JsonSerializer.Create().Deserialize<JObject>(jsonTextReader);
//        }
//    }

        itemJObj = EncryptionUtils.parseToObjectNode(input);


//    JsonNode encryptionPropertiesJProp = itemJObj.get(Constants.Properties.EncryptedInfo);
//    ObjectNode encryptionPropertiesJObj = null;
//    if (encryptionPropertiesJProp != null && encryptionPropertiesJProp.textValue() != null && encryptionPropertiesJProp.getNodeType() == JsonNodeType.OBJECT)
//    {
//        encryptionPropertiesJObj = (JObject)encryptionPropertiesJProp.;
//    }

        JsonNode encryptionPropertiesJProp = itemJObj.get(Constants.Properties.EncryptedInfo);
        ObjectNode encryptionPropertiesJObj = null;
        if (encryptionPropertiesJProp != null && !encryptionPropertiesJProp.isNull() && encryptionPropertiesJProp.getNodeType() == JsonNodeType.OBJECT) {

            encryptionPropertiesJObj = (ObjectNode) encryptionPropertiesJProp;
        }

        if (encryptionPropertiesJProp == null) {
            return input;
//        input.Position = 0;
//        return input;
        }

        EncryptionProperties encryptionProperties = EncryptionUtils.asObject(Utils.getSimpleObjectMapper(), encryptionPropertiesJObj, EncryptionProperties.class);
        if (encryptionProperties.EncryptionFormatVersion != 1) {
            throw new InternalServerErrorException(Strings.lenientFormat("Unknown encryption format version: %s. Please upgrade your SDK to the latest version.", encryptionProperties.EncryptionFormatVersion));
        }

//    DataEncryptionKeyCore tempDek = (DataEncryptionKeyInlineCore)database.GetDataEncryptionKey(id: "unknown");
//    (DataEncryptionKeyProperties _, InMemoryRawDek inMemoryRawDek) = await tempDek.FetchUnwrappedByRidAsync(
//    encryptionProperties.DataEncryptionKeyRid,
//    diagnosticsContext,
//    cancellationToken);

        // getKey

        JavaDataEncryptionKey inMemoryRawDek = keyProvider.loadKey(encryptionProperties.DataEncryptionKeyRid);

        byte[] plainText = inMemoryRawDek.getEncryptionAlgorithm().decryptData(encryptionProperties.EncryptedData);

        ObjectNode plainTextJObj = null;
//    using (MemoryStream memoryStream = new MemoryStream(plainText))
//    using (StreamReader streamReader = new StreamReader(memoryStream))
//    using (JsonTextReader jsonTextReader = new JsonTextReader(streamReader))
//    {
//        plainTextJObj = JObject.Load(jsonTextReader);
//    }

        plainTextJObj = EncryptionUtils.parseToObjectNode(plainText);

//    Map<String, Object> properties = Utils.getSimpleObjectMapper().convertValue(plainTextJObj, HashMap.class);
//    while(it.hasNext()) {
//        JsonNode prop = it.next();
//
//        itemJObj.put(prop.field);
//    }

        Iterator<Map.Entry<String, JsonNode>> it = plainTextJObj.fields();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> entry = it.next();
            itemJObj.put(entry.getKey(), entry.getValue());

        }


        itemJObj.remove(Constants.Properties.EncryptedInfo);
        return EncryptionUtils.serializeToByteArray(Utils.getSimpleObjectMapper(), itemJObj);

    }
}

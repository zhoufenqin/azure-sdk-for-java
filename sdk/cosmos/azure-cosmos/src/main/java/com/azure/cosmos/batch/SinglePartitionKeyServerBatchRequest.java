// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

public final class SinglePartitionKeyServerBatchRequest extends ServerBatchRequest {
    /**
     * PartitionKey that applies to all operations in this request.
     */
    private PartitionKey PartitionKey = null;

    /**
     * Initializes a new instance of the {@link SinglePartitionKeyServerBatchRequest} class. Single partition key server
     * request.
     *
     * @param partitionKey Partition key that applies to all operations in this request.
     * @param serializerCore Serializer to serialize user provided objects to JSON.
     */
    private SinglePartitionKeyServerBatchRequest(PartitionKey partitionKey, CosmosSerializerCore serializerCore) {
        //C# TO JAVA CONVERTER TODO TASK: C# to Java Converter could not resolve the named parameters in the
        // following line:
        //ORIGINAL LINE: base(maxBodyLength: int.MaxValue, maxOperationCount: int.MaxValue, serializerCore:
        // serializerCore);
        super(maxBodyLength:Integer.MAX_VALUE, maxOperationCount:Integer.MAX_VALUE, serializerCore:serializerCore)
        this.PartitionKey = partitionKey;
    }

    public PartitionKey getPartitionKey() {
        return PartitionKey;
    }

    /**
     * Creates an instance of {@link SinglePartitionKeyServerBatchRequest}. The body of the request is populated with
     * operations till it reaches the provided maxBodyLength.
     *
     * @param partitionKey Partition key of the request.
     * @param operations Operations to be added into this batch request.
     * @param serializerCore Serializer to serialize user provided objects to JSON.
     * @param cancellationToken {@link CancellationToken} representing request cancellation.
     *
     * @return A newly created instance of {@link SinglePartitionKeyServerBatchRequest}.
     */
    //C# TO JAVA CONVERTER TODO TASK: There is no equivalent in Java to the 'async' keyword:
    //ORIGINAL LINE: public static async Task<SinglePartitionKeyServerBatchRequest> CreateAsync
    // (Nullable<PartitionKey> partitionKey, ArraySegment<ItemBatchOperation> operations, CosmosSerializerCore
    // serializerCore, CancellationToken cancellationToken)
    public static Task<SinglePartitionKeyServerBatchRequest> CreateAsync(PartitionKey partitionKey,
                                                                         ArraySegment<ItemBatchOperation> operations,
                                                                         CosmosSerializerCore serializerCore,
                                                                         CancellationToken cancellationToken) {
        SinglePartitionKeyServerBatchRequest request = new SinglePartitionKeyServerBatchRequest(partitionKey,
            serializerCore);
        //C# TO JAVA CONVERTER TODO TASK: There is no equivalent to 'await' in Java:
        await request.CreateBodyStreamAsync(operations, cancellationToken);
        return request;
    }
}

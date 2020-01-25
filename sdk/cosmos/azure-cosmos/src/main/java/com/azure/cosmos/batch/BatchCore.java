// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

import com.azure.cosmos.PartitionKey;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.RequestOptions;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

public class BatchCore implements TransactionalBatch {

    private final ContainerCore container;
    private final ArrayList<ItemBatchOperation> operations;
    private final PartitionKey partitionKey;

    /**
     * Initializes a new instance of the {@link BatchCore} class.
     *
     * @param container Container that has items on which batch operations are to be performed.
     * @param partitionKey The partition key for all items in the batch. {@link PartitionKey}.
     */
    public BatchCore(@Nonnull ContainerCore container, @Nonnull PartitionKey partitionKey) {

        checkNotNull(container, "expected non-null container");
        checkNotNull(partitionKey, "expected non-null partitionKey");

        this.container = container;
        this.partitionKey = partitionKey;
        this.operations = new ArrayList<ItemBatchOperation>();
    }


    @Override
    public <T> TransactionalBatch CreateItem(@Nonnull T item, TransactionalBatchItemRequestOptions requestOptions) {

        checkNotNull(item, "expected non-null item");

        this.operations.add(new ItemBatchOperation<T>(OperationType.Create, this.operations.size(), item, requestOptions));
        return this;
    }


    @Override
    public TransactionalBatch CreateItemStream(
        @Nonnull Stream streamPayload, TransactionalBatchItemRequestOptions requestOptions) {

        checkNotNull(streamPayload, "expected non-null streamPayload");

        this.operations.add(new ItemBatchOperation(OperationType.Create, this.operations.size(), streamPayload, requestOptions));
        return this;
    }

    @Override
    public TransactionalBatch DeleteItem(@Nonnull String id, TransactionalBatchItemRequestOptions requestOptions) {

        checkNotNull(id, "expected non-null id");

        this.operations.add(new ItemBatchOperation(OperationType.Delete, this.operations.size(), id, requestOptions));

        return this;
    }

    @Override
    public CompletableFuture<TransactionalBatchResponse> ExecuteAsync() {
        return this.ExecuteAsync(null);
    }

    /**
     * Executes the batch at the Azure Cosmos service as an asynchronous operation.
     *
     * @param requestOptions Options that apply to the batch. Used only for EPK routing.
     *
     * @return An awaitable {@link TransactionalBatchResponse} which contains the completion status and results of each
     * operation.
     */
    public CompletableFuture<TransactionalBatchResponse> ExecuteAsync(RequestOptions requestOptions) {

        CosmosDiagnosticsContext diagnosticsContext = new CosmosDiagnosticsContext();
        BatchExecutor executor = new BatchExecutor(this.container, this.partitionKey, this.operations, requestOptions, diagnosticsContext);

        this.operations = new ArrayList<>();
        return executor.ExecuteAsync();
    }

    /**
     * Adds an operation to patch an item into the batch.
     *
     * @param id The cosmos item id.
     * @param patchStream A {@link Stream} containing the patch specification.
     * @param requestOptions (Optional) The options for the item request. {@link TransactionalBatchItemRequestOptions}.
     *
     * @return The {@link TransactionalBatch} instance with the operation added.
     */
    public TransactionalBatch PatchItemStream(
        @Nonnull String id, @Nonnull Stream patchStream, TransactionalBatchItemRequestOptions requestOptions) {

        checkNotNull(id, "expected non-null id");

        this.operations.add(new ItemBatchOperation(OperationType.Patch, this.operations.size(), id, patchStream, requestOptions));
        return this;
    }

    @Override
    public TransactionalBatch ReadItem(@Nonnull String id, TransactionalBatchItemRequestOptions requestOptions) {

        checkNotNull(id, "expected non-null id");

        this.operations.add(new ItemBatchOperation(OperationType.Read, this.operations.size(), id, requestOptions));
        return this;
    }

    @Override
    public <T> TransactionalBatch ReplaceItem(
        @Nonnull String id, @Nonnull T item, TransactionalBatchItemRequestOptions requestOptions) {

        checkNotNull(id, "expected non-null id");
        checkNotNull(item, "expected non-null item");

        this.operations.add(new ItemBatchOperation<T>(OperationType.Replace, this.operations.size(), item, id, requestOptions));
        return this;
    }

    @Override
    public TransactionalBatch ReplaceItemStream(
        @Nonnull String id, @Nonnull Stream streamPayload, TransactionalBatchItemRequestOptions requestOptions) {

        checkNotNull(id, "expected non-null id");
        checkNotNull(streamPayload, "expected non-null streamPayload");

        this.operations.add(new ItemBatchOperation(OperationType.Replace, this.operations.size(), id, streamPayload, requestOptions));
        return this;
    }

    @Override
    public <T> TransactionalBatch UpsertItem(@Nonnull T item, TransactionalBatchItemRequestOptions requestOptions) {

        checkNotNull(item, "expected non-null item");

        this.operations.add(new ItemBatchOperation<T>(OperationType.Upsert, this.operations.size(), item, null, requestOptions));
        return this;
    }

    @Override
    public TransactionalBatch UpsertItemStream(
        @Nonnull Stream streamPayload, TransactionalBatchItemRequestOptions requestOptions) {

        checkNotNull(streamPayload, "expected non-null streamPayload");

        this.operations.add(new ItemBatchOperation(OperationType.Upsert, this.operations.size(), streamPayload, requestOptions));
        return this;
    }
}

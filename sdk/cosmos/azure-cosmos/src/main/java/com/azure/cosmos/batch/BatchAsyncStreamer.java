// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

import java.io.IOException;

/**
 * Handles operation queueing and dispatching.
 * <p>
 * Fills batches efficiently and maintains a timer for early dispatching in case of partially-filled batches and to
 * optimize for throughput. There is always one batch at a time being filled. Locking is in place to avoid concurrent
 * threads trying to Add operations while the timer might be Dispatching the current batch. The current batch is
 * dispatched and a new one is readied to be filled by new operations, the dispatched batch runs independently through a
 * fire and forget pattern.
 * <p>
 * {@link BatchAsyncBatcher}
 */
public class BatchAsyncStreamer implements AutoCloseable {

    private final CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
    private final Object dispatchLimiter = new Object();
    private volatile BatchAsyncBatcher currentBatcher;
    private PooledTimer currentTimer;
    private int dispatchTimerInSeconds;
    private BatchAsyncBatcherExecuteDelegate executor;
    private int maxBatchByteSize;
    private int maxBatchOperationCount;
    private BatchAsyncBatcherRetryDelegate retrier;
    private CosmosSerializerCore serializerCore;
    private TimerPool timerPool;
    private Task timerTask;

    public BatchAsyncStreamer(
        int maxBatchOperationCount,
        int maxBatchByteSize,
        int dispatchTimerInSeconds,
        TimerPool timerPool,
        CosmosSerializerCore serializerCore,
        BatchAsyncBatcherExecuteDelegate executor,
        BatchAsyncBatcherRetryDelegate retrier) {

        if (maxBatchOperationCount < 1) {
            throw new IndexOutOfBoundsException("maxBatchOperationCount");
        }

        if (maxBatchByteSize < 1) {
            throw new IndexOutOfBoundsException("maxBatchByteSize");
        }

        if (dispatchTimerInSeconds < 1) {
            throw new IndexOutOfBoundsException("dispatchTimerInSeconds");
        }

        if (executor == null) {
            throw new NullPointerException("executor");
        }

        if (retrier == null) {
            throw new NullPointerException("retrier");
        }

        if (serializerCore == null) {
            throw new NullPointerException("serializerCore");
        }

        this.maxBatchOperationCount = maxBatchOperationCount;
        this.maxBatchByteSize = maxBatchByteSize;
        this.executor =
            (PartitionKeyRangeServerBatchRequest request, CancellationToken cancellationToken) -> executor.invoke(request, cancellationToken);
        this.retrier =
            (ItemBatchOperation operation, CancellationToken cancellationToken) -> retrier.invoke(operation,
                cancellationToken);
        this.dispatchTimerInSeconds = dispatchTimerInSeconds;
        this.timerPool = timerPool;
        this.serializerCore = serializerCore;
        this.currentBatcher = this.CreateBatchAsyncBatcher();

        this.ResetTimer();
    }

    public final void Add(ItemBatchOperation operation) {
        BatchAsyncBatcher toDispatch = null;
        synchronized (this.dispatchLimiter) {
            while (!this.currentBatcher.TryAdd(operation)) {
                // Batcher is full
                toDispatch = this.GetBatchToDispatchAndCreate();
            }
        }

        if (toDispatch != null) {
            // Discarded for Fire & Forget
            _ = toDispatch.DispatchAsync(this.cancellationTokenSource.Token);
        }
    }

    public final void close() throws IOException {
        this.cancellationTokenSource.Cancel();
        this.cancellationTokenSource.Dispose();
        this.currentTimer.CancelTimer();
        this.currentTimer = null;
        this.timerTask = null;
    }

    private BatchAsyncBatcher CreateBatchAsyncBatcher() {
        return new BatchAsyncBatcher(this.maxBatchOperationCount, this.maxBatchByteSize, this.serializerCore,
            this.executor, this.retrier);
    }

    private void DispatchTimer() {
        if (this.cancellationTokenSource.IsCancellationRequested) {
            return;
        }

        BatchAsyncBatcher toDispatch;
        synchronized (this.dispatchLimiter) {
            toDispatch = this.GetBatchToDispatchAndCreate();
        }

        if (toDispatch != null) {
            // Discarded for Fire & Forget
            _ = toDispatch.DispatchAsync(this.cancellationTokenSource.Token);
        }

        this.ResetTimer();
    }

    private BatchAsyncBatcher GetBatchToDispatchAndCreate() {
        if (this.currentBatcher.getIsEmpty()) {
            return null;
        }

        BatchAsyncBatcher previousBatcher = this.currentBatcher;
        this.currentBatcher = this.CreateBatchAsyncBatcher();
        return previousBatcher;
    }

    private void ResetTimer() {
        this.currentTimer = this.timerPool.GetPooledTimer(this.dispatchTimerInSeconds);
        this.timerTask = this.currentTimer.StartTimerAsync().ContinueWith((task) ->
        {
            this.DispatchTimer();
        }, this.cancellationTokenSource.Token);
    }
}

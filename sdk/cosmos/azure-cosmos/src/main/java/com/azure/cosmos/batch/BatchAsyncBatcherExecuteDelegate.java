// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

/**
 * Executor implementation that processes a list of operations.
 */
@FunctionalInterface
public interface BatchAsyncBatcherExecuteDelegate {
    Task<PartitionKeyRangeBatchExecutionResult> invoke(PartitionKeyRangeServerBatchRequest request, CancellationToken cancellationToken);
}

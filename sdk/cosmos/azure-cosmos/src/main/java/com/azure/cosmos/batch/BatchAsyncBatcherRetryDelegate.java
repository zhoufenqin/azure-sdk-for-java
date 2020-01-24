// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

import java.util.*;

/** 
 * Delegate to process a request for retry an operation
 *
 * @return An instance of {@link PartitionKeyRangeBatchResponse}.
 */
@FunctionalInterface
public interface BatchAsyncBatcherRetryDelegate
{
	Task invoke(ItemBatchOperation operation, CancellationToken cancellationToken);
}
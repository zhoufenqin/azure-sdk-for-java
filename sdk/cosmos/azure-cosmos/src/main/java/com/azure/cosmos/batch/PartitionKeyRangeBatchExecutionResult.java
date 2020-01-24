// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

public class PartitionKeyRangeBatchExecutionResult
{
	private String PartitionKeyRangeId;
	public final String getPartitionKeyRangeId()
	{
		return PartitionKeyRangeId;
	}

	private TransactionalBatchResponse ServerResponse;
	public final TransactionalBatchResponse getServerResponse()
	{
		return ServerResponse;
	}

	private java.lang.Iterable<ItemBatchOperation> Operations;
	public final java.lang.Iterable<ItemBatchOperation> getOperations()
	{
		return Operations;
	}

	public PartitionKeyRangeBatchExecutionResult(String pkRangeId, java.lang.Iterable<ItemBatchOperation> operations, TransactionalBatchResponse serverResponse)
	{
		this.PartitionKeyRangeId = pkRangeId;
		this.ServerResponse = serverResponse;
		this.Operations = operations;
	}

	public final boolean IsSplit()
	{
		return this.getServerResponse() != null && this.getServerResponse().getStatusCode() == HttpStatusCode.Gone && (this.getServerResponse().getSubStatusCode() == Documents.SubStatusCodes.CompletingSplit || this.getServerResponse().getSubStatusCode() == Documents.SubStatusCodes.CompletingPartitionMigration || this.getServerResponse().getSubStatusCode() == Documents.SubStatusCodes.PartitionKeyRangeGone);
	}
}
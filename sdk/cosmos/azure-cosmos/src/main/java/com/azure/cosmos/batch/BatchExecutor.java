// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

public final class BatchExecutor
{
	private ContainerCore container;

	private CosmosClientContext clientContext;

	private IReadOnlyList<ItemBatchOperation> inputOperations;

	private PartitionKey partitionKey;

	private RequestOptions batchOptions;

	private CosmosDiagnosticsContext diagnosticsContext;

	public BatchExecutor(ContainerCore container, PartitionKey partitionKey, IReadOnlyList<ItemBatchOperation> operations, RequestOptions batchOptions, CosmosDiagnosticsContext diagnosticsContext)
	{
		this.container = container;
		this.clientContext = this.container.ClientContext;
		this.inputOperations = operations;
		this.partitionKey = partitionKey;
		this.batchOptions = batchOptions;
		this.diagnosticsContext = diagnosticsContext;
	}

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent in Java to the 'async' keyword:
//ORIGINAL LINE: public async Task<TransactionalBatchResponse> ExecuteAsync(CancellationToken cancellationToken)
	public Task<TransactionalBatchResponse> ExecuteAsync(CancellationToken cancellationToken)
	{
		try (this.diagnosticsContext.CreateOverallScope("BatchExecuteAsync"))
		{
			BatchExecUtils.EnsureValid(this.inputOperations, this.batchOptions);

			PartitionKey serverRequestPartitionKey = this.partitionKey;
			if (this.batchOptions != null && this.batchOptions.IsEffectivePartitionKeyRouting)
			{
				serverRequestPartitionKey = null;
			}

			SinglePartitionKeyServerBatchRequest serverRequest;
			try (this.diagnosticsContext.CreateScope("CreateBatchRequest"))
			{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to 'await' in Java:
				serverRequest = await SinglePartitionKeyServerBatchRequest.CreateAsync(serverRequestPartitionKey, new ArraySegment<ItemBatchOperation>(this.inputOperations.ToArray()), this.clientContext.SerializerCore, cancellationToken);
			}

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to 'await' in Java:
			return await this.ExecuteServerRequestAsync(serverRequest, cancellationToken);
		}
	}

	/** 
	 Makes a single batch request to the server.
	 
	 @param serverRequest A server request with a set of operations on items.
	 @param cancellationToken {@link CancellationToken} representing request cancellation.
	 @return Response from the server.
	*/
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent in Java to the 'async' keyword:
//ORIGINAL LINE: private async Task<TransactionalBatchResponse> ExecuteServerRequestAsync(SinglePartitionKeyServerBatchRequest serverRequest, CancellationToken cancellationToken)
	private Task<TransactionalBatchResponse> ExecuteServerRequestAsync(SinglePartitionKeyServerBatchRequest serverRequest, CancellationToken cancellationToken)
	{
		try (Stream serverRequestPayload = serverRequest.TransferBodyStream())
		{
			Debug.Assert(serverRequestPayload != null, "Server request payload expected to be non-null");
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to 'await' in Java:
			ResponseMessage responseMessage = await this.clientContext.ProcessResourceOperationStreamAsync(this.container.LinkUri, ResourceType.Document, OperationType.Batch, this.batchOptions, this.container, serverRequest.getPartitionKey(), serverRequestPayload, requestMessage ->
			{
						requestMessage.Headers.Add(HttpConstants.HttpHeaders.IsBatchRequest, Boolean.TRUE.toString());
						requestMessage.Headers.Add(HttpConstants.HttpHeaders.IsBatchAtomic, Boolean.TRUE.toString());
						requestMessage.Headers.Add(HttpConstants.HttpHeaders.IsBatchOrdered, Boolean.TRUE.toString());
			}, diagnosticsScope:this.diagnosticsContext, cancellationToken);

			try (this.diagnosticsContext.CreateScope("TransactionalBatchResponse"))
			{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to 'await' in Java:
				return await TransactionalBatchResponse.FromResponseMessageAsync(responseMessage, serverRequest, this.clientContext.SerializerCore);
			}
		}
	}
}
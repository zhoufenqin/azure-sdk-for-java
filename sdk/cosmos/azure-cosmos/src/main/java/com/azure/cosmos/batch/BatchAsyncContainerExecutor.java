// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

import java.util.*;
import java.io.*;

/**
 * Bulk batch executor for operations in the same container.
 * <p>
 * It maintains one {@link BatchAsyncStreamer} for each Partition Key Range, which allows independent execution of requests.
 * Semaphores are in place to rate limit the operations at the Streamer / Partition Key Range level, this means that we can send parallel and independent requests to different Partition Key Ranges, but for the same Range, requests will be limited.
 * Two delegate implementations define how a particular request should be executed, and how operations should be retried. When the {@link BatchAsyncStreamer} dispatches a batch, the batch will create a request and call the execute delegate, if conditions are met, it might call the retry delegate.
 * 
 * {@link BatchAsyncStreamer}
 */
public class BatchAsyncContainerExecutor implements Closeable
{
	private static final int DefaultDispatchTimerInSeconds = 1;
	private static final int MinimumDispatchTimerInSeconds = 1;

	private ContainerCore cosmosContainer;
	private CosmosClientContext cosmosClientContext;
	private int maxServerRequestBodyLength;
	private int maxServerRequestOperationCount;
	private int dispatchTimerInSeconds;
	private final java.util.concurrent.ConcurrentHashMap<String, BatchAsyncStreamer> streamersByPartitionKeyRange = new java.util.concurrent.ConcurrentHashMap<String, BatchAsyncStreamer>();
	private final java.util.concurrent.ConcurrentHashMap<String, SemaphoreSlim> limitersByPartitionkeyRange = new java.util.concurrent.ConcurrentHashMap<String, SemaphoreSlim>();
	private TimerPool timerPool;
	private RetryOptions retryOptions;

	/** 
	 For unit testing.
	*/
	public BatchAsyncContainerExecutor()
	{
	}


	public BatchAsyncContainerExecutor(ContainerCore cosmosContainer, CosmosClientContext cosmosClientContext, int maxServerRequestOperationCount, int maxServerRequestBodyLength)
	{
		this(cosmosContainer, cosmosClientContext, maxServerRequestOperationCount, maxServerRequestBodyLength, BatchAsyncContainerExecutor.DefaultDispatchTimerInSeconds);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public BatchAsyncContainerExecutor(ContainerCore cosmosContainer, CosmosClientContext cosmosClientContext, int maxServerRequestOperationCount, int maxServerRequestBodyLength, int dispatchTimerInSeconds = BatchAsyncContainerExecutor.DefaultDispatchTimerInSeconds)
	public BatchAsyncContainerExecutor(ContainerCore cosmosContainer, CosmosClientContext cosmosClientContext, int maxServerRequestOperationCount, int maxServerRequestBodyLength, int dispatchTimerInSeconds)
	{
		if (cosmosContainer == null)
		{
			throw new NullPointerException("cosmosContainer");
		}

		if (maxServerRequestOperationCount < 1)
		{
			throw new IndexOutOfBoundsException("maxServerRequestOperationCount");
		}

		if (maxServerRequestBodyLength < 1)
		{
			throw new IndexOutOfBoundsException("maxServerRequestBodyLength");
		}

		if (dispatchTimerInSeconds < 1)
		{
			throw new IndexOutOfBoundsException("dispatchTimerInSeconds");
		}

		this.cosmosContainer = cosmosContainer;
		this.cosmosClientContext = cosmosClientContext;
		this.maxServerRequestBodyLength = maxServerRequestBodyLength;
		this.maxServerRequestOperationCount = maxServerRequestOperationCount;
		this.dispatchTimerInSeconds = dispatchTimerInSeconds;
		this.timerPool = new TimerPool(BatchAsyncContainerExecutor.MinimumDispatchTimerInSeconds);
		this.retryOptions = cosmosClientContext.ClientOptions.GetConnectionPolicy().RetryOptions;
	}


	public Task<TransactionalBatchOperationResult> AddAsync(ItemBatchOperation operation, ItemRequestOptions itemRequestOptions)
	{
		return AddAsync(operation, itemRequestOptions, null);
	}

	public Task<TransactionalBatchOperationResult> AddAsync(ItemBatchOperation operation)
	{
		return AddAsync(operation, null, null);
	}

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent in Java to the 'async' keyword:
//ORIGINAL LINE: public virtual async Task<TransactionalBatchOperationResult> AddAsync(ItemBatchOperation operation, ItemRequestOptions itemRequestOptions = null, CancellationToken cancellationToken = default(CancellationToken))
//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
	public Task<TransactionalBatchOperationResult> AddAsync(ItemBatchOperation operation, ItemRequestOptions itemRequestOptions, CancellationToken cancellationToken)
	{
		if (operation == null)
		{
			throw new NullPointerException("operation");
		}

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to 'await' in Java:
		await this.ValidateOperationAsync(operation, itemRequestOptions, cancellationToken);

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to 'await' in Java:
		String resolvedPartitionKeyRangeId = await this.ResolvePartitionKeyRangeIdAsync(operation, cancellationToken).ConfigureAwait(false);
		BatchAsyncStreamer streamer = this.GetOrAddStreamerForPartitionKeyRange(resolvedPartitionKeyRangeId);
		ItemBatchOperationContext context = new ItemBatchOperationContext(resolvedPartitionKeyRangeId, BatchAsyncContainerExecutor.GetRetryPolicy(this.retryOptions));
		operation.AttachContext(context);
		streamer.Add(operation);
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to 'await' in Java:
		return await context.getOperationTask();
	}

	public final void close() throws IOException
	{
		for (Map.Entry<String, BatchAsyncStreamer> streamer : this.streamersByPartitionKeyRange.entrySet())
		{
			streamer.getValue().Dispose();
		}

		for (Map.Entry<String, SemaphoreSlim> limiter : this.limitersByPartitionkeyRange.entrySet())
		{
			limiter.getValue().Dispose();
		}

		this.timerPool.Dispose();
	}


	public Task ValidateOperationAsync(ItemBatchOperation operation, ItemRequestOptions itemRequestOptions)
	{
		return ValidateOperationAsync(operation, itemRequestOptions, null);
	}

	public Task ValidateOperationAsync(ItemBatchOperation operation)
	{
		return ValidateOperationAsync(operation, null, null);
	}

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent in Java to the 'async' keyword:
//ORIGINAL LINE: internal virtual async Task ValidateOperationAsync(ItemBatchOperation operation, ItemRequestOptions itemRequestOptions = null, CancellationToken cancellationToken = default(CancellationToken))
//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
	public Task ValidateOperationAsync(ItemBatchOperation operation, ItemRequestOptions itemRequestOptions, CancellationToken cancellationToken)
	{
		if (itemRequestOptions != null)
		{
			if (itemRequestOptions.BaseConsistencyLevel.HasValue || itemRequestOptions.PreTriggers != null || itemRequestOptions.PostTriggers != null || itemRequestOptions.SessionToken != null)
			{
				throw new IllegalStateException(ClientResources.UnsupportedBulkRequestOptions);
			}

			assert BatchAsyncContainerExecutor.ValidateOperationEPK(operation, itemRequestOptions);
		}

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to 'await' in Java:
		await operation.MaterializeResourceAsync(this.cosmosClientContext.SerializerCore, cancellationToken);
	}

	private static IDocumentClientRetryPolicy GetRetryPolicy(RetryOptions retryOptions)
	{
		return new BulkPartitionKeyRangeGoneRetryPolicy(new ResourceThrottleRetryPolicy(retryOptions.MaxRetryAttemptsOnThrottledRequests, retryOptions.MaxRetryWaitTimeInSeconds));
	}

	private static boolean ValidateOperationEPK(ItemBatchOperation operation, ItemRequestOptions itemRequestOptions)
	{
		Object epkObj;
		Object epkStrObj;
		Object pkStringObj;
//C# TO JAVA CONVERTER TODO TASK: The following method call contained an unresolved 'out' keyword - these cannot be converted using the 'OutObject' helper class unless the method is within the code being modified:
		if (itemRequestOptions.Properties != null && (itemRequestOptions.Properties.TryGetValue(WFConstants.BackendHeaders.EffectivePartitionKey, out epkObj) | itemRequestOptions.Properties.TryGetValue(WFConstants.BackendHeaders.EffectivePartitionKeyString, out epkStrObj) | itemRequestOptions.Properties.TryGetValue(HttpConstants.HttpHeaders.PartitionKey, out pkStringObj)))
		{
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: byte[] epk = epkObj instanceof byte[] ? (byte[])epkObj : null;
			byte[] epk = epkObj instanceof byte[] ? (byte[])epkObj : null;
			String epkStr = epkStrObj instanceof String ? (String)epkStrObj : null;
			String pkString = pkStringObj instanceof String ? (String)pkStringObj : null;
			if ((epk == null && pkString == null) || epkStr == null)
			{
				throw new IllegalStateException(String.format(ClientResources.EpkPropertiesPairingExpected, WFConstants.BackendHeaders.EffectivePartitionKey, WFConstants.BackendHeaders.EffectivePartitionKeyString));
			}

			if (operation.getPartitionKey() != null)
			{
				throw new IllegalStateException(ClientResources.PKAndEpkSetTogether);
			}
		}

		return true;
	}

	private static void AddHeadersToRequestMessage(RequestMessage requestMessage, String partitionKeyRangeId)
	{
		requestMessage.Headers.PartitionKeyRangeId = partitionKeyRangeId;
		requestMessage.Headers.Add(HttpConstants.HttpHeaders.ShouldBatchContinueOnError, Boolean.TRUE.toString());
		requestMessage.Headers.Add(HttpConstants.HttpHeaders.IsBatchRequest, Boolean.TRUE.toString());
	}

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent in Java to the 'async' keyword:
//ORIGINAL LINE: private async Task ReBatchAsync(ItemBatchOperation operation, CancellationToken cancellationToken)
	private Task ReBatchAsync(ItemBatchOperation operation, CancellationToken cancellationToken)
	{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to 'await' in Java:
		String resolvedPartitionKeyRangeId = await this.ResolvePartitionKeyRangeIdAsync(operation, cancellationToken).ConfigureAwait(false);
		BatchAsyncStreamer streamer = this.GetOrAddStreamerForPartitionKeyRange(resolvedPartitionKeyRangeId);
		streamer.Add(operation);
	}

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent in Java to the 'async' keyword:
//ORIGINAL LINE: private async Task<string> ResolvePartitionKeyRangeIdAsync(ItemBatchOperation operation, CancellationToken cancellationToken)
	private Task<String> ResolvePartitionKeyRangeIdAsync(ItemBatchOperation operation, CancellationToken cancellationToken)
	{
		cancellationToken.ThrowIfCancellationRequested();
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to 'await' in Java:
		PartitionKeyDefinition partitionKeyDefinition = await this.cosmosContainer.GetPartitionKeyDefinitionAsync(cancellationToken);
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to 'await' in Java:
		CollectionRoutingMap collectionRoutingMap = await this.cosmosContainer.GetRoutingMapAsync(cancellationToken);

		Object epkObj;
//C# TO JAVA CONVERTER TODO TASK: The following method call contained an unresolved 'out' keyword - these cannot be converted using the 'OutObject' helper class unless the method is within the code being modified:
		Debug.Assert(operation.getRequestOptions() == null ? null : (operation.getRequestOptions().Properties == null ? null : operation.getRequestOptions().Properties.TryGetValue(WFConstants.BackendHeaders.EffectivePartitionKeyString, out epkObj)) == null, "EPK is not supported");
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to 'await' in Java:
		Documents.Routing.PartitionKeyInternal partitionKeyInternal = await this.GetPartitionKeyInternalAsync(operation, cancellationToken);
		operation.setPartitionKeyJson(partitionKeyInternal.ToJsonString());
		String effectivePartitionKeyString = partitionKeyInternal.GetEffectivePartitionKeyString(partitionKeyDefinition);
		return collectionRoutingMap.GetRangeByEffectivePartitionKey(effectivePartitionKeyString).Id;
	}

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent in Java to the 'async' keyword:
//ORIGINAL LINE: private async Task<Documents.Routing.PartitionKeyInternal> GetPartitionKeyInternalAsync(ItemBatchOperation operation, CancellationToken cancellationToken)
	private Task<Documents.Routing.PartitionKeyInternal> GetPartitionKeyInternalAsync(ItemBatchOperation operation, CancellationToken cancellationToken)
	{
		Debug.Assert(operation.getPartitionKey() != null, "PartitionKey should be set on the operation");
		if (operation.getPartitionKey().getValue().IsNone)
		{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to 'await' in Java:
			return await this.cosmosContainer.GetNonePartitionKeyValueAsync(cancellationToken).ConfigureAwait(false);
		}

		return operation.getPartitionKey().getValue().InternalKey;
	}

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent in Java to the 'async' keyword:
//ORIGINAL LINE: private async Task<PartitionKeyRangeBatchExecutionResult> ExecuteAsync(PartitionKeyRangeServerBatchRequest serverRequest, CancellationToken cancellationToken)
	private Task<PartitionKeyRangeBatchExecutionResult> ExecuteAsync(PartitionKeyRangeServerBatchRequest serverRequest, CancellationToken cancellationToken)
	{
		CosmosDiagnosticsContext diagnosticsContext = new CosmosDiagnosticsContext();
		CosmosDiagnosticScope limiterScope = diagnosticsContext.CreateScope("BatchAsyncContainerExecutor.Limiter");
		SemaphoreSlim limiter = this.GetOrAddLimiterForPartitionKeyRange(serverRequest.getPartitionKeyRangeId());
		try (await limiter.UsingWaitAsync(cancellationToken))
		{
			limiterScope.Dispose();
			try (Stream serverRequestPayload = serverRequest.TransferBodyStream())
			{
				Debug.Assert(serverRequestPayload != null, "Server request payload expected to be non-null");
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to 'await' in Java:
				ResponseMessage responseMessage = await this.cosmosClientContext.ProcessResourceOperationStreamAsync(this.cosmosContainer.LinkUri, ResourceType.Document, OperationType.Batch, new RequestOptions(), cosmosContainerCore:this.cosmosContainer, partitionKey: null, streamPayload: serverRequestPayload, requestEnricher: requestMessage -> BatchAsyncContainerExecutor.AddHeadersToRequestMessage(requestMessage, serverRequest.getPartitionKeyRangeId()), diagnosticsScope: diagnosticsContext, cancellationToken: cancellationToken).ConfigureAwait(false);

				try (diagnosticsContext.CreateScope("BatchAsyncContainerExecutor.ToResponse"))
				{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to 'await' in Java:
					TransactionalBatchResponse serverResponse = await TransactionalBatchResponse.FromResponseMessageAsync(responseMessage, serverRequest, this.cosmosClientContext.SerializerCore).ConfigureAwait(false);

					return new PartitionKeyRangeBatchExecutionResult(serverRequest.getPartitionKeyRangeId(), serverRequest.getOperations(), serverResponse);
				}
			}
		}
	}

	private BatchAsyncStreamer GetOrAddStreamerForPartitionKeyRange(String partitionKeyRangeId)
	{
		BatchAsyncStreamer streamer;
//C# TO JAVA CONVERTER TODO TASK: There is no Java ConcurrentHashMap equivalent to this .NET ConcurrentDictionary method:
//C# TO JAVA CONVERTER TODO TASK: The following method call contained an unresolved 'out' keyword - these cannot be converted using the 'OutObject' helper class unless the method is within the code being modified:
		if (this.streamersByPartitionKeyRange.TryGetValue(partitionKeyRangeId, out streamer))
		{
			return streamer;
		}

		BatchAsyncStreamer newStreamer = new BatchAsyncStreamer(this.maxServerRequestOperationCount, this.maxServerRequestBodyLength, this.dispatchTimerInSeconds, this.timerPool, this.cosmosClientContext.SerializerCore, (PartitionKeyRangeServerBatchRequest request, CancellationToken cancellationToken) -> ExecuteAsync(request, cancellationToken), (ItemBatchOperation operation, CancellationToken cancellationToken) -> ReBatchAsync(operation, cancellationToken));

//C# TO JAVA CONVERTER TODO TASK: There is no Java ConcurrentHashMap equivalent to this .NET ConcurrentDictionary method:
		if (!this.streamersByPartitionKeyRange.TryAdd(partitionKeyRangeId, newStreamer))
		{
			newStreamer.close();
		}

		return this.streamersByPartitionKeyRange.get(partitionKeyRangeId);
	}

	private SemaphoreSlim GetOrAddLimiterForPartitionKeyRange(String partitionKeyRangeId)
	{
		SemaphoreSlim limiter;
//C# TO JAVA CONVERTER TODO TASK: There is no Java ConcurrentHashMap equivalent to this .NET ConcurrentDictionary method:
//C# TO JAVA CONVERTER TODO TASK: The following method call contained an unresolved 'out' keyword - these cannot be converted using the 'OutObject' helper class unless the method is within the code being modified:
		if (this.limitersByPartitionkeyRange.TryGetValue(partitionKeyRangeId, out limiter))
		{
			return limiter;
		}

		SemaphoreSlim newLimiter = new SemaphoreSlim(1, 1);
//C# TO JAVA CONVERTER TODO TASK: There is no Java ConcurrentHashMap equivalent to this .NET ConcurrentDictionary method:
		if (!this.limitersByPartitionkeyRange.TryAdd(partitionKeyRangeId, newLimiter))
		{
			newLimiter.Dispose();
		}

		return this.limitersByPartitionkeyRange.get(partitionKeyRangeId);
	}
}
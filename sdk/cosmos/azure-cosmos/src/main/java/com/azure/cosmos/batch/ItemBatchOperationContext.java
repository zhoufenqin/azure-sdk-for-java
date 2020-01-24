// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

import java.io.*;

/** 
 * Context for a particular Batch operation.
 */
public class ItemBatchOperationContext implements Closeable
{
	private String PartitionKeyRangeId;
	public final String getPartitionKeyRangeId()
	{
		return PartitionKeyRangeId;
	}

	private BatchAsyncBatcher CurrentBatcher;
	public final BatchAsyncBatcher getCurrentBatcher()
	{
		return CurrentBatcher;
	}
	public final void setCurrentBatcher(BatchAsyncBatcher value)
	{
		CurrentBatcher = value;
	}

	public final Task<TransactionalBatchOperationResult> getOperationTask()
	{
		return this.taskCompletionSource.Task;
	}

	private IDocumentClientRetryPolicy retryPolicy;

	private TaskCompletionSource<TransactionalBatchOperationResult> taskCompletionSource = new TaskCompletionSource<TransactionalBatchOperationResult>();


	public ItemBatchOperationContext(String partitionKeyRangeId)
	{
		this(partitionKeyRangeId, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public ItemBatchOperationContext(string partitionKeyRangeId, IDocumentClientRetryPolicy retryPolicy = null)
	public ItemBatchOperationContext(String partitionKeyRangeId, IDocumentClientRetryPolicy retryPolicy)
	{
		this.PartitionKeyRangeId = partitionKeyRangeId;
		this.retryPolicy = retryPolicy;
	}

	/** 
	 Based on the Retry Policy, if a failed response should retry.
	*/
	public final Task<ShouldRetryResult> ShouldRetryAsync(TransactionalBatchOperationResult batchOperationResult, CancellationToken cancellationToken)
	{
		if (this.retryPolicy == null || batchOperationResult.getIsSuccessStatusCode())
		{
			return Task.FromResult(ShouldRetryResult.NoRetry());
		}

		ResponseMessage responseMessage = batchOperationResult.ToResponseMessage();
		return this.retryPolicy.ShouldRetryAsync(responseMessage, cancellationToken);
	}

	public final void Complete(BatchAsyncBatcher completer, TransactionalBatchOperationResult result)
	{
		if (this.AssertBatcher(completer))
		{
			this.taskCompletionSource.SetResult(result);
		}

		this.close();
	}

	public final void Fail(BatchAsyncBatcher completer, RuntimeException exception)
	{
		if (this.AssertBatcher(completer, exception))
		{
			this.taskCompletionSource.SetException(exception);
		}

		this.close();
	}

	public final void close() throws IOException
	{
		this.setCurrentBatcher(null);
	}


	private boolean AssertBatcher(BatchAsyncBatcher completer)
	{
		return AssertBatcher(completer, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: private bool AssertBatcher(BatchAsyncBatcher completer, Exception innerException = null)
	private boolean AssertBatcher(BatchAsyncBatcher completer, RuntimeException innerException)
	{
		if (completer != this.getCurrentBatcher())
		{
			DefaultTrace.TraceCritical(String.format("Operation was completed by incorrect batcher."));
			this.taskCompletionSource.SetException(new RuntimeException(String.format("Operation was completed by incorrect batcher."), innerException));
			return false;
		}

		return true;
	}
}
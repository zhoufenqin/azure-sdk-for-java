// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

import java.util.*;

public class BatchCore extends TransactionalBatch
{
	private PartitionKey partitionKey;

	private ContainerCore container;

	private ArrayList<ItemBatchOperation> operations;

	/** 
	 Initializes a new instance of the {@link BatchCore} class.
	 
	 @param container Container that has items on which batch operations are to be performed.
	 @param partitionKey The partition key for all items in the batch. {@link PartitionKey}.
	*/
	public BatchCore(ContainerCore container, PartitionKey partitionKey)
	{
		this.container = container;
		this.partitionKey = partitionKey;
		this.operations = new ArrayList<ItemBatchOperation>();
	}


	@Override
	public <T> TransactionalBatch CreateItem(T item)
	{
		return CreateItem(item, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public override TransactionalBatch CreateItem<T>(T item, TransactionalBatchItemRequestOptions requestOptions = null)
	@Override
	public <T> TransactionalBatch CreateItem(T item, TransactionalBatchItemRequestOptions requestOptions)
	{
		if (item == null)
		{
			throw new NullPointerException("item");
		}

		this.operations.add(new ItemBatchOperation<T>(OperationType.Create, this.operations.size(), , item, null, requestOptions));

		return this;
	}


	@Override
	public TransactionalBatch CreateItemStream(Stream streamPayload)
	{
		return CreateItemStream(streamPayload, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public override TransactionalBatch CreateItemStream(Stream streamPayload, TransactionalBatchItemRequestOptions requestOptions = null)
//C# TO JAVA CONVERTER TODO TASK: C# to Java Converter cannot determine whether this System.IO.Stream is input or output:
	@Override
	public TransactionalBatch CreateItemStream(Stream streamPayload, TransactionalBatchItemRequestOptions requestOptions)
	{
		if (streamPayload == null)
		{
			throw new NullPointerException("streamPayload");
		}

//C# TO JAVA CONVERTER TODO TASK: C# to Java Converter could not resolve the named parameters in the following line:
//ORIGINAL LINE: this.operations.Add(new ItemBatchOperation(operationType: OperationType.Create, operationIndex:this.operations.Count, resourceStream: streamPayload, requestOptions: requestOptions));
		this.operations.add(new ItemBatchOperation(operationType: OperationType.Create, operationIndex:this.operations.size(), resourceStream: streamPayload, requestOptions: requestOptions));

		return this;
	}


	@Override
	public TransactionalBatch ReadItem(String id)
	{
		return ReadItem(id, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public override TransactionalBatch ReadItem(string id, TransactionalBatchItemRequestOptions requestOptions = null)
	@Override
	public TransactionalBatch ReadItem(String id, TransactionalBatchItemRequestOptions requestOptions)
	{
		if (id == null)
		{
			throw new NullPointerException("id");
		}

		this.operations.add(new ItemBatchOperation(OperationType.Read, this.operations.size(), , , id, requestOptions));

		return this;
	}


	@Override
	public <T> TransactionalBatch UpsertItem(T item)
	{
		return UpsertItem(item, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public override TransactionalBatch UpsertItem<T>(T item, TransactionalBatchItemRequestOptions requestOptions = null)
	@Override
	public <T> TransactionalBatch UpsertItem(T item, TransactionalBatchItemRequestOptions requestOptions)
	{
		if (item == null)
		{
			throw new NullPointerException("item");
		}

		this.operations.add(new ItemBatchOperation<T>(OperationType.Upsert, this.operations.size(), , item, null, requestOptions));

		return this;
	}


	@Override
	public TransactionalBatch UpsertItemStream(Stream streamPayload)
	{
		return UpsertItemStream(streamPayload, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public override TransactionalBatch UpsertItemStream(Stream streamPayload, TransactionalBatchItemRequestOptions requestOptions = null)
//C# TO JAVA CONVERTER TODO TASK: C# to Java Converter cannot determine whether this System.IO.Stream is input or output:
	@Override
	public TransactionalBatch UpsertItemStream(Stream streamPayload, TransactionalBatchItemRequestOptions requestOptions)
	{
		if (streamPayload == null)
		{
			throw new NullPointerException("streamPayload");
		}

//C# TO JAVA CONVERTER TODO TASK: C# to Java Converter could not resolve the named parameters in the following line:
//ORIGINAL LINE: this.operations.Add(new ItemBatchOperation(operationType: OperationType.Upsert, operationIndex:this.operations.Count, resourceStream: streamPayload, requestOptions: requestOptions));
		this.operations.add(new ItemBatchOperation(operationType: OperationType.Upsert, operationIndex:this.operations.size(), resourceStream: streamPayload, requestOptions: requestOptions));

		return this;
	}


	@Override
	public <T> TransactionalBatch ReplaceItem(String id, T item)
	{
		return ReplaceItem(id, item, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public override TransactionalBatch ReplaceItem<T>(string id, T item, TransactionalBatchItemRequestOptions requestOptions = null)
	@Override
	public <T> TransactionalBatch ReplaceItem(String id, T item, TransactionalBatchItemRequestOptions requestOptions)
	{
		if (id == null)
		{
			throw new NullPointerException("id");
		}

		if (item == null)
		{
			throw new NullPointerException("item");
		}

		this.operations.add(new ItemBatchOperation<T>(OperationType.Replace, this.operations.size(), , item, id, requestOptions));

		return this;
	}


	@Override
	public TransactionalBatch ReplaceItemStream(String id, Stream streamPayload)
	{
		return ReplaceItemStream(id, streamPayload, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public override TransactionalBatch ReplaceItemStream(string id, Stream streamPayload, TransactionalBatchItemRequestOptions requestOptions = null)
//C# TO JAVA CONVERTER TODO TASK: C# to Java Converter cannot determine whether this System.IO.Stream is input or output:
	@Override
	public TransactionalBatch ReplaceItemStream(String id, Stream streamPayload, TransactionalBatchItemRequestOptions requestOptions)
	{
		if (id == null)
		{
			throw new NullPointerException("id");
		}

		if (streamPayload == null)
		{
			throw new NullPointerException("streamPayload");
		}

//C# TO JAVA CONVERTER TODO TASK: C# to Java Converter could not resolve the named parameters in the following line:
//ORIGINAL LINE: this.operations.Add(new ItemBatchOperation(operationType: OperationType.Replace, operationIndex:this.operations.Count, id: id, resourceStream: streamPayload, requestOptions: requestOptions));
		this.operations.add(new ItemBatchOperation(operationType: OperationType.Replace, operationIndex:this.operations.size(), id: id, resourceStream: streamPayload, requestOptions: requestOptions));

		return this;
	}


	@Override
	public TransactionalBatch DeleteItem(String id)
	{
		return DeleteItem(id, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public override TransactionalBatch DeleteItem(string id, TransactionalBatchItemRequestOptions requestOptions = null)
	@Override
	public TransactionalBatch DeleteItem(String id, TransactionalBatchItemRequestOptions requestOptions)
	{
		if (id == null)
		{
			throw new NullPointerException("id");
		}

		this.operations.add(new ItemBatchOperation(OperationType.Delete, this.operations.size(), , , id, requestOptions));

		return this;
	}


	@Override
	public Task<TransactionalBatchResponse> ExecuteAsync()
	{
		return ExecuteAsync(null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public override Task<TransactionalBatchResponse> ExecuteAsync(CancellationToken cancellationToken = default(CancellationToken))
	@Override
	public Task<TransactionalBatchResponse> ExecuteAsync(CancellationToken cancellationToken)
	{
		return this.ExecuteAsync(null, cancellationToken);
	}

	/** 
	 Executes the batch at the Azure Cosmos service as an asynchronous operation.
	 
	 @param requestOptions Options that apply to the batch. Used only for EPK routing.
	 @param cancellationToken (Optional) {@link CancellationToken} representing request cancellation.
	 @return An awaitable {@link TransactionalBatchResponse} which contains the completion status and results of each operation.
	*/

	public Task<TransactionalBatchResponse> ExecuteAsync(RequestOptions requestOptions)
	{
		return ExecuteAsync(requestOptions, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public virtual Task<TransactionalBatchResponse> ExecuteAsync(RequestOptions requestOptions, CancellationToken cancellationToken = default(CancellationToken))
	public Task<TransactionalBatchResponse> ExecuteAsync(RequestOptions requestOptions, CancellationToken cancellationToken)
	{
		CosmosDiagnosticsContext diagnosticsContext = new CosmosDiagnosticsContext();
		BatchExecutor executor = new BatchExecutor(this.container, this.partitionKey, this.operations, requestOptions, diagnosticsContext);

		this.operations = new ArrayList<ItemBatchOperation>();
		return executor.ExecuteAsync(cancellationToken);
	}

	/** 
	 Adds an operation to patch an item into the batch.
	 
	 @param id The cosmos item id.
	 @param patchStream A {@link Stream} containing the patch specification.
	 @param requestOptions (Optional) The options for the item request. {@link TransactionalBatchItemRequestOptions}.
	 @return The {@link TransactionalBatch} instance with the operation added.
	*/

	public TransactionalBatch PatchItemStream(String id, Stream patchStream)
	{
		return PatchItemStream(id, patchStream, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public virtual TransactionalBatch PatchItemStream(string id, Stream patchStream, TransactionalBatchItemRequestOptions requestOptions = null)
//C# TO JAVA CONVERTER TODO TASK: C# to Java Converter cannot determine whether this System.IO.Stream is input or output:
	public TransactionalBatch PatchItemStream(String id, Stream patchStream, TransactionalBatchItemRequestOptions requestOptions)
	{
//C# TO JAVA CONVERTER TODO TASK: C# to Java Converter could not resolve the named parameters in the following line:
//ORIGINAL LINE: this.operations.Add(new ItemBatchOperation(operationType: OperationType.Patch, operationIndex:this.operations.Count, id: id, resourceStream: patchStream, requestOptions: requestOptions));
		this.operations.add(new ItemBatchOperation(operationType: OperationType.Patch, operationIndex:this.operations.size(), id: id, resourceStream: patchStream, requestOptions: requestOptions));

		return this;
	}
}
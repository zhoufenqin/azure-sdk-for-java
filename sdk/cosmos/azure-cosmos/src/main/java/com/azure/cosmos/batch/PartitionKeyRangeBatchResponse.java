// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

import java.util.*;

/** 
 * Response of a cross partition key batch request.
 */
public class PartitionKeyRangeBatchResponse extends TransactionalBatchResponse
{
	// Results sorted in the order operations had been added.
	private TransactionalBatchOperationResult[] resultsByOperationIndex;
	private TransactionalBatchResponse serverResponse;
	private boolean isDisposed;

	/** 
	 * Initializes a new instance of the {@link PartitionKeyRangeBatchResponse} class.
	 * 
	 * @param originalOperationsCount Original operations that generated the server responses.
	 * @param serverResponse Response from the server.
	 * @param serializerCore Serializer to deserialize response resource body streams.
	 */
	public PartitionKeyRangeBatchResponse(int originalOperationsCount, TransactionalBatchResponse serverResponse, CosmosSerializerCore serializerCore)
	{
		this.setStatusCode(serverResponse.getStatusCode());

		this.serverResponse = serverResponse;
		this.resultsByOperationIndex = new TransactionalBatchOperationResult[originalOperationsCount];

		StringBuilder errorMessageBuilder = new StringBuilder();
		ArrayList<ItemBatchOperation> itemBatchOperations = new ArrayList<ItemBatchOperation>();
		// We expect number of results == number of operations here
		for (int index = 0; index < serverResponse.getOperations().Count; index++)
		{
			int operationIndex = serverResponse.getOperations().get(index).OperationIndex;
			if (this.resultsByOperationIndex[operationIndex] == null || this.resultsByOperationIndex[operationIndex].getStatusCode() == (HttpStatusCode)StatusCodes.TooManyRequests)
			{
				this.resultsByOperationIndex[operationIndex] = serverResponse.get(index);
			}
		}

		itemBatchOperations.addAll(serverResponse.getOperations());
		this.setRequestCharge(this.getRequestCharge() + serverResponse.getRequestCharge());

		if (!tangible.StringHelper.isNullOrEmpty(serverResponse.getErrorMessage()))
		{
			errorMessageBuilder.append(String.format("%1$s; ", serverResponse.getErrorMessage()));
		}

		this.setErrorMessage(errorMessageBuilder.length() > 2 ? errorMessageBuilder.toString(0, errorMessageBuilder.length() - 2) : null);
		this.setOperations(itemBatchOperations);
		this.SerializerCore = serializerCore;
	}

	/** 
	 Gets the ActivityId that identifies the server request made to execute the batch request.
	*/
	@Override
	public String getActivityId()
	{
		return this.serverResponse.getActivityId();
	}

	/** <inheritdoc />
	*/
	@Override
	public CosmosDiagnostics getDiagnostics()
	{
		return this.serverResponse.getDiagnostics();
	}

	@Override
	public CosmosDiagnosticsContext getDiagnosticsContext()
	{
		return this.serverResponse.getDiagnosticsContext();
	}

	private CosmosSerializerCore SerializerCore;
	@Override
	public CosmosSerializerCore getSerializerCore()
	{
		return SerializerCore;
	}

	/** 
	 Gets the number of operation results.
	*/
	@Override
	public int getCount()
	{
		return this.resultsByOperationIndex.length;
	}

	/** <inheritdoc />
	*/
	@Override
	public TransactionalBatchOperationResult get(int index)
	{
		return this.resultsByOperationIndex[index];
	}

	/** 
	 Gets the result of the operation at the provided index in the batch - the returned result has a Resource of provided type.
	 
	 <typeparam name="T">Type to which the Resource in the operation result needs to be deserialized to, when present.</typeparam>
	 @param index 0-based index of the operation in the batch whose result needs to be returned.
	 @return Result of batch operation that contains a Resource deserialized to specified type.
	*/
	@Override
	public <T> TransactionalBatchOperationResult<T> GetOperationResultAtIndex(int index)
	{
		if (index >= this.getCount())
		{
			throw new IndexOutOfBoundsException();
		}

		TransactionalBatchOperationResult result = this.resultsByOperationIndex[index];

		T resource = null;
		if (result.getResourceStream() != null)
		{
			resource = this.getSerializerCore().<T>FromStream(result.getResourceStream());
		}

		return new TransactionalBatchOperationResult<T>(result, resource);
	}

	/** 
	 Gets an enumerator over the operation results.
	 
	 @return Enumerator over the operation results.
	*/
	@Override
	public Iterator<TransactionalBatchOperationResult> GetEnumerator()
	{
		for (TransactionalBatchOperationResult result : this.resultsByOperationIndex)
		{
//C# TO JAVA CONVERTER TODO TASK: Java does not have an equivalent to the C# 'yield' keyword:
			yield return result;
		}
	}

//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if INTERNAL
//C# TO JAVA CONVERTER TODO TASK: Statements that are interrupted by preprocessor statements are not converted by C# to Java Converter:
	public
//#else
//C# TO JAVA CONVERTER TODO TASK: Statements that are interrupted by preprocessor statements are not converted by C# to Java Converter:
	internal
//#endif
	@Override
	private java.lang.Iterable<String> GetActivityIds()
	{
		return new String[] {this.getActivityId()};
	}

	/** 
	 Disposes the disposable members held.
	 
	 @param disposing Indicates whether to dispose managed resources or not.
	*/
	@Override
	protected void Dispose(boolean disposing)
	{
		if (disposing && !this.isDisposed)
		{
			this.isDisposed = true;
			this.serverResponse == null ? null : this.serverResponse.close();
		}

		super.Dispose(disposing);
	}
}
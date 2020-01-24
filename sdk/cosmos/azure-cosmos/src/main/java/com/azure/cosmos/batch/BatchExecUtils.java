// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

import java.io.*;


/** 
 * Util methods for batch requests.
 */
public final class BatchExecUtils
{
	// Using the same buffer size as the Stream.DefaultCopyBufferSize
	private static final int BufferSize = 81920;

	/** 
	 Converts a Stream to a Memory{byte} wrapping a byte array.
	 
	 @param stream Stream to be converted to bytes.
	 @param cancellationToken {@link CancellationToken} to cancel the operation.
	 @return A Memory{byte}.
	*/
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent in Java to the 'async' keyword:
//ORIGINAL LINE: public static async Task<Memory<byte>> StreamToMemoryAsync(Stream stream, CancellationToken cancellationToken)
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
	public static Task<Memory<Byte>> StreamToMemoryAsync(InputStream stream, CancellationToken cancellationToken)
	{
		if (stream.CanSeek)
		{
			// Some derived implementations of MemoryStream (such as versions of RecyclableMemoryStream prior to 1.2.2 that we may be using)
			// return an incorrect response from TryGetBuffer. Use TryGetBuffer only on the MemoryStream type and not derived types.
//C# TO JAVA CONVERTER TODO TASK: C# to Java Converter cannot determine whether this System.IO.MemoryStream is input or output:
			MemoryStream memStream = stream instanceof MemoryStream ? (MemoryStream)stream : null;
			ArraySegment<Byte> memBuffer;
			tangible.OutObject<System.ArraySegment<Byte>> tempOut_memBuffer = new tangible.OutObject<System.ArraySegment<Byte>>();
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (memStream != null && memStream.GetType() == typeof(MemoryStream) && memStream.TryGetBuffer(out ArraySegment<byte> memBuffer))
//C# TO JAVA CONVERTER TODO TASK: C# to Java Converter cannot determine whether this System.IO.MemoryStream is input or output:
			if (memStream != null && memStream.getClass() == MemoryStream.class && memStream.TryGetBuffer(tempOut_memBuffer))
			{
			memBuffer = tempOut_memBuffer.argValue;
				return memBuffer;
			}
		else
		{
			memBuffer = tempOut_memBuffer.argValue;
		}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: byte[] bytes = new byte[stream.Length];
			byte[] bytes = new byte[stream.Length];
			int sum = 0;
			int count;
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to 'await' in Java:
			while ((count = await stream.ReadAsync(bytes, sum, bytes.length - sum, cancellationToken)) > 0)
			{
				sum += count;
			}

			return bytes;
		}
		else
		{
			int bufferSize = BatchExecUtils.BufferSize;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: byte[] buffer = new byte[bufferSize];
			byte[] buffer = new byte[bufferSize];

			try (MemoryStream memoryStream = new MemoryStream(bufferSize))
			{
				int sum = 0;
				int count;
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to 'await' in Java:
				while ((count = await stream.ReadAsync(buffer, 0, bufferSize, cancellationToken)) > 0)
				{
					sum += count;

//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
///#pragma warning disable VSTHRD103 // Call async methods when in an async method
					memoryStream.Write(buffer, 0, count);
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
///#pragma warning restore VSTHRD103 // Call async methods when in an async method
				}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: return new Memory<byte>(memoryStream.GetBuffer(), 0, (int)memoryStream.Length);
				return new Memory<Byte>(memoryStream.GetBuffer(), 0, (int)memoryStream.Length);
			}
		}
	}

	public static void EnsureValid(IReadOnlyList<ItemBatchOperation> operations, RequestOptions batchOptions)
	{
		String errorMessage = BatchExecUtils.IsValid(operations, batchOptions);

		if (errorMessage != null)
		{
			throw new IllegalArgumentException(errorMessage);
		}
	}

	public static String IsValid(IReadOnlyList<ItemBatchOperation> operations, RequestOptions batchOptions)
	{
		String errorMessage = null;

		if (operations.Count == 0)
		{
			errorMessage = ClientResources.BatchNoOperations;
		}

		if (errorMessage == null && batchOptions != null)
		{
			if (batchOptions.IfMatchEtag != null || batchOptions.IfNoneMatchEtag != null)
			{
				errorMessage = ClientResources.BatchRequestOptionNotSupported;
			}
		}

		if (errorMessage == null)
		{
			for (ItemBatchOperation operation : operations)
			{
				Object epkObj;
				Object epkStrObj;
				Object pkStrObj;
//C# TO JAVA CONVERTER TODO TASK: The following method call contained an unresolved 'out' keyword - these cannot be converted using the 'OutObject' helper class unless the method is within the code being modified:
				if (operation.getRequestOptions() != null && operation.getRequestOptions().Properties != null && (operation.getRequestOptions().Properties.TryGetValue(WFConstants.BackendHeaders.EffectivePartitionKey, out epkObj) | operation.getRequestOptions().Properties.TryGetValue(WFConstants.BackendHeaders.EffectivePartitionKeyString, out epkStrObj) | operation.getRequestOptions().Properties.TryGetValue(HttpConstants.HttpHeaders.PartitionKey, out pkStrObj)))
				{
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: byte[] epk = epkObj instanceof byte[] ? (byte[])epkObj : null;
					byte[] epk = epkObj instanceof byte[] ? (byte[])epkObj : null;
					String epkStr = epkStrObj instanceof String ? (String)epkStrObj : null;
					String partitionKeyJsonString = pkStrObj instanceof String ? (String)pkStrObj : null;
					if ((epk == null && partitionKeyJsonString == null) || epkStr == null)
					{
						errorMessage = String.format(ClientResources.EpkPropertiesPairingExpected, WFConstants.BackendHeaders.EffectivePartitionKey, WFConstants.BackendHeaders.EffectivePartitionKeyString);
					}

					if (operation.getPartitionKey() != null && !operation.getRequestOptions().IsEffectivePartitionKeyRouting)
					{
						errorMessage = ClientResources.PKAndEpkSetTogether;
					}
				}
			}
		}

		return errorMessage;
	}

	public static String GetPartitionKeyRangeId(PartitionKey partitionKey, PartitionKeyDefinition partitionKeyDefinition, Routing.CollectionRoutingMap collectionRoutingMap)
	{
		String effectivePartitionKey = partitionKey.InternalKey.GetEffectivePartitionKeyString(partitionKeyDefinition);
		return collectionRoutingMap.GetRangeByEffectivePartitionKey(effectivePartitionKey).Id;
	}
}
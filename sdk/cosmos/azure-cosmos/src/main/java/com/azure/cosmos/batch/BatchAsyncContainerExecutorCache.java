// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

import java.util.*;
import java.io.*;

/** 
 * Cache to create and share Executor instances across the client's lifetime.
 */
public class BatchAsyncContainerExecutorCache implements Closeable
{
	private java.util.concurrent.ConcurrentHashMap<String, BatchAsyncContainerExecutor> executorsPerContainer = new java.util.concurrent.ConcurrentHashMap<String, BatchAsyncContainerExecutor>();

	public final BatchAsyncContainerExecutor GetExecutorForContainer(ContainerCore container, CosmosClientContext cosmosClientContext)
	{
		if (!cosmosClientContext.ClientOptions.AllowBulkExecution)
		{
			throw new IllegalStateException("AllowBulkExecution is not currently enabled.");
		}

		String containerLink = container.LinkUri.toString();
		BatchAsyncContainerExecutor executor;
//C# TO JAVA CONVERTER TODO TASK: There is no Java ConcurrentHashMap equivalent to this .NET ConcurrentDictionary method:
//C# TO JAVA CONVERTER TODO TASK: The following method call contained an unresolved 'out' keyword - these cannot be converted using the 'OutObject' helper class unless the method is within the code being modified:
		if (this.executorsPerContainer.TryGetValue(containerLink, out executor))
		{
			return executor;
		}

		BatchAsyncContainerExecutor newExecutor = new BatchAsyncContainerExecutor(container, cosmosClientContext, Constants.MaxOperationsInDirectModeBatchRequest, Constants.MaxDirectModeBatchRequestBodySizeInBytes);
//C# TO JAVA CONVERTER TODO TASK: There is no Java ConcurrentHashMap equivalent to this .NET ConcurrentDictionary method:
		if (!this.executorsPerContainer.TryAdd(containerLink, newExecutor))
		{
			newExecutor.close();
		}

		return this.executorsPerContainer.get(containerLink);
	}

	public final void close() throws IOException
	{
		for (Map.Entry<String, BatchAsyncContainerExecutor> cacheEntry : this.executorsPerContainer.entrySet())
		{
			cacheEntry.getValue().Dispose();
		}
	}
}
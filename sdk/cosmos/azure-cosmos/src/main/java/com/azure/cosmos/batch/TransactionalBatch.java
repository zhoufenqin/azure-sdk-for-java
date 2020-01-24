// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

 string activityType="personal";
     ToDoActivity test1=new ToDoActivity()
     {
     type=activityType,
     id="learning",
     status="ToBeDone"
     };

     ToDoActivity test2=new ToDoActivity()
     {
     type=activityType,
     id="shopping",
     status="Done"
     };

     ToDoActivity test3=new ToDoActivity()
     {
     type=activityType,
     id="swimming",
     status="ToBeDone"
     };

     using(TransactionalBatchResponse batchResponse=
     await container.CreateTransactionalBatch(new Cosmos.PartitionKey(activityType))
     .CreateItem<ToDoActivity>(test1)
    .ReplaceItem<ToDoActivity>(test2.id,test2)
    .UpsertItem<ToDoActivity>(test3)
    .DeleteItem("reading")
    .CreateItemStream(streamPayload1)
    .ReplaceItemStream("eating",streamPayload2)
    .UpsertItemStream(streamPayload3)
    .ExecuteAsync())
    {
    if(!batchResponse.IsSuccessStatusCode)
    {
    // Handle and log exception
    return;
    }

    // Look up interested results - eg. via typed access on operation results
    TransactionalBatchOperationResult<ToDoActivity> replaceResult=batchResponse.GetOperationResultAtIndex<ToDoActivity>(0);
    ToDoActivity readActivity=replaceResult.Resource;
    }
    ]]>
</code>
</example>
<example>
 This example atomically reads a set of documents as a batch.
<code language="c#">
<![CDATA[
    string activityType="personal";
    using(TransactionalBatchResponse batchResponse=
    await container.CreateTransactionalBatch(new Cosmos.PartitionKey(activityType))
    .ReadItem("playing")
    .ReadItem("walking")
    .ReadItem("jogging")
    .ReadItem("running")
    .ExecuteAsync())
    {
    // Look up interested results - eg. via direct access to operation result stream
    List<string> resultItems=new List<string>();
    foreach(TransactionalBatchOperationResult operationResult in batchResponse)
    {
    using(StreamReader streamReader=new StreamReader(operationResult.ResourceStream))
    {
    resultItems.Add(await streamReader.ReadToEndAsync());
    }
    }
    }
    ]]>
</code>
</example>
<seealso href="https://docs.microsoft.com/azure/cosmos-db/concepts-limits">Limits on TransactionalBatch requests
    */

public abstract class TransactionalBatch {
    /**
     * Adds an operation to create an item into the batch.
     *
     * @param item A JSON serializable object that must contain an id property. See {@link CosmosSerializer} to
     * implement a custom serializer.
     * @param requestOptions (Optional) The options for the item request.
     *
     * @return The transactional batch instance with the operation added.
     * <typeparam name="T">The type of item to be created.</typeparam>
     */

    public final abstract <T> TransactionalBatch CreateItem(T item);

    //C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
    //ORIGINAL LINE: public abstract TransactionalBatch CreateItem<T>(T item, TransactionalBatchItemRequestOptions
    // requestOptions = null);
    public abstract <T> TransactionalBatch CreateItem(T item, TransactionalBatchItemRequestOptions requestOptions);

    /**
     * Adds an operation to create an item into the batch.
     *
     * @param streamPayload A Stream containing the payload of the item. The stream must have a UTF-8 encoded JSON
     * object which contains an id property.
     * @param requestOptions (Optional) The options for the item request.
     *
     * @return The transactional batch instance with the operation added.
     */

    public final abstract TransactionalBatch CreateItemStream(Stream streamPayload);

    //C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
    //ORIGINAL LINE: public abstract TransactionalBatch CreateItemStream(Stream streamPayload,
    // TransactionalBatchItemRequestOptions requestOptions = null);
    //C# TO JAVA CONVERTER TODO TASK: C# to Java Converter cannot determine whether this System.IO.Stream is input or
    // output:
    public abstract TransactionalBatch CreateItemStream(Stream streamPayload,
                                                        TransactionalBatchItemRequestOptions requestOptions);

    /**
     * Adds an operation to delete an item into the batch.
     *
     * @param id The unique id of the item.
     * @param requestOptions (Optional) The options for the item request.
     *
     * @return The transactional batch instance with the operation added.
     */

    public final abstract TransactionalBatch DeleteItem(String id);

    //C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
    //ORIGINAL LINE: public abstract TransactionalBatch DeleteItem(string id, TransactionalBatchItemRequestOptions
    // requestOptions = null);
    public abstract TransactionalBatch DeleteItem(String id, TransactionalBatchItemRequestOptions requestOptions);

    /**
     * Executes the transactional batch at the Azure Cosmos service as an asynchronous operation.
     *
     * @param cancellationToken (Optional) Cancellation token representing request cancellation.
     *
     * @return An awaitable response which contains details of execution of the transactional batch.
     * <p>
     * If the transactional batch executes successfully, the <see cref="TransactionalBatchResponse.StatusCode"/> on the
     * response returned will be set to <see cref="HttpStatusCode.OK"/>.
     * </p>
     * <p>
     * If an operation within the transactional batch fails during execution, no changes from the batch will be
     * committed and the status of the failing operation is made available in the <see
     * cref="TransactionalBatchResponse.StatusCode"/>. To get more details about the operation that failed, the response
     * can be enumerated - this returns <see cref="TransactionalBatchOperationResult" /> instances corresponding to each
     * operation in the transactional batch in the order they were added into the transactional batch. For a result
     * corresponding to an operation within the transactional batch, the <see cref="TransactionalBatchOperationResult
     * .StatusCode"/>
     * indicates the status of the operation - if the operation was not executed or it was aborted due to the failure of
     * another operation within the transactional batch, the value of this field will be HTTP 424 (Failed Dependency);
     * for the operation that caused the batch to abort, the value of this field will indicate the cause of failure as a
     * HTTP status code.
     * </p>
     * <p>
     * The <see cref="TransactionalBatchResponse.StatusCode"/> on the response returned may also have values such as
     * HTTP 5xx in case of server errors and HTTP 429 (Too Many Requests).
     * </p>
     * <p>
     * <p>
     * This API only throws on client side exceptions. This is to increase performance and prevent the overhead of
     * throwing exceptions. Use <see cref="TransactionalBatchResponse.IsSuccessStatusCode"/> on the response returned to
     * ensure that the transactional batch succeeded.
     */

    public final abstract Task<TransactionalBatchResponse> ExecuteAsync();

    //C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
    //ORIGINAL LINE: public abstract Task<TransactionalBatchResponse> ExecuteAsync(CancellationToken cancellationToken = default(CancellationToken));
    public abstract Task<TransactionalBatchResponse> ExecuteAsync(CancellationToken cancellationToken);

    /**
     * Adds an operation to read an item into the batch.
     *
     * @param id The unique id of the item.
     * @param requestOptions (Optional) The options for the item request.
     *
     * @return The transactional batch instance with the operation added.
     */

    public final abstract TransactionalBatch ReadItem(String id);

    //C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
    //ORIGINAL LINE: public abstract TransactionalBatch ReadItem(string id, TransactionalBatchItemRequestOptions
    // requestOptions = null);
    public abstract TransactionalBatch ReadItem(String id, TransactionalBatchItemRequestOptions requestOptions);

    /**
     * Adds an operation to replace an item into the batch.
     *
     * @param id The unique id of the item.
     * @param item A JSON serializable object that must contain an id property. See {@link CosmosSerializer} to
     * implement a custom serializer.
     * @param requestOptions (Optional) The options for the item request.
     *
     * @return The transactional batch instance with the operation added.
     * <typeparam name="T">The type of item to be created.</typeparam>
     */

    public final abstract <T> TransactionalBatch ReplaceItem(String id, T item);

    //C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
    //ORIGINAL LINE: public abstract TransactionalBatch ReplaceItem<T>(string id, T item,
    // TransactionalBatchItemRequestOptions requestOptions = null);
    public abstract <T> TransactionalBatch ReplaceItem(String id, T item,
                                                       TransactionalBatchItemRequestOptions requestOptions);

    /**
     * Adds an operation to replace an item into the batch.
     *
     * @param id The unique id of the item.
     * @param streamPayload A Stream containing the payload of the item. The stream must have a UTF-8 encoded JSON
     * object which contains an id property.
     * @param requestOptions (Optional) The options for the item request.
     *
     * @return The transactional batch instance with the operation added.
     */

    public final abstract TransactionalBatch ReplaceItemStream(String id, Stream streamPayload);

    //C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
    //ORIGINAL LINE: public abstract TransactionalBatch ReplaceItemStream(string id, Stream streamPayload,
    // TransactionalBatchItemRequestOptions requestOptions = null);
    //C# TO JAVA CONVERTER TODO TASK: C# to Java Converter cannot determine whether this System.IO.Stream is input or
    // output:
    public abstract TransactionalBatch ReplaceItemStream(String id, Stream streamPayload,
                                                         TransactionalBatchItemRequestOptions requestOptions);

    /**
     * Adds an operation to upsert an item into the batch.
     *
     * @param item A JSON serializable object that must contain an id property. See {@link CosmosSerializer} to
     * implement a custom serializer.
     * @param requestOptions (Optional) The options for the item request.
     *
     * @return The transactional batch instance with the operation added.
     * <typeparam name="T">The type of item to be created.</typeparam>
     */

    public final abstract <T> TransactionalBatch UpsertItem(T item);

    //C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
    //ORIGINAL LINE: public abstract TransactionalBatch UpsertItem<T>(T item, TransactionalBatchItemRequestOptions
    // requestOptions = null);
    public abstract <T> TransactionalBatch UpsertItem(T item, TransactionalBatchItemRequestOptions requestOptions);

    /**
     * Adds an operation to upsert an item into the batch.
     *
     * @param streamPayload A Stream containing the payload of the item. The stream must have a UTF-8 encoded JSON
     * object which contains an id property.
     * @param requestOptions (Optional) The options for the item request.
     *
     * @return The transactional batch instance with the operation added.
     */

    public final abstract TransactionalBatch UpsertItemStream(Stream streamPayload);

    //C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
    //ORIGINAL LINE: public abstract TransactionalBatch UpsertItemStream(Stream streamPayload,
    // TransactionalBatchItemRequestOptions requestOptions = null);
    //C# TO JAVA CONVERTER TODO TASK: C# to Java Converter cannot determine whether this System.IO.Stream is input or
    // output:
    public abstract TransactionalBatch UpsertItemStream(Stream streamPayload,
                                                        TransactionalBatchItemRequestOptions requestOptions);
}

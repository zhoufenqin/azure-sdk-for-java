// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

import com.azure.cosmos.implementation.HttpConstants.SubStatusCodes;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Response of a {@link TransactionalBatch} request.
 */
public class TransactionalBatchResponse implements IReadOnlyList<TransactionalBatchOperationResult>, AutoCloseable {

    private static String EMPTY_UUID = "00000000-0000-0000-0000-000000000000";

    /**
     * Gets the ActivityId that identifies the server request made to execute the batch.
     */
    private String activityId;
    /**
     * Gets the cosmos diagnostic information for the current request to Azure Cosmos DB service
     */
    private CosmosDiagnostics Diagnostics;
    private CosmosDiagnosticsContext DiagnosticsContext;
    /**
     * Gets the reason for failure of the batch request.
     *
     * <value>The reason for failure, if any.</value>
     */
    private String errorMessage;
    private IReadOnlyList<ItemBatchOperation> Operations;
    /**
     * Gets the request charge for the batch request.
     *
     * <value>
     * The request charge measured in request units.
     * </value>
     */
    private double requestCharge;
    /**
     * Gets the amount of time to wait before retrying this or any other request within Cosmos container or collection
     * due to throttling.
     */
    private Duration retryAfter = null;
    private CosmosSerializerCore SerializerCore;
    /**
     * Gets the completion status code of the batch request.
     *
     * <value>The request completion status code.</value>
     */
    private int statusCode;
    private int subStatusCode;
    private boolean isClosed;
    private ArrayList<TransactionalBatchOperationResult> results;

    /**
     * Initializes a new instance of the {@link TransactionalBatchResponse} class. This method is intended to be used
     * only when a response from the server is not available.
     *
     * @param statusCode Indicates why the batch was not processed.
     * @param subStatusCode Provides further details about why the batch was not processed.
     * @param errorMessage The reason for failure.
     * @param operations Operations that were to be executed.
     * @param diagnosticsContext Diagnostics for the operation
     */
    public TransactionalBatchResponse(
        int statusCode,
        int subStatusCode,
        String errorMessage,
        IReadOnlyList<ItemBatchOperation> operations,
        CosmosDiagnosticsContext diagnosticsContext) {

        this(statusCode, subStatusCode, errorMessage, 0.0D, null, EMPTY_UUID, diagnosticsContext, operations, null);
        this.CreateAndPopulateResults(operations);
    }

    /**
     * Initializes a new instance of the {@link TransactionalBatchResponse} class.
     */
    protected TransactionalBatchResponse() {
    }

    private TransactionalBatchResponse(
        int statusCode,
        int subStatusCode,
        String errorMessage,
        double requestCharge,
        Duration retryAfter,
        String activityId,
        CosmosDiagnosticsContext diagnosticsContext,
        IReadOnlyList<ItemBatchOperation> operations,
        CosmosSerializerCore serializer) {

        this.setStatusCode(statusCode);
        this.subStatusCode = subStatusCode;
        this.setErrorMessage(errorMessage);
        this.setOperations(operations);
        this.SerializerCore = serializer;
        this.setRequestCharge(requestCharge);
        this.retryAfter = retryAfter;
        this.activityId = activityId;
        this.Diagnostics = diagnosticsContext;
        checkNotNull(diagnosticsContext, "expected non-null diagnosticsContext");
        this.DiagnosticsContext = diagnosticsContext;
    }

    public String getActivityId() {
        return activityId;
    }

    /**
     * Gets the number of operation results.
     */
    public int getCount() {
        return this.results == null ? null : this.results.size() != null ? this.results.size() : 0;
    }

    public CosmosDiagnostics getDiagnostics() {
        return Diagnostics;
    }

    public CosmosDiagnosticsContext getDiagnosticsContext() {
        return DiagnosticsContext;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String value) {
        errorMessage = value;
    }

    /**
     * Gets a value indicating whether the batch was processed.
     */
    public boolean getIsSuccessStatusCode() {
        int statusCodeInt = (int) this.getStatusCode();
        return statusCodeInt >= 200 && statusCodeInt <= 299;
    }

    public final IReadOnlyList<ItemBatchOperation> getOperations() {
        return Operations;
    }

    public final void setOperations(IReadOnlyList<ItemBatchOperation> value) {
        Operations = value;
    }

    public double getRequestCharge() {
        return requestCharge;
    }

    public void setRequestCharge(double value) {
        requestCharge = value;
    }

    public Duration getRetryAfter() {
        return retryAfter;
    }

    public CosmosSerializerCore getSerializerCore() {
        return SerializerCore;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public TransactionalBatchResponse setStatusCode(int value) {
        this.statusCode = value;
        return this;
    }

    public int getSubStatusCode() {
        return this.subStatusCode;
    }

    public static Task<TransactionalBatchResponse> FromResponseMessageAsync(
        ResponseMessage responseMessage,
        ServerBatchRequest serverRequest,
        CosmosSerializerCore serializer) {
        return FromResponseMessageAsync(responseMessage, serverRequest, serializer, true);
    }

    //C# TO JAVA CONVERTER TODO TASK: There is no equivalent in Java to the 'async' keyword:
    //ORIGINAL LINE: internal static async Task<TransactionalBatchResponse> FromResponseMessageAsync(ResponseMessage
    // responseMessage, ServerBatchRequest serverRequest, CosmosSerializerCore serializer, bool
    // shouldPromoteOperationStatus = true)
    //C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
    public static Task<TransactionalBatchResponse> FromResponseMessageAsync(ResponseMessage responseMessage,
                                                                            ServerBatchRequest serverRequest,
                                                                            CosmosSerializerCore serializer,
                                                                            boolean shouldPromoteOperationStatus) {
        try (responseMessage) {
            TransactionalBatchResponse response = null;
            if (responseMessage.Content != null) {
                //C# TO JAVA CONVERTER TODO TASK: C# to Java Converter cannot determine whether this System.IO.Stream
                // is input or output:
                Stream content = responseMessage.Content;

                // Shouldn't be the case practically, but handle it for safety.
                if (!responseMessage.Content.CanSeek) {
                    //C# TO JAVA CONVERTER TODO TASK: C# to Java Converter cannot determine whether this System.IO
                    // .MemoryStream is input or output:
                    content = new MemoryStream();
                    //C# TO JAVA CONVERTER TODO TASK: There is no equivalent to 'await' in Java:
                    await responseMessage.Content.CopyToAsync(content);
                }

                if (content.ReadByte() == (int) HybridRowVersion.V1) {
                    content.Position = 0;
                    //C# TO JAVA CONVERTER TODO TASK: There is no equivalent to 'await' in Java:
                    response = await
                    TransactionalBatchResponse.PopulateFromContentAsync(content, responseMessage, serverRequest,
                        serializer, shouldPromoteOperationStatus);

                    if (response == null) {
                        // Convert any payload read failures as InternalServerError
                        response = new TransactionalBatchResponse(HttpStatusCode.InternalServerError,
                            SubStatusCodes.UNKNOWN, ClientResources.ServerResponseDeserializationFailure,
                            responseMessage.Headers.RequestCharge, responseMessage.Headers.RetryAfter,
                            responseMessage.Headers.ActivityId, responseMessage.DiagnosticsContext,
                            serverRequest.getOperations(), serializer);
                    }
                }
            }

            if (response == null) {
                response = new TransactionalBatchResponse(responseMessage.StatusCode,
                    responseMessage.Headers.SubStatusCode, responseMessage.ErrorMessage,
                    responseMessage.Headers.RequestCharge, responseMessage.Headers.RetryAfter,
                    responseMessage.Headers.ActivityId, responseMessage.DiagnosticsContext,
                    serverRequest.getOperations(), serializer);
            }

            if (response.results == null || response.results.size() != serverRequest.getOperations().Count) {
                if (responseMessage.IsSuccessStatusCode) {
                    // Server should be guaranteeing number of results equal to operations when
                    // batch request is successful - so fail as InternalServerError if this is not the case.
                    response = new TransactionalBatchResponse(HttpStatusCode.InternalServerError,
                        SubStatusCodes.Unknown, ClientResources.InvalidServerResponse,
                        responseMessage.Headers.RequestCharge, responseMessage.Headers.RetryAfter,
                        responseMessage.Headers.ActivityId, responseMessage.DiagnosticsContext,
                        serverRequest.getOperations(), serializer);
                }

                // When the overall response status code is TooManyRequests, propagate the RetryAfter into the
                // individual operations.
                int retryAfterMilliseconds = 0;

                if ((int) responseMessage.StatusCode == (int) StatusCodes.TooManyRequests) {
                    tangible.OutObject<Integer> tempOut_retryAfterMilliseconds = new tangible.OutObject<Integer>();
                    String retryAfter;
                    //C# TO JAVA CONVERTER TODO TASK: The following method call contained an unresolved 'out' keyword
                    // - these cannot be converted using the 'OutObject' helper class unless the method is within the
                    // code being modified:
                    if (!responseMessage.Headers.TryGetValue(HttpConstants.HttpHeaders.RetryAfterInMilliseconds,
                        out retryAfter) || retryAfter == null || !tangible.TryParseHelper.tryParseInt(retryAfter,
                        tempOut_retryAfterMilliseconds)) {
                        retryAfterMilliseconds = tempOut_retryAfterMilliseconds.argValue;
                        retryAfterMilliseconds = 0;
                    } else {
                        retryAfterMilliseconds = tempOut_retryAfterMilliseconds.argValue;
                    }
                }

                response.CreateAndPopulateResults(serverRequest.getOperations(), retryAfterMilliseconds);
            }

            return response;
        }
    }

    /**
     * Gets an enumerator over the operation results.
     *
     * @return Enumerator over the operation results.
     */
    public Iterator<TransactionalBatchOperationResult> GetEnumerator() {
        return this.results.iterator();
    }

    /**
     Gets all the Activity IDs associated with the response.

     @return An enumerable that contains the Activity IDs.
     */
    //C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
    //#if INTERNAL
    //C# TO JAVA CONVERTER TODO TASK: Statements that are interrupted by preprocessor statements are not converted by
    // C# to Java Converter:
    public
    //#else
    //C# TO JAVA CONVERTER TODO TASK: Statements that are interrupted by preprocessor statements are not converted by
    // C# to Java Converter:
    internal

    /**
     * <inheritdoc />
     */
    public final Iterator GetEnumerator() {
        return this.iterator();
    }

    /**
     * Gets the result of the operation at the provided index in the batch - the returned result has a Resource of
     * provided type.
     *
     * <typeparam name="T">Type to which the Resource in the operation result needs to be deserialized to, when
     * present.</typeparam>
     *
     * @param index 0-based index of the operation in the batch whose result needs to be returned.
     *
     * @return Result of batch operation that contains a Resource deserialized to specified type.
     */
    public <T> TransactionalBatchOperationResult<T> GetOperationResultAtIndex(int index) {
        TransactionalBatchOperationResult result = this.results.get(index);

        T resource = null;
        if (result.getResourceStream() != null) {
            resource = this.getSerializerCore().<T>FromStream(result.getResourceStream());
        }

        return new TransactionalBatchOperationResult<T>(result, resource);
    }

    /**
     * Disposes the current {@link TransactionalBatchResponse}.
     */
    public final void close() throws IOException {
        this.Dispose(true);
        GC.SuppressFinalize(this);
    }

    /**
     * Gets the result of the operation at the provided index in the batch.
     *
     * @param index 0-based index of the operation in the batch whose result needs to be returned.
     *
     * @return Result of operation at the provided index in the batch.
     */
    public TransactionalBatchOperationResult get(int index) {
        return this.results.get(index);
    }

    /**
     * Disposes the disposable members held by this class.
     *
     * @param disposing Indicates whether to dispose managed resources or not.
     */
    protected void Dispose(boolean disposing) {
        if (disposing && !this.isClosed) {
            this.isClosed = true;
            if (this.getOperations() != null) {
                for (ItemBatchOperation operation : this.getOperations()) {
                    operation.close();
                }

                this.setOperations(null);
            }
        }
    }

    private void CreateAndPopulateResults(IReadOnlyList<ItemBatchOperation> operations) {
        CreateAndPopulateResults(operations, 0);
    }

    //C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
    //ORIGINAL LINE: private void CreateAndPopulateResults(IReadOnlyList<ItemBatchOperation> operations, int
    // retryAfterMilliseconds = 0)
    private void CreateAndPopulateResults(IReadOnlyList<ItemBatchOperation> operations, int retryAfterMilliseconds) {
        this.results = new ArrayList<TransactionalBatchOperationResult>();
        for (int i = 0; i < operations.Count; i++) {
            TransactionalBatchOperationResult tempVar = new TransactionalBatchOperationResult(this.getStatusCode());
            tempVar.setSubStatusCode(this.getSubStatusCode());
            tempVar.setRetryAfter(TimeSpan.FromMilliseconds(retryAfterMilliseconds));
            this.results.add(tempVar);
        }
    }

    //#endif
    private java.lang.Iterable<String> GetActivityIds() {
        //C# TO JAVA CONVERTER TODO TASK: Java does not have an equivalent to the C# 'yield' keyword:
        yield return this.getActivityId();
    }

    //C# TO JAVA CONVERTER TODO TASK: There is no equivalent in Java to the 'async' keyword:
    //ORIGINAL LINE: private static async Task<TransactionalBatchResponse> PopulateFromContentAsync(Stream content,
    // ResponseMessage responseMessage, ServerBatchRequest serverRequest, CosmosSerializerCore serializer, bool
    // shouldPromoteOperationStatus)
    //C# TO JAVA CONVERTER TODO TASK: C# to Java Converter cannot determine whether this System.IO.Stream is input or
    // output:
    private static Task<TransactionalBatchResponse> PopulateFromContentAsync(
        Stream content,
        ResponseMessage responseMessage,
        ServerBatchRequest serverRequest,
        CosmosSerializerCore serializer,
        boolean shouldPromoteOperationStatus) {

        ArrayList<TransactionalBatchOperationResult> results = new ArrayList<TransactionalBatchOperationResult>();

        // content is ensured to be seekable in caller.
        int resizerInitialCapacity = (int) content.Length;

        //C# TO JAVA CONVERTER TODO TASK: There is no equivalent to 'await' in Java:
        //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
        //ORIGINAL LINE: Result res = await content.ReadRecordIOAsync(record =>
        Result res = await content.ReadRecordIOAsync(record ->
        {
            TransactionalBatchOperationResult operationResult;
            Result r = TransactionalBatchOperationResult.ReadOperationResult(record, out operationResult);
            if (r != Result.Success) {
                return r;
            }

            results.add(operationResult);
            return r;
        }, resizer:new MemorySpanResizer<Byte>(resizerInitialCapacity))

        if (res != Result.Success) {
            return null;
        }

        HttpStatusCode responseStatusCode = responseMessage.StatusCode;
        SubStatusCodes responseSubStatusCode = responseMessage.Headers.SubStatusCode;

        // Promote the operation error status as the Batch response error status if we have a MultiStatus response
        // to provide users with status codes they are used to.
        if ((int) responseMessage.StatusCode == (int) StatusCodes.MultiStatus && shouldPromoteOperationStatus) {
            for (TransactionalBatchOperationResult result : results) {
                if ((int) result.getStatusCode() != (int) StatusCodes.FailedDependency) {
                    responseStatusCode = result.getStatusCode();
                    responseSubStatusCode = result.getSubStatusCode();
                    break;
                }
            }
        }

        TransactionalBatchResponse response = new TransactionalBatchResponse(responseStatusCode,
            responseSubStatusCode, responseMessage.ErrorMessage, responseMessage.Headers.RequestCharge,
            responseMessage.Headers.RetryAfter, responseMessage.Headers.ActivityId,
            responseMessage.DiagnosticsContext, serverRequest.getOperations(), serializer);

        response.results = results;
        return response;
    }
}

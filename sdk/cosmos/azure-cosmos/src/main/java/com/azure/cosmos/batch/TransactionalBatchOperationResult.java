// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

/**
 * Represents a result for a specific operation that was part of a {@link TransactionalBatch} request.
 */
public class TransactionalBatchOperationResult {
    /**
     * Gets the cosmos diagnostic information for the current request to Azure Cosmos DB service
     */
    private CosmosDiagnosticsContext DiagnosticsContext;
    /**
     * Gets the entity tag associated with the resource.
     *
     * <value>
     * The entity tag associated with the resource.
     * </value>
     * <p>
     * ETags are used for concurrency checking when updating resources.
     */
    private String ETag;
    /**
     * Request charge in request units for the operation.
     */
    private double RequestCharge;
    /**
     * Gets the content of the resource.
     *
     * <value>
     * The content of the resource as a Stream.
     * </value>
     */
    //C# TO JAVA CONVERTER TODO TASK: C# to Java Converter cannot determine whether this System.IO.Stream is input or
    // output:
    private Stream ResourceStream;
    /**
     * In case the operation is rate limited, indicates the time post which a retry can be attempted.
     */
    private TimeSpan RetryAfter = new TimeSpan();
    /**
     * Gets the completion status of the operation.
     */
    private HttpStatusCode StatusCode;
    /**
     * Gets detail on the completion status of the operation.
     */
    private SubStatusCodes SubStatusCode;

    public TransactionalBatchOperationResult(HttpStatusCode statusCode) {
        this.setStatusCode(statusCode);
    }

    public TransactionalBatchOperationResult(TransactionalBatchOperationResult other) {
        this.setStatusCode(other.getStatusCode());
        this.setSubStatusCode(other.getSubStatusCode());
        this.setETag(other.getETag());
        this.setResourceStream(other.getResourceStream());
        this.setRequestCharge(other.getRequestCharge());
        this.setRetryAfter(other.getRetryAfter());
    }

    /**
     * Initializes a new instance of the {@link TransactionalBatchOperationResult} class.
     */
    protected TransactionalBatchOperationResult() {
    }

    public CosmosDiagnosticsContext getDiagnosticsContext() {
        return DiagnosticsContext;
    }

    public void setDiagnosticsContext(CosmosDiagnosticsContext value) {
        DiagnosticsContext = value;
    }

    public String getETag() {
        return ETag;
    }

    public void setETag(String value) {
        ETag = value;
    }

    /**
     * Gets a value indicating whether the current operation completed successfully.
     */
    public boolean getIsSuccessStatusCode() {
        int statusCodeInt = (int) this.getStatusCode();
        return statusCodeInt >= 200 && statusCodeInt <= 299;
    }

    public double getRequestCharge() {
        return RequestCharge;
    }

    public void setRequestCharge(double value) {
        RequestCharge = value;
    }

    //C# TO JAVA CONVERTER TODO TASK: C# to Java Converter cannot determine whether this System.IO.Stream is input or
    // output:
    public Stream getResourceStream() {
        return ResourceStream;
    }

    //C# TO JAVA CONVERTER TODO TASK: C# to Java Converter cannot determine whether this System.IO.Stream is input or
    // output:
    public void setResourceStream(Stream value) {
        ResourceStream = value;
    }

    public TimeSpan getRetryAfter() {
        return RetryAfter;
    }

    public void setRetryAfter(TimeSpan value) {
        RetryAfter = value;
    }

    public HttpStatusCode getStatusCode() {
        return StatusCode;
    }

    private void setStatusCode(HttpStatusCode value) {
        StatusCode = value;
    }

    public SubStatusCodes getSubStatusCode() {
        return SubStatusCode;
    }

    public void setSubStatusCode(SubStatusCodes value) {
        SubStatusCode = value;
    }

    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
    //ORIGINAL LINE: internal static Result ReadOperationResult(Memory<byte> input, out
    // TransactionalBatchOperationResult batchOperationResult)
    public static Result ReadOperationResult(Memory<Byte> input,
                                             tangible.OutObject<TransactionalBatchOperationResult> batchOperationResult) {
        RowBuffer row = new RowBuffer(input.Length);
        if (!row.ReadFrom(input.Span, HybridRowVersion.V1, BatchSchemaProvider.getBatchLayoutResolver())) {
            batchOperationResult.argValue = null;
            return Result.Failure;
        }

        tangible.RefObject<RowBuffer> tempRef_row = new tangible.RefObject<RowBuffer>(row);
        RowReader reader = new RowReader(tempRef_row);
        row = tempRef_row.argValue;
        tangible.RefObject<RowReader> tempRef_reader = new tangible.RefObject<RowReader>(reader);
        Result result = TransactionalBatchOperationResult.ReadOperationResult(tempRef_reader, batchOperationResult);
        reader = tempRef_reader.argValue;
        if (result != Result.Success) {
            return result;
        }

        // Ensure the mandatory fields were populated
        if (batchOperationResult.argValue.getStatusCode() == null) {
            return Result.Failure;
        }

        return Result.Success;
    }

    public final ResponseMessage ToResponseMessage() {
        Headers headers = new Headers();
        headers.SubStatusCode = this.getSubStatusCode();
        headers.ETag = this.getETag();
        headers.RetryAfter = this.getRetryAfter();
        headers.RequestCharge = this.getRequestCharge();

        //C# TO JAVA CONVERTER TODO TASK: C# to Java Converter could not resolve the named parameters in the following line:
        //ORIGINAL LINE: ResponseMessage responseMessage = new ResponseMessage(statusCode:this.StatusCode, requestMessage: null, errorMessage: null, error: null, headers: headers, diagnostics: this.DiagnosticsContext ?? new CosmosDiagnosticsContext())
        ResponseMessage responseMessage = new ResponseMessage(statusCode:this.getStatusCode(), requestMessage:
        null, errorMessage:null, error:null, headers:headers, diagnostics:
        this.getDiagnosticsContext() != null ? this.getDiagnosticsContext() : new CosmosDiagnosticsContext())
        {
            Content = this.getResourceStream()
        }

        return responseMessage;
    }

    private static Result ReadOperationResult(tangible.RefObject<RowReader> reader,
                                              tangible.OutObject<TransactionalBatchOperationResult> batchOperationResult) {
        batchOperationResult.argValue = new TransactionalBatchOperationResult();
        while (reader.argValue.Read()) {
            Result r;
            switch (reader.argValue.Path) {
                case "statusCode":
                    int statusCode;
                    //C# TO JAVA CONVERTER TODO TASK: The following method call contained an unresolved 'out' keyword
                    // - these cannot be converted using the 'OutObject' helper class unless the method is within the
                    // code being modified:
                    r = reader.argValue.ReadInt32(out statusCode);
                    if (r != Result.Success) {
                        return r;
                    }

                    batchOperationResult.argValue.setStatusCode((HttpStatusCode) statusCode);
                    break;

                case "subStatusCode":
                    int subStatusCode;
                    //C# TO JAVA CONVERTER TODO TASK: The following method call contained an unresolved 'out' keyword
                    // - these cannot be converted using the 'OutObject' helper class unless the method is within the
                    // code being modified:
                    r = reader.argValue.ReadInt32(out subStatusCode);
                    if (r != Result.Success) {
                        return r;
                    }

                    batchOperationResult.argValue.setSubStatusCode((SubStatusCodes) subStatusCode);
                    break;

                case "eTag":
                    String eTag;
                    //C# TO JAVA CONVERTER TODO TASK: The following method call contained an unresolved 'out' keyword
                    // - these cannot be converted using the 'OutObject' helper class unless the method is within the
                    // code being modified:
                    r = reader.argValue.ReadString(out eTag);
                    if (r != Result.Success) {
                        return r;
                    }

                    batchOperationResult.argValue.setETag(eTag);
                    break;

                case "resourceBody":
                    byte[] resourceBody;
                    //C# TO JAVA CONVERTER TODO TASK: The following method call contained an unresolved 'out' keyword
                    // - these cannot be converted using the 'OutObject' helper class unless the method is within the
                    // code being modified:
                    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
                    //ORIGINAL LINE: r = reader.ReadBinary(out byte[] resourceBody);
                    r = reader.argValue.ReadBinary(out resourceBody);
                    if (r != Result.Success) {
                        return r;
                    }

                    //C# TO JAVA CONVERTER TODO TASK: C# to Java Converter could not resolve the named parameters in
                    // the following line:
                    //ORIGINAL LINE: batchOperationResult.ResourceStream = new MemoryStream(buffer: resourceBody,
                    // index: 0, count: resourceBody.Length, writable: false, publiclyVisible: true);
                    //C# TO JAVA CONVERTER TODO TASK: C# to Java Converter cannot determine whether this System.IO
                    // .MemoryStream is input or output:
                    batchOperationResult.argValue.setResourceStream(new MemoryStream(buffer:resourceBody, index:0,
                    count:resourceBody.Length, writable:false, publiclyVisible:true))
                    break;

                case "requestCharge":
                    double requestCharge;
                    //C# TO JAVA CONVERTER TODO TASK: The following method call contained an unresolved 'out' keyword
                    // - these cannot be converted using the 'OutObject' helper class unless the method is within the
                    // code being modified:
                    r = reader.argValue.ReadFloat64(out requestCharge);
                    if (r != Result.Success) {
                        return r;
                    }

                    // Round request charge to 2 decimals on the operation results
                    // similar to how we round them for the full response.
                    batchOperationResult.argValue.setRequestCharge(Double.isNaN(requestCharge) ? Double.NaN :
                        Math.round(requestCharge * Math.pow(10, 2)) / Math.pow(10, 2));
                    break;

                case "retryAfterMilliseconds":
                    int retryAfterMilliseconds;
                    //C# TO JAVA CONVERTER TODO TASK: The following method call contained an unresolved 'out' keyword
                    // - these cannot be converted using the 'OutObject' helper class unless the method is within the
                    // code being modified:
                    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
                    //ORIGINAL LINE: r = reader.ReadUInt32(out uint retryAfterMilliseconds);
                    r = reader.argValue.ReadUInt32(out retryAfterMilliseconds);
                    if (r != Result.Success) {
                        return r;
                    }

                    batchOperationResult.argValue.setRetryAfter(TimeSpan.FromMilliseconds(retryAfterMilliseconds));
                    break;
            }
        }

        return Result.Success;
    }
}

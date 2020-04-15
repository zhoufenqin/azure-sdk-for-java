// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.management.monitor.models;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.Delete;
import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.Headers;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.Patch;
import com.azure.core.annotation.PathParam;
import com.azure.core.annotation.Put;
import com.azure.core.annotation.QueryParam;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.annotation.UnexpectedResponseExceptionType;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.management.monitor.AutoscaleSettingResourcePatch;
import com.azure.management.monitor.ErrorResponseException;
import com.azure.management.resources.fluentcore.collection.InnerSupportsDelete;
import com.azure.management.resources.fluentcore.collection.InnerSupportsGet;
import com.azure.management.resources.fluentcore.collection.InnerSupportsListing;
import reactor.core.publisher.Mono;

/** An instance of this class provides access to all the operations defined in AutoscaleSettings. */
public final class AutoscaleSettingsInner
    implements InnerSupportsGet<AutoscaleSettingResourceInner>,
        InnerSupportsListing<AutoscaleSettingResourceInner>,
        InnerSupportsDelete<Void> {
    /** The proxy service used to perform REST calls. */
    private final AutoscaleSettingsService service;

    /** The service client containing this operation class. */
    private final MonitorClientImpl client;

    /**
     * Initializes an instance of AutoscaleSettingsInner.
     *
     * @param client the instance of the service client containing this operation class.
     */
    AutoscaleSettingsInner(MonitorClientImpl client) {
        this.service =
            RestProxy.create(AutoscaleSettingsService.class, client.getHttpPipeline(), client.getSerializerAdapter());
        this.client = client;
    }

    /**
     * The interface defining all the services for MonitorClientAutoscaleSettings to be used by the proxy service to
     * perform REST calls.
     */
    @Host("{$host}")
    @ServiceInterface(name = "MonitorClientAutosca")
    private interface AutoscaleSettingsService {
        @Headers({"Accept: application/json", "Content-Type: application/json"})
        @Get(
            "/subscriptions/{subscriptionId}/resourcegroups/{resourceGroupName}/providers/microsoft.insights"
                + "/autoscalesettings")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(ErrorResponseException.class)
        Mono<SimpleResponse<AutoscaleSettingResourceCollectionInner>> listByResourceGroup(
            @HostParam("$host") String host,
            @PathParam("resourceGroupName") String resourceGroupName,
            @QueryParam("api-version") String apiVersion,
            @PathParam("subscriptionId") String subscriptionId,
            Context context);

        @Headers({"Accept: application/json", "Content-Type: application/json"})
        @Put(
            "/subscriptions/{subscriptionId}/resourcegroups/{resourceGroupName}/providers/microsoft.insights"
                + "/autoscalesettings/{autoscaleSettingName}")
        @ExpectedResponses({200, 201})
        @UnexpectedResponseExceptionType(ErrorResponseException.class)
        Mono<SimpleResponse<AutoscaleSettingResourceInner>> createOrUpdate(
            @HostParam("$host") String host,
            @PathParam("resourceGroupName") String resourceGroupName,
            @PathParam("autoscaleSettingName") String autoscaleSettingName,
            @QueryParam("api-version") String apiVersion,
            @PathParam("subscriptionId") String subscriptionId,
            @BodyParam("application/json") AutoscaleSettingResourceInner parameters,
            Context context);

        @Headers({"Accept: application/json;q=0.9", "Content-Type: application/json"})
        @Delete(
            "/subscriptions/{subscriptionId}/resourcegroups/{resourceGroupName}/providers/microsoft.insights"
                + "/autoscalesettings/{autoscaleSettingName}")
        @ExpectedResponses({200, 204})
        @UnexpectedResponseExceptionType(ErrorResponseException.class)
        Mono<Response<Void>> delete(
            @HostParam("$host") String host,
            @PathParam("resourceGroupName") String resourceGroupName,
            @PathParam("autoscaleSettingName") String autoscaleSettingName,
            @QueryParam("api-version") String apiVersion,
            @PathParam("subscriptionId") String subscriptionId,
            Context context);

        @Headers({"Accept: application/json", "Content-Type: application/json"})
        @Get(
            "/subscriptions/{subscriptionId}/resourcegroups/{resourceGroupName}/providers/microsoft.insights"
                + "/autoscalesettings/{autoscaleSettingName}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(ErrorResponseException.class)
        Mono<SimpleResponse<AutoscaleSettingResourceInner>> getByResourceGroup(
            @HostParam("$host") String host,
            @PathParam("resourceGroupName") String resourceGroupName,
            @PathParam("autoscaleSettingName") String autoscaleSettingName,
            @QueryParam("api-version") String apiVersion,
            @PathParam("subscriptionId") String subscriptionId,
            Context context);

        @Headers({"Accept: application/json", "Content-Type: application/json"})
        @Patch(
            "/subscriptions/{subscriptionId}/resourcegroups/{resourceGroupName}/providers/microsoft.insights"
                + "/autoscalesettings/{autoscaleSettingName}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(ErrorResponseException.class)
        Mono<SimpleResponse<AutoscaleSettingResourceInner>> update(
            @HostParam("$host") String host,
            @PathParam("subscriptionId") String subscriptionId,
            @PathParam("resourceGroupName") String resourceGroupName,
            @PathParam("autoscaleSettingName") String autoscaleSettingName,
            @QueryParam("api-version") String apiVersion,
            @BodyParam("application/json") AutoscaleSettingResourcePatch autoscaleSettingResource,
            Context context);

        @Headers({"Accept: application/json", "Content-Type: application/json"})
        @Get("/subscriptions/{subscriptionId}/providers/microsoft.insights/autoscalesettings")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(ErrorResponseException.class)
        Mono<SimpleResponse<AutoscaleSettingResourceCollectionInner>> list(
            @HostParam("$host") String host,
            @QueryParam("api-version") String apiVersion,
            @PathParam("subscriptionId") String subscriptionId,
            Context context);

        @Headers({"Accept: application/json", "Content-Type: application/json"})
        @Get("{nextLink}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(ErrorResponseException.class)
        Mono<SimpleResponse<AutoscaleSettingResourceCollectionInner>> listByResourceGroupNext(
            @PathParam(value = "nextLink", encoded = true) String nextLink, Context context);

        @Headers({"Accept: application/json", "Content-Type: application/json"})
        @Get("{nextLink}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(ErrorResponseException.class)
        Mono<SimpleResponse<AutoscaleSettingResourceCollectionInner>> listBySubscriptionNext(
            @PathParam(value = "nextLink", encoded = true) String nextLink, Context context);
    }

    /**
     * Lists the autoscale settings for a resource group.
     *
     * @param resourceGroupName The name of the resource group.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return represents a collection of autoscale setting resources.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<AutoscaleSettingResourceInner>> listByResourceGroupSinglePageAsync(
        String resourceGroupName) {
        final String apiVersion = "2015-04-01";
        return FluxUtil
            .withContext(
                context ->
                    service
                        .listByResourceGroup(
                            this.client.getHost(),
                            resourceGroupName,
                            apiVersion,
                            this.client.getSubscriptionId(),
                            context))
            .<PagedResponse<AutoscaleSettingResourceInner>>map(
                res ->
                    new PagedResponseBase<>(
                        res.getRequest(),
                        res.getStatusCode(),
                        res.getHeaders(),
                        res.getValue().value(),
                        res.getValue().nextLink(),
                        null))
            .subscriberContext(context -> context.putAll(FluxUtil.toReactorContext(this.client.getContext())));
    }

    /**
     * Lists the autoscale settings for a resource group.
     *
     * @param resourceGroupName The name of the resource group.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return represents a collection of autoscale setting resources.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<AutoscaleSettingResourceInner> listByResourceGroupAsync(String resourceGroupName) {
        return new PagedFlux<>(
            () -> listByResourceGroupSinglePageAsync(resourceGroupName),
            nextLink -> listByResourceGroupNextSinglePageAsync(nextLink));
    }

    /**
     * Lists the autoscale settings for a resource group.
     *
     * @param resourceGroupName The name of the resource group.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return represents a collection of autoscale setting resources.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<AutoscaleSettingResourceInner> listByResourceGroup(String resourceGroupName) {
        return new PagedIterable<>(listByResourceGroupAsync(resourceGroupName));
    }

    /**
     * Creates or updates an autoscale setting.
     *
     * @param resourceGroupName The name of the resource group.
     * @param autoscaleSettingName The autoscale setting name.
     * @param parameters The autoscale setting resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the autoscale setting resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<AutoscaleSettingResourceInner>> createOrUpdateWithResponseAsync(
        String resourceGroupName, String autoscaleSettingName, AutoscaleSettingResourceInner parameters) {
        final String apiVersion = "2015-04-01";
        return FluxUtil
            .withContext(
                context ->
                    service
                        .createOrUpdate(
                            this.client.getHost(),
                            resourceGroupName,
                            autoscaleSettingName,
                            apiVersion,
                            this.client.getSubscriptionId(),
                            parameters,
                            context))
            .subscriberContext(context -> context.putAll(FluxUtil.toReactorContext(this.client.getContext())));
    }

    /**
     * Creates or updates an autoscale setting.
     *
     * @param resourceGroupName The name of the resource group.
     * @param autoscaleSettingName The autoscale setting name.
     * @param parameters The autoscale setting resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the autoscale setting resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AutoscaleSettingResourceInner> createOrUpdateAsync(
        String resourceGroupName, String autoscaleSettingName, AutoscaleSettingResourceInner parameters) {
        return createOrUpdateWithResponseAsync(resourceGroupName, autoscaleSettingName, parameters)
            .flatMap(
                (SimpleResponse<AutoscaleSettingResourceInner> res) -> {
                    if (res.getValue() != null) {
                        return Mono.just(res.getValue());
                    } else {
                        return Mono.empty();
                    }
                });
    }

    /**
     * Creates or updates an autoscale setting.
     *
     * @param resourceGroupName The name of the resource group.
     * @param autoscaleSettingName The autoscale setting name.
     * @param parameters The autoscale setting resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the autoscale setting resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AutoscaleSettingResourceInner createOrUpdate(
        String resourceGroupName, String autoscaleSettingName, AutoscaleSettingResourceInner parameters) {
        return createOrUpdateAsync(resourceGroupName, autoscaleSettingName, parameters).block();
    }

    /**
     * Deletes and autoscale setting.
     *
     * @param resourceGroupName The name of the resource group.
     * @param autoscaleSettingName The autoscale setting name.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteWithResponseAsync(String resourceGroupName, String autoscaleSettingName) {
        final String apiVersion = "2015-04-01";
        return FluxUtil
            .withContext(
                context ->
                    service
                        .delete(
                            this.client.getHost(),
                            resourceGroupName,
                            autoscaleSettingName,
                            apiVersion,
                            this.client.getSubscriptionId(),
                            context))
            .subscriberContext(context -> context.putAll(FluxUtil.toReactorContext(this.client.getContext())));
    }

    /**
     * Deletes and autoscale setting.
     *
     * @param resourceGroupName The name of the resource group.
     * @param autoscaleSettingName The autoscale setting name.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteAsync(String resourceGroupName, String autoscaleSettingName) {
        return deleteWithResponseAsync(resourceGroupName, autoscaleSettingName)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Deletes and autoscale setting.
     *
     * @param resourceGroupName The name of the resource group.
     * @param autoscaleSettingName The autoscale setting name.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void delete(String resourceGroupName, String autoscaleSettingName) {
        deleteAsync(resourceGroupName, autoscaleSettingName).block();
    }

    /**
     * Gets an autoscale setting.
     *
     * @param resourceGroupName The name of the resource group.
     * @param autoscaleSettingName The autoscale setting name.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return an autoscale setting.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<AutoscaleSettingResourceInner>> getByResourceGroupWithResponseAsync(
        String resourceGroupName, String autoscaleSettingName) {
        final String apiVersion = "2015-04-01";
        return FluxUtil
            .withContext(
                context ->
                    service
                        .getByResourceGroup(
                            this.client.getHost(),
                            resourceGroupName,
                            autoscaleSettingName,
                            apiVersion,
                            this.client.getSubscriptionId(),
                            context))
            .subscriberContext(context -> context.putAll(FluxUtil.toReactorContext(this.client.getContext())));
    }

    /**
     * Gets an autoscale setting.
     *
     * @param resourceGroupName The name of the resource group.
     * @param autoscaleSettingName The autoscale setting name.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return an autoscale setting.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AutoscaleSettingResourceInner> getByResourceGroupAsync(
        String resourceGroupName, String autoscaleSettingName) {
        return getByResourceGroupWithResponseAsync(resourceGroupName, autoscaleSettingName)
            .flatMap(
                (SimpleResponse<AutoscaleSettingResourceInner> res) -> {
                    if (res.getValue() != null) {
                        return Mono.just(res.getValue());
                    } else {
                        return Mono.empty();
                    }
                });
    }

    /**
     * Gets an autoscale setting.
     *
     * @param resourceGroupName The name of the resource group.
     * @param autoscaleSettingName The autoscale setting name.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return an autoscale setting.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AutoscaleSettingResourceInner getByResourceGroup(String resourceGroupName, String autoscaleSettingName) {
        return getByResourceGroupAsync(resourceGroupName, autoscaleSettingName).block();
    }

    /**
     * Updates an existing AutoscaleSettingsResource. To update other fields use the CreateOrUpdate method.
     *
     * @param resourceGroupName The name of the resource group.
     * @param autoscaleSettingName The autoscale setting name.
     * @param autoscaleSettingResource The autoscale setting object for patch operations.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the autoscale setting resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<AutoscaleSettingResourceInner>> updateWithResponseAsync(
        String resourceGroupName, String autoscaleSettingName, AutoscaleSettingResourcePatch autoscaleSettingResource) {
        final String apiVersion = "2015-04-01";
        return FluxUtil
            .withContext(
                context ->
                    service
                        .update(
                            this.client.getHost(),
                            this.client.getSubscriptionId(),
                            resourceGroupName,
                            autoscaleSettingName,
                            apiVersion,
                            autoscaleSettingResource,
                            context))
            .subscriberContext(context -> context.putAll(FluxUtil.toReactorContext(this.client.getContext())));
    }

    /**
     * Updates an existing AutoscaleSettingsResource. To update other fields use the CreateOrUpdate method.
     *
     * @param resourceGroupName The name of the resource group.
     * @param autoscaleSettingName The autoscale setting name.
     * @param autoscaleSettingResource The autoscale setting object for patch operations.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the autoscale setting resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AutoscaleSettingResourceInner> updateAsync(
        String resourceGroupName, String autoscaleSettingName, AutoscaleSettingResourcePatch autoscaleSettingResource) {
        return updateWithResponseAsync(resourceGroupName, autoscaleSettingName, autoscaleSettingResource)
            .flatMap(
                (SimpleResponse<AutoscaleSettingResourceInner> res) -> {
                    if (res.getValue() != null) {
                        return Mono.just(res.getValue());
                    } else {
                        return Mono.empty();
                    }
                });
    }

    /**
     * Updates an existing AutoscaleSettingsResource. To update other fields use the CreateOrUpdate method.
     *
     * @param resourceGroupName The name of the resource group.
     * @param autoscaleSettingName The autoscale setting name.
     * @param autoscaleSettingResource The autoscale setting object for patch operations.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the autoscale setting resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AutoscaleSettingResourceInner update(
        String resourceGroupName, String autoscaleSettingName, AutoscaleSettingResourcePatch autoscaleSettingResource) {
        return updateAsync(resourceGroupName, autoscaleSettingName, autoscaleSettingResource).block();
    }

    /**
     * Lists the autoscale settings for a subscription.
     *
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return represents a collection of autoscale setting resources.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<AutoscaleSettingResourceInner>> listSinglePageAsync() {
        final String apiVersion = "2015-04-01";
        return FluxUtil
            .withContext(
                context -> service.list(this.client.getHost(), apiVersion, this.client.getSubscriptionId(), context))
            .<PagedResponse<AutoscaleSettingResourceInner>>map(
                res ->
                    new PagedResponseBase<>(
                        res.getRequest(),
                        res.getStatusCode(),
                        res.getHeaders(),
                        res.getValue().value(),
                        res.getValue().nextLink(),
                        null))
            .subscriberContext(context -> context.putAll(FluxUtil.toReactorContext(this.client.getContext())));
    }

    /**
     * Lists the autoscale settings for a subscription.
     *
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return represents a collection of autoscale setting resources.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<AutoscaleSettingResourceInner> listAsync() {
        return new PagedFlux<>(
            () -> listSinglePageAsync(), nextLink -> listBySubscriptionNextSinglePageAsync(nextLink));
    }

    /**
     * Lists the autoscale settings for a subscription.
     *
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return represents a collection of autoscale setting resources.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<AutoscaleSettingResourceInner> list() {
        return new PagedIterable<>(listAsync());
    }

    /**
     * Get the next page of items.
     *
     * @param nextLink The nextLink parameter.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return represents a collection of autoscale setting resources.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<AutoscaleSettingResourceInner>> listByResourceGroupNextSinglePageAsync(String nextLink) {
        return FluxUtil
            .withContext(context -> service.listByResourceGroupNext(nextLink, context))
            .<PagedResponse<AutoscaleSettingResourceInner>>map(
                res ->
                    new PagedResponseBase<>(
                        res.getRequest(),
                        res.getStatusCode(),
                        res.getHeaders(),
                        res.getValue().value(),
                        res.getValue().nextLink(),
                        null))
            .subscriberContext(context -> context.putAll(FluxUtil.toReactorContext(this.client.getContext())));
    }

    /**
     * Get the next page of items.
     *
     * @param nextLink The nextLink parameter.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return represents a collection of autoscale setting resources.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<AutoscaleSettingResourceInner>> listBySubscriptionNextSinglePageAsync(String nextLink) {
        return FluxUtil
            .withContext(context -> service.listBySubscriptionNext(nextLink, context))
            .<PagedResponse<AutoscaleSettingResourceInner>>map(
                res ->
                    new PagedResponseBase<>(
                        res.getRequest(),
                        res.getStatusCode(),
                        res.getHeaders(),
                        res.getValue().value(),
                        res.getValue().nextLink(),
                        null))
            .subscriberContext(context -> context.putAll(FluxUtil.toReactorContext(this.client.getContext())));
    }
}

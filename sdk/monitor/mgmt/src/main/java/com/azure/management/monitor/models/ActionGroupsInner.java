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
import com.azure.core.annotation.Post;
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
import com.azure.management.monitor.ActionGroupPatchBody;
import com.azure.management.monitor.EnableRequest;
import com.azure.management.monitor.ErrorResponseException;
import com.azure.management.resources.fluentcore.collection.InnerSupportsDelete;
import com.azure.management.resources.fluentcore.collection.InnerSupportsGet;
import com.azure.management.resources.fluentcore.collection.InnerSupportsListing;
import reactor.core.publisher.Mono;

/** An instance of this class provides access to all the operations defined in ActionGroups. */
public final class ActionGroupsInner
    implements InnerSupportsGet<ActionGroupResourceInner>,
        InnerSupportsListing<ActionGroupResourceInner>,
        InnerSupportsDelete<Void> {
    /** The proxy service used to perform REST calls. */
    private final ActionGroupsService service;

    /** The service client containing this operation class. */
    private final MonitorClientImpl client;

    /**
     * Initializes an instance of ActionGroupsInner.
     *
     * @param client the instance of the service client containing this operation class.
     */
    ActionGroupsInner(MonitorClientImpl client) {
        this.service =
            RestProxy.create(ActionGroupsService.class, client.getHttpPipeline(), client.getSerializerAdapter());
        this.client = client;
    }

    /**
     * The interface defining all the services for MonitorClientActionGroups to be used by the proxy service to perform
     * REST calls.
     */
    @Host("{$host}")
    @ServiceInterface(name = "MonitorClientActionG")
    private interface ActionGroupsService {
        @Headers({"Accept: application/json", "Content-Type: application/json"})
        @Put(
            "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/microsoft.insights"
                + "/actionGroups/{actionGroupName}")
        @ExpectedResponses({200, 201})
        @UnexpectedResponseExceptionType(ErrorResponseException.class)
        Mono<SimpleResponse<ActionGroupResourceInner>> createOrUpdate(
            @HostParam("$host") String host,
            @PathParam("resourceGroupName") String resourceGroupName,
            @PathParam("actionGroupName") String actionGroupName,
            @PathParam("subscriptionId") String subscriptionId,
            @QueryParam("api-version") String apiVersion,
            @BodyParam("application/json") ActionGroupResourceInner actionGroup,
            Context context);

        @Headers({"Accept: application/json", "Content-Type: application/json"})
        @Get(
            "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/microsoft.insights"
                + "/actionGroups/{actionGroupName}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(ErrorResponseException.class)
        Mono<SimpleResponse<ActionGroupResourceInner>> getByResourceGroup(
            @HostParam("$host") String host,
            @PathParam("resourceGroupName") String resourceGroupName,
            @PathParam("actionGroupName") String actionGroupName,
            @PathParam("subscriptionId") String subscriptionId,
            @QueryParam("api-version") String apiVersion,
            Context context);

        @Headers({"Accept: application/json;q=0.9", "Content-Type: application/json"})
        @Delete(
            "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/microsoft.insights"
                + "/actionGroups/{actionGroupName}")
        @ExpectedResponses({200, 204})
        @UnexpectedResponseExceptionType(ErrorResponseException.class)
        Mono<Response<Void>> delete(
            @HostParam("$host") String host,
            @PathParam("resourceGroupName") String resourceGroupName,
            @PathParam("actionGroupName") String actionGroupName,
            @PathParam("subscriptionId") String subscriptionId,
            @QueryParam("api-version") String apiVersion,
            Context context);

        @Headers({"Accept: application/json", "Content-Type: application/json"})
        @Patch(
            "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/microsoft.insights"
                + "/actionGroups/{actionGroupName}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(ErrorResponseException.class)
        Mono<SimpleResponse<ActionGroupResourceInner>> update(
            @HostParam("$host") String host,
            @PathParam("subscriptionId") String subscriptionId,
            @PathParam("resourceGroupName") String resourceGroupName,
            @PathParam("actionGroupName") String actionGroupName,
            @QueryParam("api-version") String apiVersion,
            @BodyParam("application/json") ActionGroupPatchBody actionGroupPatch,
            Context context);

        @Headers({"Accept: application/json", "Content-Type: application/json"})
        @Get("/subscriptions/{subscriptionId}/providers/microsoft.insights/actionGroups")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(ErrorResponseException.class)
        Mono<SimpleResponse<ActionGroupListInner>> list(
            @HostParam("$host") String host,
            @PathParam("subscriptionId") String subscriptionId,
            @QueryParam("api-version") String apiVersion,
            Context context);

        @Headers({"Accept: application/json", "Content-Type: application/json"})
        @Get(
            "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/microsoft.insights"
                + "/actionGroups")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(ErrorResponseException.class)
        Mono<SimpleResponse<ActionGroupListInner>> listByResourceGroup(
            @HostParam("$host") String host,
            @PathParam("resourceGroupName") String resourceGroupName,
            @PathParam("subscriptionId") String subscriptionId,
            @QueryParam("api-version") String apiVersion,
            Context context);

        @Headers({"Accept: application/json;q=0.9", "Content-Type: application/json"})
        @Post(
            "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/microsoft.insights"
                + "/actionGroups/{actionGroupName}/subscribe")
        @ExpectedResponses({200, 409})
        @UnexpectedResponseExceptionType(ErrorResponseException.class)
        Mono<Response<Void>> enableReceiver(
            @HostParam("$host") String host,
            @PathParam("resourceGroupName") String resourceGroupName,
            @PathParam("actionGroupName") String actionGroupName,
            @PathParam("subscriptionId") String subscriptionId,
            @QueryParam("api-version") String apiVersion,
            @BodyParam("application/json") EnableRequest enableRequest,
            Context context);
    }

    /**
     * Create a new action group or update an existing one.
     *
     * @param resourceGroupName The name of the resource group.
     * @param actionGroupName The name of the action group.
     * @param actionGroup An action group resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return an action group resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<ActionGroupResourceInner>> createOrUpdateWithResponseAsync(
        String resourceGroupName, String actionGroupName, ActionGroupResourceInner actionGroup) {
        final String apiVersion = "2019-06-01";
        return FluxUtil
            .withContext(
                context ->
                    service
                        .createOrUpdate(
                            this.client.getHost(),
                            resourceGroupName,
                            actionGroupName,
                            this.client.getSubscriptionId(),
                            apiVersion,
                            actionGroup,
                            context))
            .subscriberContext(context -> context.putAll(FluxUtil.toReactorContext(this.client.getContext())));
    }

    /**
     * Create a new action group or update an existing one.
     *
     * @param resourceGroupName The name of the resource group.
     * @param actionGroupName The name of the action group.
     * @param actionGroup An action group resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return an action group resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ActionGroupResourceInner> createOrUpdateAsync(
        String resourceGroupName, String actionGroupName, ActionGroupResourceInner actionGroup) {
        return createOrUpdateWithResponseAsync(resourceGroupName, actionGroupName, actionGroup)
            .flatMap(
                (SimpleResponse<ActionGroupResourceInner> res) -> {
                    if (res.getValue() != null) {
                        return Mono.just(res.getValue());
                    } else {
                        return Mono.empty();
                    }
                });
    }

    /**
     * Create a new action group or update an existing one.
     *
     * @param resourceGroupName The name of the resource group.
     * @param actionGroupName The name of the action group.
     * @param actionGroup An action group resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return an action group resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ActionGroupResourceInner createOrUpdate(
        String resourceGroupName, String actionGroupName, ActionGroupResourceInner actionGroup) {
        return createOrUpdateAsync(resourceGroupName, actionGroupName, actionGroup).block();
    }

    /**
     * Get an action group.
     *
     * @param resourceGroupName The name of the resource group.
     * @param actionGroupName The name of the action group.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return an action group.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<ActionGroupResourceInner>> getByResourceGroupWithResponseAsync(
        String resourceGroupName, String actionGroupName) {
        final String apiVersion = "2019-06-01";
        return FluxUtil
            .withContext(
                context ->
                    service
                        .getByResourceGroup(
                            this.client.getHost(),
                            resourceGroupName,
                            actionGroupName,
                            this.client.getSubscriptionId(),
                            apiVersion,
                            context))
            .subscriberContext(context -> context.putAll(FluxUtil.toReactorContext(this.client.getContext())));
    }

    /**
     * Get an action group.
     *
     * @param resourceGroupName The name of the resource group.
     * @param actionGroupName The name of the action group.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return an action group.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ActionGroupResourceInner> getByResourceGroupAsync(String resourceGroupName, String actionGroupName) {
        return getByResourceGroupWithResponseAsync(resourceGroupName, actionGroupName)
            .flatMap(
                (SimpleResponse<ActionGroupResourceInner> res) -> {
                    if (res.getValue() != null) {
                        return Mono.just(res.getValue());
                    } else {
                        return Mono.empty();
                    }
                });
    }

    /**
     * Get an action group.
     *
     * @param resourceGroupName The name of the resource group.
     * @param actionGroupName The name of the action group.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return an action group.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ActionGroupResourceInner getByResourceGroup(String resourceGroupName, String actionGroupName) {
        return getByResourceGroupAsync(resourceGroupName, actionGroupName).block();
    }

    /**
     * Delete an action group.
     *
     * @param resourceGroupName The name of the resource group.
     * @param actionGroupName The name of the action group.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteWithResponseAsync(String resourceGroupName, String actionGroupName) {
        final String apiVersion = "2019-06-01";
        return FluxUtil
            .withContext(
                context ->
                    service
                        .delete(
                            this.client.getHost(),
                            resourceGroupName,
                            actionGroupName,
                            this.client.getSubscriptionId(),
                            apiVersion,
                            context))
            .subscriberContext(context -> context.putAll(FluxUtil.toReactorContext(this.client.getContext())));
    }

    /**
     * Delete an action group.
     *
     * @param resourceGroupName The name of the resource group.
     * @param actionGroupName The name of the action group.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteAsync(String resourceGroupName, String actionGroupName) {
        return deleteWithResponseAsync(resourceGroupName, actionGroupName)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Delete an action group.
     *
     * @param resourceGroupName The name of the resource group.
     * @param actionGroupName The name of the action group.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void delete(String resourceGroupName, String actionGroupName) {
        deleteAsync(resourceGroupName, actionGroupName).block();
    }

    /**
     * Updates an existing action group's tags. To update other fields use the CreateOrUpdate method.
     *
     * @param resourceGroupName The name of the resource group.
     * @param actionGroupName The name of the action group.
     * @param actionGroupPatch An action group object for the body of patch operations.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return an action group resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<ActionGroupResourceInner>> updateWithResponseAsync(
        String resourceGroupName, String actionGroupName, ActionGroupPatchBody actionGroupPatch) {
        final String apiVersion = "2019-06-01";
        return FluxUtil
            .withContext(
                context ->
                    service
                        .update(
                            this.client.getHost(),
                            this.client.getSubscriptionId(),
                            resourceGroupName,
                            actionGroupName,
                            apiVersion,
                            actionGroupPatch,
                            context))
            .subscriberContext(context -> context.putAll(FluxUtil.toReactorContext(this.client.getContext())));
    }

    /**
     * Updates an existing action group's tags. To update other fields use the CreateOrUpdate method.
     *
     * @param resourceGroupName The name of the resource group.
     * @param actionGroupName The name of the action group.
     * @param actionGroupPatch An action group object for the body of patch operations.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return an action group resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ActionGroupResourceInner> updateAsync(
        String resourceGroupName, String actionGroupName, ActionGroupPatchBody actionGroupPatch) {
        return updateWithResponseAsync(resourceGroupName, actionGroupName, actionGroupPatch)
            .flatMap(
                (SimpleResponse<ActionGroupResourceInner> res) -> {
                    if (res.getValue() != null) {
                        return Mono.just(res.getValue());
                    } else {
                        return Mono.empty();
                    }
                });
    }

    /**
     * Updates an existing action group's tags. To update other fields use the CreateOrUpdate method.
     *
     * @param resourceGroupName The name of the resource group.
     * @param actionGroupName The name of the action group.
     * @param actionGroupPatch An action group object for the body of patch operations.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return an action group resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ActionGroupResourceInner update(
        String resourceGroupName, String actionGroupName, ActionGroupPatchBody actionGroupPatch) {
        return updateAsync(resourceGroupName, actionGroupName, actionGroupPatch).block();
    }

    /**
     * Get a list of all action groups in a subscription.
     *
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of all action groups in a subscription.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<ActionGroupResourceInner>> listSinglePageAsync() {
        final String apiVersion = "2019-06-01";
        return FluxUtil
            .withContext(
                context -> service.list(this.client.getHost(), this.client.getSubscriptionId(), apiVersion, context))
            .<PagedResponse<ActionGroupResourceInner>>map(
                res ->
                    new PagedResponseBase<>(
                        res.getRequest(), res.getStatusCode(), res.getHeaders(), res.getValue().value(), null, null))
            .subscriberContext(context -> context.putAll(FluxUtil.toReactorContext(this.client.getContext())));
    }

    /**
     * Get a list of all action groups in a subscription.
     *
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of all action groups in a subscription.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ActionGroupResourceInner> listAsync() {
        return new PagedFlux<>(() -> listSinglePageAsync());
    }

    /**
     * Get a list of all action groups in a subscription.
     *
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of all action groups in a subscription.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ActionGroupResourceInner> list() {
        return new PagedIterable<>(listAsync());
    }

    /**
     * Get a list of all action groups in a resource group.
     *
     * @param resourceGroupName The name of the resource group.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of all action groups in a resource group.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<ActionGroupResourceInner>> listByResourceGroupSinglePageAsync(String resourceGroupName) {
        final String apiVersion = "2019-06-01";
        return FluxUtil
            .withContext(
                context ->
                    service
                        .listByResourceGroup(
                            this.client.getHost(),
                            resourceGroupName,
                            this.client.getSubscriptionId(),
                            apiVersion,
                            context))
            .<PagedResponse<ActionGroupResourceInner>>map(
                res ->
                    new PagedResponseBase<>(
                        res.getRequest(), res.getStatusCode(), res.getHeaders(), res.getValue().value(), null, null))
            .subscriberContext(context -> context.putAll(FluxUtil.toReactorContext(this.client.getContext())));
    }

    /**
     * Get a list of all action groups in a resource group.
     *
     * @param resourceGroupName The name of the resource group.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of all action groups in a resource group.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ActionGroupResourceInner> listByResourceGroupAsync(String resourceGroupName) {
        return new PagedFlux<>(() -> listByResourceGroupSinglePageAsync(resourceGroupName));
    }

    /**
     * Get a list of all action groups in a resource group.
     *
     * @param resourceGroupName The name of the resource group.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of all action groups in a resource group.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ActionGroupResourceInner> listByResourceGroup(String resourceGroupName) {
        return new PagedIterable<>(listByResourceGroupAsync(resourceGroupName));
    }

    /**
     * Enable a receiver in an action group. This changes the receiver's status from Disabled to Enabled. This operation
     * is only supported for Email or SMS receivers.
     *
     * @param resourceGroupName The name of the resource group.
     * @param actionGroupName The name of the action group.
     * @param receiverName The name of the receiver to resubscribe.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> enableReceiverWithResponseAsync(
        String resourceGroupName, String actionGroupName, String receiverName) {
        final String apiVersion = "2019-06-01";
        EnableRequest enableRequest = new EnableRequest();
        enableRequest.withReceiverName(receiverName);
        return FluxUtil
            .withContext(
                context ->
                    service
                        .enableReceiver(
                            this.client.getHost(),
                            resourceGroupName,
                            actionGroupName,
                            this.client.getSubscriptionId(),
                            apiVersion,
                            enableRequest,
                            context))
            .subscriberContext(context -> context.putAll(FluxUtil.toReactorContext(this.client.getContext())));
    }

    /**
     * Enable a receiver in an action group. This changes the receiver's status from Disabled to Enabled. This operation
     * is only supported for Email or SMS receivers.
     *
     * @param resourceGroupName The name of the resource group.
     * @param actionGroupName The name of the action group.
     * @param receiverName The name of the receiver to resubscribe.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> enableReceiverAsync(String resourceGroupName, String actionGroupName, String receiverName) {
        return enableReceiverWithResponseAsync(resourceGroupName, actionGroupName, receiverName)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Enable a receiver in an action group. This changes the receiver's status from Disabled to Enabled. This operation
     * is only supported for Email or SMS receivers.
     *
     * @param resourceGroupName The name of the resource group.
     * @param actionGroupName The name of the action group.
     * @param receiverName The name of the receiver to resubscribe.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void enableReceiver(String resourceGroupName, String actionGroupName, String receiverName) {
        enableReceiverAsync(resourceGroupName, actionGroupName, receiverName).block();
    }
}

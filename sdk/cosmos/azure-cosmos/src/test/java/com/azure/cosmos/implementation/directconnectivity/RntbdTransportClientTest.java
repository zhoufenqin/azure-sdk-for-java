// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.BadRequestException;
import com.azure.cosmos.ConflictException;
import com.azure.cosmos.CosmosClientException;
import com.azure.cosmos.ForbiddenException;
import com.azure.cosmos.GoneException;
import com.azure.cosmos.InternalServerErrorException;
import com.azure.cosmos.InvalidPartitionException;
import com.azure.cosmos.LockedException;
import com.azure.cosmos.MethodNotAllowedException;
import com.azure.cosmos.NotFoundException;
import com.azure.cosmos.PartitionIsMigratingException;
import com.azure.cosmos.PartitionKeyRangeGoneException;
import com.azure.cosmos.PartitionKeyRangeIsSplittingException;
import com.azure.cosmos.PreconditionFailedException;
import com.azure.cosmos.RequestEntityTooLargeException;
import com.azure.cosmos.RequestRateTooLargeException;
import com.azure.cosmos.RequestTimeoutException;
import com.azure.cosmos.RetryWithException;
import com.azure.cosmos.ServiceUnavailableException;
import com.azure.cosmos.UnauthorizedException;
import com.azure.cosmos.implementation.FailureValidator;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.UserAgentContainer;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdClientChannelHealthChecker;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdContext;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdContextNegotiator;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdContextRequest;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpoint;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestArgs;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestEncoder;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestManager;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestRecord;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestTimer;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdResponse;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdResponseDecoder;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdUUID;
import com.google.common.collect.ImmutableMap;
import io.micrometer.core.instrument.Tag;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.reactivex.subscribers.TestSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static com.azure.cosmos.implementation.HttpConstants.HttpHeaders;
import static com.azure.cosmos.implementation.HttpConstants.SubStatusCodes;
import static com.google.common.base.Strings.lenientFormat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertTrue;

public final class RntbdTransportClientTest {

    private static final int LSN = 5;
    private static final ByteBuf NO_CONTENT = Unpooled.EMPTY_BUFFER;
    private static final String PARTITION_KEY_RANGE_ID = "3";
    private static final Uri PHYSICAL_ADDRESS = new Uri("rntbd://host:10251/replica-path/");
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

    private static final Logger logger = LoggerFactory.getLogger(RntbdTransportClientTest.class);

    @DataProvider(name = "fromMockedNetworkFailureToExpectedCosmosClientException")
    public Object[][] fromMockedNetworkFailureToExpectedCosmosClientException() {

        return new Object[][] {
        };
    }

    @DataProvider(name = "fromMockedRntbdResponseToExpectedCosmosClientException")
    public Object[][] fromMockedRntbdResponseToExpectedCosmosClientException() {

        return new Object[][] {
            {
                // 1 BadRequestException

                BadRequestException.class, RxDocumentServiceRequest.create(
                OperationType.Read,
                ResourceType.DocumentCollection,
                "/dbs/db/colls/col",
                ImmutableMap.of(
                    HttpHeaders.LSN, Integer.toString(LSN),
                    HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID
                )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    400,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(LSN),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID,
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(1L)
                    ),
                    NO_CONTENT)
            },
            {
                // 2 UnauthorizedException

                UnauthorizedException.class, RxDocumentServiceRequest.create(
                OperationType.Read,
                ResourceType.DocumentCollection,
                "/dbs/db/colls/col",
                ImmutableMap.of(
                    HttpHeaders.LSN, Integer.toString(LSN),
                    HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID
                )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    401,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(LSN),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID,
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(2L)
                    ),
                    NO_CONTENT)
            },
            {
                // 3 ForbiddenException

                ForbiddenException.class, RxDocumentServiceRequest.create(
                OperationType.Read,
                ResourceType.DocumentCollection,
                "/dbs/db/colls/col",
                ImmutableMap.of(
                    HttpHeaders.LSN, Integer.toString(LSN),
                    HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID
                )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    403,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(LSN),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID,
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(3L)
                    ),
                    NO_CONTENT)
            },
            {
                // 4 NotFoundException

                NotFoundException.class, RxDocumentServiceRequest.create(
                OperationType.Read,
                ResourceType.DocumentCollection,
                "/dbs/db/colls/col",
                ImmutableMap.of(
                    HttpHeaders.LSN, Integer.toString(LSN),
                    HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID
                )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    404,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(LSN),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID,
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(4L)
                    ),
                    NO_CONTENT)
            }, {
            // 5 MethodNotAllowedException

            MethodNotAllowedException.class, RxDocumentServiceRequest.create(
            OperationType.Read,
            ResourceType.DocumentCollection,
            "/dbs/db/colls/col",
            ImmutableMap.of(
                HttpHeaders.LSN, Integer.toString(LSN),
                HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID
            )),
            new RntbdResponse(
                RntbdUUID.EMPTY,
                405,
                ImmutableMap.of(
                    HttpHeaders.LSN, Integer.toString(LSN),
                    HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID,
                    HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(5L)
                ),
                NO_CONTENT)
            },
            {
                // 6 RequestTimeoutException
                RequestTimeoutException.class, RxDocumentServiceRequest.create(
                OperationType.Read,
                ResourceType.DocumentCollection,
                "/dbs/db/colls/col",
                ImmutableMap.of(
                    HttpHeaders.LSN, Integer.toString(LSN),
                    HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID
                )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    408,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(LSN),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID,
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(6L)
                    ),
                    NO_CONTENT)
            },
            {
                // 7 ConflictException
                ConflictException.class, RxDocumentServiceRequest.create(
                OperationType.Read,
                ResourceType.DocumentCollection,
                "/dbs/db/colls/col",
                ImmutableMap.of(
                    HttpHeaders.LSN, Integer.toString(LSN),
                    HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID
                )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    409,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(LSN),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID,
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(7L)
                    ),
                    NO_CONTENT)
            },
            {
                // 8 InvalidPartitionException

                InvalidPartitionException.class, RxDocumentServiceRequest.create(
                OperationType.Read,
                ResourceType.DocumentCollection,
                "/dbs/db/colls/col",
                ImmutableMap.of(
                    HttpHeaders.LSN, Integer.toString(LSN),
                    HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID
                )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    410,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(LSN),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID,
                        HttpHeaders.SUB_STATUS, Integer.toString(SubStatusCodes.NAME_CACHE_IS_STALE),
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(8L)
                    ),
                    NO_CONTENT)
            },
            {
                // 9 PartitionKeyRangeGoneException

                PartitionKeyRangeGoneException.class, RxDocumentServiceRequest.create(
                OperationType.Read,
                ResourceType.DocumentCollection,
                "/dbs/db/colls/col",
                ImmutableMap.of(
                    HttpHeaders.LSN, Integer.toString(LSN),
                    HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID
                )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    410,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(LSN),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID,
                        HttpHeaders.SUB_STATUS, Integer.toString(SubStatusCodes.PARTITION_KEY_RANGE_GONE),
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(9L)
                    ),
                    NO_CONTENT)
            },
            {
                // 10 PartitionKeyRangeIsSplittingException

                PartitionKeyRangeIsSplittingException.class, RxDocumentServiceRequest.create(
                OperationType.Read,
                ResourceType.DocumentCollection,
                "/dbs/db/colls/col",
                ImmutableMap.of(
                    HttpHeaders.LSN, Integer.toString(LSN),
                    HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID
                )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    410,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(LSN),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID,
                        HttpHeaders.SUB_STATUS, Integer.toString(SubStatusCodes.COMPLETING_SPLIT),
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(10L)
                    ),
                    NO_CONTENT)
            },
            {
                // 11 PartitionIsMigratingException

                PartitionIsMigratingException.class, RxDocumentServiceRequest.create(
                OperationType.Read,
                ResourceType.DocumentCollection,
                "/dbs/db/colls/col",
                ImmutableMap.of(
                    HttpHeaders.LSN, Integer.toString(LSN),
                    HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID
                )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    410,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(LSN),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID,
                        HttpHeaders.SUB_STATUS, Integer.toString(SubStatusCodes.COMPLETING_PARTITION_MIGRATION),
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(11L)
                    ),
                    NO_CONTENT)
            },
            {
                // 12 GoneException

                GoneException.class, RxDocumentServiceRequest.create(
                OperationType.Read,
                ResourceType.DocumentCollection,
                "/dbs/db/colls/col",
                ImmutableMap.of(
                    HttpHeaders.LSN, Integer.toString(LSN),
                    HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID
                )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    410,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(LSN),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID,
                        HttpHeaders.SUB_STATUS, String.valueOf(SubStatusCodes.UNKNOWN),
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(12L)
                    ),
                    NO_CONTENT)
            },
            {
                // 13 PreconditionFailedException

                PreconditionFailedException.class, RxDocumentServiceRequest.create(
                OperationType.Read,
                ResourceType.DocumentCollection,
                "/dbs/db/colls/col",
                ImmutableMap.of(
                    HttpHeaders.LSN, Integer.toString(LSN),
                    HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID
                )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    412,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(LSN),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID,
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(13L)
                    ),
                    NO_CONTENT)
            },
            {
                // 14 RequestEntityTooLargeException

                RequestEntityTooLargeException.class, RxDocumentServiceRequest.create(
                OperationType.Read,
                ResourceType.DocumentCollection,
                "/dbs/db/colls/col",
                ImmutableMap.of(
                    HttpHeaders.LSN, Integer.toString(LSN),
                    HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID
                )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    413,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(LSN),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID,
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(14L)
                    ),
                    NO_CONTENT)
            },
            {
                // 15 LockedException

                LockedException.class, RxDocumentServiceRequest.create(
                OperationType.Read,
                ResourceType.DocumentCollection,
                "/dbs/db/colls/col",
                ImmutableMap.of(
                    HttpHeaders.LSN, Integer.toString(LSN),
                    HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID
                )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    423,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(LSN),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID,
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(15L)
                    ),
                    NO_CONTENT)
            },
            {
                // 16 RequestRateTooLargeException

                RequestRateTooLargeException.class, RxDocumentServiceRequest.create(
                OperationType.Read,
                ResourceType.DocumentCollection,
                "/dbs/db/colls/col",
                ImmutableMap.of(
                    HttpHeaders.LSN, Integer.toString(LSN),
                    HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID
                )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    429,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(LSN),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID,
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(16L)
                    ),
                    NO_CONTENT)
            },
            {
                // 17 RetryWithException

                RetryWithException.class, RxDocumentServiceRequest.create(
                OperationType.Read,
                ResourceType.DocumentCollection,
                "/dbs/db/colls/col",
                ImmutableMap.of(
                    HttpHeaders.LSN, Integer.toString(LSN),
                    HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID
                )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    449,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(LSN),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID,
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(17L)
                    ),
                    NO_CONTENT)
            },
            {
                // 18 InternalServerErrorException

                InternalServerErrorException.class, RxDocumentServiceRequest.create(
                OperationType.Read,
                ResourceType.DocumentCollection,
                "/dbs/db/colls/col",
                ImmutableMap.of(
                    HttpHeaders.LSN, Integer.toString(LSN),
                    HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID
                )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    500,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(LSN),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID,
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(18L)
                    ),
                    NO_CONTENT)
            },
            {
                // 19 ServiceUnavailableException

                ServiceUnavailableException.class, RxDocumentServiceRequest.create(
                OperationType.Read,
                ResourceType.DocumentCollection,
                "/dbs/db/colls/col",
                ImmutableMap.of(
                    HttpHeaders.LSN, Integer.toString(LSN),
                    HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID
                )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    503,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(LSN),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID,
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(19L)
                    ),
                    NO_CONTENT)
            },
        };
    }

    /**
     * Validates the error handling behavior of {@link RntbdTransportClient} for network failures
     * <p>
     * These are the exceptions that cannot be derived from server responses. They are mapped from Netty channel
     * failures simulated by {@link FakeChannel}.
     *
     * @param builder   A feature validator builder to confirm that response is correctly mapped to an exception
     * @param request   An RNTBD request instance
     * @param exception An exception mapping
     */
    @Test(enabled = false, groups = { "unit" }, dataProvider = "fromMockedNetworkFailureToExpectedCosmosClientException")
    public void verifyNetworkFailure(
        final FailureValidator.Builder builder,
        final RxDocumentServiceRequest request,
        final CosmosClientException exception
    ) {
        // TODO: DANOBLE: Implement RntbdTransportClientTest.verifyNetworkFailure
        //  Links:
        //  https://msdata.visualstudio.com/CosmosDB/_workitems/edit/378750
        throw new UnsupportedOperationException("TODO: DANOBLE: Implement this test");
    }

    @Test(enabled = true, groups = "unit")
    public void verifyRequestCancellation(Method method) {

        final UserAgentContainer userAgent = new UserAgentContainer();
        final Duration requestTimeout = Duration.ofSeconds(5);

        final RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            OperationType.Read,
            ResourceType.DocumentCollection,
            "/dbs/db/colls/col",
            ImmutableMap.of(
                HttpHeaders.LSN, Integer.toString(LSN),
                HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID
            ));

        final RntbdResponse response = new RntbdResponse(
            RntbdUUID.EMPTY,
            400,
            ImmutableMap.of(
                HttpHeaders.LSN, Integer.toString(LSN),
                HttpHeaders.PARTITION_KEY_RANGE_ID, PARTITION_KEY_RANGE_ID,
                HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(1L)
            ),
            NO_CONTENT);

        try (RntbdTransportClient client = getRntbdTransportClientUnderTest(userAgent, requestTimeout, response)) {

            final AtomicReference<StoreResponse> storeResponse = new AtomicReference<>();
            final AtomicReference<Throwable> throwable = new AtomicReference<>();
            final AtomicBoolean cancelled = new AtomicBoolean();
            final Mono<StoreResponse> storeResponseMono;

            try {
                storeResponseMono = client.invokeStoreAsync(PHYSICAL_ADDRESS, request).doOnCancel(() -> {
                    logger.info("{}: request cancelled as expected", method.getName());
                    cancelled.set(true);
                }).doOnError(error -> {
                    logger.info("{}: unexpected {}", method.getName(), error.getClass().getSimpleName());
                    throwable.set(error);
                }).doOnSuccess(result -> {
                    logger.info("{}: unexpected {}}", method.getName(), result.getClass().getSimpleName());
                    storeResponse.set(result);
                });
            } catch (final Throwable error) {
                throw new AssertionError(lenientFormat("%s: %s", error.getClass(), error));
            }

            StepVerifier.create(storeResponseMono).thenCancel().verify();

            assertThat(cancelled.get()).isTrue();
            assertThat(throwable.get()).isNull();
            assertThat(storeResponse.get()).isNull();
        }
    }

    /**
     * Validates the error handling behavior of the {@link RntbdTransportClient} for HTTP status codes >= 400
     *
     * @param expectedError a {@link CosmosClientException} specifying the kind of request failure expected.
     * @param request an RNTBD request instance
     * @param response the RNTBD response instance to be returned as a result of the request
     */
    @Test(enabled = true, groups = "unit", dataProvider = "fromMockedRntbdResponseToExpectedCosmosClientException")
    public void verifyRequestFailures(
        final Class<? extends CosmosClientException> expectedError,
        final RxDocumentServiceRequest request,
        final RntbdResponse response) {

        final UserAgentContainer userAgent = new UserAgentContainer();
        final Duration requestTimeout = Duration.ofSeconds(10);

        try (RntbdTransportClient client = getRntbdTransportClientUnderTest(userAgent, requestTimeout, response)) {

            final Mono<StoreResponse> responseMono;

            try {
                responseMono = client.invokeStoreAsync(PHYSICAL_ADDRESS, request);
            } catch (final Exception error) {
                throw new AssertionError(String.format("%s: %s", error.getClass(), error));
            }

            this.validateFailure(responseMono, expectedError);
        }
    }

    private static RntbdTransportClient getRntbdTransportClientUnderTest(
        final UserAgentContainer userAgent, final Duration requestTimeout, final RntbdResponse expected) {

        final RntbdTransportClient.Options options = new RntbdTransportClient.Options.Builder(requestTimeout)
            .userAgent(userAgent)
            .build();

        final SslContext sslContext;

        try {
            sslContext = SslContextBuilder.forClient().build();
        } catch (final Exception error) {
            throw new AssertionError(lenientFormat("%s: %s", error.getClass(), error.getMessage()));
        }

        return new RntbdTransportClient(new FakeEndpoint.Provider(options, sslContext, expected));
    }

    private void validateFailure(
        final Mono<StoreResponse> responseMono,
        final Class<? extends CosmosClientException> expectedError) {
        StepVerifier.create(responseMono).expectErrorMatches(error -> error.getClass().equals(expectedError)).verify();
    }

    // region Types

    private static final class FakeChannel extends EmbeddedChannel {

        private static final ServerProperties SERVER_PROPERTIES = new ServerProperties("agent", "4.0.0");

        FakeChannel(final RntbdResponse[] responses, final ChannelHandler... handlers) {
            super(handlers);
            for (RntbdResponse response : responses) {
                assertThat(super.inboundMessages().offer(response));
            }
        }

        @Override
        public boolean writeInbound(Object... msgs) {
            logger.info("writeInbound");
            return super.writeInbound(msgs);
        }

        @Override
        protected void doClose() throws Exception {
            logger.info("doClose");
            super.doClose();
        }

        @Override
        protected void handleInboundMessage(final Object message) {
            logger.info("handleInboundMessage");
            super.handleInboundMessage(message);
        }

        @Override
        public Channel flush() {
            logger.info("flush");
            return super.flush();
        }

        @Override
        protected void handleOutboundMessage(final Object message) {

            // This is the end of the outbound pipeline and so we can do what we wish with the outbound message

            logger.info("handleOutboundMessage");
            assertTrue(message instanceof ByteBuf);

            final ByteBuf out = Unpooled.buffer().retain();
            final ByteBuf in = (ByteBuf) message;

            if (in.getUnsignedIntLE(4) == 0) {

                final RntbdContextRequest request = RntbdContextRequest.decode(in.copy());
                final RntbdContext context = RntbdContext.from(request, SERVER_PROPERTIES, HttpResponseStatus.OK);
                context.encode(out);
                this.writeInbound(out);

            } else {

                final RntbdResponse rntbdResponse;

                try {
                    rntbdResponse = (RntbdResponse) this.inboundMessages().poll();
                } catch (final Exception error) {
                    throw new AssertionError(String.format("%s: %s", error.getClass(), error.getMessage()));
                }

                rntbdResponse.encode(out);
                out.setBytes(8, in.slice(8, 16));  // to overwrite activityId
            }

            this.writeInbound(out);
        }
    }

    private static final class FakeEndpoint implements RntbdEndpoint {

        final FakeChannel fakeChannel;
        final URI physicalAddress;
        final RntbdRequestTimer requestTimer;
        final Tag tag;

        private FakeEndpoint(
            final Config config, final RntbdRequestTimer timer, final URI physicalAddress,
            final RntbdResponse... responses) {

            RntbdRequestManager requestManager = new RntbdRequestManager(new RntbdClientChannelHealthChecker(config), 30);
            this.physicalAddress = physicalAddress;
            this.requestTimer = timer;

            this.fakeChannel = new FakeChannel(responses,
                new RntbdContextNegotiator(requestManager, config.userAgent()),
                new RntbdRequestEncoder(),
                new RntbdResponseDecoder(),
                requestManager
            );

            this.tag = Tag.of(FakeEndpoint.class.getSimpleName(), this.fakeChannel.remoteAddress().toString());
        }

        // region Accessors

        @Override
        public int channelsAcquired() {
            return 0;
        }

        @Override
        public int channelsAvailable() {
            return 0;
        }

        @Override
        public int concurrentRequests() {
            return 0;
        }

        @Override
        public long id() {
            return 0L;
        }

        @Override
        public boolean isClosed() {
            return !this.fakeChannel.isOpen();
        }

        @Override
        public SocketAddress remoteAddress() {
            return this.fakeChannel.remoteAddress();
        }

        @Override
        public int requestQueueLength() {
            return 0;
        }

        @Override
        public Tag tag() {
            return this.tag;
        }

        @Override
        public long usedDirectMemory() {
            return 0;
        }

        @Override
        public long usedHeapMemory() {
            return 0;
        }

        // endregion

        // region Methods

        @Override
        public void close() {
            this.fakeChannel.close().syncUninterruptibly();
        }

        @Override
        public RntbdRequestRecord request(final RntbdRequestArgs requestArgs) {
            final RntbdRequestRecord requestRecord = new RntbdRequestRecord(requestArgs, this.requestTimer);
            this.fakeChannel.writeAndFlush(requestRecord.stage(RntbdRequestRecord.Stage.PIPELINED));
            return requestRecord;
        }

        // endregion

        // region Types

        static class Provider implements RntbdEndpoint.Provider {

            private final Config config;
            private final RntbdResponse expected;
            private final ConcurrentHashMap<URI, RntbdEndpoint> fakeEndpoints;
            private final RntbdRequestTimer requestTimer;

            Provider(RntbdTransportClient.Options options, SslContext sslContext, RntbdResponse expected) {
                this.config = new Config(options, sslContext, LogLevel.WARN);
                this.fakeEndpoints = new ConcurrentHashMap<>();
                this.requestTimer = new RntbdRequestTimer(
                    config.requestTimeoutInNanos(),
                    config.requestTimerResolutionInNanos());
                this.expected = expected;
            }

            @Override
            public void close() throws RuntimeException {
                this.requestTimer.close();
                for (RntbdEndpoint fakeEndpoint : this.fakeEndpoints.values()) {
                    fakeEndpoint.close();
                }
            }

            @Override
            public Config config() {
                return this.config;
            }

            @Override
            public int count() {
                return this.fakeEndpoints.size();
            }

            @Override
            public int evictions() {
                return 0;
            }

            @Override
            public RntbdEndpoint get(URI physicalAddress) {
                return this.fakeEndpoints.computeIfAbsent(physicalAddress, address -> {
                    return new FakeEndpoint(config, requestTimer, physicalAddress, expected);
                });
            }

            @Override
            public Stream<RntbdEndpoint> list() {
                return this.fakeEndpoints.values().stream();
            }
        }

        // endregion
    }

    // endregion
}

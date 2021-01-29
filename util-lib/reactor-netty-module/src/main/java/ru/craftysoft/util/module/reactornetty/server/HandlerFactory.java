package ru.craftysoft.util.module.reactornetty.server;

import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.util.context.Context;
import ru.craftysoft.util.module.common.uuid.UuidUtils;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Optional.ofNullable;
import static ru.craftysoft.util.module.common.logging.MdcKey.*;
import static ru.craftysoft.util.module.common.reactor.MdcUtils.*;
import static ru.craftysoft.util.module.common.uuid.UuidUtils.generateDefaultUuid;
import static ru.craftysoft.util.module.reactornetty.HeaderName.X_B3_SPAN_ID;
import static ru.craftysoft.util.module.reactornetty.HeaderName.X_B3_TRACE_ID;

@Slf4j
public class HandlerFactory {

    private final ServerLoggerHelper loggerHelper;

    public HandlerFactory(String loggerName) {
        this.loggerHelper = new ServerLoggerHelper(loggerName);
    }

    public BiFunction<HttpServerRequest, HttpServerResponse, ? extends Publisher<Void>> handle(BiFunction<HttpServerRequest, byte[], Mono<HttpResponse>> processor) {
        return handle(processor, this::buildLoggingContext);
    }

    public BiFunction<HttpServerRequest, HttpServerResponse, ? extends Publisher<Void>> handle(BiFunction<HttpServerRequest, byte[], Mono<HttpResponse>> processor,
                                                                                               BiFunction<HttpServerRequest, Context, Context> contextTransformer) {
        return (request, response) -> extractBody(request)
                .doOnEach(onNext(bytes -> loggerHelper.logIn(request, bytes)))
                .flatMap(bytes -> processor.apply(request, bytes)
                        .doOnEach(onNext(loggerHelper::logOut))
                        .flatMap(rs -> buildResponse(response, rs))
                )
                .doOnEach(onError(e -> log.error("HandlerFactory.handleMono.thrown", e)))
                .contextWrite(context -> contextTransformer.apply(request, context));
    }

    public BiFunction<HttpServerRequest, HttpServerResponse, ? extends Publisher<Void>> handle(Function<HttpServerRequest, Mono<HttpResponse>> processor) {
        return handle(processor, this::buildLoggingContext);
    }

    public BiFunction<HttpServerRequest, HttpServerResponse, ? extends Publisher<Void>> handle(Function<HttpServerRequest, Mono<HttpResponse>> processor,
                                                                                               BiFunction<HttpServerRequest, Context, Context> contextTransformer) {
        return (request, response) -> processor.apply(request)
                .doOnEach(onSubscribe(() -> loggerHelper.logIn(request)))
                .doOnEach(onNext(loggerHelper::logOut))
                .flatMap(rs -> buildResponse(response, rs))
                .doOnEach(onError(e -> log.error("HandlerFactory.handleMono.thrown", e)))
                .contextWrite(context -> contextTransformer.apply(request, context));
    }

    private Context buildLoggingContext(HttpServerRequest request, Context context) {
        var headers = request.requestHeaders();
        var traceId = ofNullable(headers.get(X_B3_TRACE_ID)).orElseGet(UuidUtils::generateDefaultUuid);
        var spanId = generateDefaultUuid();
        var parentSpanId = ofNullable(headers.get(X_B3_SPAN_ID)).orElse("null");
        var newMdc = Map.of(
                TRACE_ID, traceId,
                SPAN_ID, spanId,
                PARENT_SPAN_ID, parentSpanId
        );
        return contextWithMdc(newMdc, context);
    }

    private Mono<byte[]> extractBody(HttpServerRequest request) {
        return ByteBufFlux.fromInbound(request.receiveContent())
                .aggregate()
                .asByteArray()
                .switchIfEmpty(Mono.just(new byte[0]));
    }

    private Mono<Void> buildResponse(HttpServerResponse response, HttpResponse httpResponse) {
        response.headers(httpResponse.headers());
        response.status(httpResponse.status());
        return ofNullable(httpResponse.message())
                .filter(message -> message.length > 0)
                .map(message -> response.sendObject(Unpooled.copiedBuffer(message))
                        .then())
                .orElseGet(response::send);
    }

}

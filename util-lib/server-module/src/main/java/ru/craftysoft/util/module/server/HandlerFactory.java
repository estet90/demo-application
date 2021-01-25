package ru.craftysoft.util.module.server;

import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.util.context.Context;

import java.util.Map;
import java.util.function.BiFunction;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;
import static ru.craftysoft.util.module.common.reactor.MdcUtils.contextWithMdc;

@Slf4j
public class HandlerFactory {

    private static final Logger requestLogger = LoggerFactory.getLogger("ru.craftysoft.util.module.server.request");
    private static final Logger responseLogger = LoggerFactory.getLogger("ru.craftysoft.util.module.server.response");

    public BiFunction<HttpServerRequest, HttpServerResponse, ? extends Publisher<Void>> handleMono(BiFunction<HttpServerRequest, byte[], Mono<HttpResponse>> processor) {
        return handleMono(processor, this::buildContext);
    }

    public BiFunction<HttpServerRequest, HttpServerResponse, ? extends Publisher<Void>> handleMono(BiFunction<HttpServerRequest, byte[], Mono<HttpResponse>> processor,
                                                                                                   BiFunction<HttpServerRequest, Context, Context> contextTransformer) {
        return (request, response) -> extractBody(request)
                .doOnNext(bytes -> logIn(request, bytes))
                .flatMap(bytes -> processor.apply(request, bytes)
                        .doOnNext(this::logOut)
                        .flatMap(rs -> buildResponse(response, rs))
                )
                .doOnError(e -> log.error("HandlerFactory.handleMono.thrown", e))
                .contextWrite(context -> contextTransformer.apply(request, context));
    }

    public BiFunction<HttpServerRequest, HttpServerResponse, ? extends Publisher<Void>> handle(BiFunction<HttpServerRequest, byte[], HttpResponse> processor) {
        return handle(processor, this::buildContext);
    }

    public BiFunction<HttpServerRequest, HttpServerResponse, ? extends Publisher<Void>> handle(BiFunction<HttpServerRequest, byte[], HttpResponse> processor,
                                                                                               BiFunction<HttpServerRequest, Context, Context> contextTransformer) {
        return (request, response) -> extractBody(request)
                .doOnNext(bytes -> logIn(request, bytes))
                .flatMap(bytes -> {
                    var httpResponse = processor.apply(request, bytes);
                    logOut(httpResponse);
                    return buildResponse(response, httpResponse);
                })
                .doOnError(e -> log.error("HandlerFactory.handle.thrown", e))
                .contextWrite(context -> contextTransformer.apply(request, context));
    }

    private Context buildContext(HttpServerRequest request, Context context) {
        var headers = request.requestHeaders();
        var traceId = ofNullable(headers.get("X-B3-TraceId")).orElseGet(() -> RandomStringUtils.random(16, true, true));
        var spanId = RandomStringUtils.random(16, true, true);
        var parentSpanId = ofNullable(headers.get("X-B3-SpanId")).orElse("null");
        return contextWithMdc(Map.of(
                "traceId", traceId,
                "spanId", spanId,
                "parentSpanId", parentSpanId
        ), context);
    }

    private Mono<byte[]> extractBody(HttpServerRequest request) {
        return ByteBufFlux.fromInbound(request.receiveContent())
                .aggregate()
                .asByteArray()
                .switchIfEmpty(Mono.just(new byte[0]));
    }

    private Mono<Void> buildResponse(HttpServerResponse response, HttpResponse httpResponse) {
        var message = httpResponse.message();
        for (var header : httpResponse.headers().entrySet()) {
            response.addHeader(header.getKey(), header.getValue());
        }
        response.status(httpResponse.status());
        if (message != null && message.length > 0) {
            return response.sendObject(Unpooled.copiedBuffer(message))
                    .then();
        }
        return response.send();
    }

    private void logIn(HttpServerRequest request, byte[] bytes) {
        if (requestLogger.isDebugEnabled()) {
            var uri = request.uri();
            var method = request.method().name();
            var headers = request.requestHeaders().toString();
            if (requestLogger.isTraceEnabled()) {
                if (bytes.length > 0) {
                    requestLogger.trace("Server.request\nURI={}\nMethod={}\nHeaders={}\nBody={}", uri, method, headers, new String(bytes, UTF_8));
                } else {
                    requestLogger.trace("Server.request\nURI={}\nMethod={}\nHeaders={}", uri, method, headers);
                }
            } else {
                requestLogger.debug("Server.request\nURI={}\nMethod={}\nHeaders={}", uri, method, headers);
            }
        }
    }

    private void logOut(HttpResponse httpResponse) {
        if (responseLogger.isDebugEnabled()) {
            var status = httpResponse.status();
            var headers = httpResponse.headers();
            if (responseLogger.isTraceEnabled()) {
                var message = httpResponse.message();
                if (message != null && message.length > 0) {
                    responseLogger.trace("Server.response\nStatus={}\nHeaders={}\nBody={}", status, headers, new String(message, UTF_8));
                } else {
                    responseLogger.trace("Server.response\nStatus={}\nHeaders={}", status, headers);
                }
            } else {
                responseLogger.debug("Server.response\nStatus={}\nHeaders={}", status, headers);
            }
        }
    }

}

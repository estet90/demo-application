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

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.BiFunction;

import static java.util.Optional.ofNullable;
import static ru.craftysoft.util.module.common.reactor.MdcUtils.contextWithMdc;

@Slf4j
public class HandlerFactory {

    private static final Logger requestLogger = LoggerFactory.getLogger("ru.craftysoft.util.module.server.request");
    private static final Logger responseLogger = LoggerFactory.getLogger("ru.craftysoft.util.module.server.response");

    public BiFunction<HttpServerRequest, HttpServerResponse, ? extends Publisher<Void>> handleMono(BiFunction<HttpServerRequest, byte[], Mono<HttpResponse>> processor) {
        return (request, response) -> extractBody(request)
                .flatMap(bytes -> {
                    logIn(request, bytes);
                    return processor.apply(request, bytes).flatMap(rs -> buildResponse(response, rs));
                })
                .doOnError(e -> log.error("HandlerFactory.handleMono.thrown", e))
                .contextWrite(context -> buildContext(request, context));
    }

    public BiFunction<HttpServerRequest, HttpServerResponse, ? extends Publisher<Void>> handle(BiFunction<HttpServerRequest, byte[], HttpResponse> processor) {
        return (request, response) -> extractBody(request)
                .flatMap(bytes -> {
                    logIn(request, bytes);
                    var httpResponse = processor.apply(request, bytes);
                    return buildResponse(response, httpResponse);
                })
                .doOnError(e -> log.error("HandlerFactory.handle.thrown", e))
                .contextWrite(context -> buildContext(request, context));
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
        logOut(httpResponse, message);
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
            if (requestLogger.isTraceEnabled()) {
                if (bytes.length > 0) {
                    requestLogger.trace(
                            "Server.request\nURI={}\nMethod={}\nHeaders={}\nBody={}",
                            request.uri(),
                            request.method().name(),
                            request.requestHeaders().toString(),
                            new String(bytes, StandardCharsets.UTF_8)
                    );
                } else {
                    requestLogger.trace(
                            "Server.request\nURI={}\nMethod={}\nHeaders={}",
                            request.uri(),
                            request.method().name(),
                            request.requestHeaders().toString()
                    );
                }
            } else {
                requestLogger.debug(
                        "Server.request\nURI={}\nMethod={}\nHeaders={}",
                        request.uri(),
                        request.method().name(),
                        request.requestHeaders().toString()
                );
            }
        }
    }

    private void logOut(HttpResponse httpResponse, byte[] message) {
        if (responseLogger.isDebugEnabled()) {
            if (responseLogger.isTraceEnabled()) {
                if (message != null && message.length > 0) {
                    responseLogger.trace(
                            "Server.response\nStatus={}\nHeaders={}\nBody={}",
                            httpResponse.status(),
                            httpResponse.headers(),
                            new String(message, StandardCharsets.UTF_8)
                    );
                } else {
                    responseLogger.trace(
                            "Server.response\nStatus={}\nHeaders={}",
                            httpResponse.status(),
                            httpResponse.headers()
                    );
                }
            } else {
                responseLogger.debug(
                        "Server.response\nStatus={}\nHeaders={}",
                        httpResponse.status(),
                        httpResponse.headers()
                );
            }
        }
    }

}

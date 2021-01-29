package ru.craftysoft.demoservice.controller;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import reactor.core.publisher.Mono;
import ru.craftysoft.util.module.reactornetty.server.HttpResponse;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Objects;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;

@Singleton
public class SwaggerController {

    private final byte[] swagger;

    @Inject
    public SwaggerController() {
        try (var inputStream = SwaggerController.class.getClassLoader().getResourceAsStream("openapi/self/demo-service.yaml")) {
            this.swagger = Objects.requireNonNull(inputStream).readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Mono<HttpResponse> process() {
        return Mono.just(
                new HttpResponse(
                        swagger,
                        HttpResponseStatus.OK,
                        new DefaultHttpHeaders()
                                .add(CONTENT_TYPE.toString(), "application/x-yaml")
                                .add(CONTENT_LENGTH.toString(), String.valueOf(this.swagger.length))
                )
        );
    }
}

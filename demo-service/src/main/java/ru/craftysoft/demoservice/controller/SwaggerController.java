package ru.craftysoft.demoservice.controller;

import io.netty.handler.codec.http.HttpResponseStatus;
import ru.craftysoft.util.module.server.HttpResponse;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

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

    public HttpResponse process() {
        return new HttpResponse(
                swagger,
                HttpResponseStatus.OK,
                Map.of(
                        "Content-Type", "application/x-yaml",
                        "Content-Length", String.valueOf(this.swagger.length)
                )
        );
    }
}

package ru.craftysoft.demoservice.service.client;

import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.charset.StandardCharsets;

@Singleton
@Slf4j
public class RestClient {

    private final HttpClient client;

    @Inject
    public RestClient(HttpClient client) {
        this.client = client;
    }

    public Mono<Void> test() {
        return client
                .post()
//                .uri("/users/1/tasks")
                .send(Mono.just(Unpooled.copiedBuffer("message".getBytes(StandardCharsets.UTF_8))))
                .response()
                .then();
    }
}

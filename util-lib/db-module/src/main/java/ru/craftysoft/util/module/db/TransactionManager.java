package ru.craftysoft.util.module.db;

import io.vertx.core.Future;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.SqlClient;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@RequiredArgsConstructor
public class TransactionManager {

    private final PgPool pgPool;

    public <T> Flux<T> executeInTransactionFlux(Function<SqlClient, Flux<T>> process) {
        return Flux.create(sink -> pgPool.withTransaction(sqlClient -> {
                    var result = process.apply(sqlClient);
                    return Mono.just(result)
                            .map(Future::succeededFuture)
                            .onErrorResume(e -> Mono.just(Future.failedFuture(e)))
                            .block();
                },
                event -> {
                    if (event.failed()) {
                        sink.error(event.cause());
                    } else {
                        var result = event.result();
                        result.subscribe(sink::next, sink::error, sink::complete, sink.currentContext());
                    }
                }
        ));
    }

    public <T> Mono<T> executeInTransactionMono(Function<SqlClient, Mono<T>> process) {
        return Mono.create(sink -> pgPool.withTransaction(
                sqlClient ->
                        process.apply(sqlClient)
                                .map(Mono::just)
                                .map(Future::succeededFuture)
                                .onErrorResume(e -> Mono.just(Future.failedFuture(e)))
                                .block(),
                event -> {
                    if (event.failed()) {
                        sink.error(event.cause());
                    } else {
                        var result = event.result();
                        result.subscribe(sink::success, sink::error, sink::success, sink.currentContext());
                    }
                }
        ));
    }

}

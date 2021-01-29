package ru.craftysoft.util.module.db;

import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.SqlClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

import static ru.craftysoft.util.module.common.reactor.MdcUtils.withContext;

@RequiredArgsConstructor
@Slf4j
public class TransactionManager {

    private final PgPool pgPool;

    public <T> Flux<T> executeInTransactionFlux(Function<SqlClient, Flux<T>> process) {
        return Flux.create(sink -> pgPool.getConnection(connectionAsyncResult -> {
            var context = sink.currentContext();
            var point = "TransactionManager.executeInTransactionFlux";
            if (connectionAsyncResult.failed()) {
                withContext(context, () -> log.error("{}.connectionThrown {}", point, connectionAsyncResult.cause().getMessage()));
                sink.error(connectionAsyncResult.cause());
            } else {
                var connection = connectionAsyncResult.result();
                connection.begin(transactionAsyncResult -> {
                    if (transactionAsyncResult.failed()) {
                        withContext(context, () -> log.error("{}.transactionThrown {}", point, transactionAsyncResult.cause().getMessage()));
                        sink.error(transactionAsyncResult.cause());
                    } else {
                        var transaction = transactionAsyncResult.result();
                        var result = process.apply(connection)
                                .onErrorResume(e -> Mono.create(rollbackSink -> transaction.rollback(voidAsyncResult -> {
                                    withContext(context, () -> log.error("{}.transactionRollbackThrown {}", point, e.getMessage()));
                                    rollbackSink.error(e);
                                })))
                                .concatWith(Mono.create(commitSink -> transaction.commit(
                                        voidAsyncResult -> {
                                            if (voidAsyncResult.failed()) {
                                                withContext(context, () -> log.error("{}.transactionCommitThrown {}", point, voidAsyncResult.cause().getMessage()));
                                                commitSink.error(voidAsyncResult.cause());
                                            } else {
                                                commitSink.success();
                                            }
                                        })));
                        result.subscribe(sink::next, sink::error, sink::complete, sink.currentContext());
                    }
                });
            }
        }));
    }

    public <T> Mono<T> executeInTransactionMono(Function<SqlClient, Mono<T>> process) {
        return Mono.create(sink -> pgPool.getConnection(connectionAsyncResult -> {
            var context = sink.currentContext();
            var point = "TransactionManager.executeInTransactionMono";
            if (connectionAsyncResult.failed()) {
                withContext(context, () -> log.error("{}.connectionThrown {}", point, connectionAsyncResult.cause().getMessage()));
                sink.error(connectionAsyncResult.cause());
            } else {
                var connection = connectionAsyncResult.result();
                connection.begin(transactionAsyncResult -> {
                    if (transactionAsyncResult.failed()) {
                        withContext(context, () -> log.error("{}.transactionThrown {}", point, transactionAsyncResult.cause().getMessage()));
                        sink.error(transactionAsyncResult.cause());
                    } else {
                        var transaction = transactionAsyncResult.result();
                        var result = process.apply(connection)
                                .onErrorResume(e -> Mono.create(rollbackSink -> transaction.rollback(voidAsyncResult -> {
                                    withContext(context, () -> log.error("{}.transactionRollbackThrown {}", point, e.getMessage()));
                                    rollbackSink.error(e);
                                })))
                                .concatWith(Mono.create(commitSink -> transaction.commit(
                                        voidAsyncResult -> {
                                            if (voidAsyncResult.failed()) {
                                                withContext(context, () -> log.error("{}.transactionCommitThrown {}", point, voidAsyncResult.cause().getMessage()));
                                                commitSink.error(voidAsyncResult.cause());
                                            } else {
                                                commitSink.success();
                                            }
                                        })))
                                .collectList()
                                .flatMap(l -> l.isEmpty() ? Mono.empty() : Mono.just(l.get(0)));
                        result.subscribe(sink::success, sink::error, sink::success, sink.currentContext());
                    }
                });
            }
        }));
    }

}

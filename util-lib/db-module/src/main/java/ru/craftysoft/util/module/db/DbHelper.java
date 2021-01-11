package ru.craftysoft.util.module.db;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.util.context.Context;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Function;

import static ru.craftysoft.util.module.db.DbLoggerHelper.*;

@RequiredArgsConstructor
public class DbHelper {

    private final SqlClient sqlClient;

    public <T> Flux<T> executeQueryForFlux(Logger log,
                                           String point,
                                           String sql,
                                           Tuple args,
                                           Function<Row, T> mapper) {
        return executeQueryForFlux(sqlClient, log, point, sql, args, mapper);
    }

    public static <T> Flux<T> executeQueryForFlux(SqlClient sqlClient,
                                           Logger log,
                                           String point,
                                           String sql,
                                           Tuple args,
                                           Function<Row, T> mapper) {
        return Flux.
                <T>create(sink -> {
                    withQueryId(sink.currentContext(), () -> logIn(log, point, sql, args));
                    sqlClient.preparedQuery(sql).execute(args, handler(log, point, sink, mapper));
                })
                .doOnEach(signal -> logError(log, point, signal));
    }

    public <T> Mono<T> executeQueryForMono(Logger log,
                                           String point,
                                           String sql,
                                           Tuple args,
                                           Function<Row, T> mapper) {
        return executeQueryForMono(sqlClient, log, point, sql, args, mapper);
    }

    public static <T> Mono<T> executeQueryForMono(SqlClient sqlClient,
                                           Logger log,
                                           String point,
                                           String sql,
                                           Tuple args,
                                           Function<Row, T> mapper) {
        return Mono
                .<T>create(sink -> {
                    withQueryId(sink.currentContext(), () -> logIn(log, point, sql, args));
                    sqlClient.preparedQuery(sql).execute(args, handler(log, point, sink, mapper));
                })
                .doOnEach(signal -> logError(log, point, signal));
    }

    private static <T> Handler<AsyncResult<RowSet<Row>>> handler(Logger log, String point, FluxSink<T> sink, Function<Row, T> mapper) {
        return result -> {
            var context = sink.currentContext();
            if (result.succeeded()) {
                try {
                    addDataToSink(log, point, sink, result, context, mapper);
                    sink.complete();
                } catch (Exception e) {
                    sink.error(e);
                }
            } else {
                sink.error(result.cause());
            }
        };
    }

    private static <T> Handler<AsyncResult<RowSet<Row>>> handler(Logger log, String point, MonoSink<T> sink, Function<Row, T> mapper) {
        return result -> {
            var context = sink.currentContext();
            if (result.succeeded()) {
                try {
                    addDataToSink(log, point, sink, result, context, mapper);
                    sink.success();
                } catch (Exception e) {
                    sink.error(e);
                }
            } else {
                sink.error(result.cause());
            }
        };
    }

    private static <T> void addDataToSink(Logger log, String point, FluxSink<T> sink, AsyncResult<RowSet<Row>> result, Context context, Function<Row, T> mapper) {
        if (log.isTraceEnabled()) {
            var mapperResults = new ArrayList<T>();
            for (var row : result.result()) {
                var mapperResult = mapper.apply(row);
                sink.next(mapper.apply(row));
                mapperResults.add(mapperResult);
            }
            withQueryId(context, () -> log.trace("{}.out result={}", point, mapperResults));
        } else {
            var rowSet = result.result();
            rowSet.forEach(row -> sink.next(mapper.apply(row)));
            withQueryId(context, () -> log.debug("{}.out size={}", point, rowSet.size()));
        }
    }

    private static <T> void addDataToSink(Logger log, String point, MonoSink<T> sink, AsyncResult<RowSet<Row>> result, Context context, Function<Row, T> mapper) {
        var rowSet = result.result();
        rowSet.forEach(row -> {
            var mapperResult = mapper.apply(row);
            sink.success(mapperResult);
            if (log.isTraceEnabled()) {
                withQueryId(context, () -> log.trace("{}.out result={}", point, mapperResult));
            } else {
                withQueryId(context, () -> log.debug("{}.out size={}", point, rowSet.size()));
            }
        });
    }
}

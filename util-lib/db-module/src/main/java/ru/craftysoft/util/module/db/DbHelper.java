package ru.craftysoft.util.module.db;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.pgclient.PgException;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;
import org.jooq.Query;
import org.jooq.conf.ParamType;
import org.slf4j.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.util.context.ContextView;

import java.util.function.BiFunction;
import java.util.function.Function;

import static ru.craftysoft.util.module.common.reactor.MdcUtils.appendMdc;
import static ru.craftysoft.util.module.common.reactor.MdcUtils.withContext;
import static ru.craftysoft.util.module.common.uuid.UuidUtils.generateDefaultUuid;
import static ru.craftysoft.util.module.db.DbLoggerHelper.logIn;
import static ru.craftysoft.util.module.db.DbLoggerHelper.logOut;

@RequiredArgsConstructor
public class DbHelper {

    private final SqlClient sqlClient;

    public <T> Flux<T> executeQueryForFlux(Logger log,
                                           String point,
                                           String sql,
                                           Tuple args,
                                           Function<Row, T> mapper,
                                           BiFunction<ContextView, PgException, Exception> exceptionMapper) {
        return executeQueryForFlux(sqlClient, log, point, sql, args, mapper, exceptionMapper);
    }

    public static <T> Flux<T> executeQueryForFlux(SqlClient sqlClient,
                                                  Logger log,
                                                  String point,
                                                  String sql,
                                                  Tuple args,
                                                  Function<Row, T> mapper,
                                                  BiFunction<ContextView, PgException, Exception> exceptionMapper) {
        return Flux.
                <T>create(sink -> {
                    withContext(sink.currentContext(), () -> logIn(log, point, sql, args));
                    sqlClient.preparedQuery(sql).execute(args, handler(log, point, sink, mapper, exceptionMapper));
                })
                .contextWrite(appendMdc(MdcKey.QUERY_ID, generateDefaultUuid()));
    }

    public <T> Flux<T> executeQueryForFlux(Logger log,
                                           String point,
                                           Query query,
                                           Function<Row, T> mapper,
                                           BiFunction<ContextView, PgException, Exception> exceptionMapper) {
        return executeQueryForFlux(sqlClient, log, point, query, mapper, exceptionMapper);
    }

    public static <T> Flux<T> executeQueryForFlux(SqlClient sqlClient,
                                                  Logger log,
                                                  String point,
                                                  Query query,
                                                  Function<Row, T> mapper,
                                                  BiFunction<ContextView, PgException, Exception> exceptionMapper) {
        return Flux.
                <T>create(sink -> {
                    withContext(sink.currentContext(), () -> logIn(log, point, query));
                    var args = Tuple.from(query.getBindValues());
                    var sql = query.getSQL(ParamType.NAMED).replaceAll("(?<!:):(?!:)", "\\$");
                    sqlClient.preparedQuery(sql).execute(args, handler(log, point, sink, mapper, exceptionMapper));
                })
                .contextWrite(appendMdc(MdcKey.QUERY_ID, generateDefaultUuid()));
    }

    public <T> Mono<T> executeQueryForMono(Logger log,
                                           String point,
                                           String sql,
                                           Tuple args,
                                           Function<Row, T> mapper,
                                           BiFunction<ContextView, PgException, Exception> exceptionMapper) {
        return executeQueryForMono(sqlClient, log, point, sql, args, mapper, exceptionMapper);
    }

    public static <T> Mono<T> executeQueryForMono(SqlClient sqlClient,
                                                  Logger log,
                                                  String point,
                                                  String sql,
                                                  Tuple args,
                                                  Function<Row, T> mapper,
                                                  BiFunction<ContextView, PgException, Exception> exceptionMapper) {
        return Mono
                .<T>create(sink -> {
                    withContext(sink.currentContext(), () -> logIn(log, point, sql, args));
                    sqlClient.preparedQuery(sql).execute(args, handler(log, point, sink, mapper, exceptionMapper));
                })
                .contextWrite(appendMdc(MdcKey.QUERY_ID, generateDefaultUuid()));
    }

    public <T> Mono<T> executeQueryForMono(Logger log,
                                           String point,
                                           Query query,
                                           Function<Row, T> mapper,
                                           BiFunction<ContextView, PgException, Exception> exceptionMapper) {
        return executeQueryForMono(sqlClient, log, point, query, mapper, exceptionMapper);
    }

    public static <T> Mono<T> executeQueryForMono(SqlClient sqlClient,
                                                  Logger log,
                                                  String point,
                                                  Query query,
                                                  Function<Row, T> mapper,
                                                  BiFunction<ContextView, PgException, Exception> exceptionMapper) {
        return Mono
                .<T>create(sink -> {
                    withContext(sink.currentContext(), () -> logIn(log, point, query));
                    var args = Tuple.from(query.getBindValues());
                    var sql = query.getSQL(ParamType.NAMED).replaceAll("(?<!:):(?!:)", "\\$");
                    sqlClient.preparedQuery(sql).execute(args, handler(log, point, sink, mapper, exceptionMapper));
                })
                .contextWrite(appendMdc(MdcKey.QUERY_ID, generateDefaultUuid()));
    }

    private static <T> Handler<AsyncResult<RowSet<Row>>> handler(Logger log,
                                                                 String point,
                                                                 FluxSink<T> sink,
                                                                 Function<Row, T> mapper,
                                                                 BiFunction<ContextView, PgException, Exception> exceptionMapper) {
        return result -> {
            var context = sink.currentContext();
            if (result.succeeded()) {
                try {
                    var rowSet = result.result();
                    logOut(log, point, context, mapper, rowSet);
                    rowSet.forEach(row -> sink.next(mapper.apply(row)));
                    sink.complete();
                } catch (Exception e) {
                    withContext(context, () -> log.error("{}.thrown {}", point, e.getMessage()));
                    var exception = e instanceof PgException pgException ? exceptionMapper.apply(context, pgException) : e;
                    sink.error(exception);
                }
            } else {
                var cause = result.cause();
                withContext(context, () -> log.error("{}.thrown {}", point, cause.getMessage()));
                var exception = cause instanceof PgException e ? exceptionMapper.apply(context, e) : cause;
                sink.error(exception);
            }
        };
    }

    private static <T> Handler<AsyncResult<RowSet<Row>>> handler(Logger log,
                                                                 String point,
                                                                 MonoSink<T> sink,
                                                                 Function<Row, T> mapper,
                                                                 BiFunction<ContextView, PgException, Exception> exceptionMapper) {
        return result -> {
            var context = sink.currentContext();
            if (result.succeeded()) {
                try {
                    var rowSet = result.result();
                    rowSet.forEach(row -> {
                        var mapperResult = mapper.apply(row);
                        sink.success(mapperResult);
                        logOut(log, point, context, rowSet, mapperResult);
                    });
                    sink.success();
                } catch (Exception e) {
                    withContext(context, () -> log.error("{}.thrown {}", point, e.getMessage()));
                    var exception = e instanceof PgException pgException ? exceptionMapper.apply(context, pgException) : e;
                    sink.error(exception);
                }
            } else {
                var cause = result.cause();
                withContext(context, () -> log.error("{}.thrown {}", point, cause.getMessage()));
                var exception = cause instanceof PgException e ? exceptionMapper.apply(context, e) : cause;
                sink.error(exception);
            }
        };
    }
}

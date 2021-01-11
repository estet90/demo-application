package ru.craftysoft.demoservice.service.dao;

import io.vertx.pgclient.PgException;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Transaction;
import io.vertx.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import javax.inject.Inject;
import javax.inject.Singleton;

import static ru.craftysoft.demoservice.error.exception.InvocationExceptionCode.DB;
import static ru.craftysoft.demoservice.error.operation.ModuleOperationCode.resolve;
import static ru.craftysoft.error.exception.ExceptionFactory.newInvocationException;
import static ru.craftysoft.util.module.db.DbHelper.executeQueryForMono;

@Singleton
@Slf4j
public class UserDao {

    @Inject
    public UserDao() {
    }

    public Mono<Boolean> checkUserById(SqlClient sqlClient, Long id) {
        var sql = """
                SELECT EXISTS(SELECT 1 
                FROM users 
                WHERE id = $1) AS exists""";
        return executeQueryForMono(sqlClient, log, "UserDao.checkUserById", sql, Tuple.of(id), row -> row.getBoolean("exists"))
                .onErrorMap(PgException.class, e -> newInvocationException(e, resolve(), DB, e.getMessage()));
    }
}

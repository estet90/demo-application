package ru.craftysoft.demoservice.service.dao;

import io.vertx.pgclient.PgException;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.craftysoft.demoservice.dto.db.Task;
import ru.craftysoft.util.module.db.DbHelper;

import javax.inject.Inject;
import javax.inject.Singleton;

import static ru.craftysoft.demoservice.error.exception.InvocationExceptionCode.DB;
import static ru.craftysoft.demoservice.error.operation.ModuleOperationCode.resolve;
import static ru.craftysoft.error.exception.ExceptionFactory.newInvocationException;
import static ru.craftysoft.util.module.db.DbHelper.executeQueryForMono;

@Singleton
@Slf4j
public class TaskDao {

    private final DbHelper dbHelper;

    @Inject
    public TaskDao(DbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public Flux<Task> getAllTasksByUserId(Long userId) {
        var sql = """
                SELECT id, name, date_time
                FROM tasks
                WHERE user_id = $1""";
        return dbHelper
                .executeQueryForFlux(log, "TaskDao.getAllTasksByUserId", sql, Tuple.of(userId), row -> new Task(
                        row.getLong("id"),
                        row.getString("name"),
                        row.getOffsetDateTime("date_time")
                ))
                .onErrorMap(PgException.class, e -> newInvocationException(e, resolve(), DB, e.getMessage()));
    }

    public Mono<Long> addTaskToUser(SqlClient sqlClient, Long userId, Task task) {
        var sql = """
                INSERT INTO tasks (name, date_time, user_id)
                VALUES ($1, $2, $3)
                RETURNING id""";
        var args = Tuple.of(task.name(), task.dateTime(), userId);
        return executeQueryForMono(sqlClient, log, "TaskDao.addTaskToUser", sql, args, row -> row.getLong("id"))
                .onErrorMap(PgException.class, e -> newInvocationException(e, resolve(), DB, e.getMessage()));
    }
}

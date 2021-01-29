package ru.craftysoft.demoservice.service.dao;

import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.jooq.impl.DSL;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.craftysoft.todo.tables.records.TasksRecord;
import ru.craftysoft.util.module.db.DbHelper;

import javax.inject.Inject;
import javax.inject.Singleton;

import static ru.craftysoft.demoservice.error.exception.InvocationExceptionCode.DB;
import static ru.craftysoft.demoservice.error.operation.ModuleOperationCode.resolve;
import static ru.craftysoft.error.exception.ExceptionFactory.newInvocationException;
import static ru.craftysoft.todo.tables.Tasks.TASKS;
import static ru.craftysoft.util.module.db.DbHelper.executeQueryForMono;

@Singleton
@Slf4j
public class TaskDao {

    private final DbHelper dbHelper;

    @Inject
    public TaskDao(DbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public Flux<TasksRecord> getAllTasksByUserId(Long userId) {
        var query = DSL
                .select(TASKS.ID.as("id"), TASKS.NAME.as("name"), TASKS.DATE_TIME.as("date_time"))
                .from(TASKS)
                .where(TASKS.USER_ID.eq(userId));
        return dbHelper
                .executeQueryForFlux(log, "TaskDao.getAllTasksByUserId", query, row -> new TasksRecord(
                        row.getLong("id"),
                        row.getString("name"),
                        row.getLocalDateTime("date_time"),
                        null
                ), (context, e) -> newInvocationException(e, resolve(context), DB, e.getMessage()));
    }

    public Mono<Long> addTaskToUser(SqlClient sqlClient, TasksRecord task) {
        var sql = """
                INSERT INTO tasks (name, date_time, user_id)
                VALUES ($1, $2, $3)
                RETURNING id""";
        var args = Tuple.of(task.getName(), task.getDateTime(), task.getUserId());
        return executeQueryForMono(sqlClient, log, "TaskDao.addTaskToUser", sql, args, row -> row.getLong("id"),
                (context, e) -> newInvocationException(e, resolve(context), DB, e.getMessage()));
    }
}

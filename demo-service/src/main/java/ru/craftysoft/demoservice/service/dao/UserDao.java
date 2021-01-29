package ru.craftysoft.demoservice.service.dao;

import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.jooq.impl.DSL;
import reactor.core.publisher.Mono;
import ru.craftysoft.todo.tables.records.UsersRecord;
import ru.craftysoft.util.module.db.DbHelper;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jooq.impl.DSL.field;
import static ru.craftysoft.demoservice.error.exception.InvocationExceptionCode.DB;
import static ru.craftysoft.demoservice.error.operation.ModuleOperationCode.resolve;
import static ru.craftysoft.error.exception.ExceptionFactory.newInvocationException;
import static ru.craftysoft.todo.tables.Users.USERS;
import static ru.craftysoft.util.module.db.DbHelper.executeQueryForMono;

@Singleton
@Slf4j
public class UserDao {

    private final DbHelper dbHelper;

    @Inject
    public UserDao(DbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public Mono<Boolean> checkUserById(SqlClient sqlClient, Long id) {
        var query = DSL.select(
                field(DSL
                        .exists(DSL
                                .select(USERS.ID)
                                .from(USERS)
                                .where(USERS.ID.eq(id))
                        )).as("exists")
        );
        return executeQueryForMono(sqlClient, log, "UserDao.checkUserById", query, row -> row.getBoolean("exists"),
                (context, e) -> newInvocationException(e, resolve(context), DB, e.getMessage()));
    }

    public Mono<Boolean> checkUserById(Long id) {
        var query = DSL.select(
                field(DSL
                        .exists(DSL
                                .select(USERS.ID)
                                .from(USERS)
                                .where(USERS.ID.eq(id))
                        )).as("exists")
        );
        return dbHelper.executeQueryForMono(log, "UserDao.checkUserById", query, row -> row.getBoolean("exists"),
                (context, e) -> newInvocationException(e, resolve(context), DB, e.getMessage()));
    }

    public Mono<Long> addUser(UsersRecord user) {
        var sql = """
                INSERT INTO todo.users (name, login, password)
                VALUES ($1, $2, $3)
                RETURNING id""";
        var args = Tuple.of(user.getName(), user.getLogin(), user.getPassword());
        return dbHelper.executeQueryForMono(log, "UserDao.addUser", sql, args, row -> row.getLong("id"),
                (context, e) -> newInvocationException(e, resolve(context), DB, e.getMessage()));
    }
}

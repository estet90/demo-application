package ru.craftysoft.demoservice.service.dao;

import io.vertx.sqlclient.SqlClient;
import reactor.core.publisher.Mono;
import ru.craftysoft.demoservice.model.User;
import ru.craftysoft.todo.tables.records.UsersRecord;

import javax.inject.Inject;
import javax.inject.Singleton;

import static ru.craftysoft.demoservice.error.exception.BusinessExceptionCode.USER_NOT_FOUND;
import static ru.craftysoft.demoservice.error.operation.ModuleOperationCode.resolve;
import static ru.craftysoft.error.exception.ExceptionFactory.newBusinessException;

@Singleton
public class UserDaoAdapter {

    private final UserDao dao;

    @Inject
    public UserDaoAdapter(UserDao dao) {
        this.dao = dao;
    }

    public Mono<Boolean> checkUserById(SqlClient sqlClient, Long id) {
        return Mono.deferContextual(context -> dao.checkUserById(sqlClient, id)
                .filter(b -> b)
                .switchIfEmpty(Mono.error(newBusinessException(resolve(context), USER_NOT_FOUND, String.format("id='%s'", id)))));
    }

    public Mono<Boolean> checkUserById(Long id) {
        return Mono.deferContextual(context -> dao.checkUserById(id)
                .filter(b -> b)
                .switchIfEmpty(Mono.error(newBusinessException(resolve(context), USER_NOT_FOUND, String.format("id='%s'", id)))));
    }

    public Mono<Long> addUser(User user) {
        var userRecord = new UsersRecord(
                null,
                user.getName(),
                user.getLogin(),
                user.getPassword()
        );
        return dao.addUser(userRecord);
    }
}

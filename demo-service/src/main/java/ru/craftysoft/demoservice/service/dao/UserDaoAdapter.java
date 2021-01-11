package ru.craftysoft.demoservice.service.dao;

import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Transaction;
import reactor.core.publisher.Mono;

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
        return dao.checkUserById(sqlClient, id)
                .filter(b -> b)
                .switchIfEmpty(Mono.error(newBusinessException(resolve(), USER_NOT_FOUND, String.format("id='%s'", id))));
    }
}

package ru.craftysoft.demoservice.logic;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;
import ru.craftysoft.demoservice.model.ResultWithId;
import ru.craftysoft.demoservice.model.User;
import ru.craftysoft.demoservice.service.dao.UserDaoAdapter;

import javax.inject.Inject;
import javax.inject.Singleton;

import static ru.craftysoft.demoservice.util.OperationWrapper.wrap;

@Singleton
@Slf4j
public class AddUserOperation {

    private final UserDaoAdapter userDaoAdapter;

    @Inject
    public AddUserOperation(UserDaoAdapter userDaoAdapter) {
        this.userDaoAdapter = userDaoAdapter;
    }

    public Mono<ResultWithId> process(ContextView context, User user) {
        return wrap(
                log, "AddUserOperation.process", context,
                ctx -> userDaoAdapter.addUser(user)
                        .map(id -> new ResultWithId().id(id.intValue()))
        );
    }
}

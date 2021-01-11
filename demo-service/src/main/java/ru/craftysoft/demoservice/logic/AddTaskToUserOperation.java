package ru.craftysoft.demoservice.logic;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import ru.craftysoft.demoservice.model.AddTaskToUserResponse;
import ru.craftysoft.demoservice.model.Task;
import ru.craftysoft.demoservice.service.dao.TaskDaoAdapter;
import ru.craftysoft.demoservice.service.dao.UserDaoAdapter;
import ru.craftysoft.util.module.db.TransactionManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import static ru.craftysoft.demoservice.util.OperationWrapper.wrap;

@Singleton
@Slf4j
public class AddTaskToUserOperation {

    private final TaskDaoAdapter taskDaoAdapter;
    private final UserDaoAdapter userDaoAdapter;
    private final TransactionManager transactionManager;

    @Inject
    public AddTaskToUserOperation(TaskDaoAdapter taskDaoAdapter,
                                  UserDaoAdapter userDaoAdapter,
                                  TransactionManager transactionManager) {
        this.taskDaoAdapter = taskDaoAdapter;
        this.userDaoAdapter = userDaoAdapter;
        this.transactionManager = transactionManager;
    }

    public Mono<AddTaskToUserResponse> process(Long userId, Task task) {
        return wrap(
                log, "AddTaskToUserOperation.process",
                () -> transactionManager.executeInTransactionMono(sqlClient -> //транзакция здесь не нужна, сделано для демонстрации
                        userDaoAdapter.checkUserById(sqlClient, userId)
                                .then(taskDaoAdapter.addTaskToUser(sqlClient, userId, task)
                                        .map(id -> new AddTaskToUserResponse(id.intValue())))
                )
        );
    }
}

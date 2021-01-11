package ru.craftysoft.demoservice.logic;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import ru.craftysoft.demoservice.model.TaskWithId;
import ru.craftysoft.demoservice.service.dao.TaskDaoAdapter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static ru.craftysoft.demoservice.util.OperationWrapper.wrap;

@Singleton
@Slf4j
public class GetAllTasksByUserIdOperation {

    private final TaskDaoAdapter taskDaoAdapter;

    @Inject
    public GetAllTasksByUserIdOperation(TaskDaoAdapter taskDaoAdapter) {
        this.taskDaoAdapter = taskDaoAdapter;
    }

    public Mono<List<TaskWithId>> process(Long userId) {
        return wrap(
                log, "GetAllTasksByUserIdOperation.process",
                () -> taskDaoAdapter.getAllTasksByUserId(userId)
                        .map(task -> new TaskWithId(task.id().intValue(), task.name(), task.dateTime()))
                        .collectList()
        );
    }
}

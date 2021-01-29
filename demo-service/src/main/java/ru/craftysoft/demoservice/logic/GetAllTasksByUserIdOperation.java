package ru.craftysoft.demoservice.logic;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;
import ru.craftysoft.demoservice.model.TaskWithId;
import ru.craftysoft.demoservice.service.client.RestClient;
import ru.craftysoft.demoservice.service.dao.TaskDaoAdapter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static ru.craftysoft.demoservice.util.OperationWrapper.wrap;

@Singleton
@Slf4j
public class GetAllTasksByUserIdOperation {

    private final TaskDaoAdapter taskDaoAdapter;
    private final RestClient restClient;

    @Inject
    public GetAllTasksByUserIdOperation(TaskDaoAdapter taskDaoAdapter, RestClient restClient) {
        this.taskDaoAdapter = taskDaoAdapter;
        this.restClient = restClient;
    }

    public Mono<List<TaskWithId>> process(ContextView context, Long userId) {
        return wrap(
                log, "GetAllTasksByUserIdOperation.process", context,
                ctx -> restClient.test()
                        .then(taskDaoAdapter.getAllTasksByUserId(userId)
                                .map(task -> new TaskWithId()
                                        .id(task.getId().intValue())
                                        .name(task.getName())
                                        .dateTime(OffsetDateTime.of(task.getDateTime(), ZoneOffset.ofHours(3)))
                                ).collectList())
        );
    }
}

package ru.craftysoft.demoservice.service.dao;

import io.vertx.sqlclient.SqlClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.craftysoft.demoservice.model.Task;
import ru.craftysoft.todo.tables.records.TasksRecord;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TaskDaoAdapter {

    private final TaskDao dao;

    @Inject
    public TaskDaoAdapter(TaskDao dao) {
        this.dao = dao;
    }

    public Flux<TasksRecord> getAllTasksByUserId(Long userId) {
        return dao.getAllTasksByUserId(userId);
    }

    public Mono<Long> addTaskToUser(SqlClient sqlClient, Long userId, Task task) {
        var tasksRecord = new TasksRecord(null, task.getName(), task.getDateTime().toLocalDateTime(), userId);
        return dao.addTaskToUser(sqlClient, tasksRecord);
    }
}

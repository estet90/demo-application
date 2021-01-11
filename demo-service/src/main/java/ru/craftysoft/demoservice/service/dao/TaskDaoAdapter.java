package ru.craftysoft.demoservice.service.dao;

import io.vertx.sqlclient.SqlClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.craftysoft.demoservice.dto.db.Task;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TaskDaoAdapter {

    private final TaskDao dao;

    @Inject
    public TaskDaoAdapter(TaskDao dao) {
        this.dao = dao;
    }

    public Flux<Task> getAllTasksByUserId(Long userId) {
        return dao.getAllTasksByUserId(userId);
    }

    public Mono<Long> addTaskToUser(SqlClient sqlClient, Long userId, ru.craftysoft.demoservice.model.Task task) {
        var taskDb = new ru.craftysoft.demoservice.dto.db.Task(null, task.getName(), task.getDateTime());
        return dao.addTaskToUser(sqlClient, userId, taskDb);
    }
}

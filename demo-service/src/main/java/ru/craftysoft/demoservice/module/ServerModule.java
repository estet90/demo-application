package ru.craftysoft.demoservice.module;

import dagger.Module;
import dagger.Provides;
import reactor.netty.http.server.HttpServer;
import ru.craftysoft.demoservice.controller.SwaggerController;
import ru.craftysoft.demoservice.controller.TaskController;
import ru.craftysoft.demoservice.controller.UserController;
import ru.craftysoft.util.module.common.properties.annotation.Property;
import ru.craftysoft.util.module.reactornetty.server.HandlerFactory;

import javax.inject.Named;
import javax.inject.Singleton;

@Module
public class ServerModule {

    @Provides
    @Singleton
    static HttpServer httpServer(@Property("server.port:8080") @Named("serverPort") int serverPort,
                                 SwaggerController swaggerController,
                                 TaskController taskController,
                                 UserController userController) {
        var handlerFactory = new HandlerFactory("ru.craftysoft.demoservice");
        return HttpServer.create()
                .port(serverPort)
                .route(routes -> routes
                        .get("/swagger", handlerFactory.handle(request -> swaggerController.process()))
                        .get("/users/{id}/tasks", handlerFactory.handle(taskController::getAllTasksByUserId))
                        .post("/users/{id}/tasks", handlerFactory.handle(taskController::addTaskToUser))
                        .post("/users", handlerFactory.handle((request, bytes) -> userController.addUser(bytes)))
                );
    }

}

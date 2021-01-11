package ru.craftysoft.demoservice.module;

import dagger.Module;
import dagger.Provides;
import reactor.netty.http.server.HttpServer;
import ru.craftysoft.demoservice.controller.SwaggerController;
import ru.craftysoft.demoservice.controller.TaskController;
import ru.craftysoft.util.module.server.HandlerFactory;
import ru.craftysoft.util.module.server.HandlerFactoryModule;

import javax.inject.Singleton;

@Module(includes = HandlerFactoryModule.class)
public class ServerModule {

    @Provides
    @Singleton
    static HttpServer httpServer(HandlerFactory handlerFactory, SwaggerController swaggerController, TaskController taskController) {
        return HttpServer.create()
                .port(8080)
                .route(routes -> routes
                        .get("/swagger", handlerFactory.handle((request, bytes) -> swaggerController.process()))
                        .get("/users/{id}/tasks", handlerFactory.handleMono((request, bytes) -> taskController.getAllTasksByUserId(request)))
                        .post("/users/{id}/tasks", handlerFactory.handleMono(taskController::addTaskToUser))
                );
    }

}

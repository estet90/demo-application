package ru.craftysoft.demoservice;

import dagger.Component;
import reactor.netty.http.server.HttpServer;
import ru.craftysoft.demoservice.module.DbModule;
import ru.craftysoft.demoservice.module.ServerModule;
import ru.craftysoft.util.module.common.CommonModule;
import ru.craftysoft.util.module.common.ScheduledTaskManager;
import ru.craftysoft.util.module.common.json.JacksonModule;
import ru.craftysoft.util.module.common.logging.LogbackModule;
import ru.craftysoft.util.module.common.properties.ConfigurationPropertiesPublisher;

import javax.inject.Named;
import javax.inject.Singleton;

@Component(modules = {
        JacksonModule.class,
        CommonModule.class,
        LogbackModule.class,

        DbModule.class,
        ServerModule.class,
})
@Singleton
public interface ApplicationComponent {

    HttpServer httpServer();

    @Named("refreshPropertiesScheduledTaskManager")
    ScheduledTaskManager refreshPropertiesScheduledTaskManager();

    ConfigurationPropertiesPublisher configurationPropertiesPublisher();

}

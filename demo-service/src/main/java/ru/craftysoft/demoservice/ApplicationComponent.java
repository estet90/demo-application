package ru.craftysoft.demoservice;

import dagger.Component;
import reactor.netty.http.server.HttpServer;
import ru.craftysoft.demoservice.module.DbModule;
import ru.craftysoft.demoservice.module.ServerModule;
import ru.craftysoft.util.module.common.json.JacksonModule;
import ru.craftysoft.util.module.common.properties.PropertyModule;

import javax.inject.Singleton;

@Component(modules = {
        JacksonModule.class,
        PropertyModule.class,

        DbModule.class,
        ServerModule.class,
})
@Singleton
public interface ApplicationComponent {

    HttpServer httpServer();

}
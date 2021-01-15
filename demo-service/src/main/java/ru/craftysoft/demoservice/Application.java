package ru.craftysoft.demoservice;

import lombok.extern.slf4j.Slf4j;
import ru.craftysoft.error.exception.ExceptionFactory;
import ru.craftysoft.util.module.common.properties.ConfigurationPropertyRefresher;
import ru.craftysoft.util.module.common.properties.PropertiesModule;
import ru.craftysoft.util.module.common.reactor.ReactorMdc;

@Slf4j
public class Application {

    public static void main(String[] args) {
        ReactorMdc.init();
        new ExceptionFactory("0001");
        var startTimeMillis = System.currentTimeMillis();
        var component = DaggerApplicationComponent.builder()
                .propertiesModule(new PropertiesModule(args))
                .build();
        try {
            var refreshers = component.refreshers();
            var logback = component.logback();
            var disposableServer = component.httpServer().bindNow();
            log.info("Application.main.start {}ms", System.currentTimeMillis() - startTimeMillis);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                disposableServer.dispose();
                logback.dispose();
                refreshers.forEach(ConfigurationPropertyRefresher::stop);
            }));
            disposableServer.onDispose()
                    .block();
        } catch (Exception e) {
            log.error("Application.main", e);
            throw e;
        }
    }

}

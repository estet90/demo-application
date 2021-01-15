package ru.craftysoft.util.module.common.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import dagger.Module;
import dagger.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import ru.craftysoft.util.module.common.properties.ApplicationProperties;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Map;

import static java.util.Optional.ofNullable;

@Module
public class LogbackModule {
    private static final Logger log = LoggerFactory.getLogger(LogbackModule.class);
    public static final LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();

    @Singleton
    @Provides
    @Named("logback")
    static Disposable providesLogbackPropertiesRefresh(ApplicationProperties applicationProperties) {
        applicationProperties.getProperties("logging.level.")
                .entrySet()
                .forEach(LogbackModule::applyLevel);
        return applicationProperties.changes("logging.level.").subscribe(LogbackModule::applyLevel);
    }

    private static void applyLevel(Map.Entry<String, String> logLevel) {
        var logger = logLevel.getKey();
        var level = ofNullable(logLevel.getValue()).map(String::toUpperCase).map(Level::toLevel).orElse(null);
        log.debug("{} -> {}", logger, level);
        ctx.getLogger(logger).setLevel(level);
    }
}

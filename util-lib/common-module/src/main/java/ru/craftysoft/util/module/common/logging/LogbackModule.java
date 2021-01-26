package ru.craftysoft.util.module.common.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import ru.craftysoft.util.module.common.properties.ConfigurationPropertiesSubscriber;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.Optional.ofNullable;
import static ru.craftysoft.util.module.common.logging.LogbackConfigurator.DEFAULT_LOG_LEVEL;

@Module
@Slf4j
public class LogbackModule {
    @Singleton
    @Provides
    @IntoSet
    static ConfigurationPropertiesSubscriber logLevelRefresher() {
        return new ConfigurationPropertiesSubscriber("logging.level.", log, new HashMap<>()) {
            private final LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();

            @Override
            protected void refresh(Map<String, String> propertiesToAdd, Set<String> propertiesToRemove) {
                propertiesToAdd.entrySet().forEach(this::changeLogger);
                propertiesToRemove.forEach(this::removeLogger);
                super.refresh(propertiesToAdd, propertiesToRemove);
            }

            private void removeLogger(String logger) {
                log.debug("LogbackModule.bind изменён уровень логирования {} {} -> {}", logger, properties.get(logger), DEFAULT_LOG_LEVEL);
                ctx.getLogger(logger).setLevel(DEFAULT_LOG_LEVEL);
            }

            private void changeLogger(Map.Entry<String, String> logLevel) {
                var logger = logLevel.getKey();
                var level = ofNullable(logLevel.getValue()).map(String::toUpperCase).map(Level::toLevel).orElse(null);
                var oldLevel = ctx.getLogger(logger).getLevel();
                ctx.getLogger(logger).setLevel(level);
                log.debug("LogbackModule.bind изменён уровень логирования {} {} -> {}", logger, oldLevel, level);
            }
        };
    }
}

package ru.craftysoft.util.module.common.logging;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.spi.ContextAwareBase;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.nio.charset.StandardCharsets;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogManager;

public class LogbackConfigurator extends ContextAwareBase implements Configurator {
    private static final String CONSOLE_PATTERN = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X] %logger{80} %msg%n";
    private static final String ASYNC_ENABLED = System.getenv("LOGBACK_ASYNC");
    public static final Level DEFAULT_LOG_LEVEL = Level.INFO;

    public LogbackConfigurator() {
    }

    @Override
    public void configure(LoggerContext ctx) {
        var root = ctx.getLogger("ROOT");
        root.setLevel(DEFAULT_LOG_LEVEL);
        root.detachAndStopAllAppenders();
        this.configureJdkLoggingBridgeHandler();
        var async = "true".equalsIgnoreCase(ASYNC_ENABLED);
        if (async) {
            var asyncAppender = new AsyncAppender();
            asyncAppender.setName("console-async");
            asyncAppender.setContext(this.context);
            root.addAppender(asyncAppender);
            asyncAppender.addAppender(consoleAppender(ctx));
            asyncAppender.start();
        } else {
            root.addAppender(consoleAppender(ctx));
        }
    }

    private OutputStreamAppender<ILoggingEvent> consoleAppender(LoggerContext ctx) {
        var layout = new PatternLayoutEncoder();
        layout.setPattern(CONSOLE_PATTERN);
        layout.setContext(ctx);
        layout.setCharset(StandardCharsets.UTF_8);
        layout.start();
        var appender = new ConsoleAppender<ILoggingEvent>();
        appender.setName("console");
        appender.setContext(ctx);
        appender.setEncoder(layout);
        appender.start();
        return appender;
    }

    private void configureJdkLoggingBridgeHandler() {
        try {
            removeJdkLoggingBridgeHandler();
            SLF4JBridgeHandler.install();
        } catch (Throwable ex) {
            // Ignore. No java.util.logging bridge is installed.
        }
    }

    private void removeJdkLoggingBridgeHandler() {
        try {
            removeDefaultRootHandler();
            SLF4JBridgeHandler.uninstall();
        } catch (Throwable ex) {
            // Ignore and continue
        }
    }

    private void removeDefaultRootHandler() {
        try {
            var rootLogger = LogManager.getLogManager().getLogger("");
            var handlers = rootLogger.getHandlers();
            if (handlers.length == 1 && handlers[0] instanceof ConsoleHandler) {
                rootLogger.removeHandler(handlers[0]);
            }
        } catch (Throwable ex) {
            // Ignore and continue
        }
    }
}

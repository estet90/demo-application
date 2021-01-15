package ru.craftysoft.util.module.common.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.concat;

public class YamlPropertySource implements PropertySource, SignalHandler {
    public static Logger log = LoggerFactory.getLogger(YamlPropertySource.class);

    private final EmitterProcessor<List<String>> changes;
    private final String configFileLocation;
    private final FluxSink<List<String>> changesSink;
    private Map<String, String> properties;

    public YamlPropertySource(String configFileLocation) {
        this.configFileLocation = configFileLocation;
        this.changes = EmitterProcessor.create();
        this.changesSink = this.changes.sink();
        this.properties = this.read();
    }

    private Map<String, String> read() {
        var isClassPath = this.configFileLocation.startsWith("classpath:/");
        var actualPath = isClassPath
                ? this.configFileLocation.substring(10)
                : this.configFileLocation;
        try (var is = isClassPath
                ? YamlPropertySource.class.getResourceAsStream(actualPath)
                : Files.newInputStream(Paths.get(actualPath))) {
            if (is == null) {
                log.warn("Config file location doesn't exist: {}", this.configFileLocation);
                return Map.of();
            } else {
                return YamlParser.parseYaml(is);
            }
        } catch (java.nio.file.NoSuchFileException e) {
            log.warn("Config file location doesn't exist: {}", this.configFileLocation);
            return Map.of();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    @Override
    public String getProperty(String key) {
        return this.properties.get(key);
    }

    @Override
    public Map<String, String> getProperties(String startsWith) {
        return this.properties.entrySet()
                .stream()
                .filter(e -> e.getKey().startsWith(startsWith))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (s1, s2) -> s1));
    }

    @Override
    public Flux<List<String>> changes() {
        return this.changes;
    }

    @Override
    public void handle(Signal sig) {
        var newProperties = this.read();

        var newKeys = newProperties.keySet().stream()
                .filter(key -> !this.properties.containsKey(key));
        var deletedKeys = this.properties.keySet().stream()
                .filter(key -> !newProperties.containsKey(key));
        var changedKeys = newProperties.entrySet().stream()
                .filter(e -> {
                    var oldValue = this.properties.get(e.getKey());
                    return oldValue != null && !oldValue.equals(e.getValue());
                })
                .map(Map.Entry::getKey);
        var changedEntries = concat(concat(newKeys, deletedKeys), changedKeys).collect(Collectors.toList());
        this.properties = newProperties;
        try {
            for (var changedEntry : changedEntries) {
                this.changesSink.next(List.of(changedEntry));
            }
        } catch (Exception e) {
            log.warn("YamlPropertySource.handle.sink.next.thrown", e);
        }
    }

    public static void registerSignalHandler(YamlPropertySource propertySource) {
        try {
            Signal.handle(new Signal("HUP"), propertySource);
        } catch (IllegalArgumentException ignore) {
            log.info("Can't register signal handler for SIGHUP");
        }
    }
}

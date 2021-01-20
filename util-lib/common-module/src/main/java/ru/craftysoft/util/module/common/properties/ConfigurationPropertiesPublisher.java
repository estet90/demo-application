package ru.craftysoft.util.module.common.properties;

import lombok.extern.slf4j.Slf4j;
import ru.craftysoft.util.module.common.properties.source.PropertySource;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

@Slf4j
public class ConfigurationPropertiesPublisher {

    private final ConfigurationPropertiesSubscriber subscriber;
    private final List<PropertySource> sources;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final AtomicBoolean isStarted = new AtomicBoolean(false);
    private final SubmissionPublisher<Map<String, String>> publisher = new SubmissionPublisher<>();

    public ConfigurationPropertiesPublisher(ConfigurationPropertiesSubscriber subscriber, Set<PropertySource> sources) {
        this.subscriber = subscriber;
        this.sources = sources.stream()
                .sorted(comparing(PropertySource::priority).reversed())
                .collect(Collectors.toList());
    }

    public void start() {
        if (this.isStarted.compareAndSet(false, true)) {
            publisher.subscribe(subscriber);
            executorService.scheduleAtFixedRate(() -> {
                var properties = this.sources.stream()
                        .map(PropertySource::getProperties)
                        .flatMap(stringStringMap -> stringStringMap.entrySet().stream())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v2, ConcurrentHashMap::new));
                publisher.submit(properties);
            }, 0, 5, TimeUnit.SECONDS);
            log.info("ConfigurationPropertiesPublisher.start");
        }
    }

    public void stop() {
        if (this.isStarted.compareAndSet(true, false)) {
            publisher.close();
            this.executorService.shutdown();
            try {
                if (!this.executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    this.executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.error("ConfigurationPropertiesPublisher.stop.thrown", e);
            }
            log.info("ConfigurationPropertiesPublisher.stop");
        }
    }
}

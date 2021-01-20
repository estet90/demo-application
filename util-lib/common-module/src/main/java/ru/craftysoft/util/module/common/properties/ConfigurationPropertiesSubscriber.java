package ru.craftysoft.util.module.common.properties;

import lombok.extern.slf4j.Slf4j;
import ru.craftysoft.util.module.common.properties.source.PropertySource;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Flow;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;

@Slf4j
public class ConfigurationPropertiesSubscriber implements Flow.Subscriber<Map<String, String>> {

    private final Map<String, String> properties;
    private Flow.Subscription subscription;

    public ConfigurationPropertiesSubscriber(Set<PropertySource> sources) {
        //если не инициализировать, будут возникать ошибки
        this.properties = sources.stream()
                .sorted(comparing(PropertySource::priority).reversed())
                .map(PropertySource::getProperties)
                .flatMap(stringStringMap -> stringStringMap.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v2, ConcurrentHashMap::new));
    }

    public Map<String, String> getProperties() {
        return this.properties;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        this.subscription.request(1);
    }

    @Override
    public void onNext(Map<String, String> item) {
        var entries = item.entrySet();
        var point = "ConfigurationPropertiesSubscriber.onNext";
        var newProperties = entries.stream()
                .filter(entry -> !this.properties.containsKey(entry.getKey()))
                .peek(entry -> log.debug("{} новое свойство {}", point, entry));
        var changedProperties = entries.stream()
                .filter(e -> {
                    var oldValue = this.properties.get(e.getKey());
                    return oldValue != null && !oldValue.equals(e.getValue());
                })
                .peek(entry -> log.debug(
                        "{} новое значение свойства {}: {} -> {}",
                        point, entry.getKey(), properties.get(entry.getKey()), entry.getValue()
                ));
        var propertiesToAdd = Stream.concat(newProperties, changedProperties)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        this.properties.putAll(propertiesToAdd);
        this.properties.keySet().stream()
                .filter(key -> !item.containsKey(key))
                .forEach(key -> {
                    this.properties.remove(key);
                    log.debug("{} удалено свойство {}", point, key);
                });
        this.subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("ConfigurationPropertiesSubscriber.onError", throwable);
    }

    @Override
    public void onComplete() {
        this.subscription.cancel();
    }
}

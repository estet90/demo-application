package ru.craftysoft.util.module.common.properties;

import org.slf4j.Logger;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Flow;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

public abstract class ConfigurationPropertiesSubscriber implements Flow.Subscriber<Map<String, String>> {

    private Flow.Subscription subscription;
    private final String prefix;
    protected final Logger logger;
    protected final Map<String, String> properties;

    public ConfigurationPropertiesSubscriber(String prefix,
                                             Logger logger,
                                             Map<String, String> properties) {
        this.prefix = prefix;
        this.logger = logger;
        this.properties = properties;
    }

    protected void refresh(Map<String, String> propertiesToAdd, Set<String> propertiesToRemove) {
        properties.putAll(propertiesToAdd);
        propertiesToRemove.forEach(properties::remove);
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        this.subscription.request(1);
    }

    @Override
    public void onNext(Map<String, String> actualProperties) {
        if (actualProperties.isEmpty()) {
            this.subscription.request(1);
            return;
        }
        var dottedPrefix = prefix == null || prefix.isEmpty() || prefix.equals("*") || prefix.endsWith(".")
                ? prefix
                : prefix + ".";
        var propertiesToCheck = dottedPrefix == null || dottedPrefix.isEmpty() || dottedPrefix.equals("*")
                ? actualProperties
                : actualProperties.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(prefix))
                .collect(Collectors.toMap(entry -> entry.getKey().substring(dottedPrefix.length()), Map.Entry::getValue));
        if (propertiesToCheck.isEmpty()) {
            this.subscription.request(1);
            return;
        }
        ofNullable(properties).ifPresentOrElse(properties -> {
            var newProperties = propertiesToCheck.entrySet().stream()
                    .filter(entry -> !properties.containsKey(entry.getKey()))
                    .peek(entry -> logger.debug("onNext новое свойство {}", entry));
            var changedProperties = propertiesToCheck.entrySet().stream()
                    .filter(e -> {
                        var oldValue = properties.get(e.getKey());
                        return oldValue != null && !oldValue.equals(e.getValue());
                    })
                    .peek(entry -> logger.debug(
                            "onNext новое значение свойства {}: {} -> {}",
                            entry.getKey(), properties.get(entry.getKey()), entry.getValue()
                    ));
            var propertiesToAdd = Stream.concat(newProperties, changedProperties)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            var propertiesToRemove = properties.keySet().stream()
                    .filter(key -> !propertiesToCheck.containsKey(key))
                    .peek(key -> logger.debug("onNext свойство {} будет удалено", key))
                    .collect(Collectors.toSet());
            if (!propertiesToAdd.isEmpty() || !propertiesToRemove.isEmpty()) {
                refresh(propertiesToAdd, propertiesToRemove);
            }
        }, () -> refresh(propertiesToCheck, Set.of()));
        this.subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
        logger.error("onError", throwable);
    }

    @Override
    public void onComplete() {
        this.subscription.cancel();
    }
}

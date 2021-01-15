package ru.craftysoft.util.module.common.properties;

import reactor.core.Disposable;

public class ConfigurationPropertyRefresher<T> {
    private final String prefix;
    private final T properties;
    private final ApplicationProperties applicationProperties;
    private final ConfigurationPropertiesBinder<T> binder;
    private final Disposable disposable;

    public ConfigurationPropertyRefresher(String prefix,
                                          T properties,
                                          ApplicationProperties applicationProperties,
                                          ConfigurationPropertiesBinder<T> binder) {
        this.prefix = prefix;
        this.properties = properties;
        this.applicationProperties = applicationProperties;
        this.binder = binder;
        this.disposable = this.applicationProperties.changes(this.prefix)
                .subscribe(e -> this.binder.bind(this.properties, e));
    }

    public void stop() {
        this.disposable.dispose();
    }
}

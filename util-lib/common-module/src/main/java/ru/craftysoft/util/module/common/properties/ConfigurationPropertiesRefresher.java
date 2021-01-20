package ru.craftysoft.util.module.common.properties;

public class ConfigurationPropertiesRefresher<T> {
    private final String prefix;
    private final T target;
    private final ApplicationProperties applicationProperties;
    private final ConfigurationPropertiesBinder<T> binder;

    public ConfigurationPropertiesRefresher(String prefix,
                                            T target,
                                            ApplicationProperties applicationProperties,
                                            ConfigurationPropertiesBinder<T> binder) {
        this.prefix = prefix;
        this.target = target;
        this.applicationProperties = applicationProperties;
        this.binder = binder;
    }

    public void refresh() {
        this.binder.bind(target, applicationProperties.getProperties(prefix));
    }
}

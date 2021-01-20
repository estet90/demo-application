package ru.craftysoft.util.module.common.properties;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import ru.craftysoft.util.module.common.properties.source.EnvironmentPropertySource;
import ru.craftysoft.util.module.common.properties.source.PropertySource;
import ru.craftysoft.util.module.common.properties.source.SystemPropertiesPropertySource;
import ru.craftysoft.util.module.common.properties.source.YamlPropertySource;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import java.util.Set;

@Module
public class PropertiesModule {
    @Nullable
    private final String configFileLocation;

    public PropertiesModule(String[] args) {
        var options = new Options();
        options.addOption("c", "config", true, "Yaml config file location");
        try {
            var parsed = new DefaultParser().parse(options, args);
            this.configFileLocation = parsed.getOptionValue("config", "classpath:/application-dev.yaml");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Provides
    @Singleton
    static ApplicationProperties applicationProperties(ConfigurationPropertiesSubscriber subscriber) {
        return new ApplicationProperties(subscriber);
    }

    @Provides
    @Singleton
    static ConfigurationPropertiesPublisher configurationPropertiesPublisher(ConfigurationPropertiesSubscriber subscriber,
                                                                             Set<PropertySource> propertySources) {
        return new ConfigurationPropertiesPublisher(subscriber, propertySources);
    }

    @Provides
    @Singleton
    static ConfigurationPropertiesSubscriber subscriber(Set<PropertySource> propertySources) {
        return new ConfigurationPropertiesSubscriber(propertySources);
    }

    @Provides
    @Singleton
    @IntoSet
    PropertySource providesYamlPropertySource() {
        return new YamlPropertySource(this.configFileLocation);
    }

    @Provides
    @Singleton
    @IntoSet
    static PropertySource providesEnvironmentPropertySource() {
        return new EnvironmentPropertySource();
    }

    @Provides
    @Singleton
    @IntoSet
    static PropertySource providesSystemPropertiesPropertySource() {
        return new SystemPropertiesPropertySource();
    }
}

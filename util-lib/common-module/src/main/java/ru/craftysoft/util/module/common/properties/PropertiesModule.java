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

import javax.inject.Singleton;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

@Module
public class PropertiesModule {
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
    @IntoSet
    static ConfigurationPropertiesSubscriber applicationPropertiesSubscriber(ApplicationProperties applicationProperties) {
        return applicationProperties;
    }

    @Provides
    @Singleton
    static ApplicationProperties applicationProperties(Set<PropertySource> propertySources) {
        var initialProperties = propertySources.stream()
                .sorted(comparing(PropertySource::priority).reversed())
                .map(PropertySource::getProperties)
                .flatMap(stringStringMap -> stringStringMap.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v2, ConcurrentHashMap::new));
        return new ApplicationProperties(initialProperties);
    }

    @Provides
    @Singleton
    static ConfigurationPropertiesPublisher configurationPropertiesPublisher(Set<ConfigurationPropertiesSubscriber> subscribers,
                                                                             Set<PropertySource> propertySources) {
        return new ConfigurationPropertiesPublisher(subscribers, propertySources);
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

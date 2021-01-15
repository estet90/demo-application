package ru.craftysoft.util.module.common.properties;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import javax.annotation.Nullable;
import javax.inject.Singleton;

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
    @IntoSet
    static PropertySource providesEnvironmentPropertySourceIntoSet(EnvironmentPropertySource environmentPropertySource) {
        return environmentPropertySource;
    }

    @Provides
    @Singleton
    static EnvironmentPropertySource providesEnvironmentPropertySource() {
        return new EnvironmentPropertySource();
    }

    @Provides
    @Singleton
    public YamlPropertySource providesYamlPropertySource(EnvironmentPropertySource environmentPropertySource) {
        var ps = new YamlPropertySource(this.configFileLocation);
        YamlPropertySource.registerSignalHandler(ps);
        return ps;
    }

    @Provides
    @Singleton
    @IntoSet
    static PropertySource providesYamlPropertySourceIntoSet(YamlPropertySource yamlPropertySource) {
        return yamlPropertySource;
    }

    @Provides
    @Singleton
    static SystemPropertiesPropertySource providesSystemPropertiesPropertySource() {
        return new SystemPropertiesPropertySource();
    }

    @Provides
    @Singleton
    @IntoSet
    static PropertySource providesSystemPropertiesPropertySourceIntoSet(SystemPropertiesPropertySource systemPropertiesPropertySource) {
        return systemPropertiesPropertySource;
    }
}

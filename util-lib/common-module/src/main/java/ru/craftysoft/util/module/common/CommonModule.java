package ru.craftysoft.util.module.common;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import ru.craftysoft.util.module.common.properties.ApplicationProperties;
import ru.craftysoft.util.module.common.properties.ConfigurationPropertyRefresher;
import ru.craftysoft.util.module.common.properties.PropertiesModule;
import ru.craftysoft.util.module.common.properties.PropertySource;

import javax.inject.Named;
import javax.inject.Singleton;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

@Module(includes = PropertiesModule.class)
public class CommonModule {
    @Provides
    @Singleton
    static ApplicationProperties providesApplicationProperties(Set<PropertySource> propertySources) {
        return new ApplicationProperties(propertySources);
    }

    @Provides
    @Singleton
    @Named("hostName")
    static String providesHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Provides
    @ElementsIntoSet
    static Set<ConfigurationPropertyRefresher<?>> empty() {
        return new HashSet<>();
    }
}

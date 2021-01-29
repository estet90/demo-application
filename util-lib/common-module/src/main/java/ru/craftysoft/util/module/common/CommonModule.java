package ru.craftysoft.util.module.common;

import dagger.Module;
import dagger.Provides;
import ru.craftysoft.util.module.common.properties.PropertiesModule;

import javax.inject.Named;
import javax.inject.Singleton;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Module(includes = PropertiesModule.class)
public class CommonModule {

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
}

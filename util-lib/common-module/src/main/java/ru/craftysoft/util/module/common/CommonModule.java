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

//    @Provides
//    @Singleton
//    @Named("refreshPropertiesScheduledTaskManager")
//    static ScheduledTaskManager refreshPropertiesScheduledTaskManager(@Named("refreshPropertiesExecutor") ScheduledExecutorService refreshPropertiesExecutor,
//                                                                      Set<ConfigurationPropertiesRefresher<?>> refreshers) {
//        Set<Runnable> refresh = Set.of(() -> refreshers.forEach(ConfigurationPropertiesRefresher::refresh));
//        return new ScheduledTaskManager(refreshPropertiesExecutor, 2, refresh);
//    }
//
//    @Provides
//    @Singleton
//    @Named("refreshPropertiesExecutor")
//    static ScheduledExecutorService refreshPropertiesExecutor(ApplicationProperties applicationProperties,
//                                                              @Named("refreshPropertiesThreadFactory") ThreadFactory threadFactory) {
//        var threadsCount = ofNullable(applicationProperties.getProperty("application.refresh-properties.threads-count", Integer::parseInt))
//                .orElse(1);
//        return Executors.newScheduledThreadPool(threadsCount, threadFactory);
//    }
//
//    @Provides
//    @Singleton
//    @Named("refreshPropertiesThreadFactory")
//    static ThreadFactory refreshPropertiesThreadFactory() {
//        return new ThreadFactory() {
//
//            private final AtomicInteger counter = new AtomicInteger(0);
//
//            @Override
//            public Thread newThread(@Nonnull Runnable r) {
//                return new Thread(r, "refresh-properties" + "-" + counter.getAndIncrement());
//            }
//        };
//    }
}

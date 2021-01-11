package ru.craftysoft.util.module.server;

import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class HandlerFactoryModule {

    @Provides
    @Singleton
    static HandlerFactory handlerFactory() {
        return new HandlerFactory();
    }

}

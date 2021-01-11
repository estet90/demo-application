package ru.craftysoft.demoservice.module;

import dagger.Module;
import dagger.Provides;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import ru.craftysoft.util.module.common.properties.PropertyResolver;
import ru.craftysoft.util.module.db.DbHelper;
import ru.craftysoft.util.module.db.TransactionManager;

import javax.inject.Singleton;
import java.util.HashMap;

import static io.vertx.pgclient.PgConnectOptions.DEFAULT_PROPERTIES;

@Module
public class DbModule {

    @Provides
    @Singleton
    static DbHelper dbHelper(PgPool pgPool) {
        return new DbHelper(pgPool);
    }

    @Provides
    @Singleton
    static TransactionManager transactionManager(PgPool pgPool) {
        return new TransactionManager(pgPool);
    }

    @Provides
    @Singleton
    static PgPool pgPool(PropertyResolver propertyResolver) {
        var conProps = new HashMap<>(DEFAULT_PROPERTIES);
        conProps.remove("intervalStyle");
        var config = new PgConnectOptions()
                .setDatabase(propertyResolver.getRequiredProperty("db.demo.name"))
                .setHost(propertyResolver.getRequiredProperty("db.demo.host"))
                .setPort(propertyResolver.getRequiredIntProperty("db.demo.port"))
                .setUser(propertyResolver.getRequiredProperty("db.demo.username"))
                .setPassword(propertyResolver.getRequiredProperty("db.demo.password"))
                .setConnectTimeout(1000)
                .setReconnectAttempts(2)
                .setReconnectInterval(5000)
                .setProperties(conProps)
                .setIdleTimeout(30)
                .setTracingPolicy(TracingPolicy.ALWAYS);
        var poolConfig = new PoolOptions()
                .setMaxSize(3);
        var vertx = Vertx.vertx(new VertxOptions().setPreferNativeTransport(true));
        return PgPool.pool(vertx, config, poolConfig);
    }

}

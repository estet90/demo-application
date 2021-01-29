package ru.craftysoft.demoservice.module;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.client.HttpClient;
import ru.craftysoft.util.module.common.properties.ConfigurationPropertiesSubscriber;
import ru.craftysoft.util.module.common.properties.annotation.Property;
import ru.craftysoft.util.module.reactornetty.client.ApplicationChannelPipelineConfigurer;
import ru.craftysoft.util.module.reactornetty.client.HttpClientPropertiesSubscriberFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

@Module
@Slf4j
public class HttpClientModule {

    @Provides
    @Singleton
    static HttpClient client(@Property("rest.demo.url") @Named("restDemoUrl") String url,
                             @Property("rest.demo.connect-timeout") @Named("restDemoConnectTimeout") int connectTimeout,
                             @Property("rest.demo.read-timeout:10000") @Named("restDemoReadTimeout") int readTimeout) {
        return HttpClient.create()
                .baseUrl(url)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                .doOnConnected(connection ->
                        connection.addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
                )
                .doOnChannelInit(new ApplicationChannelPipelineConfigurer("ru.craftysoft.demoservice"));
    }

    @Provides
    @Singleton
    @IntoSet
    static ConfigurationPropertiesSubscriber httpClientPropertiesSubscriber(HttpClient client) {
        return HttpClientPropertiesSubscriberFactory.httpClientPropertiesSubscriber("rest.demo", log, client);
    }

}

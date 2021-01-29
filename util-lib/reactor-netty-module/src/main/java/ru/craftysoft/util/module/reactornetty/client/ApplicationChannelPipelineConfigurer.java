package ru.craftysoft.util.module.reactornetty.client;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.ChannelPipelineConfigurer;
import reactor.netty.ConnectionObserver;
import reactor.netty.NettyPipeline;

import javax.annotation.Nonnull;
import java.net.SocketAddress;

import static ru.craftysoft.util.module.common.uuid.UuidUtils.generateDefaultUuid;
import static ru.craftysoft.util.module.common.reactor.MdcUtils.contextWithMdc;

public class ApplicationChannelPipelineConfigurer implements ChannelPipelineConfigurer {

    private final Logger requestLogger;
    private final Logger responseLogger;

    public ApplicationChannelPipelineConfigurer(String loggerName) {
        this.requestLogger = LoggerFactory.getLogger(loggerName + ".client.request");
        this.responseLogger = LoggerFactory.getLogger(loggerName + ".client.response");
    }

    @Override
    public void onChannelInit(@Nonnull ConnectionObserver connectionObserver, @Nonnull Channel channel, SocketAddress remoteAddress) {
        var context = contextWithMdc("webRequestId", generateDefaultUuid(), connectionObserver.currentContext());
        var pipeline = channel.pipeline();
        if (requestLogger.isDebugEnabled() || responseLogger.isDebugEnabled()) {
            pipeline.addFirst(NettyPipeline.LoggingHandler, new LoggingChannelHandler(requestLogger, responseLogger, context));
        }
        pipeline.addLast("HeaderHandler", new RequestHeaderChannelHandler(context));
    }

}

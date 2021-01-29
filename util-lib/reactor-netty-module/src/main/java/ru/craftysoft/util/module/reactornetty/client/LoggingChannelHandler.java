package ru.craftysoft.util.module.reactornetty.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import reactor.util.context.Context;

import javax.annotation.Nonnull;

import static java.nio.charset.StandardCharsets.UTF_8;
import static ru.craftysoft.util.module.common.reactor.MdcUtils.withContext;

public class LoggingChannelHandler extends ChannelDuplexHandler {

    private final Logger requestLogger;
    private final Logger responseLogger;
    private final Context context;

    public LoggingChannelHandler(Logger requestLogger, Logger responseLogger, Context context) {
        this.requestLogger = requestLogger;
        this.responseLogger = responseLogger;
        this.context = context;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        logging(requestLogger, msg);
        super.write(ctx, msg, promise);
    }

    @Override
    public void channelRead(@Nonnull ChannelHandlerContext ctx, @Nonnull Object msg) throws Exception {
        logging(responseLogger, msg);
        super.channelRead(ctx, msg);
    }

    private void logging(Logger logger, Object msg) {
        if (logger.isDebugEnabled()) {
            withContext(context, () -> {
                if (msg instanceof ByteBuf byteBuf) {
                    logger.debug("\n{}", byteBuf.toString(UTF_8));
                } else if (msg instanceof ByteBufHolder byteBufHolder) {
                    logger.debug("\n{}", byteBufHolder.content().toString(UTF_8));
                }
            });
        }
    }

}

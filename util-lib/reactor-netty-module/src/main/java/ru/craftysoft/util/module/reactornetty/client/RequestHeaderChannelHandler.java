package ru.craftysoft.util.module.reactornetty.client;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import reactor.util.context.Context;

import javax.annotation.Nonnull;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static ru.craftysoft.util.module.common.logging.MdcKey.SPAN_ID;
import static ru.craftysoft.util.module.common.logging.MdcKey.TRACE_ID;
import static ru.craftysoft.util.module.reactornetty.HeaderName.X_B3_SPAN_ID;
import static ru.craftysoft.util.module.reactornetty.HeaderName.X_B3_TRACE_ID;

public class RequestHeaderChannelHandler extends ChannelDuplexHandler {

    private final Context context;

    public RequestHeaderChannelHandler(Context context) {
        this.context = context;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof HttpRequest request) {
            ofNullable(context.getOrDefault("mdc", Map.of())).ifPresent(mdc -> request.headers().add(new DefaultHttpHeaders()
                    .add(X_B3_TRACE_ID, mdc.get(TRACE_ID))
                    .add(X_B3_SPAN_ID, mdc.get(SPAN_ID))
            ));
        }
        super.write(ctx, msg, promise);
    }

    @Override
    public void channelRead(@Nonnull ChannelHandlerContext ctx, @Nonnull Object msg) throws Exception {
        super.channelRead(ctx, msg);
    }

}

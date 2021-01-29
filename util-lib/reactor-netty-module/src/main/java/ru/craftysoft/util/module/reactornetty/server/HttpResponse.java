package ru.craftysoft.util.module.reactornetty.server;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

public record HttpResponse(byte[] message,
                           HttpResponseStatus status,
                           HttpHeaders headers) {
}

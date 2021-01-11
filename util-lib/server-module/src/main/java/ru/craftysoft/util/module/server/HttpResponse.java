package ru.craftysoft.util.module.server;

import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Map;

public record HttpResponse(byte[] message,
                           HttpResponseStatus status,
                           Map<String, String> headers) {
}

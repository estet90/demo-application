package ru.craftysoft.util.module.reactornetty.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.http.server.HttpServerRequest;

import static java.nio.charset.StandardCharsets.UTF_8;

class ServerLoggerHelper {

    private final Logger requestLogger;
    private final Logger responseLogger;

    ServerLoggerHelper(String loggerName) {
        this.requestLogger = LoggerFactory.getLogger(loggerName + ".server.request");
        this.responseLogger = LoggerFactory.getLogger(loggerName + ".server.response");
    }

    void logIn(HttpServerRequest request, byte[] bytes) {
        if (requestLogger.isDebugEnabled()) {
            var uri = request.uri();
            var method = request.method().name();
            var headers = request.requestHeaders().toString();
            if (requestLogger.isTraceEnabled() && bytes.length > 0) {
                requestLogger.trace("\nURI={}\nMethod={}\nHeaders={}\nBody={}", uri, method, headers, new String(bytes, UTF_8));
            } else {
                requestLogger.debug("\nURI={}\nMethod={}\nHeaders={}", uri, method, headers);
            }
        }
    }

    void logIn(HttpServerRequest request) {
        if (requestLogger.isDebugEnabled()) {
            var uri = request.uri();
            var method = request.method().name();
            var headers = request.requestHeaders().toString();
            requestLogger.debug("\nURI={}\nMethod={}\nHeaders={}", uri, method, headers);
        }
    }

    void logOut(HttpResponse httpResponse) {
        if (responseLogger.isDebugEnabled()) {
            var status = httpResponse.status();
            var headers = httpResponse.headers();
            if (responseLogger.isTraceEnabled() && httpResponse.message() != null && httpResponse.message().length > 0) {
                responseLogger.trace("\nStatus={}\nHeaders={}\nBody={}", status, headers, new String(httpResponse.message(), UTF_8));
            } else {
                responseLogger.debug("\nStatus={}\nHeaders={}", status, headers);
            }
        }
    }
}

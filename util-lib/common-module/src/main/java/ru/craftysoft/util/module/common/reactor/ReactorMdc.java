package ru.craftysoft.util.module.common.reactor;

import io.jaegertracing.internal.JaegerTracer;
import io.jaegertracing.internal.MDCScopeManager;
import io.jaegertracing.internal.propagation.B3TextMapCodec;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import org.reactivestreams.Subscription;
import org.slf4j.MDC;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Operators;
import reactor.util.context.Context;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;

public class ReactorMdc {

    public static void init(String serviceName) {
        Hooks.onEachOperator(Operators.lift((scannable, subscriber) -> new MdcContextLifter<>(subscriber, serviceName)));
    }

    private static class MdcContextLifter<T> implements CoreSubscriber<T> {
        private final CoreSubscriber<T> delegate;
        private final Tracer tracer;
        private final Span span;

        public MdcContextLifter(CoreSubscriber<T> delegate, String serviceName) {
            this.delegate = delegate;
            var scopeManager = new MDCScopeManager.Builder().build();
            this.tracer = new JaegerTracer.Builder(serviceName)
                    .withScopeManager(scopeManager)
                    .registerExtractor(Format.Builtin.HTTP_HEADERS, new B3TextMapCodec.Builder().build())
                    .registerInjector(Format.Builtin.HTTP_HEADERS, new B3TextMapCodec.Builder().build())
                    .build();
            this.span = delegate.currentContext().getOrDefault("span", this.tracer.buildSpan("logging").start());
        }

        @Override
        public void onSubscribe(@Nonnull Subscription subscription) {
            var ctx = currentContext();
            var mdc = ctx.getOrDefault("mdc", Map.<String, String>of());
            var oldMdc = MDC.getCopyOfContextMap();
            MDC.setContextMap(mdc);
            try (var ignored = tracer.scopeManager().activate(span)) {
                this.delegate.onSubscribe(subscription);
            } finally {
                MDC.setContextMap(Objects.requireNonNullElseGet(oldMdc, Map::of));
            }
        }

        @Override
        public void onNext(T t) {
            var ctx = currentContext();
            var mdc = ctx.getOrDefault("mdc", Map.<String, String>of());
            var oldMdc = MDC.getCopyOfContextMap();
            MDC.setContextMap(mdc);
            try (var ignored = tracer.scopeManager().activate(span)) {
                this.delegate.onNext(t);
            } finally {
                MDC.setContextMap(Objects.requireNonNullElseGet(oldMdc, Map::of));
            }
        }

        @Override
        public void onError(Throwable throwable) {
            var ctx = currentContext();
            var mdc = ctx.getOrDefault("mdc", Map.<String, String>of());
            var oldMdc = MDC.getCopyOfContextMap();
            MDC.setContextMap(mdc);
            try (var ignored = tracer.scopeManager().activate(span)) {
                this.delegate.onError(throwable);
            } finally {
                MDC.setContextMap(Objects.requireNonNullElseGet(oldMdc, Map::of));
            }
        }

        @Override
        public void onComplete() {
            var ctx = currentContext();
            var mdc = ctx.getOrDefault("mdc", Map.<String, String>of());
            var oldMdc = MDC.getCopyOfContextMap();
            MDC.setContextMap(mdc);
            try (var ignored = tracer.scopeManager().activate(span)) {
                this.delegate.onComplete();
            } finally {
                if (oldMdc != null) {
                    MDC.setContextMap(oldMdc);
                } else {
                    MDC.clear();
                }
            }
        }

        @Override
        @Nonnull
        public Context currentContext() {
            return this.delegate.currentContext();
        }
    }

}

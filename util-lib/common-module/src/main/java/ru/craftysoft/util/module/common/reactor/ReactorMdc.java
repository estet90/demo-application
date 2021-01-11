package ru.craftysoft.util.module.common.reactor;

import org.reactivestreams.Subscription;
import org.slf4j.MDC;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Operators;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static java.util.Optional.ofNullable;

public class ReactorMdc {

    public static void init() {
        Hooks.onEachOperator(Operators.lift((scannable, subscriber) -> new MdcContextLifter<>(subscriber)));
    }

    private static class MdcContextLifter<T> implements CoreSubscriber<T> {
        private final CoreSubscriber<T> delegate;

        public MdcContextLifter(CoreSubscriber<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public void onSubscribe(@Nonnull Subscription s) {
            var ctx = this.delegate.currentContext();
            var mdc = ctx.getOrDefault("mdc", Map.<String, String>of());
            var oldMdc = MDC.getCopyOfContextMap();
            MDC.setContextMap(mdc);
            try {
                this.delegate.onSubscribe(s);
            } finally {
                MDC.setContextMap(Objects.requireNonNullElseGet(oldMdc, Map::of));
            }
        }

        @Override
        public void onNext(T t) {
            var ctx = this.delegate.currentContext();
            var mdc = ctx.getOrDefault("mdc", Map.<String, String>of());
            var oldMdc = MDC.getCopyOfContextMap();
            MDC.setContextMap(mdc);
            try {
                this.delegate.onNext(t);
            } finally {
                MDC.setContextMap(Objects.requireNonNullElseGet(oldMdc, Map::of));
            }
        }

        @Override
        public void onError(Throwable throwable) {
            var ctx = this.delegate.currentContext();
            var mdc = ctx.getOrDefault("mdc", Map.<String, String>of());
            var oldMdc = MDC.getCopyOfContextMap();
            MDC.setContextMap(mdc);
            try {
                this.delegate.onError(throwable);
            } finally {
                MDC.setContextMap(Objects.requireNonNullElseGet(oldMdc, Map::of));
            }
        }

        @Override
        public void onComplete() {
            var ctx = this.delegate.currentContext();
            var mdc = ctx.getOrDefault("mdc", Map.<String, String>of());
            var oldMdc = MDC.getCopyOfContextMap();
            MDC.setContextMap(mdc);
            try {
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

package ru.craftysoft.util.module.common.reactor;

import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Operators;
import reactor.util.context.Context;

import javax.annotation.Nonnull;

import static ru.craftysoft.util.module.common.reactor.MdcUtils.withContext;

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
        public void onSubscribe(@Nonnull Subscription subscription) {
            withContext(currentContext(), () -> this.delegate.onSubscribe(subscription));
        }

        @Override
        public void onNext(T t) {
            withContext(currentContext(), () -> this.delegate.onNext(t));
        }

        @Override
        public void onError(Throwable throwable) {
            withContext(currentContext(), () -> this.delegate.onError(throwable));
        }

        @Override
        public void onComplete() {
            withContext(currentContext(), this.delegate::onComplete);
        }

        @Override
        @Nonnull
        public Context currentContext() {
            return this.delegate.currentContext();
        }
    }

}

package ru.craftysoft.util.module.db;

import io.vertx.sqlclient.Tuple;
import org.slf4j.Logger;
import reactor.core.publisher.Signal;
import reactor.util.context.ContextView;
import ru.craftysoft.util.module.common.reactor.SwitchMdc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Objects.nonNull;
import static reactor.core.publisher.SignalType.ON_ERROR;

public class DbLoggerHelper {

    static void logIn(Logger log, String point, String sql) {
        if (log.isDebugEnabled()) {
            if (log.isTraceEnabled()) {
                log.trace("{}.in execute query\n{}", point, sql);
            } else {
                log.debug("{}.in", point);
            }
        }
    }

    static void logIn(Logger log, String point, String sql, Tuple args) {
        if (log.isDebugEnabled()) {
            if (log.isTraceEnabled()) {
                log.trace("{}.in execute query\n{}\nwith parameters: {}", point, sql, getParameters(args));
            } else {
                log.debug("{}.in execute query with parameters: {}", point, getParameters(args));
            }
        }
    }

    static void logIn(Logger log, String point, String sql, List<Tuple> args) {
        if (log.isDebugEnabled()) {
            if (log.isTraceEnabled()) {
                log.trace("{}.in execute query\n{}\nwith parameters: {}", point, sql, getParameters(args));
            } else {
                log.debug("{}.in execute query with parameters: {}", point, getParameters(args));
            }
        }
    }

    private static List<Object> getParameters(Tuple args) {
        var size = args.size();
        var list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(args.getValue(i));
        }
        return list;
    }

    private static List<List<Object>> getParameters(List<Tuple> list) {
        var result = new ArrayList<List<Object>>(list.size());
        for (var tuple : list) {
            result.add(getParameters(tuple));
        }
        return result;
    }

    static void logError(Logger log, String point, Signal<?> signal) {
        if (ON_ERROR.equals(signal.getType()) && nonNull(signal.getThrowable())) {
            DbLoggerHelper.withQueryId(signal.getContextView(), () -> log.error("{}.thrown {}", point, signal.getThrowable().getMessage()));
        }
    }

    static void withQueryId(ContextView context, Runnable runnable) {
        try (var ignored = new SwitchMdc(context, Map.of(MdcKey.QUERY_ID, UUID.randomUUID().toString()))) {
            runnable.run();
        }
    }

}

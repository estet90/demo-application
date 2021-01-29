package ru.craftysoft.util.module.db;

import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import org.jooq.Query;
import org.slf4j.Logger;
import reactor.util.context.ContextView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static ru.craftysoft.util.module.common.reactor.MdcUtils.withContext;

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

    static void logIn(Logger log, String point, Query query) {
        if (log.isDebugEnabled()) {
            if (log.isTraceEnabled()) {
                log.trace("{}.in execute query\n{}", point, query);
            } else {
                log.trace("{}.in execute query with parameters: {}", point, query.getBindValues());
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

    static <T> void logOut(Logger log, String point, ContextView context, Function<Row, T> mapper, RowSet<Row> rowSet) {
        if (log.isTraceEnabled()) {
            var mapperResults = new ArrayList<T>();
            for (var row : rowSet) {
                var mapperResult = mapper.apply(row);
                mapperResults.add(mapperResult);
            }
            withContext(context, () -> log.trace("{}.out result={}", point, mapperResults));
        } else {
            withContext(context, () -> log.debug("{}.out size={}", point, rowSet.size()));
        }
    }

    static <T> void logOut(Logger log, String point, ContextView context, RowSet<Row> rowSet, T mapperResult) {
        if (log.isTraceEnabled()) {
            withContext(context, () -> log.trace("{}.out result={}", point, mapperResult));
        } else {
            withContext(context, () -> log.debug("{}.out size={}", point, rowSet.size()));
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

}

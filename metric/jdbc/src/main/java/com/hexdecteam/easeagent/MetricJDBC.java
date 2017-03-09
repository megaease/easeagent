package com.hexdecteam.easeagent;

import com.google.auto.service.AutoService;
import com.hexdecteam.easeagent.MetricEvents.Update;
import com.hexdecteam.easeagent.Transformation.Feature.Compoundable;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.matcher.ElementMatchers;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.hexdecteam.easeagent.TypeMatchers.*;
import static com.hexdecteam.easeagent.TypeMatchers.any;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static net.bytebuddy.matcher.ElementMatchers.*;

@AutoService(Plugin.class)
public class MetricJDBC extends Transformation<MetricJDBC.Configuration> {

    @Override
    protected Feature feature(Configuration conf) {
        return new Feature.Compound(Arrays.asList(
                new Compoundable(
                        isSubTypeOf(PreparedStatement.class).or(isSubTypeOf(Statement.class))
                ) {
                    final String key = UUID.randomUUID().toString();

                    @Override
                    protected Builder<?> config(Builder<?> b) {
                        return b.visit(Advice.withCustomMapping()
                                             .bind(ForwardDetection.Key.class, key)
                                             .to(StatementAdvice.class)
                                             .on(nameStartsWith("execute")
                                                     .and(ElementMatchers.<MethodDescription>isPublic())));
                    }
                },
                new Compoundable(
                        conf.data_source_classes().isEmpty()
                                ? isSubTypeOf(DataSource.class)
                                : any(NAMED, conf.data_source_classes())
                ) {
                    final String key = UUID.randomUUID().toString();

                    @Override
                    protected Builder<?> config(Builder<?> b) {
                        return b.visit(Advice.withCustomMapping()
                                             .bind(ForwardDetection.Key.class, key)
                                             .to(DataSourceAdvice.class)
                                             .on(named("getConnection").and(returns(isSubTypeOf(Connection.class)))));
                    }
                },
                new Caller.Feature(compound(
                        NAME_STARTS_WITH,
                        conf.include_caller_class_prefix_list(),
                        conf.exclude_caller_class_prefix_list()
                ).and(not(nameContains("CGLIB$$"))))
        ));
    }

    @ConfigurationDecorator.Binding("metric.jdbc")
    static abstract class Configuration {
        /**
         * Target {@link javax.sql.DataSource} implementations for metrics.
         *
         * @return empty as default.
         */
        List<String> data_source_classes() {return Collections.emptyList();}

        List<String> exclude_caller_class_prefix_list() { return Collections.emptyList();}

        List<String> include_caller_class_prefix_list() { return Collections.emptyList();}

    }


    static class DataSourceAdvice {
        @Advice.OnMethodEnter
        public static Object[] enter(@ForwardDetection.Key String key) {
            return new Object[]{ForwardDetection.Mark.markIfAbsent(key), System.nanoTime()};
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public static void exit(@ForwardDetection.Key String key, @Advice.Enter Object[] enter, @Advice.Return Connection connection)
                throws SQLException {
            final boolean marked = (Boolean) enter[0];

            if (!marked) return;

            ForwardDetection.Mark.clear(key);

            final long started = (Long) enter[1];

            final DatabaseMetaData data = connection.getMetaData();
            final long duration = System.nanoTime() - started;
            final String url = data.getURL() + '-' + data.getUserName();
            EventBus.publish(new Update("get_jdbc_connection", duration, NANOSECONDS).tag("url", url));
            // TODO lazy calculation in streaming
            EventBus.publish(new Update("get_jdbc_connection", duration, NANOSECONDS).tag("url", "All"));
        }
    }

    static class StatementAdvice {
        @Advice.OnMethodEnter
        public static Object[] enter(@ForwardDetection.Key String key) {
            return new Object[]{ForwardDetection.Mark.markIfAbsent(key), System.nanoTime()};
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public static void exit(@ForwardDetection.Key String key, @Advice.Enter Object[] enter) throws SQLException {
            final boolean marked = (Boolean) enter[0];

            if (!marked) return;

            ForwardDetection.Mark.clear(key);

            final long started = (Long) enter[1];
            final long duration = System.nanoTime() - started;

            EventBus.publish(new Update("jdbc_statement", duration, NANOSECONDS).tag("signature", "All"));

            final String caller = SignatureHolder.CALLER.get();

            if (caller == null) return;

            EventBus.publish(new Update("jdbc_statement", duration, NANOSECONDS).tag("signature", caller));
        }
    }

}

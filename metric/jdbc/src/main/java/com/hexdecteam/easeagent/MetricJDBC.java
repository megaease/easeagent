package com.hexdecteam.easeagent;

import com.google.auto.service.AutoService;
import com.google.common.base.Function;
import com.hexdecteam.easeagent.MetricEvents.Update;
import com.hexdecteam.easeagent.ReduceF.BiFunction;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.matcher.ElementMatcher.Junction;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import javax.sql.DataSource;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.sql.*;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Iterators.transform;
import static com.hexdecteam.easeagent.ReduceF.reduce;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static net.bytebuddy.matcher.ElementMatchers.*;

@AutoService(Plugin.class)
public class MetricJDBC extends Transformation<MetricJDBC.Configuration> {

    public static final BiFunction<Junction<TypeDescription>>       OR    = new BiFunction<Junction<TypeDescription>>() {
        @Override
        public Junction<TypeDescription> apply(Junction<TypeDescription> l, Junction<TypeDescription> r) {
            return l.or(r);
        }
    };
    public static final Function<String, Junction<TypeDescription>> NAMED = new Function<String, Junction<TypeDescription>>() {
        @Override
        public Junction<TypeDescription> apply(String input) {
            return named(input);
        }
    };

    @Override
    protected Feature feature(Configuration conf) {
        final List<String> names = conf.data_source_classes();
        return new Feature() {
            @Override
            public Junction<TypeDescription> type() {
                return (names.isEmpty() ? isSubTypeOf(DataSource.class)
                                        : reduce(transform(names.iterator(), NAMED), OR))
                        .or(isSubTypeOf(PreparedStatement.class))
                        .or(isSubTypeOf(Statement.class));
            }

            @Override
            public AgentBuilder.Transformer transformer() {
                final String connectionKey = UUID.randomUUID().toString();
                final String statementKey = UUID.randomUUID().toString();
                return new AgentBuilder.Transformer() {
                    @Override
                    public Builder<?> transform(Builder<?> b, TypeDescription td, ClassLoader cld, JavaModule m) {
                        if (td.isAssignableTo(DataSource.class)) {
                            final Junction<MethodDescription> getConnection = named("getConnection")
                                    .and(returns(isSubTypeOf(Connection.class)));
                            return b.visit(Advice.withCustomMapping()
                                                 .bind(Key.class, connectionKey)
                                                 .to(DataSourceAdvice.class).on(getConnection));
                        } else {
                            final Junction<MethodDescription> execute = nameStartsWith("execute")
                                    .and(ElementMatchers.<MethodDescription>isPublic());
                            return b.visit(Advice.withCustomMapping()
                                                 .bind(Key.class, statementKey)
                                                 .to(StatementAdvice.class).on(execute));
                        }
                    }
                };
            }
        };
    }

    @ConfigurationDecorator.Binding("metric.jdbc")
    static abstract class Configuration {
        /**
         * Target {@link javax.sql.DataSource} implementations for metrics.
         *
         * @return empty as default.
         */
        List<String> data_source_classes() {return Collections.emptyList();}
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Key {}

    static class DataSourceAdvice {
        @Advice.OnMethodEnter
        public static Object[] enter(@Key String key) {
            return new Object[]{ForwardDetection.markIfAbsent(key), System.nanoTime()};
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public static void exit(@Key String key, @Advice.Enter Object[] enter, @Advice.Return Connection connection)
                throws SQLException {
            final boolean marked = (Boolean) enter[0];

            if (!marked) return;

            ForwardDetection.clear(key);

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
        public static Object[] enter(@Key String key) {
            return new Object[]{ForwardDetection.markIfAbsent(key), System.nanoTime()};
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public static void exit(@Key String key, @Advice.Enter Object[] enter) throws SQLException {
            final boolean marked = (Boolean) enter[0];

            if (!marked) return;

            ForwardDetection.clear(key);

            final long started = (Long) enter[1];
            final long duration = System.nanoTime() - started;

            EventBus.publish(new Update("jdbc_statement", duration, NANOSECONDS).tag("signature", "All"));
            // TODO support get signature
        }
    }
}

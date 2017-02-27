package com.hexdecteam.easeagent;

import com.google.auto.service.AutoService;
import com.hexdecteam.easeagent.MetricEvents.Update;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.matcher.ElementMatcher.Junction;
import net.bytebuddy.matcher.ElementMatchers;

import javax.sql.DataSource;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
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

        final BindKeyFeature jdbcStatement = new BindKeyFeature(
                isSubTypeOf(PreparedStatement.class).or(isSubTypeOf(Statement.class)),
                nameStartsWith("execute").and(ElementMatchers.<MethodDescription>isPublic()),
                StatementAdvice.class
        );
        final BindKeyFeature getConnection = new BindKeyFeature(
                conf.data_source_classes().isEmpty() ? isSubTypeOf(DataSource.class) : any(NAMED, conf.data_source_classes()),
                named("getConnection").and(returns(isSubTypeOf(Connection.class))),
                DataSourceAdvice.class
        );
        final Caller.Feature callerFeature = new Caller.Feature(compound(
                NAME_STARTS_WITH,
                conf.include_caller_class_prefix_list(),
                conf.exclude_caller_class_prefix_list()
        ).and(not(nameContains("CGLIB$$"))));

        return new Feature.Compound(Arrays.asList(jdbcStatement, getConnection, callerFeature));
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

    static class BindKeyFeature extends Feature.Compoundable {

        final Junction<MethodDescription> method;
        final String key;
        final Class<?> adviceClass;

        BindKeyFeature(Junction<TypeDescription> type, Junction<MethodDescription> method, Class<?> adviceClass) {
            super(type);
            this.method = method;
            this.adviceClass = adviceClass;
            key = UUID.randomUUID().toString();
        }

        @Override
        protected Builder<?> config(Builder<?> b) {
            return b.visit(Advice.withCustomMapping().bind(Key.class, key).to(adviceClass).on(method));
        }
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

            final String caller = SignatureHolder.CALLER.get();

            if (caller == null) return;

            EventBus.publish(new Update("jdbc_statement", duration, NANOSECONDS).tag("signature", caller));
        }
    }

}

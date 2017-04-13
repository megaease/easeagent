package com.megaease.easeagent.zipkin;

import brave.Span;
import brave.Tracer;
import com.megaease.easeagent.common.CallTrace;
import com.megaease.easeagent.common.ForwardLock;
import com.megaease.easeagent.core.AdviceTo;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Injection;
import com.megaease.easeagent.core.Transformation;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.sql.PreparedStatement;
import java.sql.Statement;

import static net.bytebuddy.matcher.ElementMatchers.*;

@Injection.Provider(Provider.class)
public abstract class TraceJdbcStatement implements Transformation {
    @Override
    public <T extends Definition> T define(Definition<T> def) {
        // com.mysql.jdbc.PreparedStatement#asSql()
        final ElementMatcher.Junction<MethodDescription> asSql = named("asSql")
                .and(ElementMatchers.<MethodDescription>isPublic())
                .and(returns(String.class)).and(takesArguments(0));
        return def.type(isSubTypeOf(Statement.class))
                  .transform(statement(nameStartsWith("execute").and(takesArgument(0, String.class))))
                  .type(isSubTypeOf(PreparedStatement.class).and(declaresMethod(asSql)))
                  .transform(preparedStatement(nameStartsWith("execute").and(takesArguments(0))))
                  .end();
    }

    @AdviceTo(ExecutePreparedSql.class)
    abstract Definition.Transformer preparedStatement(ElementMatcher<? super MethodDescription> matcher);

    @AdviceTo(ExecuteSql.class)
    abstract Definition.Transformer statement(ElementMatcher<? super MethodDescription> matcher);

    static class ExecuteSql extends AbstractExecuteSql {
        @Injection.Autowire
        ExecuteSql(CallTrace trace, Tracer tracer) {
            super(trace, tracer);
        }

        @Override
        @Advice.OnMethodEnter
        void enter(@Advice.Origin String method) {
            super.enter(method);
        }

        @Override
        @Advice.OnMethodExit(onThrowable = Throwable.class)
        void exit(@Advice.Origin String method, @Advice.This Statement stmt, @Advice.Thrown Throwable error, @Advice.Argument(0) String sql) {
            exit(method, stmt, error, sql);
        }
    }

    static class ExecutePreparedSql extends AbstractExecuteSql {
        private final Logger logger = LoggerFactory.getLogger(getClass());

        @Injection.Autowire
        ExecutePreparedSql(CallTrace trace, Tracer tracer) {
            super(trace, tracer);
        }

        @Override
        @Advice.OnMethodEnter
        void enter(@Advice.Origin String method) {
            super.enter(method);
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        void exit(@Advice.Origin String method, @Advice.This Statement stmt, @Advice.Thrown Throwable error) {
            try {
                exit(method, stmt, error, stmt.getClass().getMethod("asSql").invoke(stmt).toString());
            } catch (Exception e) {
                logger.error("Failed to get sql", e);
            }
        }
    }

    static abstract class AbstractExecuteSql {
        final CallTrace trace;
        final ForwardLock lock;
        final Tracer tracer;

        AbstractExecuteSql(CallTrace trace, Tracer tracer) {
            this.trace = trace;
            this.lock = new ForwardLock();
            this.tracer = tracer;
        }

        void enter(String method) {
            if (!lock.acquire(method) || trace.peek() == null) return;

            trace.push(tracer.newChild(trace.peek().<Span>context().context()).start());
        }

        void exit(String method, Statement stmt, Throwable error, String sql) throws Exception {
            if (!lock.release(method) || trace.peek() == null) return;

            final String url = stmt.getConnection().getMetaData().getURL();
            final URI uri = URI.create(url.substring(5));
            trace.pop().<Span>context().name("jdbc_statement")
                                       .kind(Span.Kind.CLIENT)
                                       .tag("component", "jdbc")
                                       .tag("span.kind", "client")
                                       .tag("jdbc.url", url)
                                       .tag("jdbc.sql", sql)
                                       .tag("jdbc.result", String.valueOf(error == null))
                                       .tag("has.error", String.valueOf(error != null))
                                       .tag("remote.address", uri.getHost() + ":" + (uri.getPort() == -1 ? 3306 : uri.getPort()))
                                       .finish();
        }
    }
}

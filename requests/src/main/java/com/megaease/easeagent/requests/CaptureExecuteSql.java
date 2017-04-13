package com.megaease.easeagent.requests;

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

import java.sql.PreparedStatement;
import java.sql.Statement;

import static net.bytebuddy.matcher.ElementMatchers.*;

@Injection.Provider(Provider.class)
public abstract class CaptureExecuteSql implements Transformation {
    @Override
    public <T extends Definition> T define(Definition<T> def) {
        // com.mysql.jdbc.PreparedStatement#asSql()
        final ElementMatcher<MethodDescription> asSql = named("asSql")
                .and(ElementMatchers.<MethodDescription>isPublic())
                .and(returns(String.class)).and(takesArguments(0));
        return def.type(isSubTypeOf(Statement.class))
                  .transform(statement(nameStartsWith("execute").and(takesArgument(0, String.class))))
                  .type(isSubTypeOf(PreparedStatement.class).and(declaresMethod(asSql)))
                  .transform(preparedStatement(nameStartsWith("execute").and(takesArguments(0))))
                  .end();
    }

    @AdviceTo(MethodWithoutSql.class)
    abstract Definition.Transformer preparedStatement(ElementMatcher<? super MethodDescription> matcher);

    @AdviceTo(MethodWithSql.class)
    abstract Definition.Transformer statement(ElementMatcher<? super MethodDescription> matcher);

    static class MethodWithoutSql extends AbstractMethod {
        private final Logger logger = LoggerFactory.getLogger(getClass());

        @Injection.Autowire
        MethodWithoutSql(CallTrace trace) {
            super(trace);
        }

        @Advice.OnMethodEnter
        boolean enter(@Advice.Origin String method, @Advice.Origin Class<?> aClass, @Advice.Origin("#m") String methodName,
                      @Advice.This Object self) {
            try {
                return super.enter(method, aClass, methodName, self.getClass().getMethod("asSql").invoke(self).toString());
            } catch (Exception e) {
                logger.error("Failed to get sql", e);
                return false;
            }
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        void exit(@Advice.Origin String method, @Advice.Enter boolean forked) {
            super.exit(method, forked);
        }

    }

    static class MethodWithSql extends AbstractMethod {
        @Injection.Autowire
        MethodWithSql(CallTrace trace) {
            super(trace);
        }

        @Override
        @Advice.OnMethodEnter
        boolean enter(@Advice.Origin String method, @Advice.Origin Class<?> aClass, @Advice.Origin("#m") String methodName,
                      @Advice.Argument(0) String sql) {
            return super.enter(method, aClass, methodName, sql);
        }

        @Override
        @Advice.OnMethodExit(onThrowable = Throwable.class)
        void exit(@Advice.Origin String method, @Advice.Enter boolean forked) {
            super.exit(method, forked);
        }

    }

    abstract static class AbstractMethod {
        final ForwardLock lock;
        final CallTrace trace;

        AbstractMethod(CallTrace trace) {
            this.lock = new ForwardLock();
            this.trace = trace;
        }

        boolean enter(String method, Class<?> aClass, String methodName, String sql) {
            return lock.acquire(method) && Context.forkIo(trace, aClass, methodName, sql);
        }

        void exit(String method, boolean forked) {
            lock.release(method);
            if (forked) Context.join(trace);
        }
    }
}

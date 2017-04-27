package com.megaease.easeagent.metrics;

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

import java.sql.Statement;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static net.bytebuddy.matcher.ElementMatchers.isSubTypeOf;
import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;

@Injection.Provider(Provider.class)
public abstract class MeasureJdbcStatement implements Transformation {
    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def.type(isSubTypeOf(Statement.class))
                  .transform(execute(nameStartsWith("execute").and(ElementMatchers.<MethodDescription>isPublic())))
                  .end();
    }

    @AdviceTo(Execute.class)
    abstract Definition.Transformer execute(ElementMatcher<? super MethodDescription> matcher);

    static class Execute {
        static final String SIGNATURE = "signature";
        static final String JDBC_STATEMENT = "jdbc_statement";

        private final CallTrace trace;
        private final ForwardLock lock;
        private final Metrics metrics;

        @Injection.Autowire
        Execute(CallTrace trace, Metrics metrics) {
            this.trace = trace;
            this.lock = new ForwardLock();
            this.metrics = metrics;
        }

        @Advice.OnMethodEnter
        ForwardLock.Release<Long> enter() {
            return lock.acquire(new ForwardLock.Supplier<Long>() {
                @Override
                public Long get() {
                    return System.nanoTime();
                }
            }) ;
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        void exit(@Advice.Enter ForwardLock.Release<Long> release) {
            release.apply(new ForwardLock.Consumer<Long>() {
                @Override
                public void accept(Long begin) {
                    final CallTrace.Frame frame = trace.peek();
                    if (frame == null) return;

                    final long duration = System.nanoTime() - begin;

                    metrics.timer(JDBC_STATEMENT).tag(SIGNATURE, frame.<Context>context().signature).get().update(duration, NANOSECONDS);
                    // TODO lazy calculation in streaming
                    metrics.timer(JDBC_STATEMENT).tag(SIGNATURE, "All").get().update(duration, NANOSECONDS);
                }
            });

        }
    }
}

package com.megaease.easeagent.sniffer;

import com.megaease.easeagent.core.AdviceTo;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Injection;
import com.megaease.easeagent.core.Transformation;
import com.megaease.easeagent.core.utils.ThreadContextBind;
import com.megaease.easeagent.core.utils.ThreadLocalCurrentContext;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.bytebuddy.matcher.ElementMatchers.*;

@Injection.Provider(Provider.class)
public abstract class CrossThreadPropagationAdvice implements Transformation {
    public static final String CLASS_THREAD = "java.lang.Thread";
    public static final String CLASS_THREAD_POOL_EXECUTOR = "java.util.concurrent.ThreadPoolExecutor";

    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def
                .type(hasSuperType(named(CLASS_THREAD)).or(named(CLASS_THREAD)))
                .transform(threadInit(named("init").and(takesArguments(6))))
                .transform(threadRun(named("run")
                        .and(takesArguments(0))))
                .type(named(CLASS_THREAD_POOL_EXECUTOR))
                .transform(threadPoolExecutorExecute(named("execute")
                        .and(takesArguments(1))
                        .and(takesArgument(0, named("java.lang.Runnable")))
                ))
                .end();
    }

    @AdviceTo(ThreadInit.class)
    abstract Definition.Transformer threadInit(ElementMatcher<? super MethodDescription> matcher);

    @AdviceTo(ThreadRun.class)
    abstract Definition.Transformer threadRun(ElementMatcher<? super MethodDescription> matcher);

    @AdviceTo(ThreadPoolExecutorExecute.class)
    abstract Definition.Transformer threadPoolExecutorExecute(ElementMatcher<? super MethodDescription> matcher);


    static class ThreadInit {
        private final Logger logger = LoggerFactory.getLogger(getClass());

        @SuppressWarnings("unchecked")
        @Advice.OnMethodEnter
        void enter(@Advice.Origin String method,
                   @Advice.This Thread own,
                   @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] args) {
            logger.debug("enter method [{}]", method);
            Runnable task = (Runnable) args[1];
            if (task == null) {
                final ThreadLocalCurrentContext.Context ctx = ThreadLocalCurrentContext.DEFAULT.get();
                if (ctx != null) {
                    ThreadContextBind.bind(own, ctx);
                }
            } else {
                final Runnable wrap = ThreadLocalCurrentContext.DEFAULT.wrap(task);
                args[1] = wrap;
            }
        }
    }

    static class ThreadRun {
        private final Logger logger = LoggerFactory.getLogger(getClass());

        @SuppressWarnings("unchecked")
        @Advice.OnMethodEnter
        ThreadLocalCurrentContext.Scope enter(@Advice.Origin String method, @Advice.This Thread own) {
            logger.debug("enter method [{}]", method);
            ThreadLocalCurrentContext.Context ctx = ThreadContextBind.get(own);
            if (ctx == null) {
                return null;
            }
            return ThreadLocalCurrentContext.DEFAULT.maybeScope(ctx);
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        void exit(@Advice.Enter ThreadLocalCurrentContext.Scope scope,
                  @Advice.Origin("#m") String method,
                  @Advice.Thrown Throwable throwable
        ) {
            if (scope != null) {
                scope.close();
            }
        }
    }

    static class ThreadPoolExecutorExecute {
        private final Logger logger = LoggerFactory.getLogger(getClass());

        @SuppressWarnings("unchecked")
        @Advice.OnMethodEnter
        void enter(@Advice.Origin String method,
                   @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] args) {
            logger.debug("enter method [{}]", method);
            Runnable task = (Runnable) args[0];
            final Runnable wrap = ThreadLocalCurrentContext.DEFAULT.wrap(task);
            args[0] = wrap;
        }
    }

}

package com.megaease.easeagent.metrics;

import com.megaease.easeagent.common.AnyCall;
import com.megaease.easeagent.common.CallTrace;
import com.megaease.easeagent.core.AdviceTo;
import com.megaease.easeagent.core.Configurable;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Injection;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Collections;
import java.util.List;

@Configurable(bind = "metrics.caller")
@Injection.Provider(Provider.class)
public abstract class CaptureCaller extends AnyCall {
    @Override
    @AdviceTo(Method.class)
    protected abstract Definition.Transformer method(ElementMatcher<? super MethodDescription> matcher);

    @Override
    @Configurable.Item
    protected List<String> include_class_prefix_list() {
        return Collections.emptyList();
    }

    @Override
    @Configurable.Item
    protected List<String> exclude_class_prefix_list() {
        return Collections.emptyList();
    }

    static class Method {
        private final CallTrace trace;

        @Injection.Autowire
        Method(CallTrace trace) {this.trace = trace;}

        @Advice.OnMethodEnter
        boolean enter(@Advice.Origin final Class<?> aClass, @Advice.Origin("#m") final String method) {
            return Context.fork(trace, aClass, method);
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        void exit(@Advice.Enter boolean forked) {
            if (forked) Context.join(trace);
        }
    }
}

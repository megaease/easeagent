package com.megaease.easeagent.requests;

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

@Injection.Provider(Provider.class)
@Configurable(bind = "requests.trace")
public abstract class CaptureTrace extends AnyCall {

    @Override
    @Configurable.Item
    protected List<String> include_class_prefix_list() { return Collections.emptyList();}

    @Override
    @Configurable.Item
    protected List<String> exclude_class_prefix_list() { return Collections.emptyList();}

    @AdviceTo(CaptureTrace.Method.class)
    protected abstract Definition.Transformer method(ElementMatcher<? super MethodDescription> matcher);

    static class Method {
        private final CallTrace trace;

        @Injection.Autowire
        Method(CallTrace trace) {this.trace = trace;}

        @Advice.OnMethodEnter
        boolean enter(@Advice.Origin Class<?> aClass, @Advice.Origin("#m") String method) {
            return Context.forkCall(trace, aClass, method);
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        void exit(@Advice.Enter boolean forked) {
            if (forked) Context.join(trace);
        }

    }


}

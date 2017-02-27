package com.hexdecteam.easeagent;

import com.google.auto.service.AutoService;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher.Junction;

import java.util.Collections;
import java.util.List;

import static com.hexdecteam.easeagent.TypeMatchers.NAME_STARTS_WITH;
import static com.hexdecteam.easeagent.TypeMatchers.compound;
import static net.bytebuddy.matcher.ElementMatchers.nameContains;
import static net.bytebuddy.matcher.ElementMatchers.not;

@AutoService(Plugin.class)
public class TraceStack extends Transformation<TraceStack.Configuration> {

    @Override
    protected Feature feature(final Configuration conf) {
        if (conf.include_class_prefix_list().isEmpty()) return Feature.NO_OP;

        Junction<TypeDescription> type = compound(
                NAME_STARTS_WITH,
                conf.include_class_prefix_list(),
                conf.exclude_class_prefix_list()
        ).and(not(nameContains("CGLIB$$")));

        return new Caller.Feature(type, FrameAdvice.class);
    }

    @ConfigurationDecorator.Binding("trace.stack")
    static abstract class Configuration {
        /**
         * The full name of class starts with the prefix in the list would not be traced.
         *
         * @return empty list as default.
         */
        List<String> exclude_class_prefix_list() { return Collections.emptyList();}

        /**
         * The full name of class starts with the prefix in the list would be traced.
         *
         * @return empty list as default.
         */
        List<String> include_class_prefix_list() { return Collections.emptyList();}

    }

    static class FrameAdvice {
        @Advice.OnMethodEnter
        public static boolean enter(@Caller.Signature String signature) {
            return StackFrame.fork(signature);
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public static void exit(@Advice.Enter boolean forked) {
            if (forked) StackFrame.join();
        }
    }

}

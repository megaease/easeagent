package com.hexdecteam.easeagent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.lang.instrument.Instrumentation;
import java.util.Arrays;

import static net.bytebuddy.matcher.ElementMatchers.*;

public abstract class AbstractTransformation implements Transformation {
    @Override
    public void apply(Instrumentation inst) {
        new AgentBuilder.Default()
                .with(AgentBuilder.LocationStrategy.ForClassLoader.WEAK)
                .with(new DebugListener())
                .type(excludeSelf().and(matcher()), not(isBootstrapClassLoader()))
                .transform(transformer())
                .installOn(inst);
    }

    protected abstract ElementMatcher.Junction<TypeDescription> matcher();

    protected abstract AgentBuilder.Transformer transformer();

    private ElementMatcher.Junction<TypeDescription> excludeSelf() {
        return not(nameStartsWith(selfPackagePrefix()));
    }

    private String selfPackagePrefix() {
        final String name = Transformation.class.getPackage().getName();
        return Arrays.stream(name.split("\\."))
                     .limit(2)
                     .reduce((l, r) -> l + '.' + r)
                     .orElseThrow(IllegalStateException::new);
    }
}

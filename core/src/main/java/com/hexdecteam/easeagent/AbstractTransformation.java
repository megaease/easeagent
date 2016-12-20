package com.hexdecteam.easeagent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.RawMatcher.ForElementMatchers;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher.Junction;

import java.lang.instrument.Instrumentation;
import java.util.Arrays;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * This is a basic implementation of the {@link Transformation} , that makes your works more easier.
 * <p>
 * You properly need to care about two things:
 * <ul>
 * <li>What kind of classes would be transform ? And, </li>
 * <li>how to transform those classes?</li>
 * </ul>
 *
 * @see AgentBuilder
 */
public abstract class AbstractTransformation implements Transformation {
    @Override
    public void apply(Instrumentation inst) {
        new AgentBuilder.Default()
                .with(new DebugListener())
                .type(withDescription())
                .transform(withTransformer())
                .installOn(inst);
    }

    protected abstract Junction<TypeDescription> typesMatched();

    protected abstract Transformer withTransformer();

    private AgentBuilder.RawMatcher withDescription() {
        final Junction<TypeDescription> tdm =
                not(nameStartsWith(packagePrefixOfItSelf())).and(typesMatched());

        return new ForElementMatchers(tdm, not(isBootstrapClassLoader()), any());
    }

    private String packagePrefixOfItSelf() {
        final String name = Transformation.class.getPackage().getName();
        return Arrays.stream(name.split("\\."))
                     .limit(2)
                     .reduce((l, r) -> l + '.' + r)
                     .orElseThrow(IllegalStateException::new);
    }
}

package com.hexdecteam.easeagent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.lang.instrument.Instrumentation;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * This is a basic implementation of the {@link Transformation} , that makes your works more easier.
 * <p>
 * You properly need to care about two things:
 * <ul>
 * <li>What kind of classes would be transformed, and </li>
 * <li>how to transformWith them ?  </li>
 * </ul>
 *
 * @see AgentBuilder
 */
public abstract class AbstractTransformation implements Transformation {
    @Override
    public void apply(Instrumentation inst) {
        transformWith(
                new AgentBuilder.Default()
                        .with(new DebugListener())
                        .ignore(any(), isBootstrapClassLoader())
                        .or(is(selfClassLoader()))
                        .or(isInterface())
                        .or(isAnnotation())
                        .or(nameStartsWith("sun."))
                        .or(nameStartsWith("com.sun."))
                        .or(ignores())
        ).installOn(inst);
    }

    protected abstract ElementMatcher<? super TypeDescription> ignores();

    protected abstract AgentBuilder transformWith(AgentBuilder builder);

    private ClassLoader selfClassLoader() {
        return getClass().getClassLoader();
    }

}

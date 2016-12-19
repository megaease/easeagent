package com.hexdecteam.easeagent;

import java.lang.instrument.Instrumentation;

/**
 * A {@code Transformation} would decorate some java classes during the runtime,
 * like logging, profiling, tracing and so on.
 * <p>
 * Normally, there is no need to implement {@code Transformation} directly, but extend
 * {@link AbstractTransformation} instead.
 *
 * @see AbstractTransformation
 */
public interface Transformation {
    void apply(Instrumentation inst);
}

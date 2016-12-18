package com.hexdecteam.javagent;

import java.lang.instrument.Instrumentation;

public interface Transformation {
    void apply(Instrumentation inst);
}

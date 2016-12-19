package com.hexdecteam.easeagent;

import java.lang.instrument.Instrumentation;

public interface Transformation {
    void apply(Instrumentation inst);
}

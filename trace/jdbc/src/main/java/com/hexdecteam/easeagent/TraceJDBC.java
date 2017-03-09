package com.hexdecteam.easeagent;

import com.google.auto.service.AutoService;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.DynamicType;

import java.sql.Statement;

import static net.bytebuddy.matcher.ElementMatchers.*;

@AutoService(Plugin.class)
public class TraceJDBC extends Transformation<TraceJDBC.NoConfiguration> {

    @Override
    protected Feature feature(NoConfiguration conf) {

        new Feature.Compoundable(isSubTypeOf(Statement.class)) {

            @Override
            protected DynamicType.Builder<?> config(DynamicType.Builder<?> b) {
                return b.visit(Advice.withCustomMapping()
                                     .to(StatementAdvice.class)
                                     .on(nameStartsWith("execute").and(takesArgument(0, String.class))));
            }
        };
        return null;
    }

    interface NoConfiguration {}

    static class StatementAdvice {}
}

package com.megaease.easeagent.gen;

import com.megaease.easeagent.core.AdviceTo;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Transformation;
import net.bytebuddy.asm.Advice.Enter;
import net.bytebuddy.asm.Advice.OnMethodEnter;
import net.bytebuddy.asm.Advice.OnMethodExit;
import net.bytebuddy.asm.Advice.This;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

public abstract class Baz implements Transformation {
    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return null;
    }

    @AdviceTo(Demo.class)
    abstract Definition.Transformer demo(ElementMatcher<? super MethodDescription> matcher);

    abstract static class Demo {
        @OnMethodEnter
        boolean enter(@This Object self) {
            return false;
        }

        @OnMethodExit(onThrowable = Throwable.class)
        void exit(@Enter boolean flag) { }
    }

    static class Ignore {}
}

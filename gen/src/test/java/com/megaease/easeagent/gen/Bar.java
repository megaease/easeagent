package com.megaease.easeagent.gen;

import com.megaease.easeagent.core.*;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.asm.Advice.*;

@Injection.Provider(Qux.class)
@Configurable(bind = "foo.bar")
public abstract class Bar implements Transformation {

    @Configurable.Item
    abstract boolean bool();

    @Configurable.Item
    int i() {return 10;}

    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return null;
    }

    @AdviceTo(Demo.class)
    abstract Definition.Transformer demo(ElementMatcher<? super MethodDescription> matcher);

    abstract static class Demo {

        @Injection.Autowire
        Demo(@Injection.Qualifier("s") String s) { }

        @OnMethodEnter
        @OnMethodExit(onThrowable = Throwable.class)
        boolean run(@This Object self) {
            return false;
        }
    }
}

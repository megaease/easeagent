package com.megaease.easeagent.core;

import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RegisterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    final ClassLoader loader = getClass().getClassLoader();
    final Object args = "args";

    @Test
    public void should_work_with_default_constructor() throws Exception {
        final String adviceClassName = "com.megaease.easeagent.core.RegisterTest$Foo";
        new Register(Collections.<QualifiedBean>emptyList()).apply(adviceClassName, loader);
        assertThat(Dispatcher.execute(adviceClassName + "#enter", args), is(args));
    }

    @Test
    public void should_work_with_autowire_constructor() throws Exception {
        final String adviceClassName = "com.megaease.easeagent.core.RegisterTest$Bar";
        final String value = "#";
        final List<QualifiedBean> beans = Arrays.asList(
                new QualifiedBean("", new Baz()),
                new QualifiedBean("s", value)
        );
        new Register(beans).apply(adviceClassName, loader);
        assertThat(Dispatcher.execute(adviceClassName + "#exit", args), CoreMatchers.<Object>is(value + args));
    }

    @Test
    public void should_complain_missing_bean() throws Exception {
        final String adviceClassName = "com.megaease.easeagent.core.RegisterTest$Bar";
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Miss bean java.lang.String[s] for class " + adviceClassName);
        new Register(Collections.singleton(new QualifiedBean("", new Baz()))).apply(adviceClassName, loader);
    }

    static class Foo {
        Dispatcher.Advice enter() {
            return new Dispatcher.Advice() {
                @Override
                public Object execute(Object... args) {
                    return args[0];
                }
            };
        }
    }

    static class Bar {
        private final String a;

        @Injection.Autowire
        public Bar(Baz baz, @Injection.Qualifier("s") String s) {
            this.a = s;
        }

        Dispatcher.Advice exit() {
            return new Dispatcher.Advice() {
                @Override
                public Object execute(Object... args) {
                    return a + args[0];
                }
            };
        }
    }

    static class Baz {}

}
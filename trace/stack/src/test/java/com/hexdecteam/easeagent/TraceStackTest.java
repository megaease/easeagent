package com.hexdecteam.easeagent;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class TraceStackTest {

    @Test
    public void should_include_foo() throws Exception {
        final List<String> includes = singletonList("com.hexdecteam.easeagent");
        final List<String> excludes = singletonList("java.");
        final Transformation.Feature feature = featureOf(includes, excludes);
        final ElementMatcher.Junction<TypeDescription> type = feature.type();

        assertTrue(type.matches(new TypeDescription.ForLoadedType(Foo.class)));
        assertFalse(type.matches(new TypeDescription.ForLoadedType(String.class)));

    }

    @Test
    public void should_fork_frame() throws Exception {
        final Class<?> transformed = Classes.transform(Foo.class)
                                            .by(featureOf(singletonList(""), Collections.<String>emptyList()))
                                            .load();
        final Method bar = transformed.getMethod("bar");
        final Object instance = transformed.newInstance();

        assertTrue(StackFrame.setRootIfAbsent("a"));
        bar.invoke(instance);
        final List<StackFrame> children = StackFrame.join().getChildren();
        assertThat(children.size(), is(1));
        assertThat(children.get(0).getSignature(), is(bar.toString()));
    }

    static Transformation.Feature featureOf(final List<String> includes, final List<String> excludes) {
        return new TraceStack().feature(new TraceStack.Configuration() {
            @Override
            List<String> include_class_prefix_list() {
                return includes;
            }

            @Override
            List<String> exclude_class_prefix_list() {
                return excludes;
            }
        });
    }

    public static class Foo {
        public void bar() {
            System.out.println("bar");
        }
    }
}
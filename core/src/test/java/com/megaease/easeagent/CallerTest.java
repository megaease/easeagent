package com.megaease.easeagent;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CallerTest {

    final URLClassLoader loader = new URLClassLoader(new URL[0]);

    @Test
    public void should_get_last_caller_signature() throws Exception {
        final Transformer transformer = new Caller.Feature(ElementMatchers.isSubTypeOf(Runnable.class)).transformer();
        final Class<?> l1Class = doTransform(transformer, L1.class);
        final Class<?> l2Class = doTransform(transformer, L2.class);

        final Object l2 = l2Class.getConstructor(Runnable.class).newInstance(new Runnable() {
            @Override
            public void run() {
                assertThat(SignatureHolder.CALLER.get(), is("L2#run"));
            }
        });

        final Runnable l1 = (Runnable) l1Class.getConstructor(Runnable.class).newInstance(l2);
        l1.run();
    }

    private Class<?> doTransform(Transformer transformer, Class<?> aClass) {
        return transformer.transform(new ByteBuddy().redefine(aClass), typeD(aClass), null, null)
                   .make().load(loader).getLoaded();
    }

    private TypeDescription typeD(Class<?> aClass) {
        return new TypeDescription.ForLoadedType(aClass);
    }


    public static class L1 implements Runnable {

        final Runnable delegate;

        public L1(Runnable delegate) {this.delegate = delegate;}

        @Override
        public void run() {
            delegate.run();
        }
    }

    public static class L2 implements Runnable {

        final Runnable delegate;

        public L2(Runnable delegate) {this.delegate = delegate;}

        @Override
        public void run() {
            delegate.run();
        }
    }


}
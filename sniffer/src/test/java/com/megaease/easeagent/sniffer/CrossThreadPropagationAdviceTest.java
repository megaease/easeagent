package com.megaease.easeagent.sniffer;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.matcher.StringMatcher;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.util.Collections;

import static net.bytebuddy.dynamic.ClassFileLocator.ForClassLoader.read;
import static net.bytebuddy.dynamic.loading.ClassInjector.UsingInstrumentation.Target.BOOTSTRAP;
import static net.bytebuddy.matcher.ElementMatchers.*;
import static org.mockito.Matchers.any;

public class CrossThreadPropagationAdviceTest {

    //    @Test
//    public void test_feignLoadBalancer_execute() throws Exception {
//        ClassLoader loader = getClass().getClassLoader();
//        Definition.Default def = new GenCrossThreadPropagationAdvice().define(Definition.Default.EMPTY);
//        Thread thread = (Thread) Classes.transform(CrossThreadPropagationAdvice.CLASS_THREAD)
//                .with(def)
//                .load(loader).get(0).getConstructor()
//                .newInstance();
//
//        thread.start();
//    }
    public static final Class INTERCEPTOR_CLASS = MySystemInterceptor.class;

    public static void main(String[] args) throws Exception {
        final Instrumentation instrumentation = null;//ByteBuddyAgent.install();
        premain("", instrumentation);
    }

    public static void premain(String arg, Instrumentation instrumentation) throws Exception {
//        injectBootstrapClasses(instrumentation);
        new AgentBuilder.Default()
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE)
                .with(AgentBuilder.TypeStrategy.Default.REDEFINE)
                .ignore(new AgentBuilder.RawMatcher.ForElementMatchers(nameStartsWith("net.bytebuddy.").or(isSynthetic()), any(), any()))
                .with(new AgentBuilder.Listener.Filtering(
                        new StringMatcher("java.lang.Thread", StringMatcher.Mode.EQUALS_FULLY),
                        AgentBuilder.Listener.StreamWriting.toSystemOut()))
                .type(named("java.lang.Thread"))
                .transform((builder, type, classLoader, module) ->
                        builder.visit(Advice.to(INTERCEPTOR_CLASS).on(named("run")))
                )
                .installOn(instrumentation);
    }

    private static void injectBootstrapClasses(Instrumentation instrumentation) throws IOException {
        File temp = Files.createTempDirectory("tmp").toFile();
        temp.deleteOnExit();

        ClassInjector.UsingInstrumentation.of(temp, BOOTSTRAP, instrumentation)
                .inject(Collections.singletonMap(new TypeDescription.ForLoadedType(INTERCEPTOR_CLASS), read(INTERCEPTOR_CLASS)));
    }

    public static class MySystemInterceptor {

        @Advice.OnMethodEnter()
        public static void setSecurityManager() {
            System.out.println("######## intercept ###########");
        }
    }
}
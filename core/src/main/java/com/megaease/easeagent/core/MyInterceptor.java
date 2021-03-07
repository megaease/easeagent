package com.megaease.easeagent.core;

import net.bytebuddy.asm.Advice;

public class MyInterceptor {

    @Advice.OnMethodEnter
    public static void enter(@Advice.This(optional = true) Object invoker,
                             @Advice.Origin("#m") String method,
                             @Advice.AllArguments Object[] args) {
        MyInterceptor.enterInner(method);
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void exit(
            @Advice.This(optional = true) Object invoker,
            @Advice.Origin("#m") String method,
            @Advice.AllArguments Object[] args,
            @Advice.Thrown Exception throwable
    ) {
        MyInterceptor.exitInner(method);
    }

    public static void enterInner(String method) {
        System.out.println(" invoke enter " + method);
    }

    public static void exitInner(String method) {
        System.out.println(" invoke exit " + method);
    }
}

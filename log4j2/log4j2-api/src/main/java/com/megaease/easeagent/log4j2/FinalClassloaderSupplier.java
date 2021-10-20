package com.megaease.easeagent.log4j2;

import java.util.function.Supplier;

public class FinalClassloaderSupplier implements Supplier<ClassLoader> {
    public static volatile ClassLoader CLASSLOADER = null;


    @Override
    public ClassLoader get() {
        return CLASSLOADER;
    }
}

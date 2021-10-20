package com.megaease.easeagent.log4j2.supplier;

import java.net.URL;
import java.util.function.Supplier;

public class SystemUrlsSupplier implements Supplier<URL[]> {
    private final ClassloaderUrlsSupplier classloaderUrlsSupplier;

    public SystemUrlsSupplier() {
        this.classloaderUrlsSupplier = new ClassloaderUrlsSupplier(ClassLoader.getSystemClassLoader());
    }

    @Override
    public URL[] get() {
        return classloaderUrlsSupplier.get();
    }
}

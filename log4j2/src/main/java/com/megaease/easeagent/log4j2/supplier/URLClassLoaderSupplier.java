package com.megaease.easeagent.log4j2.supplier;

import javax.annotation.Nonnull;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;
import java.util.function.Supplier;

public class URLClassLoaderSupplier implements Supplier<ClassLoader> {
    private final Supplier<URL[]> urlSpplier;

    public URLClassLoaderSupplier(@Nonnull Supplier<URL[]> urlSpplier) {
        this.urlSpplier = urlSpplier;
    }

    @Override
    public ClassLoader get() {
        return new URLClassLoader(Objects.requireNonNull(urlSpplier.get(), "urls must not be null."), null);
    }
}

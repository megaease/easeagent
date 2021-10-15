package com.megaease.easeagent.log4j2.supplier;

import java.net.URL;
import java.util.function.Supplier;

public class JarUrlsSupplier implements Supplier<URL[]> {
    private final Supplier<URL[]>[] suppliers;

    public JarUrlsSupplier(Supplier<URL[]>... suppliers) {
        this.suppliers = suppliers;
    }


    @Override
    public URL[] get() {
        for (Supplier<URL[]> supplier : suppliers) {
            URL[] urls = supplier.get();
            if (urls != null && urls.length > 0) {
                return urls;
            }
        }
        return null;
    }
}

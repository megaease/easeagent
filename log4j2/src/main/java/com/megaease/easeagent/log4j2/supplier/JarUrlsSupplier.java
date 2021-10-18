package com.megaease.easeagent.log4j2.supplier;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class JarUrlsSupplier implements Supplier<URL[]> {
    private final Supplier<URL[]>[] suppliers;

    public JarUrlsSupplier(Supplier<URL[]>... suppliers) {
        this.suppliers = suppliers;
    }


    @Override
    public URL[] get() {
        List<URL> list = new ArrayList<>();
        for (Supplier<URL[]> supplier : suppliers) {
            URL[] urls = supplier.get();
            if (urls != null && urls.length > 0) {
                list.addAll(Arrays.asList(urls));
            }
        }
        URL[] result = new URL[list.size()];
        list.toArray(result);
        return result;
    }
}

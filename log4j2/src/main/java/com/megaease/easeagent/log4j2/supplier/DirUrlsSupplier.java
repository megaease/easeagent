package com.megaease.easeagent.log4j2.supplier;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Supplier;

public class DirUrlsSupplier implements Supplier<URL[]> {
    private final String dir;

    public DirUrlsSupplier(String dir) {
        this.dir = dir;
    }

    @Override
    public URL[] get() {
        File file = new File(dir);
        if (!file.isDirectory()) {
            return null;
        }
        File[] files = file.listFiles();
        URL[] urls = new URL[files.length];
        for (int i = 0; i < files.length; i++) {
            try {
                urls[i] = files[i].toURI().toURL();
            } catch (MalformedURLException e) {
                return null;
            }
        }
        return urls;
    }
}

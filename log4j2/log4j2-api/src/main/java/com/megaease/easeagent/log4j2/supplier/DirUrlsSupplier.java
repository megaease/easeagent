package com.megaease.easeagent.log4j2.supplier;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Supplier;

public class DirUrlsSupplier implements Supplier<URL[]> {
    public static final String LIB_DIR_ENV = "EASEAGENT-SLF4J2-LIB-DIR";

    public DirUrlsSupplier() {
    }

    @Override
    public URL[] get() {
        String dir = System.getProperty(LIB_DIR_ENV);
        if (dir == null) {
            return new URL[0];
        }
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

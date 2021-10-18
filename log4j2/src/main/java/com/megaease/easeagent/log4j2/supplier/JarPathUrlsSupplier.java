package com.megaease.easeagent.log4j2.supplier;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class JarPathUrlsSupplier implements Supplier<URL[]> {
    public static final String EASEAGENT_SLF4_J2_LIB_JAR_PATHS = "EASEAGENT-SLF4J2-LIB-JAR-PATHS";

    @Override
    public URL[] get() {
        String dir = System.getProperty(EASEAGENT_SLF4_J2_LIB_JAR_PATHS);
        if (dir == null) {
            return new URL[0];
        }
        String[] paths = dir.split(",");
        List<URL> urls = new ArrayList<>();
        for (String path : paths) {
            if (path.trim().isEmpty()) {
                continue;
            }
            try {
                urls.add(new URL(path));
            } catch (MalformedURLException e) {
            }
        }
        URL[] result = new URL[urls.size()];
        urls.toArray(result);
        return result;
    }


}

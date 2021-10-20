package com.megaease.easeagent.log4j2.supplier;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class ClassloaderUrlsSupplier implements Supplier<URL[]> {
    private static final Pattern[] LOG_JAR_PATTERNS = new Pattern[]{
        Pattern.compile(".*/log4j-slf4j-impl-[^/]+\\.jar$"),
        Pattern.compile(".*/slf4j-api-[^/]+\\.jar$"),
        Pattern.compile(".*/log4j-api-[^/]+\\.jar$"),
        Pattern.compile(".*/log4j-core-[^/]+\\.jar$"),
        Pattern.compile(".*/lombok-[^/]+\\.jar$"),
    };

    private final ClassLoader classLoader;

    public ClassloaderUrlsSupplier(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public URL[] get() {
        if (!(classLoader instanceof URLClassLoader)) {
            return null;
        }
        List<URL> urls = new ArrayList<>();
        URL[] appUrls = ((URLClassLoader) classLoader).getURLs();
        for (URL appUrl : appUrls) {
            String path = appUrl.getPath();
            if (path == null) {
                continue;
            }
            if (!path.endsWith(".jar")) {
                urls.add(appUrl);
            } else if (isLogJar(path)) {
                urls.add(appUrl);
            }
        }
        URL[] logUrls = new URL[urls.size()];
        urls.toArray(logUrls);
        return logUrls;
    }

    public static boolean isLogJar(String path) {
        for (Pattern logJarPattern : LOG_JAR_PATTERNS) {
            if (logJarPattern.matcher(path).matches()) {
                return true;
            }
        }
        return false;
    }
}

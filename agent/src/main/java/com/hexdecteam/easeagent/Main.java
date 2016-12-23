package com.hexdecteam.easeagent;

import org.springframework.boot.loader.archive.Archive;
import org.springframework.boot.loader.archive.JarFileArchive;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class Main {

    private static final String LOGBACK_CONFIGURATION_FILE = "logback.configurationFile";
    private static final String JAVAGENT_LOGGING_FILE      = "easeagent.logging.file";
    private static final String LIB                        = "lib/";

    public static void premain(String args, Instrumentation inst) throws Exception {
        loggingContext(() -> {
            final List<URL> urls = nestArchiveUrls(createArchive(Main.class));
            final URLClassLoader loader = URLClassLoader.newInstance(urls.toArray(new URL[urls.size()]));
            final Class<?> aClass = loader.loadClass("com.hexdecteam.easeagent.Bootstrap");
            aClass.getDeclaredMethod("premain", String.class, Instrumentation.class)
                  .invoke(null, args, inst);
            return null;
        });
    }

    private static void loggingContext(Callable<Void> callable) throws Exception {
        final String internal = System.getProperty(JAVAGENT_LOGGING_FILE, "_debug.xml");
        final String origin = System.getProperty(LOGBACK_CONFIGURATION_FILE);

        // Redirect config origin to internal
        System.setProperty(LOGBACK_CONFIGURATION_FILE, internal);
        callable.call();
        // Recovery origin configuration
        if (origin != null) System.setProperty(LOGBACK_CONFIGURATION_FILE, origin);
    }

    private static List<URL> nestArchiveUrls(Archive archive) throws IOException {
        final List<Archive> archives = archive.getNestedArchives(e -> !e.isDirectory() && e.getName().startsWith(LIB));

        List<URL> urls = new ArrayList<>();
        for (Archive entries : archives) {
            urls.add(entries.getUrl());
        }
        return urls;
    }

    private static Archive createArchive(Class<?> klass) throws Exception {
        ProtectionDomain protectionDomain = klass.getProtectionDomain();
        CodeSource codeSource = protectionDomain.getCodeSource();
        URI location = (codeSource == null ? null : codeSource.getLocation().toURI());
        String path = (location == null ? null : location.getSchemeSpecificPart());
        if (path == null) {
            throw new IllegalStateException("Unable to determine code source archive");
        }
        File root = new File(path);
        if (!root.exists() || root.isDirectory()) {
            throw new IllegalStateException(
                    "Unable to determine code source archive from " + root);
        }
        return new JarFileArchive(root);
    }

}

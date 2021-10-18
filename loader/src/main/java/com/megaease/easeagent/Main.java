/*
 * Copyright (c) 2017, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent;

import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import org.springframework.boot.loader.LaunchedURLClassLoader;
import org.springframework.boot.loader.archive.Archive;
import org.springframework.boot.loader.archive.JarFileArchive;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

public class Main {
    private static final ClassLoader BOOTSTRAP_CLASS_LOADER = null;
    private static final String LIB = "lib/";
    private static final String BOOTSTRAP = "boot/";
    private static final String SLf4J2 = "log4j2/";
    private static final String LOGGING_PROPERTY = "Logging-Property";
    private static final String EASEAGENT_LOG_CONF = "easeagent.log.conf";

    public static void premain(final String args, final Instrumentation inst) throws Exception {
        final JarFileArchive archive = new JarFileArchive(getArchiveFileContains());

        // custom classloader
        final URL[] urls = nestArchiveUrls(archive, LIB);
        final ClassLoader loader = new CompoundableClassLoader(urls);

        // install bootstrap jar
        final URL[] bootUrls = nestArchiveUrls(archive, BOOTSTRAP);
        Arrays.stream(bootUrls).forEach(url -> installBootstrapJar(url, inst));

        final Attributes attributes = archive.getManifest().getMainAttributes();
        final String loggingProperty = attributes.getValue(LOGGING_PROPERTY);
        final String bootstrap = attributes.getValue("Bootstrap-Class");
        initEaseAgentSlf4j2Dir(archive, loader);

        switchLoggingProperty(loader, loggingProperty, () -> {
            loader.loadClass(bootstrap)
                .getMethod("premain", String.class, Instrumentation.class)
                .invoke(null, args, inst);
            return null;
        });
    }

    private static void installBootstrapJar(URL url, Instrumentation inst) {
        try {
            JarFile file = JarUtils.getNestedJarFile(url);
            inst.appendToBootstrapClassLoaderSearch(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void initEaseAgentSlf4j2Dir(JarFileArchive archive, final ClassLoader bootstrapLoader) throws Exception {
        final URL[] slf4j2Urls = nestArchiveUrls(archive, SLf4J2);
        final ClassLoader slf4j2Loader = new URLClassLoader(slf4j2Urls, null);
        Class<?> classLoaderSupplier = bootstrapLoader.loadClass("com.megaease.easeagent.log4j2.FinalClassloaderSupplier");
        Field field = classLoaderSupplier.getDeclaredField("CLASSLOADER");
        field.set(null, slf4j2Loader);
    }

    /**
     * Switching the system property temporary could fix the problem of conflict of logging configuration
     * when host used the same logging library as agent.
     */
    private static void switchLoggingProperty(ClassLoader loader, String hostKey, Callable<Void> callable)
        throws Exception {
        final Thread t = Thread.currentThread();
        final ClassLoader ccl = t.getContextClassLoader();

        t.setContextClassLoader(loader);

        final String host = System.getProperty(hostKey);
        final String agent = System.getProperty(Main.EASEAGENT_LOG_CONF, "easeagent-log4j2.xml");

        // Redirect config of host to agent
        System.setProperty(hostKey, agent);

        try {
            callable.call();
        } finally {
            t.setContextClassLoader(ccl);
            // Recovery host configuration
            if (host == null) {
                System.getProperties().remove(hostKey);
            } else {
                System.setProperty(hostKey, host);
            }
        }
    }

    private static URL[] nestArchiveUrls(JarFileArchive archive, String prefix) throws IOException {
        ArrayList<Archive> archives = Lists.newArrayList(
            archive.getNestedArchives(entry -> !entry.isDirectory() && entry.getName().startsWith(prefix),
                entry -> true
            ));

        final URL[] urls = new URL[archives.size()];

        for (int i = 0; i < urls.length; i++) {
            urls[i] = archives.get(i).getUrl();
        }

        return urls;
    }

    private static File getArchiveFileContains() throws URISyntaxException {
        final ProtectionDomain protectionDomain = Main.class.getProtectionDomain();
        final CodeSource codeSource = protectionDomain.getCodeSource();
        final URI location = (codeSource == null ? null : codeSource.getLocation().toURI());
        final String path = (location == null ? null : location.getSchemeSpecificPart());

        if (path == null) {
            throw new IllegalStateException("Unable to determine code source archive");
        }

        final File root = new File(path);
        if (!root.exists() || root.isDirectory()) {
            throw new IllegalStateException("Unable to determine code source archive from " + root);
        }
        return root;
    }

    public static class CompoundableClassLoader extends LaunchedURLClassLoader {
        private final Set<ClassLoader> externals = new CopyOnWriteArraySet<>();

        CompoundableClassLoader(URL[] urls) {
            // super(urls, ClassLoader.getSystemClassLoader());
            super(urls, Main.BOOTSTRAP_CLASS_LOADER);
        }

        @SuppressWarnings("unused")
        public void add(ClassLoader cl) {
            if (cl != null && !Objects.equals(cl, this)) {
                externals.add(cl);
            }
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            try {
                return super.loadClass(name, resolve);
            } catch (ClassNotFoundException e) {
                for (ClassLoader external : externals) {
                    try {
                        final Class<?> aClass = external.loadClass(name);
                        if (resolve) resolveClass(aClass);
                        return aClass;
                    } catch (ClassNotFoundException ignore) {
                    }
                }

                throw e;
            }
        }
    }

    @SneakyThrows
    public static void main(String[] args) {
        System.out.println(void.class.getCanonicalName());

        final JarFileArchive archive = new JarFileArchive(getArchiveFileContains());

        final URL[] urls = nestArchiveUrls(archive, LIB);

        System.out.println(Arrays.toString(urls));
    }
}

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
import org.springframework.boot.loader.LaunchedURLClassLoader;
import org.springframework.boot.loader.archive.Archive;
import org.springframework.boot.loader.archive.JarFileArchive;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.jar.Attributes;

public class Main {
    private static final ClassLoader BOOTSTRAP_CLASS_LOADER = null;
    private static final String LIB = "lib/";
    private static final String LOGGING_PROPERTY = "Logging-Property";
    private static final String EASEAGENT_LOG_CONF = "easeagent.log.conf";

    public static void premain(final String args, final Instrumentation inst) throws Exception {
        final JarFileArchive archive = new JarFileArchive(getArchiveFileContains(Main.class));
        final Attributes attributes = archive.getManifest().getMainAttributes();
        final String loggingProperty = attributes.getValue(LOGGING_PROPERTY);
        final String bootstrap = attributes.getValue("Bootstrap-Class");
        final URL[] urls = nestArchiveUrls(archive);
        final ClassLoader loader = new CompoundableClassLoader(urls);

        switchLoggingProperty(loader, loggingProperty, EASEAGENT_LOG_CONF, new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                loader.loadClass(bootstrap)
                      .getMethod("premain", String.class, Instrumentation.class)
                      .invoke(null, args, inst);
                return null;
            }
        });
    }

    /**
     * Switching the system property temporary could fix the problem of conflict of logging configuration
     * when host used the same logging library as agent.
     */
    private static void switchLoggingProperty(ClassLoader loader, String hostKey, String agentKey, Callable<Void> callable)
            throws Exception {
        final Thread t = Thread.currentThread();
        final ClassLoader ccl = t.getContextClassLoader();
        t.setContextClassLoader(loader);

        final String host = System.getProperty(hostKey);
        final String agent = System.getProperty(agentKey, "log4j2.xml");
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

    private static URL[] nestArchiveUrls(Archive archive) throws IOException {
        ArrayList<Archive> archives = Lists.newArrayList(
                archive.getNestedArchives(entry -> !entry.isDirectory() && entry.getName().startsWith(LIB),
                        entry -> true
                ));
        final URL[] urls = new URL[archives.size()];

        for (int i = 0; i < urls.length; i++) {
            urls[i] = archives.get(i).getUrl();
        }

        return urls;
    }

    private static File getArchiveFileContains(Class<?> klass) throws URISyntaxException {
        final ProtectionDomain protectionDomain = klass.getProtectionDomain();
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
        private final Set<ClassLoader> externals = new CopyOnWriteArraySet<ClassLoader>();

        CompoundableClassLoader(URL[] urls) {super(urls, Main.BOOTSTRAP_CLASS_LOADER);}

        public void add(ClassLoader cl) {
            externals.add(cl);
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
                    } catch (ClassNotFoundException ignore) { }
                }

                throw e;
            }
        }
    }
}

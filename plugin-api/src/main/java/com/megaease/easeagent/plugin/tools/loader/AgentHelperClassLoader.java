/*
 * Copyright (c) 2022, MegaEase
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
 *
 */

package com.megaease.easeagent.plugin.tools.loader;

import com.megaease.easeagent.plugin.utils.common.WeakConcurrentMap;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * if there are classes with the same classname in user classloaders and Agent classloader,
 * to avoid class cast exception in plugins, only load these classes by user classloaders in plugin context.
 * Other related plugin classes loaded by this classloader.
 */
public class AgentHelperClassLoader extends URLClassLoader {
    private static final ConcurrentHashMap<URL, URL> helpUrls = new ConcurrentHashMap<>();
    private static final WeakConcurrentMap<ClassLoader, AgentHelperClassLoader> helpLoaders = new WeakConcurrentMap<>();

    private final URLClassLoader agentClassLoader;

    public AgentHelperClassLoader(URL[] urls, ClassLoader parent, URLClassLoader agent) {
        // may lead to classloader leak here
        super(urls, parent);
        this.agentClassLoader = agent;
    }

    public static void registryUrls(Class<?> clazz) {
        URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
        helpUrls.putIfAbsent(url, url);
    }

    public static AgentHelperClassLoader getClassLoader(ClassLoader parent, URLClassLoader agent) {
        AgentHelperClassLoader help = helpLoaders.getIfPresent(parent);
        if (help != null) {
            return help;
        } else {
            URL[] urls;
            if (helpUrls.isEmpty()) {
                urls = new URL[0];
            } else {
                urls = helpUrls.keySet().toArray(new URL[1]);
            }
            help = new AgentHelperClassLoader(urls, parent, agent);
            if (helpLoaders.putIfProbablyAbsent(parent, help) == null) {
                return help;
            } else {
                return helpLoaders.getIfPresent(parent);
            }
        }
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            return super.loadClass(name, resolve);
        } catch (ClassNotFoundException e) {
            try {
                final Class<?> aClass = this.agentClassLoader.loadClass(name);
                if (resolve) {
                    resolveClass(aClass);
                }
                return aClass;
            } catch (ClassNotFoundException ignored) {
                // ignored
            }
            throw e;
        }
    }

    @Override
    public URL findResource(String name) {
        URL url = super.findResource(name);
        try {
            url = this.agentClassLoader.getResource(name);
            if (url != null) {
                return url;
            }
        } catch (Exception ignored) {
            // ignored
        }
        return url;
    }
}

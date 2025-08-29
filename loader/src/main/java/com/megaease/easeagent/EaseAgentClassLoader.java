/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * EaseAgent's exclusive classloader, used to isolate classes
 */
public class EaseAgentClassLoader extends URLClassLoader {
    static {
        ClassLoader.registerAsParallelCapable();
    }

    private final Set<WeakReference<ClassLoader>> externals = new CopyOnWriteArraySet<>();

    public EaseAgentClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @SuppressWarnings("unused")
    public void add(ClassLoader cl) {
        if (cl != null && !Objects.equals(cl, this)) {
            externals.add(new WeakReference<>(cl));
        }
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            return super.loadClass(name, resolve);
        } catch (ClassNotFoundException e) {
            for (WeakReference<ClassLoader> external : externals) {
                try {
                    ClassLoader cl = external.get();
                    if (cl == null) {
                        continue;
                    }
                    final Class<?> aClass = cl.loadClass(name);
                    if (resolve) {
                        resolveClass(aClass);
                    }
                    return aClass;
                } catch (ClassNotFoundException ignored) {
                    // ignored
                }
            }

            throw e;
        }
    }

    @Override
    public URL findResource(String name) {
        URL url = super.findResource(name);
        if (url == null) {
            for (WeakReference<ClassLoader> external : externals) {
                try {
                    ClassLoader cl = external.get();
                    url = cl.getResource(name);
                    if (url != null) {
                        return url;
                    }
                } catch (Exception ignored) {
                    // ignored
                }
            }
        }
        return url;
    }
}

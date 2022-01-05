/*
 * Copyright (c) 2021, MegaEase
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

import java.io.*;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarUtils {

    private JarUtils() {
    }

    private static final int EOF = -1;
    private static final int BUFFER_SIZE = 4096;

    private static final File TMP_FILE = new File(AccessController.doPrivileged(
        new PrivilegedAction<String>() {
            @Override
            public String run() {
                return System.getProperty("java.io.tmpdir") + File.separatorChar + "easeagent" + File.separatorChar;
            }
        })
    );

    private static SoftReference<Map<String, JarFile>> fileCache;

    static {
        fileCache = new SoftReference<>(new ConcurrentHashMap<>());
    }

    private static final String SEPARATOR = "!/";
    private static final String FILE_PROTOCOL = "file:";

    static JarFile getNestedJarFile(URL url) throws IOException {
        StringSequence spec = new StringSequence(url.getFile());
        Map<String, JarFile> cache = fileCache.get();
        JarFile jarFile = (cache != null) ? cache.get(spec.toString()) : null;

        if (jarFile != null) {
            return jarFile;
        } else {
            jarFile = getRootJarFileFromUrl(url);
        }

        int separator;
        int index = indexOfRootSpec(spec);
        if (index == -1) {
            return null;
        }
        StringSequence entryName;
        if ((separator = spec.indexOf(SEPARATOR, index)) > 0) {
            entryName = spec.subSequence(index, separator);
        } else {
            entryName = spec.subSequence(index);
        }
        JarEntry jarEntry = jarFile.getJarEntry(entryName.toString());
        if (jarEntry == null) {
            return null;
        }
        try (InputStream input = jarFile.getInputStream(jarEntry)) {
            File output = createTempJarFile(input, jarEntry.getName());
            jarFile = new JarFile(output);
            addToRootFileCache(url.getPath(), jarFile);
        }

        return jarFile;
    }

    public static void copy(InputStream input, OutputStream output) throws IOException {
        int n;
        final byte[] buffer = new byte[BUFFER_SIZE];
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
    }

    private static File createTempJarFile(InputStream input, String outputName) throws IOException {
        File dir;
        String fName = (new File(outputName)).getName();
        if (fName.length() < outputName.length()) {
            String localDir = outputName.substring(0, outputName.length() - fName.length());
            Path path = Paths.get(TMP_FILE.getPath() + File.separatorChar + localDir);
            dir = Files.createDirectories(path).toFile();
        } else {
            dir = TMP_FILE;
        }
        File f = new File(dir, fName);
        f.deleteOnExit();
        try (FileOutputStream outputStream = new FileOutputStream(f)) {
            copy(input, outputStream);
        }

        return f;
    }

    private static int indexOfRootSpec(StringSequence file) {
        int separatorIndex = file.indexOf(SEPARATOR);
        if (separatorIndex < 0) {
            return -1;
        }
        return separatorIndex + SEPARATOR.length();
    }

    public static String getRootJarFileName(URL url) throws MalformedURLException {
        String spec = url.getFile();
        int separatorIndex = spec.indexOf(SEPARATOR);
        if (separatorIndex == -1) {
            throw new MalformedURLException("Jar URL does not contain !/ separator");
        }
        return spec.substring(0, separatorIndex);
    }

    public static JarFile getRootJarFileFromUrl(URL url) throws IOException {
        String name = getRootJarFileName(url);
        return getRootJarFile(name);
    }

    private static JarFile getRootJarFile(String name) throws IOException {
        try {
            if (!name.startsWith(FILE_PROTOCOL)) {
                throw new IllegalStateException("Not a file URL");
            }
            File file = new File(URI.create(name));
            Map<String, JarFile> cache = fileCache.get();
            JarFile result = (cache != null) ? cache.get(name) : null;
            if (result == null) {
                result = new JarFile(file);
                addToRootFileCache(name, result);
            }
            return result;
        } catch (Exception ex) {
            throw new IOException("Unable to open root Jar file '" + name + "'", ex);
        }
    }

    /**
     * Add the given {@link JarFile} to the root file cache.
     *
     * @param fileName the source file to add
     * @param jarFile  the jar file.
     */
    static void addToRootFileCache(String fileName, JarFile jarFile) {
        Map<String, JarFile> cache = fileCache.get();
        if (cache == null) {
            cache = new ConcurrentHashMap<>(8);
            fileCache = new SoftReference<>(cache);
        }
        cache.put(fileName, jarFile);
    }
}

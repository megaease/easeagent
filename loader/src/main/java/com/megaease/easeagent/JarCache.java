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

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class JarCache {
    private static final int EOF = -1;
    private static final int BUFFER_SIZE = 4096;

    private final JarFile jarFile;
    private final Map<String, JarFile> childJars;
    private final Map<String, URL> childUrls;

    public JarCache(JarFile jarFile, Map<String, JarFile> childJars, Map<String, URL> childUrls) throws IOException {
        this.jarFile = jarFile;
        this.childJars = childJars;
        this.childUrls = childUrls;
    }

    public ArrayList<URL> nestJarUrls(String prefix) {
        ArrayList<URL> urls = new ArrayList<>();
        for (Map.Entry<String, URL> entry : childUrls.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                urls.add(entry.getValue());
            }
        }
        return urls;
    }

    public ArrayList<JarFile> nestJarFiles(String prefix) {
        ArrayList<JarFile> jarFiles = new ArrayList<>();
        for (Map.Entry<String, JarFile> entry : childJars.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                jarFiles.add(entry.getValue());
            }
        }
        return jarFiles;
    }

    public Manifest getManifest() throws IOException {
        return this.jarFile.getManifest();
    }


    static JarCache build(File file) throws IOException {
        final JarFile jarFile = new JarFile(file);
        String tmpDir = getTmpDir(jarFile);
        Map<String, JarFile> childJars = new HashMap<>();
        Map<String, URL> childUrls = new HashMap<>();
        jarFile.stream().forEach(jarEntry -> {
            String name = jarEntry.getName();
            if (!jarEntry.isDirectory() && name.endsWith(".jar")) {
                try (InputStream input = jarFile.getInputStream(jarEntry)) {
                    File output = createTempJarFile(tmpDir, input, jarEntry.getName());
                    JarFile childJarFile = new JarFile(output);
                    childJars.put(name, childJarFile);
                    childUrls.put(name, output.toURI().toURL());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return new JarCache(jarFile, childJars, childUrls);
    }

    private static File createTempJarFile(String tmpDir, InputStream input, String outputName) throws IOException {
        File dir;
        String fName = (new File(outputName)).getName();
        if (fName.length() < outputName.length()) {
            String localDir = outputName.substring(0, outputName.length() - fName.length());
            Path path = Paths.get(tmpDir + File.separatorChar + localDir);
            dir = Files.createDirectories(path).toFile();
        } else {
            dir = new File(tmpDir);
        }
        File f = new File(dir, fName);
        f.deleteOnExit();
        try (FileOutputStream outputStream = new FileOutputStream(f)) {
            copy(input, outputStream);
        }

        return f;
    }

    public static void copy(InputStream input, OutputStream output) throws IOException {
        int n;
        final byte[] buffer = new byte[BUFFER_SIZE];
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
    }

    public static String getTmpDir(JarFile jarFile) throws IOException {
        String tmp = System.getProperty("java.io.tmpdir");
        if (tmp != null && tmp.endsWith(String.valueOf(File.separatorChar))) {
            return tmp + "easeagent-" + getAttribute(jarFile, "Easeagent-Version") + File.separatorChar;
        }
        return tmp + File.separatorChar + "easeagent-" + getAttribute(jarFile, "Easeagent-Version") + File.separatorChar;

    }

    public static String getAttribute(JarFile jarFile, String key) throws IOException {
        final Attributes attributes = jarFile.getManifest().getMainAttributes();
        return attributes.getValue(key);
    }

}

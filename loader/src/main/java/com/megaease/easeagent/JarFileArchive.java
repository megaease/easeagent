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

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class JarFileArchive {
    private final JarFile jarFile;
    private final Map<String, URL> childJarFiles;

    public JarFileArchive(JarFile jarFile, Map<String, URL> childJarFiles) {
        this.jarFile = jarFile;
        this.childJarFiles = childJarFiles;
    }

    public static JarFileArchive load(File jarFile) throws IOException {
        Map<String, URL> childJarFiles = new HashMap<>();
        try (JarArchiveInputStream jarInput = new JarArchiveInputStream(Files.newInputStream(jarFile.toPath()))) {
            ArchiveEntry entry;
            JarArchiveReader reader = new JarArchiveReader();
            while ((entry = jarInput.getNextEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().endsWith(".jar")) {
                    String name = entry.getName();
                    String nestedJarUrl = jarFile.toURI() + "!/" + name + "!/";
                    nestedJarUrl = nestedJarUrl.replace("file:////", "file://");
                    Map<String, byte[]> childByteArrays = new HashMap<>();
                    byte[] jarBytes = reader.readByte(jarInput);
                    try (JarArchiveInputStream childInput = new JarArchiveInputStream(new ByteArrayInputStream(jarBytes))) {
                        ArchiveEntry childEntry;
                        while ((childEntry = childInput.getNextEntry()) != null) {
                            String childName = childEntry.getName();
                            childByteArrays.put(childName, reader.readByte(childInput));
                        }
                    }
                    childJarFiles.put(name, new URL("jar", "", -1, nestedJarUrl, new CustomJarURLStreamHandler(name, childByteArrays)));
                }
            }
        }
        return new JarFileArchive(new JarFile(jarFile), childJarFiles);
    }

    public ArrayList<URL> nestJarUrls(String prefix) {
        ArrayList<URL> urls = new ArrayList<>();
        for (Map.Entry<String, URL> entry : childJarFiles.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                urls.add(entry.getValue());
            }
        }
        return urls;
    }

    public Manifest getManifest() throws IOException {
        return this.jarFile.getManifest();
    }

    private static class CustomJarURLStreamHandler extends URLStreamHandler {
        String name;
        Map<String, byte[]> jarByteArrayMap;

        public CustomJarURLStreamHandler(String name, Map<String, byte[]> jarByteArrayMap) throws IOException {
            this.name = name;
            this.jarByteArrayMap = jarByteArrayMap;
        }

        @Override
        protected URLConnection openConnection(URL url) throws IOException {
            return new CustomJarURLConnection(url, this.name, this.jarByteArrayMap);
        }
    }

    private static class CustomJarURLConnection extends URLConnection {
        String name;
        Map<String, byte[]> jarByteArrayMap;

        public CustomJarURLConnection(URL url, String name, Map<String, byte[]> jarByteArrayMap) {
            super(url);
            this.name = name;
            this.jarByteArrayMap = jarByteArrayMap;
        }

        @Override
        public void connect() throws IOException {
        }

        @Override
        public InputStream getInputStream() throws IOException {
            String spec = url.getFile();
            String[] parts = spec.split("!/", 3);
            if (parts.length < 3 || !name.equals(parts[1])) {
                throw new FileNotFoundException("Entry not found: " + url);
            }
            String name = parts[2];
            byte[] array = this.jarByteArrayMap.get(name);
            if (array != null) {
                return new ByteArrayInputStream(array);
            }

            throw new FileNotFoundException("Entry not found: " + url);
        }
    }

    public static class JarArchiveReader {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];

        public byte[] readByte(JarArchiveInputStream jarInput) throws IOException {
            int bytesRead;
            while ((bytesRead = jarInput.read(data)) != -1) {
                buffer.write(data, 0, bytesRead);
            }
            byte[] array = buffer.toByteArray();
            buffer.reset();
            return array;
        }

    }
}

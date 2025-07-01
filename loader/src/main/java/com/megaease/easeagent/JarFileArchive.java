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
                    childJarFiles.put(name, new URL("jar", "", -1, nestedJarUrl, new CustomJarURLStreamHandler(nestedJarUrl, jarBytes, childByteArrays)));
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

        byte[] jarByteArray;
        Map<String, byte[]> childArrayMap;

        public CustomJarURLStreamHandler(String name, byte[] jarByteArray, Map<String, byte[]> childArrayMap) {
            this.name = name;
            this.jarByteArray = jarByteArray;
            this.childArrayMap = childArrayMap;
        }

        @Override
        protected URLConnection openConnection(URL url) throws IOException {
            String spec = url.getFile();
            if (spec.equals(name)) {
                return new CustomJarURLConnection(url, this.jarByteArray);
            }
            String childName = spec.replaceFirst(this.name, "");
            byte[] childArray = childArrayMap.get(childName);
            if (childArray == null) {
                throw new FileNotFoundException("Entry not found: " + url);
            }
            return new CustomJarURLConnection(url, childArray);
        }
    }

    private static class CustomJarURLConnection extends URLConnection {
        byte[] jarByteArray;

        public CustomJarURLConnection(URL url, byte[] jarByteArray) {
            super(url);
            this.jarByteArray = jarByteArray;
        }

        @Override
        public void connect() {
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(jarByteArray);
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

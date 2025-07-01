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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
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
            while ((entry = jarInput.getNextEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().endsWith(".jar")) {
                    String name = entry.getName();
                    String nestedJarUrl = "jar:file:" + jarFile.getAbsolutePath() + "!/" + name + "!/";
                    childJarFiles.put(name, new URL(nestedJarUrl));
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
}

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

import com.google.common.collect.Lists;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.springframework.boot.loader.archive.Archive;
import org.springframework.boot.loader.archive.JarFileArchive;

import static org.junit.Assert.*;

public class MainTest {

    @Test
    public void premain1() throws IOException, ClassNotFoundException {
        File jar = new File("/Users/beyond/IdeaProjects/easeagent_dir/easeagent/build/target/easeagent-dep.jar");
        final JarFileArchive archive = new JarFileArchive(jar);
        ArrayList<URL> urls = nestArchiveUrls(archive, "log4j2/");
        ClassLoader loader = new Main.CompoundableClassLoader(urls.toArray(new URL[0]));
        Class<?> classLoaderSupplier = loader.loadClass("com.megaease.easeagent.log4j2.FinalClassloaderSupplier");
    }

    private static ArrayList<URL> nestArchiveUrls(JarFileArchive archive, String prefix) throws IOException {
        ArrayList<Archive> archives = Lists.newArrayList(
            archive.getNestedArchives(entry -> !entry.isDirectory() && entry.getName().startsWith(prefix),
                entry -> true
            ));

        final ArrayList<URL> urls = new ArrayList<>(archives.size());

        archives.forEach(item -> {
            try {
                urls.add(item.getUrl());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        });

        return urls;
    }
}

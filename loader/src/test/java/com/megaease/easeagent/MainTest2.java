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

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class MainTest2 {

    @Test
    public void premain2() throws IOException, ClassNotFoundException {
        File jar = new File("/Users/beyond/IdeaProjects/easeagent_dir/easeagent/build/target/easeagent-dep.jar");
        JarCache jarFileArchive = JarCache.build(jar);
        ArrayList<URL> urls = jarFileArchive.nestJarUrls("log4j2/");
        ClassLoader loader = new Main.CompoundableClassLoader(urls.toArray(new URL[0]));
        Class<?> classLoaderSupplier = loader.loadClass("com.megaease.easeagent.log4j2.FinalClassloaderSupplier");
    }
}

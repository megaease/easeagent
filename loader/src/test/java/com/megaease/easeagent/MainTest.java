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

import static org.junit.Assert.assertNotNull;

public class MainTest {

    @Test
    public void buildClassLoader() throws IOException, ClassNotFoundException {
        File jar = new File(ClassLoader.getSystemResource("test-mock-load.jar").getPath());
        JarCache jarFileArchive = JarCache.build(jar);
        ArrayList<URL> urls = jarFileArchive.nestJarUrls("mock/");
        ClassLoader loader = Main.buildClassLoader(urls.toArray(new URL[0]));
        Class<?> logbackPlugin = loader.loadClass("com.megaease.easeagent.mock.utils.MockSystemEnv");
        assertNotNull(logbackPlugin);
    }
}

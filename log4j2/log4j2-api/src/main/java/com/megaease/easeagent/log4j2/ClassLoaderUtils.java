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

package com.megaease.easeagent.log4j2;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ClassLoaderUtils {
    public static URL[] getAllUrls(ClassLoader classLoader) {
        List<URL> list = new ArrayList<>();
        try {
            Enumeration<URL> enumeration = classLoader.getResources("META-INF");
            fillUrls(list, enumeration);
            Enumeration<URL> enumeration2 = classLoader.getResources("");
            fillUrls(list, enumeration2);

        } catch (IOException ignore) {
            //ignore
        }
        return list.toArray(new URL[0]);
    }


    private static void fillUrls(List<URL> list, Enumeration<URL> enumeration) throws IOException {
        while (enumeration.hasMoreElements()) {
            URL url = enumeration.nextElement();
            URLConnection urlConnection = url.openConnection();
            URL resultUrl = url;
            if (urlConnection instanceof JarURLConnection) {
                resultUrl = ((JarURLConnection) urlConnection).getJarFileURL();
            }
            if (!list.contains(resultUrl)) {
                list.add(resultUrl);
            }
        }
    }
}

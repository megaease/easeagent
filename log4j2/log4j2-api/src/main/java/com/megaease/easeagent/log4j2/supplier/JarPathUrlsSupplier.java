/*
 * Copyright (c) 2017, MegaEase
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

package com.megaease.easeagent.log4j2.supplier;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class JarPathUrlsSupplier implements Supplier<URL[]> {
    public static final String EASEAGENT_SLF4_J2_LIB_JAR_PATHS = "EASEAGENT-SLF4J2-LIB-JAR-PATHS";

    @Override
    public URL[] get() {
        String dir = System.getProperty(EASEAGENT_SLF4_J2_LIB_JAR_PATHS);
        if (dir == null) {
            return new URL[0];
        }
        String[] paths = dir.split(",");
        List<URL> urls = new ArrayList<>();
        for (String path : paths) {
            if (path.trim().isEmpty()) {
                continue;
            }
            try {
                urls.add(new URL(path));
            } catch (MalformedURLException e) {
            }
        }
        URL[] result = new URL[urls.size()];
        urls.toArray(result);
        return result;
    }


}

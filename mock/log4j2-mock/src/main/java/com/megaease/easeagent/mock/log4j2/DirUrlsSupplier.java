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

package com.megaease.easeagent.mock.log4j2;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class DirUrlsSupplier implements UrlSupplier {
    public static final String LIB_DIR_ENV = "EASEAGENT-SLF4J2-LIB-DIR";

    @Override
    public URL[] get() {
        String dir = System.getProperty(LIB_DIR_ENV);
        if (dir == null) {
            return new URL[0];
        }
        File file = new File(dir);
        if (!file.isDirectory()) {
            return new URL[0];
        }
        File[] files = file.listFiles();
        if (files == null) {
            return new URL[0];
        }
        URL[] urls = new URL[files.length];
        for (int i = 0; i < files.length; i++) {
            try {
                urls[i] = files[i].toURI().toURL();
            } catch (MalformedURLException e) {
                return new URL[0];
            }
        }
        return urls;
    }
}

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

import javax.annotation.Nonnull;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JarUrlsSupplier implements UrlSupplier {
    private final UrlSupplier[] suppliers;

    public JarUrlsSupplier(@Nonnull UrlSupplier[] suppliers) {
        this.suppliers = suppliers;
    }


    @Override
    public URL[] get() {
        List<URL> list = new ArrayList<>();
        for (UrlSupplier supplier : suppliers) {
            URL[] urls = supplier.get();
            if (urls != null && urls.length > 0) {
                list.addAll(Arrays.asList(urls));
            }
        }
        URL[] result = new URL[list.size()];
        list.toArray(result);
        return result;
    }
}

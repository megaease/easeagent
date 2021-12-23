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

import javax.annotation.Nonnull;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class JarUrlsSupplier implements Supplier<URL[]> {
    private final Supplier<URL[]>[] suppliers;

    private JarUrlsSupplier(@Nonnull Supplier<URL[]>[] suppliers) {
        this.suppliers = suppliers;
    }

    @SafeVarargs
    public static JarUrlsSupplier build(Supplier<URL[]>... suppliers) {
        return new JarUrlsSupplier(suppliers);
    }


    @Override
    public URL[] get() {
        List<URL> list = new ArrayList<>();
        for (Supplier<URL[]> supplier : suppliers) {
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

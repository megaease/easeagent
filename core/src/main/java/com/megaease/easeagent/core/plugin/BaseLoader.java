/*
 * Copyright (c) 2021, MegaEase
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

package com.megaease.easeagent.core.plugin;

import com.megaease.easeagent.plugin.Ordered;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

public class BaseLoader {
    private static final Logger logger = LoggerFactory.getLogger(BaseLoader.class);

    public static <T> List<T> load(Class<T> serviceClass) {
        List<T> result = new ArrayList<>();
        java.util.ServiceLoader<T> services = ServiceLoader.load(serviceClass);
        for (Iterator<T> it = services.iterator(); it.hasNext(); ) {
            try {
                result.add(it.next());
            } catch (UnsupportedClassVersionError e) {
                logger.info("Unable to load class: {}", e.getMessage());
                logger.info("Please check the plugin compile Java version configuration,"
                    + " and it should not latter than current JVM runtime");
            }
        }
        return result;
    }

    public static <T extends Ordered> List<T> loadOrdered(Class<T> serviceClass) {
        List<T> result = load(serviceClass);
        result.sort(Comparator.comparing(Ordered::order));
        return result;
    }

    private BaseLoader() {}
}

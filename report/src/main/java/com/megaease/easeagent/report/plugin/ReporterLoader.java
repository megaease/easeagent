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
 *
 */
package com.megaease.easeagent.report.plugin;

import com.megaease.easeagent.plugin.report.Encoder;
import com.megaease.easeagent.plugin.report.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Supplier;

@SuppressWarnings("rawtypes")
public class ReporterLoader {
    static Logger logger = LoggerFactory.getLogger(ReporterLoader.class);

    private ReporterLoader() {}

    public static void load() {
        encoderLoad();
        senderLoad();
    }

    public static void encoderLoad() {
        for (Encoder<?> encoder : load(Encoder.class)) {
            try {
                Constructor<? extends Encoder> constructor = encoder.getClass().getConstructor();
                Supplier<Encoder<?>> encoderSupplier = () -> {
                    try {
                        return constructor.newInstance();
                    } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
                        logger.warn("unable to load sender: {}", encoder.name());
                        return null;
                    }
                };
                ReporterRegistry.registryEncoder(encoder.name(), encoderSupplier);
            } catch (NoSuchMethodException e) {
                    logger.warn("Sender load fail:{}", e.getMessage());
            }
        }
    }

    public static void senderLoad() {
        for (Sender sender : load(Sender.class)) {
            try {
                Constructor<? extends Sender> constructor = sender.getClass().getConstructor();
                Supplier<Sender> senderSupplier = () -> {
                    try {
                        return constructor.newInstance();
                    } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
                        logger.warn("unable to load sender: {}", sender.name());
                        return null;
                    }
                };
                ReporterRegistry.registrySender(sender.name(), senderSupplier);
            } catch (NoSuchMethodException e) {
                logger.warn("Sender load fail:{}", e.getMessage());
            }
        }
    }

    private static <T> List<T> load(Class<T> serviceClass) {
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
}

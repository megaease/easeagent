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

import java.util.Iterator;
import java.util.ServiceLoader;

public interface ClassloaderSupplier {

    ClassLoader get();

    class ClassloaderSupplierImpl implements ClassloaderSupplier {

        @Override
        public ClassLoader get() {
            FinalClassloaderSupplier supplier = new FinalClassloaderSupplier();
            ClassLoader classLoader = supplier.get();
            if (classLoader != null) {
                return classLoader;
            }
            ServiceLoader<ClassloaderSupplier> loader = ServiceLoader.load(ClassloaderSupplier.class);
            Iterator<ClassloaderSupplier> iterator = loader.iterator();
            while (iterator.hasNext()) {
                classLoader = iterator.next().get();
                if (classLoader != null) {
                    return classLoader;
                }
            }
            return null;
        }
    }
}

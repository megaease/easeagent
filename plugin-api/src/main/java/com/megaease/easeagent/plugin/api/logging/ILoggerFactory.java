/*
 * Copyright (c) 2022, MegaEase
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

package com.megaease.easeagent.plugin.api.logging;

public interface ILoggerFactory {
    /**
     * Returns a logger that logs to the Java agent log output.
     * @return A log where messages can be written to the Java agent log file or console.
     */
    public Logger getLogger(String name);

    default public Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getCanonicalName());
    }
}

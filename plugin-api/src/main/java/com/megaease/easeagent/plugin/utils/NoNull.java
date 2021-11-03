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

package com.megaease.easeagent.plugin.utils;

public class NoNull {

    /**
     * Checks that the specified object reference is not {@code null} and
     * return a default value if it is. This method is designed primarily
     * for doing verify the return value in methods that returns a
     * non-empty instance,  as demonstrated below:
     * <blockquote><pre>
     * public String getFoo() {
     *     return NoNull.of(this.bar, "default");
     * }
     * </pre></blockquote>
     *
     * @param o            the object reference to check for nullity
     * @param defaultValue default value to be used in the event
     *                     that {@code o} is {@code null}
     * @param <O>          the type of the reference
     * @return {@code o} if not {@code null} else {@code defaultValue}
     */
    public static <O> O of(O o, O defaultValue) {
        return o == null ? defaultValue : o;
    }
}

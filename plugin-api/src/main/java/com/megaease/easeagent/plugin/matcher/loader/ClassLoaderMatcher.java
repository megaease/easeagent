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

package com.megaease.easeagent.plugin.matcher.loader;

import com.megaease.easeagent.plugin.utils.common.StringUtils;

@SuppressWarnings("unused")
public class ClassLoaderMatcher implements IClassLoaderMatcher {
    public static final String BOOTSTRAP_NAME = "bootstrap";
    public static final String EXTERNAL_NAME = "external";
    public static final String SYSTEM_NAME = "system";
    public static final String AGENT_NAME = "agent";

    // predefined classloader name and matcher
    public static final ClassLoaderMatcher ALL = new ClassLoaderMatcher("all");
    public static final ClassLoaderMatcher BOOTSTRAP = new ClassLoaderMatcher(BOOTSTRAP_NAME);
    public static final ClassLoaderMatcher EXTERNAL = new ClassLoaderMatcher(EXTERNAL_NAME);
    public static final ClassLoaderMatcher SYSTEM = new ClassLoaderMatcher(SYSTEM_NAME);
    public static final ClassLoaderMatcher AGENT = new ClassLoaderMatcher(AGENT_NAME);

    // classloader class name or Predefined names
    String classLoaderName;

    public ClassLoaderMatcher(String loaderName) {
        if (StringUtils.isEmpty(loaderName)) {
            this.classLoaderName = BOOTSTRAP_NAME;
        } else {
            this.classLoaderName = loaderName;
        }
    }

    @Override
    public String getClassLoaderName() {
        return this.classLoaderName;
    }

    @Override
    public IClassLoaderMatcher negate() {
        return new NegateClassLoaderMatcher(this);
    }

    @Override
    public int hashCode() {
        return this.classLoaderName.hashCode();
    }

    public boolean equals(Object o) {
        if (!(o instanceof ClassLoaderMatcher)) {
            return false;
        }
        return this.classLoaderName.equals(((ClassLoaderMatcher) o).classLoaderName);
    }
}

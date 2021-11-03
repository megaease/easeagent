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

package com.megaease.easeagent.config;

import java.util.Objects;

public class PluginProperty {
    private final String domain;
    private final String namespace;
    private final String id;
    private final String property;

    public PluginProperty(String domain, String namespace, String id, String property) {
        this.domain = domain;
        this.namespace = namespace;
        this.id = id;
        this.property = property;
    }

    public String getDomain() {
        return domain;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getId() {
        return id;
    }

    public String getProperty() {
        return property;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginProperty that = (PluginProperty) o;
        return Objects.equals(domain, that.domain) &&
            Objects.equals(namespace, that.namespace) &&
            Objects.equals(id, that.id) &&
            Objects.equals(property, that.property);
    }

    @Override
    public int hashCode() {

        return Objects.hash(domain, namespace, id, property);
    }
}

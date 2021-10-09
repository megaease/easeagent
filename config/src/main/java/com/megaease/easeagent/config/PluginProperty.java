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

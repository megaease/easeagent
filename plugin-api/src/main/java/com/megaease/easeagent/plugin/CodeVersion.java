package com.megaease.easeagent.plugin;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * code of versions for control whether to load.
 * <p>
 * If CodeVersion.builder().build() is returned, it means it will load forever
 * <p>
 * The configuration format is as follows: runtime.code.version.points.{key}={version}
 * <p>
 * If CodeVersion.builder().key("jdk").add("default").build(), it means it will load by default,
 * but not load by specified like runtime.code.version.points.jdk=jdk10
 * <p>
 * When multiple versions are specified, it means that it can be loaded by multiple versions:
 * CodeVersion.builder().key("jdk").add("jdk10").add("jdk11").build()
 * The following two configurations are load:
 * runtime.code.version.points.jdk=jdk10
 * runtime.code.version.points.jdk=jdk11
 */
public class CodeVersion {
    private final String key;
    private final Set<String> versions;

    public CodeVersion(String key, Set<String> versions) {
        this.key = key;
        this.versions = versions;
    }

    public boolean isEmpty() {
        return key == null || key.trim().isEmpty() || versions == null || versions.isEmpty();
    }

    public String getKey() {
        return key;
    }

    public Set<String> getVersions() {
        return versions;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        String key = "";
        Set<String> versions = new HashSet<>();

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder add(String version) {
            this.versions.add(version);
            return this;
        }

        public CodeVersion build() {
            if (this.versions.isEmpty()) {
                return new CodeVersion(this.key, Collections.emptySet());
            } else if (this.versions.size() == 1) {
                return new CodeVersion(this.key, Collections.singleton(this.versions.iterator().next()));
            } else {
                return new CodeVersion(this.key, Collections.unmodifiableSet(this.versions));
            }
        }
    }
}

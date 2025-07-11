package com.megaease.easeagent.plugin;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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

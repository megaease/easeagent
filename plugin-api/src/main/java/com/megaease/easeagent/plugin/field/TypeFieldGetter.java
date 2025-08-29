package com.megaease.easeagent.plugin.field;

public interface TypeFieldGetter {
    <T> T getEaseAgent$$TypeField$$Data();

    static <T> T get(Object o) {
        if (o instanceof TypeFieldGetter) {
            return ((TypeFieldGetter) o).getEaseAgent$$TypeField$$Data();
        }
        return null;
    }
}

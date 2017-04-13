package com.megaease.easeagent.core;

public interface Transformation {
    <T extends Definition> T define(Definition<T> def);
}

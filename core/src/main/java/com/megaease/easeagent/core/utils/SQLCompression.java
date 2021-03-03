package com.megaease.easeagent.core.utils;

public interface SQLCompression {

    SQLCompression DEFAULT = origin -> origin;

    String compress(String origin);
}

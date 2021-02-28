package com.megaease.easeagent.common;

public interface SQLCompression {

    SQLCompression DEFAULT = origin -> origin;

    String compress(String origin);
}

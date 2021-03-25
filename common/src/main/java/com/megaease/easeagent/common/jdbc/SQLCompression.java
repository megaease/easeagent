package com.megaease.easeagent.common.jdbc;

public interface SQLCompression {

    SQLCompression DEFAULT = origin -> origin;

    String compress(String origin);
}

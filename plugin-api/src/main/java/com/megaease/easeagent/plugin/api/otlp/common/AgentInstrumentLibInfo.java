package com.megaease.easeagent.plugin.api.otlp.common;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;

import java.util.concurrent.ConcurrentHashMap;

public class AgentInstrumentLibInfo {
    static ConcurrentHashMap<String, InstrumentationLibraryInfo> infoMap = new ConcurrentHashMap<>();

    public static InstrumentationLibraryInfo getInfo(String loggerName) {
        InstrumentationLibraryInfo info = infoMap.get(loggerName);
        if (info != null) {
            return info;
        }
        info = InstrumentationLibraryInfo.create(loggerName, null);
        infoMap.putIfAbsent(loggerName, info);
        return info;
    }
}

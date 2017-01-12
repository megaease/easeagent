package com.hexdecteam.easeagent;

import com.google.auto.service.AutoService;

@AutoService(AppendBootstrapClassLoaderSearch.class)
public class MetricNaming {
    public static String name(String type, String method, String annotatedName, boolean absolute) {
        final String name = annotatedName.isEmpty() ? method : annotatedName;
        return absolute ? annotatedName : type + "#" + name;
    }
}

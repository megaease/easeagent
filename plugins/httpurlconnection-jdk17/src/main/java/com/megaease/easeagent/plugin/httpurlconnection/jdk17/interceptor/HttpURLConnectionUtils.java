package com.megaease.easeagent.plugin.httpurlconnection.jdk17.interceptor;

import com.megaease.easeagent.plugin.field.TypeFieldGetter;

public class HttpURLConnectionUtils {
    public static boolean isConnected(Object obj) {
        Boolean connected = TypeFieldGetter.get(obj);
        return Boolean.TRUE.equals(connected);
    }
}

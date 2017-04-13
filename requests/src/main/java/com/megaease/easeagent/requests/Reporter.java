package com.megaease.easeagent.requests;

import java.util.Map;

interface Reporter {
    void report(String url, String method, int status, Map<String, String> headers, Map<String, String> queries, Context context);
}

package com.megaease.easeagent.zipkin;

import com.megaease.easeagent.gen.Assembly;

@Assembly({TraceHttpClient.class, TraceHttpServlet.class, TraceJdbcStatement.class, TraceJedis.class, TraceRestTemplate.class})
public interface GeneratedTest { }

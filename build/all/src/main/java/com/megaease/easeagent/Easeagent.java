package com.megaease.easeagent;

import com.megaease.easeagent.gen.Assembly;
import com.megaease.easeagent.metrics.CaptureCaller;
import com.megaease.easeagent.metrics.MeasureHttpRequest;
import com.megaease.easeagent.metrics.MeasureJdbcGetConnection;
import com.megaease.easeagent.metrics.MeasureJdbcStatement;
import com.megaease.easeagent.requests.CaptureExecuteSql;
import com.megaease.easeagent.requests.CaptureHttpRequest;
import com.megaease.easeagent.requests.CaptureTrace;
import com.megaease.easeagent.zipkin.*;

@Assembly({
        CaptureTrace.class
        , CaptureExecuteSql.class
        , CaptureHttpRequest.class
        , TraceHttpServlet.class
        , TraceHttpClient.class
        , TraceRestTemplate.class
        , TraceJedis.class
        , TraceJdbcStatement.class
        , MeasureJdbcStatement.class
        , MeasureJdbcGetConnection.class
        , MeasureHttpRequest.class
        , CaptureCaller.class

})
public interface Easeagent {}

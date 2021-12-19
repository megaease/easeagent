package com.megaease.easeagent.httpserver;

import lombok.Builder;
import lombok.Data;
import org.apache.kafka.common.header.Headers;

import java.io.InputStream;

@Data
@Builder
public class HttpRequest {
    Headers headers;
    String method;
    String uri;
    String remoteIp;
    String remoteHostName;
    InputStream input;
}

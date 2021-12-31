package com.megaease.easeagent.core.config;

import com.megaease.easeagent.config.ConfigManagerMXBean;
import com.megaease.easeagent.httpserver.nano.AgentHttpHandler;
import com.megaease.easeagent.httpserver.nano.AgentHttpServer;
import com.megaease.easeagent.httpserver.nanohttpd.protocols.http.IHTTPSession;
import com.megaease.easeagent.httpserver.nanohttpd.protocols.http.response.Response;
import com.megaease.easeagent.httpserver.nanohttpd.protocols.http.response.Status;
import com.megaease.easeagent.httpserver.nanohttpd.router.RouterNanoHTTPD;
import com.megaease.easeagent.plugin.utils.common.JsonUtil;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public abstract class ConfigsUpdateAgentHttpHandler extends AgentHttpHandler {
    public abstract Response processConfig(Map<String, String> config, Map<String, String> urlParams, String version);

    protected ConfigManagerMXBean mxBeanConfig;

    static Map<String, String> toConfigMap(Map<String, Object> map) {
        Map<String, String> config = new HashMap<>();
        map.forEach((s, o) -> config.put(s, o.toString()));
        return config;
    }

    @SneakyThrows
    @Override
    public Response process(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
        String body = this.buildRequestBody(session);
        if (StringUtils.isEmpty(body)) {
            return Response.newFixedLengthResponse(Status.BAD_REQUEST, AgentHttpServer.JSON_TYPE, (String) null);
        }
        Map<String, Object> map = JsonUtil.toMap(body);
        if (map == null) {
            return Response.newFixedLengthResponse(Status.BAD_REQUEST, AgentHttpServer.JSON_TYPE, (String) null);
        }
        return processJsonConfig(map, urlParams);
    }

    public Response processJsonConfig(Map<String, Object> map, Map<String, String> urlParams) {
        String version = (String) map.remove("version");
        if (version == null) {
            return Response.newFixedLengthResponse(Status.BAD_REQUEST, AgentHttpServer.JSON_TYPE, (String) null);
        }
        Map<String, String> config = toConfigMap(map);
        Response response = processConfig(config, urlParams, version);
        if (response != null) {
            return response;
        }
        return Response.newFixedLengthResponse(Status.OK, AgentHttpServer.JSON_TYPE, (String) null);
    }
}

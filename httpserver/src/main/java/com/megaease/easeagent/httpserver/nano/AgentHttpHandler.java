/*
 *   Copyright (c) 2017, MegaEase
 *   All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.megaease.easeagent.httpserver.nano;

import com.megaease.easeagent.httpserver.nanohttpd.protocols.http.IHTTPSession;
import com.megaease.easeagent.httpserver.nanohttpd.protocols.http.request.Method;
import com.megaease.easeagent.httpserver.nanohttpd.protocols.http.response.IStatus;
import com.megaease.easeagent.httpserver.nanohttpd.protocols.http.response.Response;
import com.megaease.easeagent.httpserver.nanohttpd.protocols.http.response.Status;
import com.megaease.easeagent.httpserver.nanohttpd.router.RouterNanoHTTPD;
import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public abstract class AgentHttpHandler extends RouterNanoHTTPD.DefaultHandler {

    public abstract String getPath();

    protected String text;
    protected Set<Method> methods = new HashSet<>(Arrays.asList(Method.PUT, Method.POST));

    @Override
    public String getText() {
        return this.text;
    }

    @SneakyThrows
    protected String buildRequestBody(IHTTPSession session) {
        Map<String, String> files = new HashMap<>();
        Method method = session.getMethod();
        if (!methods.contains(method)) {
            return null;
        }
        session.parseBody(files);
        String content = files.get("content");
        if (content != null) {
            Path path = Paths.get(content);
            byte[] bytes = Files.readAllBytes(path);
            return new String(bytes, StandardCharsets.UTF_8);
        }
        return files.get("postData");
    }

    @Override
    public IStatus getStatus() {
        return Status.OK;
    }

    @Override
    public String getMimeType() {
        return null;
    }

    public abstract Response process(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, IHTTPSession session);

    @Override
    public Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
        return this.process(uriResource, urlParams, session);
    }
}

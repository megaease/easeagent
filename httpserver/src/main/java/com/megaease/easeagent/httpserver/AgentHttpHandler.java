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

package com.megaease.easeagent.httpserver;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;
import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public abstract class AgentHttpHandler extends RouterNanoHTTPD.DefaultHandler {

    public abstract String getPath();

    protected String text;

    @Override
    public String getText() {
        return this.text;
    }

    @SneakyThrows
    protected String buildRequestBody(NanoHTTPD.IHTTPSession session) {
        Map<String, String> files = new HashMap<>();
        NanoHTTPD.Method method = session.getMethod();
        if (!NanoHTTPD.Method.PUT.equals(method) && !NanoHTTPD.Method.POST.equals(method)) {
            return null;
        }
        session.parseBody(files);
        String content = files.get("content");
        Path path = Paths.get(content);
        byte[] bytes = Files.readAllBytes(path);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Override
    public NanoHTTPD.Response.IStatus getStatus() {
        return NanoHTTPD.Response.Status.OK;
    }

    @Override
    public String getMimeType() {
        return null;
    }

    public abstract NanoHTTPD.Response process(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session);

    @Override
    public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        return this.process(uriResource, urlParams, session);
    }
}

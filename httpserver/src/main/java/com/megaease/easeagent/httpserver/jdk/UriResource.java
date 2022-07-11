/*
 * Copyright (c) 2022, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.megaease.easeagent.httpserver.jdk;

import com.megaease.easeagent.plugin.api.logging.Logger;
import com.megaease.easeagent.plugin.bridge.EaseAgent;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("all")
public class UriResource implements Comparable<UriResource> {
    Logger LOG = EaseAgent.getLogger(UriResource.class);

    private static final Pattern PARAM_PATTERN = Pattern.compile("(?<=(^|/)):[a-zA-Z0-9_-]+(?=(/|$))");

    private static final String PARAM_MATCHER = "([A-Za-z0-9\\-\\._~:/?#\\[\\]@!\\$&'\\(\\)\\*\\+,;=\\s]+)";

    private static final Map<String, String> EMPTY = Collections.unmodifiableMap(new HashMap<>());

    private final String uri;

    private final Pattern uriPattern;

    private int priority;

    private final Class<?> handler;

    private final Object[] initParameter;

    private final List<String> uriParams = new ArrayList<>();

    public UriResource(String uri, int priority, Class<?> handler, Object... initParameter) {
        this(uri, handler, initParameter);
        this.priority = priority + uriParams.size() * 1000;
    }

    public UriResource(String uri, Class<?> handler, Object... initParameter) {
        this.handler = handler;
        this.initParameter = initParameter;
        if (uri != null) {
            this.uri = normalizeUri(uri);
            parse();
            this.uriPattern = createUriPattern();
        } else {
            this.uriPattern = null;
            this.uri = null;
        }
    }

    private void parse() {
    }

    private Pattern createUriPattern() {
        String patternUri = uri;
        Matcher matcher = PARAM_PATTERN.matcher(patternUri);
        int start = 0;
        while (matcher.find(start)) {
            uriParams.add(patternUri.substring(matcher.start() + 1, matcher.end()));
            patternUri = new StringBuilder(patternUri.substring(0, matcher.start()))//
                .append(PARAM_MATCHER)//
                .append(patternUri.substring(matcher.end())).toString();
            start = matcher.start() + PARAM_MATCHER.length();
            matcher = PARAM_PATTERN.matcher(patternUri);
        }
        return Pattern.compile(patternUri);
    }

    @Override
    public String toString() {
        return new StringBuilder("UrlResource{uri='").append((uri == null ? "/" : uri))//
            .append("', urlParts=").append(uriParams)//
            .append('}')//
            .toString();
    }

    public String getUri() {
        return uri;
    }

    public <T> T initParameter(Class<T> paramClazz) {
        return initParameter(0, paramClazz);
    }

    public <T> T initParameter(int parameterIndex, Class<T> paramClazz) {
        if (initParameter.length > parameterIndex) {
            return paramClazz.cast(initParameter[parameterIndex]);
        }
        LOG.error("init parameter index not available " + parameterIndex);
        return null;
    }

    public Map<String, String> match(String url) {
        Matcher matcher = uriPattern.matcher(url);
        if (matcher.matches()) {
            if (uriParams.size() > 0) {
                Map<String, String> result = new HashMap<String, String>();
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    result.put(uriParams.get(i - 1), matcher.group(i));
                }
                return result;
            } else {
                return EMPTY;
            }
        }
        return null;
    }

    @Override
    public int compareTo(UriResource that) {
        if (that == null) {
            return 1;
        } else if (this.priority > that.priority) {
            return 1;
        } else if (this.priority < that.priority) {
            return -1;
        } else {
            return 0;
        }
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }


    public static String normalizeUri(String value) {
        if (value == null) {
            return value;
        }
        if (value.startsWith("/")) {
            value = value.substring(1);
        }
        if (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }
}

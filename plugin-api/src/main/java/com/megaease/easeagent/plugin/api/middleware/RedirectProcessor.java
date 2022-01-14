/*
 * Copyright (c) 2017, MegaEase
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
 */

package com.megaease.easeagent.plugin.api.middleware;

import com.fasterxml.jackson.core.type.TypeReference;
import com.megaease.easeagent.plugin.api.logging.Logger;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.utils.SystemEnv;
import com.megaease.easeagent.plugin.utils.common.JsonUtil;
import com.megaease.easeagent.plugin.utils.common.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RedirectProcessor {
    private static final Logger LOGGER = EaseAgent.getLogger(RedirectProcessor.class);
    protected static final String ENV_EASEMESH_TAGS = "EASEMESH_TAGS";

    public static final RedirectProcessor INSTANCE = new RedirectProcessor();

    private volatile Map<Redirect, String> redirectedUris = new HashMap<>();
    private final Map<String, String> tags = getServiceTagsFromEnv();

    public static void redirected(Redirect key, String uris) {
        INSTANCE.setRedirected(key, uris);
    }

    public static void setTagsIfRedirected(Redirect key, Span span) {
        setTagsIfRedirected(key, span, null);
    }

    public static void setTagsIfRedirected(Redirect key, Span span, String uris) {
        String remote = getRemote(key, uris);
        if (!StringUtils.isEmpty(remote)) {
            span.tag("label.remote", remote);
        }
    }

    public static void setTagsIfRedirected(Redirect key, Tags tags) {
        String remote = INSTANCE.getRedirected(key);
        if (remote == null) {
            return;
        }
        Map<String, String> serviceTags = INSTANCE.getTags();
        if (serviceTags != null && !serviceTags.isEmpty()) {
            for (Map.Entry<String, String> entry : serviceTags.entrySet()) {
                tags.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public static Map<String, String> tags() {
        return INSTANCE.getTags();
    }

    public Map<String, String> getTags() {
        return tags;
    }

    private static String getRemote(Redirect key, String uris) {
        String remote = INSTANCE.getRedirected(key);
        if (remote == null) {
            return null;
        }
        if (uris != null) {
            return uris;
        } else {
            return remote;
        }
    }


    @SuppressWarnings("all")
    public void init() {
        for (Redirect redirect : Redirect.values()) {//init
            //ignore
        }
    }

    private void setRedirected(Redirect key, String uris) {
        Map<Redirect, String> uriMap = new HashMap<>(this.redirectedUris);
        uriMap.put(key, uris);
        this.redirectedUris = uriMap;
    }

    private String getRedirected(Redirect key) {
        return this.redirectedUris.get(key);
    }


    protected static Map<String, String> getServiceTagsFromEnv() {
        return getServiceTags(ENV_EASEMESH_TAGS);
    }

    protected static Map<String, String> getServiceTags(String env) {
        String str = SystemEnv.get(env);
        if (StringUtils.isEmpty(env)) {
            return Collections.emptyMap();
        }
        try {
            Map<String, String> map = JsonUtil.toObject(str, new TypeReference<Map<String, String>>() {
            });
            Map<String, String> result = new HashMap<>();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (StringUtils.isEmpty(entry.getKey()) || StringUtils.isEmpty(entry.getValue())) {
                    continue;
                }
                result.put(entry.getKey(), entry.getValue());
            }
            return result;
        } catch (Exception e) {
            LOGGER.warn("get env {} result: `{}` to map fail. {}", env, str, e.getMessage());
        }
        return Collections.emptyMap();
    }

}

/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.plugin.springweb.interceptor;

import com.megaease.easeagent.plugin.api.logging.Logger;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import feign.Request;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class HeadersFieldFinder {
    private static final Logger logger = EaseAgent.getLogger(HeadersFieldFinder.class);

    private static Field headersField;

    public static Field getHeadersField() {
        if (headersField != null) {
            return headersField;
        }
        try {
            headersField = Request.class.getDeclaredField("headers");
            headersField.setAccessible(true);
            return headersField;
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Collection<String>> getHeadersFieldValue(Field headersField, Object target) {
        try {
            return (Map<String, Collection<String>>) headersField.get(target);
        } catch (IllegalAccessException e) {
            logger.warn("can not get header in FeignClient. {}", e.getMessage());
        }
        return null;
    }

    public static void setHeadersFieldValue(Field headersField, Object target, Object fieldValue) {
        try {
            headersField.set(target, fieldValue);
        } catch (IllegalAccessException e) {
            logger.warn("can not set header in FeignClient. {}", e.getMessage());
        }
    }

    public static HashMap<String, Collection<String>> getHashMapHeaders(Request request) {
        Field headersField = HeadersFieldFinder.getHeadersField();
        if (headersField != null) {
            Map<String, Collection<String>> originHeaders = HeadersFieldFinder.getHeadersFieldValue(headersField, request);
            if (originHeaders instanceof HashMap) {
                return (HashMap<String, Collection<String>>) originHeaders;
            }
            HashMap<String, Collection<String>> headers = new HashMap<>();
            if (originHeaders != null) {
                headers.putAll(originHeaders);
            }
            HeadersFieldFinder.setHeadersFieldValue(headersField, request, headers);
            return headers;
        }
        return new HashMap<>();
    }
}

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

package com.megaease.easeagent.plugin.utils.trace;

public interface TraceConst {
    String HTTP_HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
    String HTTP_ATTRIBUTE_ROUTE = "http.route";
    String HTTP_TAG_ROUTE = HTTP_ATTRIBUTE_ROUTE;
    String HTTP_TAG_METHOD = "http.method";
    String HTTP_TAG_PATH = "http.path";
    String HTTP_TAG_STATUS_CODE = "http.status_code";
    String HTTP_TAG_ERROR = "error";
}

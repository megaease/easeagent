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

package com.megaease.easeagent.plugin.httpservlet.interceptor;

public class TestConst {
    public static final String FORWARDED_NAME = "X-Forwarded-For";
    public static final String FORWARDED_VALUE = "testForwarded";
    public static final String RESPONSE_TAG_NAME = "X-EG-Test";
    public static final String RESPONSE_TAG_VALUE = "X-EG-Test-Value";
    public static final String METHOD = "GET";
    public static final String URL = "http://192.168.5.1:8080/test";
    public static final String QUERY_STRING = "q1=10&q2=testq";
    public static final String ROUTE = "/test";
    public static final String REMOTE_ADDR = "192.168.5.1";
    public static final int REMOTE_PORT = 8080;
    public static final int RESPONSE_BUFFER_SIZE = 1024;
}

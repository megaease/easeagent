/*
 * Copyright (c) 2021 MegaEase
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

package com.megaease.easeagent.plugin.elasticsearch.interceptor;

import org.junit.Assert;
import org.junit.Test;

public class ElasticsearchCtxUtilsTest {

    @Test
    public void endpoint() {
        Assert.assertEquals("idx-1", ElasticsearchCtxUtils.getIndex("/idx-1/_search"));
        Assert.assertEquals("idx-1", ElasticsearchCtxUtils.getIndex("/idx-1/_doc/122"));
        Assert.assertEquals("", ElasticsearchCtxUtils.getIndex("/_xpack"));
        Assert.assertEquals("", ElasticsearchCtxUtils.getIndex(""));
        Assert.assertEquals("", ElasticsearchCtxUtils.getIndex("/"));
    }
}

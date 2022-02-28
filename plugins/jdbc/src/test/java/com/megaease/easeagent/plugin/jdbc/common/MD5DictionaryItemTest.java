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

package com.megaease.easeagent.plugin.jdbc.common;

import com.megaease.easeagent.plugin.tools.config.NameAndSystem;
import com.megaease.easeagent.plugin.utils.common.JsonUtil;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class MD5DictionaryItemTest {

    @Test
    public void getTimestamp() {
        MD5DictionaryItem item = MD5DictionaryItem.builder().timestamp(System.currentTimeMillis()).build();
        String json = JsonUtil.toJson(item);
        Map<String, Object> map = JsonUtil.toMap(json);
        assertEquals(item.getTimestamp(), map.get("timestamp"));
    }

    @Test
    public void getCategory() {
        MD5DictionaryItem item = MD5DictionaryItem.builder().category("getCategory").build();
        String json = JsonUtil.toJson(item);
        Map<String, Object> map = JsonUtil.toMap(json);
        assertEquals(item.getCategory(), map.get("category"));
    }

    @Test
    public void getHostName() {
        MD5DictionaryItem item = MD5DictionaryItem.builder().hostName("testHost").build();
        String json = JsonUtil.toJson(item);
        Map<String, Object> map = JsonUtil.toMap(json);
        if (map.get("hostName") != null) {
            assertEquals(item.getHostName(), map.get("hostName"));
        } else {
            assertEquals(item.getHostName(), map.get("host_name"));
        }

    }

    @Test
    public void getHostIpv4() {
        MD5DictionaryItem item = MD5DictionaryItem.builder().hostIpv4("192.168.0.15").build();
        String json = JsonUtil.toJson(item);
        Map<String, Object> map = JsonUtil.toMap(json);
        if (map.get("hostIpv4") != null) {
            assertEquals(item.getHostIpv4(), map.get("hostIpv4"));
        } else {
            assertEquals(item.getHostIpv4(), map.get("host_ipv4"));
        }

    }

    @Test
    public void getGid() {
        MD5DictionaryItem item = MD5DictionaryItem.builder().gid("testGid").build();
        String json = JsonUtil.toJson(item);
        Map<String, Object> map = JsonUtil.toMap(json);
        assertEquals(item.getGid(), map.get("gid"));
    }

    @Test
    public void getService() {
        MD5DictionaryItem item = MD5DictionaryItem.builder().service(NameAndSystem.name()).build();
        String json = JsonUtil.toJson(item);
        Map<String, Object> map = JsonUtil.toMap(json);
        assertEquals(NameAndSystem.name(), map.get("service"));
    }

    @Test
    public void getSystem() {
        MD5DictionaryItem item = MD5DictionaryItem.builder().system(NameAndSystem.system()).build();
        String json = JsonUtil.toJson(item);
        Map<String, Object> map = JsonUtil.toMap(json);
        assertEquals(NameAndSystem.system(), map.get("system"));
    }

    @Test
    public void getType() {
        MD5DictionaryItem item = MD5DictionaryItem.builder().type("testType").build();
        String json = JsonUtil.toJson(item);
        Map<String, Object> map = JsonUtil.toMap(json);
        assertEquals(item.getType(), map.get("type"));

    }

    @Test
    public void getTags() {
        MD5DictionaryItem item = MD5DictionaryItem.builder().tags("testTags").build();
        String json = JsonUtil.toJson(item);
        Map<String, Object> map = JsonUtil.toMap(json);
        assertEquals(item.getTags(), map.get("tags"));
    }

    @Test
    public void getId() {
        MD5DictionaryItem item = MD5DictionaryItem.builder().id("testId").build();
        String json = JsonUtil.toJson(item);
        Map<String, Object> map = JsonUtil.toMap(json);
        assertEquals(item.getId(), map.get("id"));
    }

    @Test
    public void getMd5() {
        MD5DictionaryItem item = MD5DictionaryItem.builder().md5("testMd5").build();
        String json = JsonUtil.toJson(item);
        Map<String, Object> map = JsonUtil.toMap(json);
        assertEquals(item.getMd5(), map.get("md5"));

    }

    @Test
    public void getSql() {
        MD5DictionaryItem item = MD5DictionaryItem.builder().md5("sql").build();
        String json = JsonUtil.toJson(item);
        Map<String, Object> map = JsonUtil.toMap(json);
        assertEquals(item.getSql(), map.get("sql"));

    }
}

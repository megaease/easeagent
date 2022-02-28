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

import com.google.common.cache.Cache;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalNotification;
import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.plugin.api.utils.TagVerifier;
import com.megaease.easeagent.mock.report.impl.LastJsonReporter;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class MD5SQLCompressionTest {

    @Test
    public void getInstance() {
        MD5SQLCompression md5SQLCompression = MD5SQLCompression.getInstance();
        assertSame(md5SQLCompression, MD5SQLCompression.getInstance());
    }

    @Test
    public void compress() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        MD5SQLCompression md5SQLCompression = MD5SQLCompression.getInstance();
        Cache<String, String> dictionary = AgentFieldReflectAccessor.getFieldValue(md5SQLCompression, "dictionary");
        Cache<String, String> md5Cache = AgentFieldReflectAccessor.getFieldValue(md5SQLCompression, "md5Cache");
        dictionary.cleanUp();
        md5Cache.cleanUp();
        String sql = "select * from data";
        String md5 = DigestUtils.md5Hex(sql);
        String result = md5SQLCompression.compress(sql);
        assertEquals(md5, result);
        assertEquals(result, md5SQLCompression.compress(sql));
        assertEquals(md5, md5Cache.getIfPresent(sql));
        assertEquals(sql, dictionary.getIfPresent(md5));
        TagVerifier tagVerifier = new TagVerifier()
            .add("category", "application")
            .add("type", "md5-dictionary");
        LastJsonReporter lastJsonReporter = MockEaseAgent.lastMetricJsonReporter(tagVerifier::verifyAnd);
        pushItems();
        Map<String, Object> report = lastJsonReporter.getLastOnlyOne();
        assertEquals(md5, report.get("md5"));
        assertEquals(sql, report.get("sql"));
        dictionary.cleanUp();
        md5Cache.cleanUp();
    }


    public static void pushItems() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = MD5SQLCompression.class.getDeclaredMethod("pushItems");
        method.setAccessible(true);
        method.invoke(MD5SQLCompression.getInstance());
    }

    @Test
    public void onRemoval() {
        String sql = "select * from data";
        String md5 = DigestUtils.md5Hex(sql);
        RemovalNotification<String, String> removalNotification = RemovalNotification.create(md5, sql, RemovalCause.SIZE);
        MD5SQLCompression md5SQLCompression = MD5SQLCompression.getInstance();
        TagVerifier tagVerifier = new TagVerifier()
            .add("category", "application")
            .add("type", "md5-dictionary");
        LastJsonReporter lastJsonReporter = MockEaseAgent.lastMetricJsonReporter(tagVerifier::verifyAnd);
        md5SQLCompression.onRemoval(removalNotification);
        Map<String, Object> result = lastJsonReporter.getLastOnlyOne();
        assertEquals(md5, result.get("md5"));
        assertEquals(sql, result.get("sql"));
    }
}

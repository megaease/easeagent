/*
 * Copyright (c) 2021, MegaEase
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

package com.megaease.easeagent.plugin.jdbc.common;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.logging.Logger;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.async.ScheduleHelper;
import com.megaease.easeagent.plugin.utils.common.DataSize;
import com.megaease.easeagent.plugin.utils.common.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

public class MD5SQLCompression implements SQLCompression, RemovalListener<String, String> {
    private static final Logger logger = EaseAgent.getLogger(MD5SQLCompression.class);

    public static final DataSize MAX_SQL_SIZE = DataSize.ofKilobytes(32);//32KB

    private static final ConcurrentHashMap<String, MD5SQLCompression> INSTANCE_MAP = new ConcurrentHashMap<>();

    private final Cache<String, String> dictionary = CacheBuilder.newBuilder().maximumSize(1000)
        .removalListener(this).build();

    private final Cache<String, String> md5Cache = CacheBuilder.newBuilder().maximumSize(1000).build();

    private final Consumer<Map<String, String>> reportConsumer;

    public MD5SQLCompression(Consumer<Map<String, String>> reportConsumer) {
        this.reportConsumer = reportConsumer;
        ScheduleHelper.DEFAULT.nonStopExecute(10, 5, this::pushItems);
    }

    public static MD5SQLCompression getInstance(Config config) {
        String key = config.namespace();
        if (INSTANCE_MAP.get(key) == null) {
            MD5SQLCompression instance = new MD5SQLCompression(new MD5ReportConsumer(config));
            INSTANCE_MAP.putIfAbsent(key, instance);
        }
        return INSTANCE_MAP.get(key);
    }

    private String cacheLoad(String str) {
        return DigestUtils.md5Hex(str);
    }

    @Override
    public String compress(String origin) {
        try {
            String cutStr = StringUtils.cutStrByDataSize(origin, MAX_SQL_SIZE);
            String md5 = md5Cache.get(cutStr, () -> cacheLoad(cutStr));
            String value = dictionary.getIfPresent(md5);
            if (value == null) {
                dictionary.put(md5, cutStr);
            }
            return md5;
        } catch (Exception e) {
            logger.warn("compress content[{}] failure", origin, e);
            return origin;
        }
    }

    private void pushItems() {
        ConcurrentMap<String, String> map = this.dictionary.asMap();
        if (map.isEmpty()) {
            return;
        }
        this.reportConsumer.accept(map);
    }

    @Override
    public void onRemoval(RemovalNotification<String, String> notification) {
        logger.info("remove md5 dictionary item. cause: {}, md5: {}, content: {}",
            notification.getCause().toString(), notification.getKey(), notification.getValue());
        Map<String, String> map = new HashMap<>();
        map.put(notification.getKey(), notification.getValue());
        reportConsumer.accept(map);
    }
}

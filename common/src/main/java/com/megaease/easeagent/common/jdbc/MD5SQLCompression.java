package com.megaease.easeagent.common.jdbc;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.megaease.easeagent.core.utils.DataSize;
import com.megaease.easeagent.core.utils.TextUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MD5SQLCompression implements SQLCompression, RemovalListener<String, String> {

    public static final DataSize MAX_SQL_SIZE = DataSize.ofKilobytes(32);//32KB

    private static final Logger logger = LoggerFactory.getLogger(MD5SQLCompression.class);

    private Cache<String, String> dictionary = CacheBuilder.newBuilder().maximumSize(1000)
            .removalListener(this).build();

    private Cache<String, String> md5Cache = CacheBuilder.newBuilder().maximumSize(1000).build();

    private final Consumer<Map<String, String>> reportConsumer;

    public MD5SQLCompression(Consumer<Map<String, String>> reportConsumer) {
        this.reportConsumer = reportConsumer;
    }

    @Override
    public String compress(String origin) {
        try {
            String cutStr = TextUtils.cutStrByDataSize(origin, MAX_SQL_SIZE);
            String md5 = md5Cache.get(cutStr, () -> DigestUtils.md5Hex(cutStr));
            String value = dictionary.getIfPresent(md5);
            if (value == null) {
                dictionary.put(md5, cutStr);
            }
            return md5;
        } catch (Exception e) {
            logger.error("compress content[{}] failure", origin, e);
            return origin;
        }
    }

    @Override
    public void onRemoval(RemovalNotification<String, String> notification) {
        logger.info("remove md5 dictionary item. cause: {}, md5: {}, content: {}", notification.getCause().toString(), notification.getKey(), notification.getValue());
        Map<String, String> map = new HashMap<>();
        map.put(notification.getKey(), notification.getValue());
        reportConsumer.accept(map);
    }
}

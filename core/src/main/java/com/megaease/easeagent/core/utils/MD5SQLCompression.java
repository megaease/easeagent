package com.megaease.easeagent.core.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MD5SQLCompression implements SQLCompression, RemovalListener<String, String> {

    public static final DataSize MAX_SQL_SIZE = DataSize.ofKilobytes(32);//32KB

    private static final Logger logger = LoggerFactory.getLogger(MD5SQLCompression.class);

    private Cache<String, String> dictionary = CacheBuilder.newBuilder().maximumSize(1000)
            .removalListener(this).build();

    private Cache<String, String> md5Cache = CacheBuilder.newBuilder().maximumSize(1000).build();


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
        // TODO: 2021/2/26 send data to server
    }
}

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

package com.megaease.easeagent.plugin.redis.interceptor;

import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.utils.ClassUtils;

public class RedisClassUtils {
    public static final HostAndPortTypeChecker HOST_AND_PORT_TYPE_CHECKER = new HostAndPortTypeChecker();
    public static final JedisShardInfoTypeCheker JEDIS_SHARD_INFO_TYPE_CHEKER = new JedisShardInfoTypeCheker();
    public static final JedisSocketFactoryTypeCheker JEDIS_SOCKET_FACTORY_TYPE_CHEKER = new JedisSocketFactoryTypeCheker();

    public static class HostAndPortTypeChecker extends ClassUtils.TypeChecker {
        public HostAndPortTypeChecker() {
            super("redis.clients.jedis.HostAndPort");
        }

        @Override
        protected boolean isType(Object o) {
            return o instanceof redis.clients.jedis.HostAndPort;
        }

        public Object newInstance(String host, int port) {
            return new redis.clients.jedis.HostAndPort(host, port);
        }
    }

    public static class JedisShardInfoTypeCheker extends ClassUtils.TypeChecker {

        public JedisShardInfoTypeCheker() {
            super("redis.clients.jedis.JedisShardInfo");
        }

        @Override
        protected boolean isType(Object o) {
            return o instanceof redis.clients.jedis.JedisShardInfo;
        }

        public void setInfo(Object o, String host, int port, String password) {
            AgentFieldReflectAccessor.setFieldValue(o, "host", host);
            AgentFieldReflectAccessor.setFieldValue(o, "port", port);
            if (password != null) {
                ((redis.clients.jedis.JedisShardInfo) o).setPassword(password);
            }

        }
    }

    public static class JedisSocketFactoryTypeCheker extends ClassUtils.TypeChecker {

        public JedisSocketFactoryTypeCheker() {
            super("redis.clients.jedis.JedisSocketFactory");
        }

        @Override
        protected boolean isType(Object o) {
            return o instanceof redis.clients.jedis.JedisSocketFactory;
        }

        public void setInfo(Object o, String host, int port) {
            redis.clients.jedis.JedisSocketFactory jedisSocketFactory = (redis.clients.jedis.JedisSocketFactory) o;
            jedisSocketFactory.setHost(host);
            jedisSocketFactory.setPort(port);
        }
    }
}

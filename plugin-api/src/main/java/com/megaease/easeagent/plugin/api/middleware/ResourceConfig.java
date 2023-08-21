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

package com.megaease.easeagent.plugin.api.middleware;

import com.fasterxml.jackson.core.type.TypeReference;
import com.megaease.easeagent.plugin.utils.SystemEnv;
import com.megaease.easeagent.plugin.utils.common.JsonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ResourceConfig {

    private String userName;
    private String password;
    private String uris;
    private final List<String> uriList = new ArrayList<>();
    private final List<HostAndPort> hostAndPorts = new ArrayList<>();

    public static ResourceConfig getResourceConfig(String env, boolean needParse) {
        String str = SystemEnv.get(env);
        if (str == null) {
            return null;
        }
        ResourceConfig resourceConfig = JsonUtil.toObject(str, new TypeReference<ResourceConfig>() {
        });
        resourceConfig.parseHostAndPorts(needParse);
        if (resourceConfig.hasUrl()) {
            return resourceConfig;
        }
        return null;
    }

    private void parseHostAndPorts(boolean needParse) {
        if (!needParse) {
            uriList.add(this.uris);
            return;
        }
        if (uris == null || uris.isEmpty()) {
            return;
        }
        String[] list = uris.split(",");
        for (String uri : list) {
            uriList.add(uri);
            int begin = uri.indexOf(":");
            int end = uri.lastIndexOf(":");
            if (begin == end) {
                String[] arr = uri.split(":");
                HostAndPort obj = new HostAndPort();
                obj.setHost(arr[0]);
                obj.setPort(Integer.parseInt(arr[1]));
                this.hostAndPorts.add(obj);
            }
        }
    }

    public HostAndPort getFirstHostAndPort() {
        if (this.hostAndPorts.isEmpty()) {
            return null;
        }
        return this.hostAndPorts.get(0);
    }

    public String getFirstUri() {
        if (uriList == null || uriList.isEmpty()) {
            return null;
        }
        return this.uriList.get(0);
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean hasUrl() {
        return this.uris != null && !this.uris.isEmpty();
    }

    public String getUris() {
        return uris;
    }

    public void setUris(String uris) {
        this.uris = uris;
    }

    public List<String> getUriList() {
        return uriList;
    }

    public List<HostAndPort> getHostAndPorts() {
        return hostAndPorts;
    }

    public static class HostAndPort {
        private String host;
        private Integer port;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            HostAndPort that = (HostAndPort) o;
            return Objects.equals(host, that.host) &&
                Objects.equals(port, that.port);
        }

        @Override
        public int hashCode() {

            return Objects.hash(host, port);
        }

        public String uri() {
            return host + ":" + port;
        }
    }
}

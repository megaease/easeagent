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

package com.megaease.easeagent.core;

import java.util.ArrayList;
import java.util.List;

public class ResourceConfig {

    private String user;
    private String password;
    private List<String> urls;
    private final List<HostAndPort> hostAndPorts = new ArrayList<>();

    public void initHostAndPorts() {
        if (urls == null || urls.isEmpty()) {
            return;
        }
        for (String url : urls) {
            int begin = url.indexOf(":");
            int end = url.lastIndexOf(":");
            if (begin == end) {
                String[] arr = url.split(":");
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

    public String getFirstUrl() {
        if (urls == null || urls.isEmpty()) {
            return null;
        }
        return this.urls.get(0);
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean hasUrl() {
        return this.urls != null && !this.urls.isEmpty();
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
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
    }
}

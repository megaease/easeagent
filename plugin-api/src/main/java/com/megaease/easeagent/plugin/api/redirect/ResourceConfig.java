package com.megaease.easeagent.plugin.api.redirect;

import java.util.ArrayList;
import java.util.List;

public class ResourceConfig {
    private String userName;
    private String password;
    private String uris;
    private final List<String> uriList = new ArrayList<>();
    private final List<HostAndPort> hostAndPorts = new ArrayList<>();

    public void parseHostAndPorts(boolean needParse) {
        if (!needParse) {
            uriList.add(this.uris);
            return;
        }
        if (uris == null || uris.isEmpty()) {
            return;
        }
        String[] list = uris.split(",");
        if (list == null) {
            return;
        }
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
    }
}

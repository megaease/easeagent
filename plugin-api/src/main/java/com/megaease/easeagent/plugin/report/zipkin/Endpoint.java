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
 *
 */
package com.megaease.easeagent.plugin.report.zipkin;

import lombok.Setter;

import java.util.Objects;

/**
 * from zipkin2.Endpoint
 */
@Setter
public class Endpoint {
    String serviceName;
    String ipv4;
    String ipv6;
    int port;

    /**
     * Lower-case label of this node in the service graph, such as "favstar". Leave absent if
     * unknown.
     *
     * <p>This is a primary label for trace lookup and aggregation, so it should be intuitive and
     * consistent. Many use a name from service discovery.
     */
    public String serviceName() {
        return serviceName;
    }

    /**
     * The text representation of the primary IPv4 address associated with this a connection. Ex.
     * 192.168.99.100 Absent if unknown.
     */
    public String ipv4() {
        return ipv4;
    }

    /**
     * The text representation of the primary IPv6 address associated with this a connection. Ex.
     * 2001:db8::c001 Absent if unknown.
     *
     * @see #ipv4() for mapped addresses
     */
    public String ipv6() {
        return ipv6;
    }

    /**
     * Port of the IP's socket or null, if not known.
     *
     * @see java.net.InetSocketAddress#getPort()
     */
    public int port() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Endpoint)) {
            return false;
        }
        Endpoint that = (Endpoint) o;
        return (Objects.equals(serviceName, that.serviceName))
            && (Objects.equals(ipv4, that.ipv4))
            && (Objects.equals(ipv6, that.ipv6))
            && port == that.port;
    }

    @Override
    public int hashCode() {
        int h = 1;
        h *= 1000003;
        h ^= (serviceName == null) ? 0 : serviceName.hashCode();
        h *= 1000003;
        h ^= (ipv4 == null) ? 0 : ipv4.hashCode();
        h *= 1000003;
        h ^= (ipv6 == null) ? 0 : ipv6.hashCode();
        h *= 1000003;
        h ^= port;
        return h;
    }
}

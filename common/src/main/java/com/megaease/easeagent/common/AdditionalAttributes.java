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

package com.megaease.easeagent.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


/**
 * AdditionalAttributes hold the global attributes field values which are
 * always same in an instance, such as</p>
 * <b>host_ipv4</b>: an ip address to identical instance address
 * <b>hostname</b>: a name to identical instance name
 * <b>serviceName</b> service name
 */
public class AdditionalAttributes {
    private final static Logger LOGGER = LoggerFactory.getLogger(AdditionalAttributes.class);
    public final Map<String, Object> attributes;

    public AdditionalAttributes(String serviceName, String systemName, String tenantId) {
        attributes = new HashMap<>();
        attributes.put("host_ipv4", getHostIpv4());
        attributes.put("service", serviceName);
        attributes.put("system", systemName);
        attributes.put("host_name", getHostName());
        attributes.put("tenant_id", tenantId);
    }

    public AdditionalAttributes(String serviceName) {
        this(serviceName, "none", "99999999");
    }

    static String getHostIpV4(Enumeration<NetworkInterface> networkInterfaces) throws Exception {
        String ip;
        String secondaryIP = "";
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface i = networkInterfaces.nextElement();
            if (i.isLoopback()) {
                continue;
            }

            if (isPrimaryInterface(i)) {
                // We treat interface name which started with "en" or "eth" as primary interface.
                // We prefer to use address of primary interface as value of the `host_ipv4`
                ip = ipAddressFromInetAddress(i);
                if (!isEmpty(ip)) {
                    return ip;
                }
            } else if (isEmpty(secondaryIP)) {
                secondaryIP = ipAddressFromInetAddress(i);
            }
        }

        return !isEmpty(secondaryIP) ? secondaryIP : "UnknownIP";
    }

    private static boolean isEmpty(String text) {
        return text == null || text.trim().length() == 0;
    }

    private static boolean isPrimaryInterface(NetworkInterface i) {
        return i.getName().startsWith("en") || i.getName().startsWith("eth");
    }

    private static String ipAddressFromInetAddress(NetworkInterface i) {
        String ip = "";
        Enumeration<InetAddress> ee = i.getInetAddresses();
        while (ee.hasMoreElements()) {
            InetAddress a = ee.nextElement();
            if (a instanceof Inet4Address) {
                if (!a.isMulticastAddress() && !a.isLoopbackAddress()) {
                    ip = a.getHostAddress();
                    break;
                }
            }
        }
        return ip;
    }

    public Map<String, Object> getAdditionalAttributes() {
        return attributes;
    }

    public static String getHostName() {
        try {
            return (InetAddress.getLocalHost()).getHostName();
        } catch (UnknownHostException uhe) {
            // host = "hostname: hostname"
            String host = uhe.getMessage();
            if (host != null) {
                int colon = host.indexOf(':');
                if (colon > 0) {
                    return host.substring(0, colon);
                }
            }
            return "UnknownHost";
        }
    }

    private String getHostIpv4() {
        try {
            return getHostIpV4(NetworkInterface.getNetworkInterfaces());
        } catch (Exception e) {
//            LOGGER.warn("can't fetch ip address ", e);
        }
        return "UnknownIP";
    }


    public static String getLocalIP() {
        try {
            return getHostIpV4(NetworkInterface.getNetworkInterfaces());
        } catch (Exception e) {
            return null;
        }
    }
}

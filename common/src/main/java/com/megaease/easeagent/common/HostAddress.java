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

import org.apache.commons.lang3.StringUtils;

import java.net.*;
import java.util.Enumeration;

public abstract class HostAddress {
    private static final String LOCALHOST_NAME;
    private static final String IPV4;
    private static final String UNKNOWN_LOCALHOST = "UNKNOWN_LOCALHOST";

    static {
        IPV4 = initHostIpv4();
        LOCALHOST_NAME = initLocalHostname();
    }

    public static String localhost() {
        return LOCALHOST_NAME;
    }

    public static String getHostIpv4() {
        return IPV4;
    }

    public static String initHostIpv4() {
        try {
            return getHostIpV4(NetworkInterface.getNetworkInterfaces());
        } catch (Exception ignored) {
        }
        return "UnknownIP";
    }

    private static String getHostIpV4(Enumeration<NetworkInterface> networkInterfaces) throws Exception {
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
                if (!StringUtils.isEmpty(ip)) {
                    return ip;
                }
            } else if (StringUtils.isEmpty(secondaryIP)) {
                secondaryIP = ipAddressFromInetAddress(i);
            }

        }
        return !StringUtils.isEmpty(secondaryIP) ? secondaryIP : "UnknownIP";
    }

    private static boolean isPrimaryInterface(NetworkInterface i) {
        return i.getName().startsWith("en") || i.getName().startsWith("eth");
    }

    private static String ipAddressFromInetAddress(NetworkInterface i) {
        Enumeration<InetAddress> ee = i.getInetAddresses();
        while (ee.hasMoreElements()) {
            InetAddress a = ee.nextElement();
            if (a instanceof Inet4Address) {
                if (!a.isMulticastAddress() && !a.isLoopbackAddress()) {
                    return a.getHostAddress();
                }
            }
        }
        return "";
    }

    public static String initLocalHostname() {
        // copy from log4j NetUtils
        try {
            final InetAddress addr = InetAddress.getLocalHost();
            return addr == null ? UNKNOWN_LOCALHOST : addr.getHostName();
        } catch (final UnknownHostException uhe) {
            try {
                final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    final NetworkInterface nic = interfaces.nextElement();
                    final Enumeration<InetAddress> addresses = nic.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        final InetAddress address = addresses.nextElement();
                        if (!address.isLoopbackAddress()) {
                            final String hostname = address.getHostName();
                            if (hostname != null) {
                                return hostname;
                            }
                        }
                    }
                }
            } catch (final SocketException se) {
                return UNKNOWN_LOCALHOST;
            }
            return UNKNOWN_LOCALHOST;
        }
    }

    private HostAddress() {
    }
}

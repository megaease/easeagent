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

package com.megaease.easeagent.sniffer.jdbc.interceptor;

import com.megaease.easeagent.core.MiddlewareConfigProcessor;
import com.megaease.easeagent.core.ResourceConfig;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;

import java.util.Map;

public class HikariSetJdbcUrlInterceptor implements AgentInterceptor {
    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        ResourceConfig cnf = MiddlewareConfigProcessor.INSTANCE.getData(MiddlewareConfigProcessor.ENV_DATABASE);
        if (cnf == null) {
            AgentInterceptor.super.before(methodInfo, context, chain);
            return;
        }
        ResourceConfig.HostAndPort hostAndPort = cnf.getFirstHostAndPort();
        if (hostAndPort == null) {
            String jdbcUrl = cnf.getFirstUrl();
            methodInfo.getArgs()[0] = jdbcUrl;

        } else {
            String host = hostAndPort.getHost();
            Integer port = hostAndPort.getPort();
            String jdbcUrl = (String) methodInfo.getArgs()[0];
            methodInfo.getArgs()[0] = this.replaceHostAndPort(jdbcUrl, host, port);
        }
        AgentInterceptor.super.before(methodInfo, context, chain);
    }

    public String replaceHostAndPort(String jdbcUrl, String host, Integer port) {
        if (jdbcUrl.startsWith("jdbc:mysql:")) {
            return replaceHostAndPort4Mysql(jdbcUrl, host, port);
        }
        //db2 - https://www.ibm.com/docs/en/db2-for-zos/11?topic=cdsudidsdjs-url-format-data-server-driver-jdbc-sqlj-type-4-connectivity
        //mssql - https://docs.microsoft.com/en-us/sql/connect/jdbc/building-the-connection-url?view=sql-server-ver15
        //oracle - https://docs.oracle.com/cd/E11882_01/appdev.112/e13995/oracle/jdbc/OracleDriver.html
        //oracle - jdbc:oracle:thin:@//host:port/service_name
        //oracle - jdbc:oracle:thin:@host:port:SID
        //oracle - jdbc:oracle:thin:@TNSName
        //postgresql - https://jdbc.postgresql.org/documentation/80/connect.html
        return null;
    }

    private static String replaceHostAndPort4Mysql(String jdbcUrl, String host, Integer port) {
        //mysql - https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-jdbc-url-format.html
        //mysql - jdbc:mysql://host1:33060/dbName
        int idx = jdbcUrl.indexOf("://");
        int hostAndPortBeginIdx = idx + 3;
        int hostAndPortEndIdx = jdbcUrl.indexOf("/", hostAndPortBeginIdx);
        return jdbcUrl.substring(0, hostAndPortBeginIdx) + host + ":" + port + jdbcUrl.substring(hostAndPortEndIdx);
    }

    public static void main(String[] args) {
        String url = "jdbc:mysql://host1/dbName";
        System.out.println(replaceHostAndPort4Mysql(url, "localhost", 90999));
    }
}

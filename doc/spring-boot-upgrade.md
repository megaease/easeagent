# Spring Boot upgrade

## Background

Different versions of spring-boot may use different technologies, depend on different jar packages, and implement
different methods.

For example, in spring-boot 2 and spring-boot 3, the httpserver of spring-boot 2 uses httpservlet, while the httpserver
of spring-boot 3 uses tomcat.

Some classes or methods have been deprecated after upgrading to spring-boot 3, and the old sampling entry points will
also be deprecated.

Therefore, different entry points and different sampling implementations are required according to different code
versions.

## Plugin Dependencies

### jdk dependencies

| spring-boot 2.x | spring-boot 3.x | 
|:----------------|:----------------|
| jdk8            | jdk17           |

### plugin dependencies

| plugin name       | spring-boot 2.x jar | spring-boot 3.x jar                               | 
|:------------------|:--------------------|:--------------------------------------------------|
| httpURLConnection | httpurlconnection   | httpurlconnection-jdk17                           |
| httpServlet       | httpservlet         |                                                   |
| tomcat            |                     | tomcat-jdk17                                      |
| springGateway     | spring-gateway      | spring-boot-3.5.3/spring-boot-gateway-3.5.3       |
| resTemplate       | springweb           | spring-boot-3.5.3/spring-boot-rest-template-3.5.3 |
| serviceName       | servicename         | spring-boot-3.5.3/spring-boot-servicename-3.5.3   |

## Base config

When your code uses Spring Boot 3.x.x, it means that your code depends on JDK 17+ and Spring Boot 3+.

In this case, you need to add two configurations for the agent to take effect:

```properties
runtime.code.version.points.jdk=jdk17
runtime.code.version.points.spring-boot=3.x.x
```

## Features
Easy to upgrade. If you upgrade your spring-boot version in the future, you only need to add the missing plugins.

If HTTP Sever is changed from tomcat to jetty, add the jetty plugin.

If there are incompatible methods in the plug-in below spring-boot-3.5.3, you only need to copy spring-boot-3.5.3 to a higher version, find the incompatible class or method, and implement the sampling logic.




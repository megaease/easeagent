# Plugin Development Guide
- [Plugin Development Guide](#plugin-development-guide)
  - [Overview](#overview)
    - [Architecture](#architecture)
  - [Plugin Structure](#plugin-structure)
    - [Points](#points)
    - [Interceptor](#interceptor)
      - [Plugin Orchestration](#plugin-orchestration)
      - [AdviceTo Annotation](#adviceto-annotation)
    - [AgentPlugin: Plugin Configuration](#agentplugin-plugin-configuration)
    - [A Simple Plugin Example](#a-simple-plugin-example)
      - [Points of Simple Plugin](#points-of-simple-plugin)
      - [Interceptor of Simple Plugin](#interceptor-of-simple-plugin)
      - [AgentPlugin](#agentplugin)
      - [Test](#test)
  - [EaseAgent Tracing Plugin of Reality Sample](#easeagent-tracing-plugin-of-reality-sample)
    - [Span and Trace](#span-and-trace)
    - [Context Overview](#context-overview)
    - [HttpServletPlugin](#httpservletplugin)
  - [Context](#context)
  - [Tracing API](#tracing-api)
  - [Metric API](#metric-api)
  - [Logging API](#logging-api)
  - [Configuration API](#configuration-api)
    - [Customize](#customize)
  - [Plugin Unit Test](#plugin-unit-test)
  - [EaseAgent Plugin Debug FAQ](#easeagent-plugin-debug-faq)
    - [Development Environment Configuration](#development-environment-configuration)
    - [Enhancement Debug](#enhancement-debug)
    - [Interceptor Debug](#interceptor-debug)
    - [Performance Verification](#performance-verification)
- [References](#references)

## Overview
### Architecture

![image](./images/EaseAgent-Architecture-v2.0.jpg)

The core pivot of a Javaagent is the ability to enhance specific methods to implement enhanced business, such as Tracing and Metric business. Therefore, EaseAgent needs an easy to understand and use, efficient and reliable plugin framework that allows users to easily enhance specific methods to achieve business requirements.

To make plugin framework easy to understand and use, we have abstracted the plugin into the `three elements`, **Points**, **Interceptor**, and **AgentPlugin**.
- **Points** is used to define where to enhance.
- **Interceptor** is used to define what to do at enhanced *Points*.
- **AgentPlugin** makes the plugin configurable and configuration can be updated dynamically at runtime.

Efficient and reliable, the architecture design need to address two situations.
- **Multiple plugins perform multiple enhancements to a method**. We let multiple plugin interceptors on the same method form an interceptors Chain. Then, let each method point only enhanced by a simple piece of bytecode, and allocate a unique Id (unique Index) for the enhanced method, which is used as an array index to get the corresponding Interceptors Chains.
- **Plugins can be independent or collaborative**.  In an Interceptors Chain, Interceptors can be scheduled by priorities and a mechanism for exchanging data between interceptors is provided.

Moreover, Interceptors are enhanced to achieve business requirements, we provide a set of API for the most common Tracing and Metric services, so that the enhancement plugin can complete the Tracing and Metric collection requirements quickly with the help of API. The Report component is responsible for formatting the data and uploading it to the back-end server. It can also be customized and extended to meet the needs of different data formats and network architectures.

This document describes how to develop plugins for Easeagent, and it will be divided into the following sections to introduce plugin development.
1. Plugin structure and examples, the plugin contains three core components, which are the **Points**, **Interceptor**, and **AgentPlugin definition**.
2. Tracing API, which helps users complete the transaction tracing task.
3. Metric API, helps users to complete metrics data collection.
4. Logging API
5. Configuration API
6. Debug FAQ

##  Plugin Structure
All plugin-modules are locate in the `plugins` folder under the top-level directory of Easeagent project and a plugin-module can contains several plugins, eg. a "Tracking Plugin" and a "Metric Plugin".
![image](./images/httpServletPlugin-module.jpg)

As mentioned before, we abstract the plugin into the "three elements" corresponding to three interfaces, **Points**, **Interceptor** and **AgentPlugin**. The development of a plugin is an implementation of these three interfaces, which complete the definition of where to enhanced, what to do at the enhancement point and the configuration of the plugin respectively.

### Points
`Points` implementation defines methods to be enhanced and if a dynamic private member with access methods for that member are added to the instance of matched classes.
When there is only one methodMatcher in the return set of `getMethodMather()`, the qualifier value defaults to 'default', and there is no need to explicitly assign a value.
When there are multiple methods in a matched class that require enhancement with different interceptors, a qualifier needs to be assigned to each `MethodMatcher` as the keyword used by different interceptors to bind.

To decouple from ByteBuddy, `ClassMatcher` and `MethodMatcher` are wrapped with reference to the DSL of **ByteBuddy**.

The DSL of `ClassMatcher` and `MethodMatcher` is described in [Matcher DSL](./matcher-DSL.md)
```java
public interface Points {
    /**
     * return the defined class matcher matching a class or a group of classes
     * eg.
     * ClassMatcher.builder()
     *      .hadInterface(A)
     *      .isPublic()
     *      .isAbstract()
     *      .build()
     *      .or()
     *        .hasSuperClass(B)
     *        .isPublic()
     *        .build())
     */
    IClassMatcher getClassMatcher();

    /**
     * return the defined method matcher
     * eg.
     * MethodMatcher.builder().named("execute")
     *      .isPublic()
     *      .argNum(2)
     *      .arg(1, "java.lang.String")
     *      .build().toSet()
     * or
     * MethodMatcher.multiBuilder()
     *      .match(MethodMatcher.builder().named("<init>")
     *          .argsLength(3)
     *          .arg(0, "org.apache.kafka.clients.consumer.ConsumerConfig")
     *          .qualifier("constructor")
     *          .build())
     *      .match(MethodMatcher.builder().named("poll")
     *          .argsLength(1)
     *          .arg(0, "java.time.Duration")
     *          .qualifier("poll")
     *          .build())
     *      .build();
     */
    Set<IMethodMatcher> getMethodMatcher();

    /**
     * when return true, the transformer will add a Object field and a accessor
     * The dynamically added member can be accessed by AgentDynamicFieldAccessor:
     *
     * AgentDynamicFieldAccessor.setDynamicFieldValue(instance, value)
     * value = AgentDynamicFieldAccessor.getDynamicFieldValue(instance)
     */
    default boolean isAddDynamicField() {
        return false;
    }
}
```

### Interceptor
Interceptors is the core of implementing specific enhancements.

`Interceptor` interface has a name method `getType` and a initialization method `init`.
- The name will be used as `serviceId` in combination with the `domain` and `namespace` of the binding plugin to get the plugin configuration which will be automatically injected into the `Context`. The description of the plugin configuration can be found in the user manual.
- The `init` method is invoked during transform, allowing users to initialize static resources of interceptor, and also allowing to load third party classes which can't load by runtime classloader.

The `before` and `after` methods of the interceptor are invoked when the method being enhanced enters and returns, respectively.
Both `before` and `after` methods have parameters `MethodInfo` and `Context`.
- `MethodInfo` contains all method information, including class name, method name, parameters, return value and exception information.
- `Context` contains the Interceptor configuration that is automatically injected and updated ant other interface that support `tracing`, for details, please refer to the [Tracing API](#tracing-api) section.

```java
public interface Interceptor extends Ordered {
    /**
     * @param methodInfo instrumented method info
     * @param context    Interceptor can pass data, method `after` of interceptor can receive context data
     */
    void before(MethodInfo methodInfo, Context context);

    /**
     * @param methodInfo instrumented method info
     * @param context    Interceptor can pass data, method `after` of interceptor can receive context data
     */
    default void after(MethodInfo methodInfo, Context context) {
    };

    /**
     * Interceptor can get interceptor config thought Config API :
     * EaseAgent.configFactory.getConfig
     * Config API require 3 params: domain, nameSpace, name
     * domain and namespace are defined by plugin, the third param, name is defined here
     *
     * @return name, eg. tracing, metric, etc.
     */
    default String getType() {
        return Order.TRACING.getName();
    }

    /**
     * Initialization method for the interceptor,
     * This method will be called and only be called once for every method which is injected by this interceptor,
     * which means this method may be called several times, when there are several methods matched
     *
     * @param config interceptor configuration
     * @param className injected method's class name
     * @param methodName injected method name
     * @param methodDescriptor injected method descriptor
     */
    default void init(Config config, String className, String methodName, String methodDescriptor) {
    }
}
```
The `Interceptor` interface also includes the `Order` interface that defines the order of the interceptors.

#### Plugin Orchestration
When several Interceptors are injected into a single enhancement point, the order of execution of the Interceptors are determined by the Order of the Interceptor and the Order of the Plugin the interceptor belongs to.
```
Effective Order = Interceptor Order << 8 + Plugin Order
```

The `before` method of the interceptor will be invoked in descending order of the `Effective Order`, that is, the smaller order will have higher priority.
The `after` method will be invoked in the opposite order as the before method was invoked.


#### AdviceTo Annotation
Within a plugin, there may be multiple interceptors, and multiple enhancement points, so which enhancement point is a particular interceptor used for?

This can be specified through the `@AdviceTo` annotation, which is applied on the Interceptor's implementation to specify the enhancement point binding with the Interceptor.


```java
/**
 * use to annotate Interceptor implementation,
 * to link Interceptor to Points and AgentPlugin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(AdvicesTo.class)
public @interface AdviceTo {
    Class<? extends Points> value();
    Class<? extends AgentPlugin> plugin() default AgentPlugin.class;
    String qualifier() default "default";
}
```
The `@AdviceTo` annotation associates an `Interceptor` to the enhanced `Points` implementation specified by `value()`, and more specifically to the method matcher in that Points with the name specified by `qualifier()` which defaults to "default".
```java
@AdviceTo(value = DoFilterPoints.class, plugin = SimplePlugin.class)
public class ResponseHeaderInterceptor implements Interceptor {
}
```
The `@AdviceTo` annotation also binds the Interceptor to a specific plugin via `plugin()`, which gives the `Interceptor` the ability to fetch dynamically updated configurations of the plugin. The Interceptor's plugin configuration is accessible via the Context's `getConfig()` method, the details will described in following `AgentPlugin` section.

### AgentPlugin: Plugin Configuration
Plugin definition, defines what `domain` and `namespace` of this plugin by implement the `AgentPlugin` interface.

```java
public interface AgentPlugin extends Ordered {
    /**
     * define the plugin name, avoiding conflicts with others
     * it will be use as namespace when get configuration.
     */
    String getNamespace();

    /**
     * define the plugin domain,
     * it will be use to get configuration when loaded:
     */
    String getDomain();
}

```
The `domain` and `namespace` of a plugin determine the configuration prefix for each interceptor bind to the plugin by `@AdviceTo` annotation.

The format of the plugin configuration is defined as follows.
```
plugin.[domain].[namespace].[type].[key] = [value]
```
Take the tracing switch of `httpclient` as an example.
```
plugin.observability.httpclient.tracing.enabled=true

domain          : observability
namespace       : httpclient
type            : tracing
key             : enabled
value           : true
```

`[domain]` and `[namespace]` are defined by `AgentPlugin` interface implementations.

The `type` is provided by `Interceptor` interface implementation's `getType()` method, and this method need return a String value like 'tracing', 'metric', and 'redirect' which are already defined by Easeagent, or any other user-defined keyword.

This prefix `plugin.[domain].[namespace].[type]` is used to maintained configuration for this `Interceptor`, and in this `Interceptor` developer can get its configuration by the `getConfig()` method of the `Context` param.

The `AgentPlugin` interface also includes the `Order` interface that defines the order of the plugins, which is related to plugin orchestration. Plugin orchestration will be described in [Plugin Orchestration](#plugin-orchestration) section.


### A Simple Plugin Example
Only three interfaces need to be implemented to complete the development of a simple plugin. Although plugin development varies in complexity depending on the business, for illustrating the plugin mechanism, the simplest Simple plugin with only three interface implementations is the most appropriate.

Suppose this plugin is to help complete the Tracing function, the name of the current microservice needs to be added to the Response header. So we can define the domain of the plugin as "observability", and then we will give the simple plugin a namespace "simple". The full source code is available [here](https://github.com/megaease/easeagent-test-demo/tree/master/simple-plugin).

![image](./images/simple-plugin.jpg)

This plugin contain only three interface implementations: `AgentPlugin`, `Points` and `Interceptor`, corresponding to the classes `SimplePlugin`, `DoFilterPoints` and `ResponseHeaderInterceptor` respectively.

```
# Full code structure
▾ src/main/java/com/megaease/easeagent/plugin/simple/
  ▾ points/
      DoFilterPoints.java
  ▾ interceptor/
      ResponseHeaderInterceptor.java
    SimplePlugin.java
  pom.xml
```

#### Points of Simple Plugin
To decouple from ByteBuddy, `ClassMatcher` and `MethodMatcher` are wrapped with reference to the DSL of **ByteBuddy**.

```java
public class DoFilterPoints implements Points {
    @Override
    public IClassMatcher getClassMatcher() {
        return ClassMatcher.builder()
            .hasInterface("javax.servlet.Filter")
            .or()
            .hasSuperClass("javax.servlet.http.HttpServlet")
            .build();
    }

    @Override
    public Set<IMethodMatcher> getMethodMatcher() {
        return MethodMatcher.builder().named("doFilter")
                .isPublic()
                .argsLength(3)
                .arg(0, "javax.servlet.ServletRequest")
                .arg(1, "javax.servlet.ServletResponse")
                .returnType("void")
                .or()
                .named("service")
                .arg(0, "javax.servlet.ServletRequest")
                .arg(1, "javax.servlet.ServletResponse")
                .build().toSet();
    }
}
```

#### Interceptor of Simple Plugin
This `ResponseHeaderInterceptor` is bound to the enhancement point defined above via the `@AdviceTo` annotation, and does not need to be explicitly assigned a qualifier value when qualifier is the default value.
```java
@AdviceTo(value = DoFilterPoints.class, plugin = SimplePlugin.class)
public class ResponseHeaderInterceptor implements Interceptor {
    @Override
    public void before(MethodInfo methodInfo, Context context) {
        HttpServletResponse httpServletResponse = (HttpServletResponse) methodInfo.getArgs()[1];
        String serviceName = EaseAgent.getConfig(ConfigConst.SERVICE_NAME);
        httpServletResponse.setHeader("easeagent-srv-name", serviceName);
    }
    ......
}
```

#### AgentPlugin
```java
public class SimplePlugin implements AgentPlugin {
    @Override
    public String getNamespace() {
        return "simple";
    }

    // ConfigConst.OBSERVABILITY;
    @Override
    public String getDomain() {
        return "observability";
    }
}
```

#### Test
1. Compile
As mention above, the source code is available [here](https://github.com/megaease/easeagent-test-demo/tree/master/simple-plugin).

```
$ git clone git@github.com:megaease/easeagent-test-demo.git
$ cd easeagent-test-demo/simple-plugin
$ mvn clean package
$
```

2. Install Plugin
This simple plugin is compiled independently of easeagent, so the compiled output plugin jar package `simple-plugin-1.0.0.jar` need to be copied to the **plugins** directory which is at the same level directory as easeagent.jar (create if not existing), to allow easeagent to detect it.
```
$ export EASE_AGENT_PATH=[Replace with agent path]
$ mkdir $EASE_AGENT_PATH/plugins
$ cp target/simple-plugin-1.0.0.jar $EASE_AGENT_PATH/plugins

```

3. Run
Taking the `spring-web` module under [ease-test-demo](https://github.com/megaease/easeagent-test-demo) as test demo project, run the demo application with the EaseAgent.
```
$ export EASE_AGENT_PATH=[Replace with agent path]
$ cd ../
$ mvn clean package -Dmaven.test.skip
$ java -javaagent:${EASE_AGENT_PATH}/easeagent-dep.jar -Deaseagent.config.path=agent.properties -jar spring-gateway/employee/target/employee-0.0.1.jar
```
4. Test
Execute the following test and the header information added can be seen in the HTTP Response.
```
easeagent-srv-name: demo-springweb
```

```
# 
# curl -v http://127.0.0.1:18081/employee/message
*   Trying 127.0.0.1:18081...
* Connected to 127.0.0.1 (127.0.0.1) port 18081 (#0)
> GET /employee/message HTTP/1.1
> Host: 127.0.0.1:18081
> User-Agent: curl/7.77.0
> Accept: */*
>
* Mark bundle as not supporting multiuse
< HTTP/1.1 200
< easeagent-srv-name: demo-springweb
< easeagent-duration: 55
< Content-Type: text/plain;charset=UTF-8
< Content-Length: 34
< Date: Thu, 17 Mar 2022 03:46:40 GMT
<
* Connection #0 to host 127.0.0.1 left intact
Gateway Called in employee Service%

```

In addition to the `easeagent-srv-name` response header, we also see another response header `easeagent-duration` indicating the response time of the request, which is achieved by adding another enhancement points `ResponseProcessPoints` and `ResponseDurationInterceptor`. if interested, please visit source code for details.
```
https://github.com/megaease/easeagent-test-demo/tree/master/simple-plugin
```

When the plugin is integrated into the `plugins` subdirectory in the easeagent project source tree, it will be compiled into the easeagent-dep.jar package.

In this simple plugin project, the `com.megaease.easeagent:plugin-api` dependency is wrapped in local maven repository which local in `simple-plugin/lib` directory, user can also download Easeagent source tree then install `plugin-api` module.

```
$ git clone https://github.com/megaease/easeagent.git
$ cd easeagent/plugin-api
$ mvn clean install

```
## EaseAgent Tracing Plugin of Reality Sample
We have described the EaseAgent architecture, plugin concepts and design details above, and now we are familiar with three core elements of a plugin through the minimalist Simple plugin example. 

However, in order to develop a practical business plugin, it is necessary to have an understanding of the business API. Next, we outline how to use the Tracing API to complete a Tracing plugin development in combination with the actual Tracing plugin HttpServletPlugin.

### Span and Trace
![image](./images/trace-and-span.png)
Note: Image From Jaeger., Retrieved March 08, 2022, from Architecture., https://www.jaegertracing.io/docs/1.31/architecture

Before we look at the concrete implementation, let's briefly introduce the core concepts in Tracing, Trace and Span.

As shown above, a Trace represents a complete transaction, containing multiple Spans, A-E; a Span represents an independent service sub-unit of a complete transaction, such as a database request, a method call or an external request; Trace is a logical concept only, and is represented by the traceId in the Span, and there are multiple Spans with the same traceId forming a directed acyclic graph.

A more concrete illustration of the Span concept and interface comes from a concrete sample of data in OpenZipkin format.
![image](./images/zipkin-span.jpg)
In the above data, `id` is the unique id of the current `Span`; `traceId` represents the unique id of the `trace` of the current transaction request, often directly using the id of the first Span, like Span-A's id in the above figure; `parentId` represents the id of the parent Span, like B's `parentId` in the above figure is the `id` of Span-A; for more specific details can be found in Openzipkin's documentation.

It is clear that the key point in the development of the Tracing plugin is the generation and reporting of Span. So what interface does EaseAgent use to provide the generation and reporting of Spans?


### Context Overview
Among the `three elements` of a plugin, the `Interceptor` interface implementation is the core of a plugin development, where the `before/after` methods carry the `Context` parameter, and it is through the `Context` interface that developers can make Tracing API calls to complete the collection and reporting of Span.

![image](./images/Context-span-api.png)

As shown above, the `nextSpan()` interface is used to create a Span, while the `servReceive()` and `clientRequest()` interfaces are wrappers of the `nextSpan()` interface, corresponding to the creation of a Span for a request received by the server and the creation of a Span for an external request, and initialize the fields within the Span.

The next step is to look at the specific HttpServlet Tracing plugin implementation.
### HttpServletPlugin
We skip the corresponding implementations of the plugin `Points` and `AgentPlugin` and look directly at Tracing's `Interceptor` implementation `DoFilterTracingInterceptor`.
```java
@Override
public void doBefore(MethodInfo methodInfo, Context context) {
  HttpServletRequest httpServletRequest = methodInfo.getArgs()[0];
  ...
  HttpRequest httpRequest = new HttpServerRequest(httpServletRequest);
  requestContext = context.serverReceive(httpRequest);

  httpServletRequest.setAttribute(PROGRESS_CONTEXT, requestContext);
  HttpUtils.handleReceive(requestContext.span(), httpRequest);
}
```

As you can see from the above code snippet, the `context.serverReceive()` method is called in `before` to create and initialize the Span, and the `HttpUtils::handleReceive()` method is used to make Http request-related injections to the fields in the Span.

```java
@Override
public void doAfter(MethodInfo methodInfo, Context context) {
  ...
  try {
    Span span = requestContext.span();
    if (!httpServletRequest.isAsyncStarted()) {
      ...
      HttpUtils.finish(span, response);
    } else if (methodInfo.getThrowable() != null) {
      span.error(methodInfo.getThrowable());
      span.finish();
    } else {
      // async
      ...
    }
  } finally {
    requestContext.scope().close();
  }
}
```

In the `after` method, the `HttpUtils::finish()` or `span.finish()` method is called to complete the Span collection and reporting. This is an overview of the Trace API interface calls in the actual Tracing plugin, for more specific details you can check the source code of this file in EaseAgent:
```
https://github.com/megaease/easeagent/blob/master/plugins/httpservlet/src/main/java/com/megaease/easeagent/plugin/httpservlet/interceptor/DoFilterTraceInterceptor.java
```

The [Context](#context) section provides a comprehensive description of the `Context`.

##  Context
* [Context](context.md)

##  Tracing API
* [Tracing API](tracing-api.md)

##  Metric API
* [Metric API](metric-api.md)

##  Logging API
If you need to print logs in the plugin to the EaseAgent log output, you can use the Slf4j interface directly.

## Configuration API

Regarding configuration, we have a set of rules to follow. For detailed rules, please see: [Plugin Configuration](#plugin-configuration)

When you want to get your own configuration file in the plugin, you only need to get it from the Context.
The framework itself will automatically maintain configuration's changes and modifications.

```java
class InterceptorImpl  implements Interceptor {
    @Override
    public void before(MethodInfo methodInfo, Context context) {
        Config config = context.getConfig();
        // You don’t need to verify enabled here, because enabled is a reserved attribute. If it is false, the Interceptor will not be run.
        // boolean enabled = config.enabled();
        Integer outputSize = config.getInt("output.size"); //it will be get config key: plugin.[domain].[namespace].[function].output.size = [value]
    }

    @Override
    public void after(MethodInfo methodInfo, Context context) {
        Config config = context.getConfig();
    }
}
```

When you want to get the configuration outside of the plugin, we provide tools to get it.

This tool will automatically maintain configuration updates and modifications, and the configuration obtained each time will be the latest configuration.

It is a singleton registration factory, which also means that the singleton acquisition is locked, so it is hoped that the user can acquire it as little as possible.

for example: acquire it once during initialization, and then put it in a static variable.

The registered key is `domain`, `namespace`, `id`.

```java
import com.megaease.easeagent.plugin.api.config.AutoRefreshPluginConfigRegistry;
import com.megaease.easeagent.plugin.api.config.AutoRefreshPluginConfigImpl;
class Demo{
  AutoRefreshConfigImpl config = AutoRefreshRegistry.getOrCreate("observability", "httpclient", "metric");
}
```

### Customize

When you need to customize Config, implement the [com.megaease.easeagent.plugin.api.config.AutoRefreshPluginConfig](../plugin-api/src/main/java/com/megaease/easeagent/plugin/api/config/AutoRefreshConfig.java) interface, and then register

The registered key is `domain`, `namespace`, `id` and the `type` of Class.

```java
public class ServiceNameConfig implements AutoRefreshConfig {
    private volatile String propagateHead = "X-Mesh-RPC-Service";

    public String getPropagateHead() {
        return propagateHead;
    }

    @Override
    public void onChange(Config oldConfig, Config newConfig) {
        String propagateHead = newConfig.getString("propagate.head");
        if (StringUtils.isEmpty(propagateHead) || StringUtils.isEmpty(propagateHead.trim())) {
            return;
        }
        this.propagateHead = propagateHead.trim();
    }
}

public class ServiceNameInterceptor implements Interceptor {
    protected static ServiceNameConfig config = null;

    @Override
    public void init(Config pConfig, String className, String methodName, String methodDescriptor) {
        config = AutoRefreshRegistry.getOrCreate(pConfig.domain(), pConfig.namespace(), pConfig.id(),
            new AutoRefreshConfigSupplier<ServiceNameConfig>() {
                @Override
                public ServiceNameConfig newInstance() {
                    return new ServiceNameConfig();
                }
            });
    }
}
```

## Plugin Unit Test
* [Plugin Unit Test](plugin-unit-test.md)

## EaseAgent Plugin Debug FAQ
The above basically covers all aspects of plugin development, the following are common debugging issues during plugin development, the following plugin debugging FAQ can also be found in the section starting at 1:05:57 in the [video](https://www.youtube.com/watch?v=u6Aoa2roGuA).

### Development Environment Configuration
Debugging environment configuration, briefly described below:
- Download the EaseAgent source code to the local host and add it to the workspace of the IDE, build and the output located in the source code directory build/target/easeagent-dep.jar, and then create the directory `build/target/plugins`；
- Add the plugin project (e.g. simple-plugin) to the same workspace and copy the compiled and packaged JAR file to the `build/target/plugins` directory created in the previous step so that the plugin can be loaded.
- Add the source code of the application (e.g. spring-gateway/employee) to the workspace and configure the JVM options in the Debug menu to start the application with easeagent-dep.jar for debugging later.
  eg.:
  ```
  -javaagent:/path-to-easeagent/build/target/easeagent-dep.jar -Deaseagent.config.path=/my-own-if-changed-or-add/agent.properties -Deaseagent.log.conf=/my-own-if-changed/easeagent-log4j2.xml -Dnet.bytebuddy.dump=/path-to-dump/
  ```
  The `path` above need to be replaced with the user's actual environment path.
- Set breakpoints, launch debug session.
### Enhancement Debug
- How can I determine whether target classes and methods are enhanced?
  The following debug options were set in step 3 of the previous section of environment configuration:
  ```
  -Dnet.bytebuddy.dump=/path-to-dump/
  ```
  The class files of all the enhanced classes will be printed to this directory. Decompile the class files (IDEA can pull them in and open them directly) to see if the corresponding method has the enhanced bytecode to call the EaseAgent method.

- If the check confirms that the target method is not enhanced, how do I debug it?
  There are three key checkpoints: ClassMatchers, MethodMatchers and all other issues.
  1. All classes that are matched will run into the ForAdviceTransformer::transform(...) method, where conditional breakpoints can be added, then checking ClassMatchers if the breakpoint is not interrupted..
  2. All methods matched will run into the AdviceRegistry::check(...) method, where conditional breakpoints can be added, then checking MethodMatchers if the breakpoint is not interrupted..
  3. Set a breakpoint by going back through the breakpoint stack in step 1 and find the the ByteBuddy source code where throw exception when enhance fail, check the cause of the exception.

  All enhancement failures can be resolved by analyzing at the three breakpoints above.
### Interceptor Debug
- Why the Interceptor is not called when the class method has definitely been enhanced?
  All enhanced methods run into the following two methods.
  ```java
  com.megaease.easeagent.core.plugin.Dispatcher::enter
  com.megaease.easeagent.core.plugin.Dispatcher::exit
  ```
  Breakpoints can be set at the entrances and exits to further trace the execution logic.
  Commonly, and most likely, the plugin's corresponding configuration has enabled=false or is not configured.
  ```
  plugin.[domain].[namespace].[type].enabled=true
  ```
### Performance Verification
To verify the impact of EageAgent on performance/latency, the Profiler tool can be used to determine the performance/latency impact by sampling the CPU and focusing on the percentage of `Megaease` related function executions within the stack.


There are profiler tools such as Async-profiler, Arthas and VisualVm.

Finally, have fun using and extending EaseAgent, and feel free to raise Issues or join the Slack community to discuss.

* [Github Issues](https://github.com/megaease/easeagent/issues)
* [Join Slack Workspace](https://join.slack.com/t/openmegaease/shared_invite/zt-upo7v306-lYPHvVwKnvwlqR0Zl2vveA) for function requirement, issues, and discussion.
* [MegaEase on Twitter](https://twitter.com/megaease)

# References
1. Data model. Data Model · OpenZipkin. (n.d.). Retrieved March 08, 2022, from https://zipkin.io/pages/data_model.html 
2. Architecture. Jaeger. (n.d.). Retrieved March 08, 2022, from https://www.jaegertracing.io/docs/1.31/architecture 
3. Oaks, S. (2020). Java performance 2nd Edition. O'Reilly.

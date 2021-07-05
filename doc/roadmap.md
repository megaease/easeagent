# EaseAgent Roadmap

## Features
* Focus on observability for Java ecosystem with a service perspective.
* Support mainstream middlewares, libraries, and components in Java-based microservices.
  
## Trace and Metric
EaseAgent uses open source components to implement data collection:
* The trace data format which EaseAgent is compatible with is [Zipkin](https://zipkin.io/pages/data_model.html).
* [Metrics](https://github.com/dropwizard/metrics) is used to measure the behavior of critical components.

## Roadmap 2021
Name | Issue | Description 
--- |--- |---
Support Zipkin | | Send trace data to Zipkin server
Support Graphite | | Send metric data to Graphite
Support Apache HttpClient | | Collecting trace and metric data (Synchronous and Asynchronous)
Support OkHttp | | Collecting trace and metric data
Custom Internal Logging | [Issue73](https://github.com/megaease/easeagent/issues/73) | Use Custom Log to replace Log4j for internal logging

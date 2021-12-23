# EaseAgent Roadmap

## Features
* Focus on observability for Java ecosystem with a service perspective.
* Support mainstream middlewares, libraries, and components in Java-based microservices.
  
## Trace and Metric
EaseAgent uses open source components to implement data collection:
* The trace data format which EaseAgent is compatible with is [Zipkin](https://zipkin.io/pages/data_model.html).
* [Metrics](https://github.com/dropwizard/metrics) is used to measure the behavior of critical components.

## Roadmap
Name | Issue | Description 
--- |--- |---
Join CNCF Landscape            | | [CNCF Landscape](https://landscape.cncf.io/card-mode?category=observability-and-analysis&grouping=category)
Join OpenAPM Landscape            | | [OpenAPM landscape](https://openapm.io/landscape)
Plugin UnitTest Framework | | Unit Test
Integrate Test Framework | | CI/CD can verify the EaseAgent for each pull request
Support loading and unloading plugin dynamiclly| | The application does not need to restart to update plugins.

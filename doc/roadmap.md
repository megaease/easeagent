# EaseAgent Roadmap

## Principles

- **Cloud Native**. Focus on Cloud Native microservice, containers, observability ecosystem perspective.
- **Good Extensibility**.  Easy to develop a new plugin for mainstream middleware, libraries, and components.
- **Be Standard**. Must follow the standard technology, protocol, best practice, 
- **Third-Part Integration**. Easy to integrate with other mainstream open-source software, such as Kafka, Elasticsearch, Prometheus, Zipkin..., etc.
  
## Data Collection
EaseAgent uses open source components to do data collection:
- **Tracing**. The trace data format which EaseAgent is compatible with is [Zipkin](https://zipkin.io/pages/data_model.html).
- **Metrics**. [Dropwizard Metrics](https://github.com/dropwizard/metrics) is used to measure the behavior of critical components.

## Roadmap
Tasks    | Issues | Description 
-------- |------- |-----------
Join CNCF Landscape        | | [CNCF Landscape](https://landscape.cncf.io/card-mode?category=observability-and-analysis&grouping=category)
Join OpenAPM Landscape     | | [OpenAPM landscape](https://openapm.io/landscape)
Plugin UnitTest Framework  | |  Unit Test
Integrate Test Framework   | |  CI/CD can verify the EaseAgent for each pull request
Performance Test Report    | |  The CPU/Memory/Netowrk perforamnce report.
Plugin Dynamiclly Loading  | |  The application does not need to restart to update plugins.
Dynamic Field Isolating    | |  When multiple plugins add dynamic field to the same class, they are all isolated.

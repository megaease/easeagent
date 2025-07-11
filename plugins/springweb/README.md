# spring-boot http client plugin 

## Points

* resTemplate points: `org.springframework.http.client.ClientHttpRequest:execute`
  * points code version spring-boot:2.x.x config and default
    ```properties
    runtime.code.version.points.spring-boot=2.x.x
    ```
    when not config `runtime.code.version.points.spring-boot` it is load
* feignClient points: `feign.Client:execute`
* webclient points: `org.springframework.web.reactive.function.client.WebClient$Builder:build`


## config

### tracing config
```properties
plugin.observability.webclient.tracing.enabled=true
plugin.observability.resTemplate.tracing.enabled=true
plugin.observability.feignClient.tracing.enabled=true
```

### support forwarded
```properties
plugin.integrability.forwarded.forwarded.enabled=true
```




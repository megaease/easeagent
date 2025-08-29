# jdk http url connection 

## Points

`java.net.HttpURLConnection:getResponseCode`

### points code version jdk:jdk8 config and default

```properties
runtime.code.version.points.jdk=jdk8
```
when not config `runtime.code.version.points.jdk` it is load

## config

### tracing config
```properties
plugin.observability.httpURLConnection.tracing.enabled=true
```

### support forwarded
```properties
plugin.integrability.forwarded.forwarded.enabled=true
```




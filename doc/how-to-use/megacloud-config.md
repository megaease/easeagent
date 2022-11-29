### First: Download and install the agent
[see our doc](https://github.com/megaease/easeagent#get-and-set-environment-variable)

### Second: Configuration
Modify the agent.properties file to configure your information.

##### 1. name

You'll name to find your data later. It's important to use a unique and meaningful name.

The service name of megacloud consists of three parts: zone, domain, name. They are joined by `.` into `ServiceName`

```properties
name=zone.domain.service
```

##### 2. MTLS

MTLS is a secure authentication protocol for EaseAgent to connect to MegaCloud.

Config: Get TLS
```properties
reporter.outputServer.tls.enable=true
reporter.outputServer.tls.key=YOUR_TLS_KEY
reporter.outputServer.tls.cert=YOUR_TLS_CERT
```

##### 3. reporter

MegaCloud uses http to receive data, so you need to change the configuration to http and MegaCloud's address.
```properties
reporter.outputServer.bootstrapServer={MEGA_CLOUD_URL}
reporter.outputServer.appendType=http
reporter.tracing.sender.appendType=http
reporter.metric.sender.appendType=http
plugin.observability.global.metric.url=/platform-metrics
plugin.observability.global.log.url=/application-log
plugin.observability.access.log.url=/application-access-log
reporter.tracing.sender.url=/application-tracing-log
reporter.tracing.encoder=SpanJsonEncoder
```

##### 4. other
Other configurations are EaseAgent related configurations such as Tracing, Metric, etc. For details, please refer to [github doc](https://github.com/megaease/easeagent/blob/master/doc/user-manual.md#configuration)

### Third: About `MEGA_CLOUD_URL` And `TLS`

When you download the `easeagent.jar` file through our megacloud, `MEGA_CLOUD_URL` and `TLS` will be filled in for you automatically.

If you need it separately, please download the `easeagent.jar` and get it by yourself.

You can use the command to get config:
```bash
jar xf easeagent.jar agent.properties 
```

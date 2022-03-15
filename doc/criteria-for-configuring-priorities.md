# Criteria For Configuring Priorities

In EaseAgent, there are the configuration of global variables and the configuration of specified items.

When a configuration item has more than two different configurations, we need a simple priority standard to manage it.

`This standard should be implemented in EaseAgent, whether it is user-configured or code-configured.`

Example:

```yaml
plugin.observability.global.metric.topic=application-meter
plugin.observability.access.metric.topic=application-log
```

When getting the topic sent by the access metric, what value is obtained?

## Configuration Standard

### Value is a boolean standard

Get the logical AND operation of two values. If there is only one configuration, get its value directly.

Example:

```yaml
plugin.observability.global.metric.enabled=true
plugin.observability.access.metric.enabled=false
plugin.observability.global.tracing.enabled=true
```

* get access metric enabled -> (true && false) -> false;
* get httpServlet tracing enabled -> global.tracing.enabled = true

### Value isn't a boolean standard

Prefer configurations with small coverage.

Example:

```yaml
plugin.observability.global.metric.topic=application-meter
plugin.observability.httpServlet.metric.topic=application-log
```

* get httpServlet metric topic ->  application-log


## FAQ

Why boolean config uses logical AND ?

>  When value is boolean, its meaning is a switch. We want it to be like a light switch: there's a master switch, and then there are switches that control individual bulbs

Why we should prioritize configurations with less coverage?

> When we configure a wide configuration, we mean configure a default value.

> When we configure a small scope configuration, it means that we need a special value that is different from other configurations.

> For this reason, we have chosen this criterion.

Which configuration is global and which configuration is specific?

> The definition of global is distinguished by the location of the configuration in the architecture, which needs to be discussed and confirmed by the MegaEase team.

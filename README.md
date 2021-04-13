
# Installation


## Download 

```
curl -Lk https://github.com/megaease/release/releases/download/easeagent/easeagent.jar -O
```

> **How to get the version of easeagent?**
> 
> `unzip -p easeagent.jar git.properties`

## Extract default configuration files

```
jar xf easeagent.jar application.conf log4j2.xml
```

## Simple Use

### Setup JAVA_OPTS, then run your java program with `$JAVA_OPTS`

```
export JAVA_OPTS="${JAVA_OPTS} -javaagent:/{path}/easeagent.jar"
```

## Custom Configuration

```
export JAVA_OPTS="${JAVA_OPTS} -javaagent:/{path}/easeagent.jar=/{path}/agent.properties" -Deaseagent.log.conf=/{path}/log4j2.xml -Deaseagent.server.port=9901
```

> Use `CATALINA_OPTS` instead when tomcat was used.

# Build From Source

```
mvn clean package -am -pl build
```

A generated `./build/target/easeagent-dep.jar` is the java agent jar with all the dependencies.


# Configuration
Agent.properties contains all configurations 

# Licensing

EaseAgent is licensed under the Apache License, Version 2.0. See [LICENSE](./LICENSE) for the full license text.
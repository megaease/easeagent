
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
jar xf easeagent.jar agent.properties log4j2.xml
```

## Simple Use

```
java -jar -javaagent:/{path}/easeagent.jar your-springboot-app.jar
```

## Custom Configuration

```
java -jar -javaagent:/{path}/easeagent.jar=/{path}/agent.properties" -Deaseagent.log.conf=/{path}/log4j2.xml -Deaseagent.server.port=9901 your-springboot-app.jar
```
## Other Configuration Detail
Detailed configuration information is in build/src/main/resources/agent.properties

# Build From Source

```
mvn clean package -am -pl build
```

A generated `./build/target/easeagent-dep.jar` is the java agent jar with all the dependencies.


# Configuration
Agent.properties contains all configurations

# Supported Modules
* JDK 8
* JDBC4, Modules implements JDBC Interface
* Spring Boot 2.2.x
* Spring WebClient, Spring RestTemplate, Spring FeignClient
* RabbitMQ Client 5.x, Kafka Client 2.4.x
* Jedis 3.5.x, Lettuce 5.3.x
* Log4j, Log4j2, Logback

# Licensing

EaseAgent is licensed under the Apache License, Version 2.0. See [LICENSE](./LICENSE) for the full license text.
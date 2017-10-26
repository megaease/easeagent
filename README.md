
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

## Setup JAVA_OPTS, then run your java program with `$JAVA_OPTS`

```
export JAVA_OPTS="${JAVA_OPTS} -javaagent:/path/to/easeagent.jar=/path/to/application.conf -Deaseagent.log.conf=/path/to/log4j2.xml"
```

> Use `CATALINA_OPTS` instead when tomcat was used.

# Build from source

```
mvn clean package -am -pl build
```

A generated `./build/target/easeagent-dep.jar` is the java agent jar with all the dependencies.



# Usage

1. Download easeagent-xxx.jar from [here](https://github.com/megaease/release/releases/tag/easeagent)
1. Setup `export JAVA_OPTS="$JAVA_OPTS -javaagent:easeagent-xxx.jar"`, then run your java program with `$JAVA_OPTS`

## Use a customized `application.conf`

```
export JAVA_OPTS="$JAVA_OPTS -javaagent:easeagent-xxx.jar=/path/to/application.conf" 
```


## Use a customized log configuration

```
export JAVA_OPTS="$JAVA_OPTS -Deaseagent.log.conf=/path/to/log4j.xml -javaagent:easeagent-xxx.jar"
```


# Build from source

```
mvn clean package -am -pl build
```

A generated `./build/target/easeagent-dep.jar` is the java agent jar with all the dependencies.

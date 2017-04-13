
# Usage


```
source /dev/stdin <<< "$(curl -SL https://git.io/vD7Bp)"
java $JAVA_OPTS <rest of your command line>
```

## Use an `application.conf`

```
export JAVA_OPTS="$JAVA_OPTS -javaagent:easeagent-sm.jar=/path/to/application.conf" 
```


## Use a log configuration file

```
export JAVA_OPTS="$JAVA_OPTS -Deaseagent.log.conf=/path/to/log4j2.xml -javaagent:easeagent-sm.jar"
```


# Build from source

```
mvn clean package -am -pl build/all
```

A generated `./build/all/target/easeagent-sm.jar` is the java agent jar with all the dependencies.

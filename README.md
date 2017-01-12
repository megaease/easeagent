# Build from source

```
mvn clean package -pl build -P sm -am
```

A generated `./build/target/easeagent-sm.jar` is the java agent jar with all the dependencies.

# Usage

```
java -javaagent:easeagent-sm.jar <rest of your command line>
```

## Use an `application.conf`

```
java -javaagent:easeagent-sm.jar=/path/to/application.conf <rest of your command line>
```

> [applicaiton.conf](build/src/main/resources/application.conf)

## Use a log configuration file

```
java -Deaseagent.log.conf=/path/to/file -javaagent:easeagent-sm.jar <rest of your command line>
```

> [log configuration](build/src/main/resources/log4j2.xml)
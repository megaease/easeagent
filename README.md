# Features

* [ ] Profile method calling tree;
* [ ] Output [metrics](http://metrics.dropwizard.io/) to the files;

# Build from source

```
mvn clean package
```

A generated `./build/target/easeagent.jar` is the java agent jar with all the dependencies.

# Usage

```
java -javaagent:easeagent.jar ...
```

# To be continued
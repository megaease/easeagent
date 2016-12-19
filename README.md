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

# How to contribute

## Get started with a new branch

No matter what you want contribute to this project, you should clone this project and create a new branch do whatever you want.

After finished your works, do `git push origin <your branch>`, and new a pull request to someone you want to ask for code review.

## Add new [Transformation][t]

Normally, there is no need to implement [Transformation][t] directly, but extend [AbstractTransformation][at] instead.
A good example you can find is [StackTrace][st].

## [ServiceLoader][sl]

Every transformation would be found in runtime by [ServiceLoader][sl], just like it be done in stagemonitor.
So, your transformation supposed to declare in `META-INF/services/com.hexdecteam.easeagent.Transformation`.

## Configuration

It is so easy to configure your transformation by annotation with `@Configurable`, as blow:

```java
@Configurable(prefix = "jdbc")
class JDBCTransformation extends AbstractTransformation {
    String configItem;

    ...
}
```

And then you just append the configuration to the YAML file named [easeagent.yml][yml]:

```yml
jdbc:
  configItem: "this is awsome!"
```

When your transformation loaded in runtime, the value in the YAML file would be bound to `JDBCTransformation#configItem` automatically.

More about supported configuration value types you can find in [ConfigurationTest][ct].

## Dependency

> **Important Rule:** New a dependency is your last option!

A new transformation may introduce some external dependencies, like a json lib.

For avoiding the conflict of classes in the host JVM process, you should always remember to add a `relocation` to `maven-shade-plugin`
in [build/pom.xml][rl], eg:

```xml
<relocation>
    <pattern>org.slf4j</pattern>
    <shadedPattern>com.hexdecteam.jar.slf4j</shadedPattern>
</relocation>
```


The shade plugin would change the package named `org.slf4j` to `com.hexdecteam.jar.slf4j` automatically in compile phase.

> **Important Rule:** the `shadedPattern` must be like `com.hexdecteam.jar.<xxx>`,
> then the dependencies classes would not be transformed during transformation.
> It could avoid problem of `StackOverflowError`.
> Your can find the magic that it work in [AbstractTransformation#withDescription][at].

## Add new module

Sometime you want to add new transformation, which could probe SQL execution for example,
but there is no module existed to place. This is the time to add new module named **jdbc**,
just like the **trace** module.


# To be continued

[t]: https://github.com/hexdecteam/easeagent/blob/master/core/src/main/java/com/hexdecteam/easeagent/Transformation.java
[at]: https://github.com/hexdecteam/easeagent/blob/master/core/src/main/java/com/hexdecteam/easeagent/AbstractTransformation.java
[st]: https://github.com/hexdecteam/easeagent/blob/master/trace/src/main/java/com/hexdecteam/easeagent/StackTrace.java
[ct]: https://github.com/hexdecteam/easeagent/blob/master/agent/src/test/java/com/hexdecteam/easeagent/ConfigurationTest.java
[rl]: https://github.com/hexdecteam/easeagent/blob/master/build/pom.xml
[yml]: https://github.com/hexdecteam/easeagent/blob/master/build/src/main/resources/easeagent.yml
[sl]: http://docs.oracle.com/javase/6/docs/api/java/util/ServiceLoader.html
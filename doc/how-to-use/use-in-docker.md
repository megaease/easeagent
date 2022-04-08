# Get the agent

Download the agent, [see our doc](https://github.com/megaease/easeagent#get-and-set-environment-variable)

# Modify startup scripts

The startup parameters of the Java application server must include the built-in argument -javaagent.

It is recommended that you set this argument with the JAVA_OPTS environment variable.

The value of that argument must contain the location where you ADD the easeagent.jar file to the image.

For example, with Tomcat, use commands like these in the Dockerfile:

```dockerfile
RUN mkdir -p /usr/local/tomcat/easeagent
ADD ./easeagent/easeagent.jar /usr/local/tomcat/easeagent/easeagent.jar
ENV JAVA_OPTS="$JAVA_OPTS -javaagent:/usr/local/tomcat/easeagent/easeagent.jar"
```

# Restart your application

Deploy your application to start using the EaseAgent to send data to MegaCloud.

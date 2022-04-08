# Install the agent

Download the agent, [see our doc](https://github.com/megaease/easeagent#get-and-set-environment-variable)

Once you’ve get the EaseAgent, these steps to start the Java agent installation.

1. Create a directory for your EaseAgent, such as /opt/easeagent. On Windows, the EaseAgent must be in a subdirectory of your application server’s directory, such as C:\Tomcat 1.0\easeagent.

2. Copy the easeagent.jar file into your new directory. 


# Specific instructions for your Java setup

To use the EaseAgent with your JVM, you’ll need to pass the -javaagent argument. In the commands below, replace EASE_AGENT_PATH with the path of the EaseAgent directory you created, eg. /opt/easeagent.

## Spring Boot
Pass the -javaagent argument to the command line where you start your app. Make sure to add it before the -jar argument:
```
java -javaagent:EASE_AGENT_PATH/easeagent.jar -jar app.jar
```

## Tomcat

Pass the -javaagent argument on catalina.sh, catalina.bat or the GUI.

1. Linux with catalina.sh

    In tomcat catalina.sh file, use the JAVA_OPTS environment variable:
    ```
    export CATALINA_OPTS="$CATALINA_OPTS -javaagent:EASE_AGENT_PATH/easeagent.jar"
    ```

2. Windows with catalina.bat

    In tomcat catalina.bat file, set the JAVA_OPTS variable near the top of the file:
    ```
    set "CATALINA_OPTS=%CATALINA_OPTS% -javaagent:EASE_AGENT_PATH\easeagent.jar"
    ```

3. Windows with GUI

    On Windows with GUI, add the full EaseAgent path to your Java options: In Apache Tomcat, click Configure Tomcat, and then click Java.
    
    In the Java Options text box, enter the following argument:
    
    ```
    -javaagent:EASE_AGENT_PATH\easeagent.jar
    ```

## Jetty

Pass the -javaagent in jetty.sh or the start.ini file:

1. jetty.sh
    ```
    export JAVA_OPTIONS="${JAVA_OPTIONS} -javaagent:EASE_AGENT_PATH/easeagent.jar"
    ```
2. start.ini
    ```
    -javaagent:EASE_AGENT_PATH/easeagent.jar
    ```


## Wildfly

If you’re using Wildfly 11+, to pass the -javaagent argument on Linux or Windows.

Add the Java agent and make it visible to the JBoss modules by adding the following lines to your file.

1. Linux with standalone.conf:

    ```
    JAVA_OPTS="$JAVA_OPTS -javaagent:EASE_AGENT_PATH/easeagent.jar"
    JAVA_OPTS="$JAVA_OPTS -Djboss.modules.system.pkgs=$JBOSS_MODULES_SYSTEM_PKGS,com.easeagent"
    ```

2. Windows with standalone.bat.conf:

    ```
    set "JAVA_OPTS=%JAVA_OPTS% -javaagent:EASE_AGENT_PATH\easeagent.jar"
    set "JAVA_OPTS=%JAVA_OPTS% -Djboss.modules.system.pkgs=%JBOSS_MODULES_SYSTEM_PKGS%,com.easeagent"
    ```

# Restart your application

Deploy your application to start using the EaseAgent to send data to MegaCloud.

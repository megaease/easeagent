package com.hexdecteam.easeagent;

import java.lang.instrument.Instrumentation;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static java.util.ServiceLoader.load;

public class Main {

    private static final String LOGBACK_CONFIGURATION_FILE = "logback.configurationFile";
    private static final String JAVAGENT_LOGGING_FILE      = "easeagent.logging.file";

    public static void premain(String args, Instrumentation inst) {
        final Configuration c = Configuration.load(args);
        loggingContext(() -> StreamSupport.stream(load(Transformation.class).spliterator(), false)
                                          .map(c::configure)
                                          .filter(Optional::isPresent)
                                          .map(Optional::get)
                                          .forEach(t -> t.apply(inst)))
        ;
    }

    private static void loggingContext(Runnable runnable) {
        final String internal = System.getProperty(JAVAGENT_LOGGING_FILE, "_debug.xml");
        final String origin = System.getProperty(LOGBACK_CONFIGURATION_FILE);

        // Redirect config origin to internal
        System.setProperty(LOGBACK_CONFIGURATION_FILE, internal);
        runnable.run();
        // Recovery origin configuration
        if (origin != null) System.setProperty(LOGBACK_CONFIGURATION_FILE, origin);
    }

}

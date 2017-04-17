package com.megaease.easeagent.gen;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GenerateStarterTest {
    @Test
    public void should_generate() throws Exception {
        final StringBuilder out = new StringBuilder();
        new GenerateStarter("a.b", "Provider.class", "Transformation.class").apply().writeTo(out);
        assertThat(out.toString(), is("package a.b;\n" +
                                              "\n" +
                                              "import com.megaease.easeagent.core.Bootstrap;\n" +
                                              "import com.megaease.easeagent.core.Transformation;\n" +
                                              "import java.lang.Exception;\n" +
                                              "import java.lang.Iterable;\n" +
                                              "import java.lang.String;\n" +
                                              "import java.lang.SuppressWarnings;\n" +
                                              "import java.lang.instrument.Instrumentation;\n" +
                                              "import java.util.Arrays;\n" +
                                              "\n" +
                                              "public final class StartBootstrap {\n" +
                                              "  @SuppressWarnings(\"unchecked\")\n" +
                                              "  public static void premain(String args, Instrumentation inst) throws Exception {\n" +
                                              "    final Iterable<Class<?>> providers = Arrays.<Class<?>>asList(\n" +
                                              "        Provider.class\n" +
                                              "        );\n" +
                                              "    final Iterable<Class<? extends Transformation>> transformations = Arrays.<Class<? extends Transformation>>asList(\n" +
                                              "        Transformation.class\n" +
                                              "        );\n" +
                                              "    Bootstrap.start(args, inst, providers, transformations);\n" +
                                              "  }\n" +
                                              "}\n"));
    }
}
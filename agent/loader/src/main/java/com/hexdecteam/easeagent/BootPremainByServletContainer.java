package com.hexdecteam.easeagent;

import com.google.auto.service.AutoService;
import net.bytebuddy.agent.ByteBuddyAgent;
import org.springframework.boot.loader.archive.JarFileArchive;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Set;
import java.util.jar.Attributes;

import static com.hexdecteam.easeagent.Archives.getArchiveFileContains;

@AutoService(ServletContainerInitializer.class)
public class BootPremainByServletContainer implements ServletContainerInitializer {

    @Override
    public void onStartup(Set<Class<?>> set, ServletContext servletContext) throws ServletException {
        try {
            final ClassLoader loader = getClass().getClassLoader();

            final JarFileArchive archive = new JarFileArchive(getArchiveFileContains(Main.class));
            final Attributes attributes = archive.getManifest().getMainAttributes();
            final String premainClassName = attributes.getValue("Premain-Class");
            final Class<?> premainClass = loader.loadClass(premainClassName);
            final Method premain = premainClass.getMethod("premain", String.class, Instrumentation.class);

            final URL conf = loader.getResource("application.conf");

            premain.invoke(null, path(conf), ByteBuddyAgent.install());
        } catch (Exception e) {
            throw new ServletException(e);
        }

    }

    private String path(URL url) {
        return url == null ? null : url.getPath();
    }
}

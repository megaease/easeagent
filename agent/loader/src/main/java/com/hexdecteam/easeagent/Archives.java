package com.hexdecteam.easeagent;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.security.ProtectionDomain;

public abstract class Archives {

    private Archives() { }

    static File getArchiveFileContains(Class<?> klass) throws URISyntaxException {
        ProtectionDomain protectionDomain = klass.getProtectionDomain();
        CodeSource codeSource = protectionDomain.getCodeSource();
        URI location = (codeSource == null ? null : codeSource.getLocation().toURI());
        String path = (location == null ? null : location.getSchemeSpecificPart());
        if (path == null) {
            throw new IllegalStateException("Unable to determine code source archive");
        }
        File root = new File(path);
        if (!root.exists() || root.isDirectory()) {
            throw new IllegalStateException(
                    "Unable to determine code source archive from " + root);
        }
        return root;
    }
}

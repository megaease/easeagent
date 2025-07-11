package easeagent.plugin.spring.gateway.advice;

import com.megaease.easeagent.plugin.CodeVersion;
import com.megaease.easeagent.plugin.Points;

public class CodeCons {
    public final static CodeVersion VERSIONS = CodeVersion.builder().key("spring-boot").add(Points.DEFAULT_VERSION).add("2.x.x").build();
}

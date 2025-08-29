package easeagent.plugin.spring.gateway.advice;

import com.megaease.easeagent.plugin.CodeVersion;
import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.api.config.ConfigConst;

public class CodeCons {
    public final static CodeVersion VERSIONS = CodeVersion.builder()
        .key(ConfigConst.CodeVersion.KEY_SPRING_BOOT)
        .add(Points.DEFAULT_VERSION)
        .add(ConfigConst.CodeVersion.VERSION_SPRING_BOOT2).build();
}

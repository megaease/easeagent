package com.megaease.easeagent.plugin.motan.interceptor.trace;

public enum MotanTags {
    APPLICATION("motan.application"),
    GROUP("motan.group"),
    MODULE("motan.module"),
    SERVICE("motan.service"),
    SERVICE_VERSION("motan.service.version"),
    METHOD("motan.method"),
    ARGUMENTS("motan.args"),
    RESULT("motan.result"),
    ;

    public final String name;

    MotanTags(String tagName) {
        this.name = tagName;
    }
}

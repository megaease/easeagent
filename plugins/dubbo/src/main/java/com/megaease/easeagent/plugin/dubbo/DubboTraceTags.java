package com.megaease.easeagent.plugin.dubbo;

public enum DubboTraceTags {
	SERVICE("dubbo.service"),
	METHOD("dubbo.method"),
	SERVICE_VERSION("dubbo.service.version"),
	SERVER_APPLICATION("dubbo.server.application"),
	CLIENT_APPLICATION("dubbo.client.application"),
	GROUP("dubbo.group"),
	ARGS("dubbo.args"),
	RESULT("dubbo.result"),
	;

	public final String name;

	DubboTraceTags(String name) {
		this.name = name;
	}
}

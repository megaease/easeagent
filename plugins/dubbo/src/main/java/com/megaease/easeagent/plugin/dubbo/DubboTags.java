package com.megaease.easeagent.plugin.dubbo;

public enum DubboTags {
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

	DubboTags(String name) {
		this.name = name;
	}
}

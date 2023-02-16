package com.megaease.easeagent.plugin.sofarpc;

public enum SofaRpcTags {
	SERVICE("sofa.rpc.service"),
	METHOD("sofa.rpc.method"),
	SERVICE_UNIQUE_ID("sofa.rpc.service.uniqueId"),
	SERVER_APPLICATION("sofa.rpc.server.application"),
	CLIENT_APPLICATION("sofa.rpc.client.application"),
	GROUP("sofa.rpc.group"),
	ARGS("sofa.rpc.args"),
	RESULT("sofa.rpc.result"),
	METRICS_KEY("method"),
			;

	public final String name;

	SofaRpcTags(String name) {
		this.name = name;
	}
}

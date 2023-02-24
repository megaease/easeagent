package com.megaease.easeagent.plugin.sofarpc;

public enum SofaRpcTraceTags {
	SERVICE("sofa.rpc.service"),
	METHOD("sofa.rpc.method"),
	SERVICE_UNIQUE_ID("sofa.rpc.service.uniqueId"),
	SERVER_APPLICATION("sofa.rpc.server.application"),
    CLIENT_APPLICATION("sofa.rpc.client.application"),
	GROUP("sofa.rpc.group"),
	ARGS("sofa.rpc.args"),
	RESULT("sofa.rpc.result"),
			;

	public final String name;

	SofaRpcTraceTags(String name) {
		this.name = name;
	}
}

package com.megaease.easeagent.plugin.sofarpc;

public enum SofaRpcMetricsTags {

	CATEGORY("application"),
	TYPE("sofarpc"),
    LABEL_NAME("interface"),
			;

	public final String name;

	SofaRpcMetricsTags(String name) {
		this.name = name;
	}
}

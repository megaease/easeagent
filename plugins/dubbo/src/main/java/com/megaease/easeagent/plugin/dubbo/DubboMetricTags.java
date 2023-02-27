package com.megaease.easeagent.plugin.dubbo;

public enum DubboMetricTags {
	CATEGORY("application"),
	TYPE("dubbo"),
	LABEL_NAME("interface"),
	;

	public final String name;

	DubboMetricTags(String name) {
		this.name = name;
	}
}

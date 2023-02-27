package com.megaease.easeagent.plugin.motan.interceptor.metrics;

public enum MotanMetricTags {
	CATEGORY("application"),
	TYPE("motan"),
	LABEL_NAME("interface"),
	;

	public final String name;

	MotanMetricTags(String name) {
		this.name = name;
	}
}

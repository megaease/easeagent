package com.megaease.easeagent.plugin.sofarpc.interceptor;

import com.megaease.easeagent.plugin.field.DynamicFieldAccessor;

public class MockBoltResponseFuture implements DynamicFieldAccessor {
	private Object data;

	@Override
	public void setEaseAgent$$DynamicField$$Data(Object data) {
		this.data = data;
	}

	@Override
	public Object getEaseAgent$$DynamicField$$Data() {
		return data;
	}

}

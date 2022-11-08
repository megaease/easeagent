package com.megaease.easeagent.plugin.motan.interceptor;

import com.megaease.easeagent.plugin.utils.ClassUtils.TypeChecker;
import com.weibo.api.motan.rpc.DefaultResponseFuture;

public enum MotanClassUtils {
	DefaultResponseFutureTypeChecker(new DefaultResponseFutureTypeChecker()),
	;

	private TypeChecker typeChecker;

	MotanClassUtils(TypeChecker typeChecker) {
		this.typeChecker = typeChecker;
	}

	public TypeChecker getTypeChecker() {
		return typeChecker;
	}

	public static class DefaultResponseFutureTypeChecker extends TypeChecker {

		public DefaultResponseFutureTypeChecker() {
			super("com.weibo.api.motan.rpc.DefaultResponseFuture");
		}

		@Override
		protected boolean isType(Object o) {
			return o instanceof DefaultResponseFuture;
		}
	}
}

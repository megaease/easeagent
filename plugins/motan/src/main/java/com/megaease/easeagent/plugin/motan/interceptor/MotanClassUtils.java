package com.megaease.easeagent.plugin.motan.interceptor;

import com.megaease.easeagent.plugin.utils.ClassUtils.TypeChecker;
import com.weibo.api.motan.rpc.DefaultResponseFuture;
import com.weibo.api.motan.transport.netty.NettyResponseFuture;

public enum MotanClassUtils {
	NettyResponseFutureTypeChecker(new NettyResponseFutureTypeChecker()),
	DefaultResponseFutureTypeChecker(new DefaultResponseFutureTypeChecker()),
	;

	private TypeChecker typeChecker;

	MotanClassUtils(TypeChecker typeChecker) {
		this.typeChecker = typeChecker;
	}

	public TypeChecker getTypeChecker() {
		return typeChecker;
	}

	public static class NettyResponseFutureTypeChecker extends TypeChecker {

		public NettyResponseFutureTypeChecker() {
			super("com.weibo.api.motan.transport.netty.NettyResponseFuture");
		}

		@Override
		protected boolean isType(Object o) {
			return o instanceof NettyResponseFuture;
		}
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

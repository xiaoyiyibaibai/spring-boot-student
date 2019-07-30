package com.xiaodonghong.customcacheconfig;

import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationSource;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.lang.reflect.Method;

abstract class CacheOperationSourcePointcut extends StaticMethodMatcherPointcut implements Serializable {

	@Override
	public boolean matches(Method method, Class<?> targetClass) {
		if (CacheManager.class.isAssignableFrom(targetClass)) {
			return false;
		}
		CacheOperationSource cas = getCacheOperationSource();
		return (cas != null && !CollectionUtils.isEmpty(cas.getCacheOperations(method, targetClass)));
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof CacheOperationSourcePointcut)) {
			return false;
		}
		CacheOperationSourcePointcut otherPc = (CacheOperationSourcePointcut) other;
		return ObjectUtils.nullSafeEquals(getCacheOperationSource(), otherPc.getCacheOperationSource());
	}

	@Override
	public int hashCode() {
		return CacheOperationSourcePointcut.class.hashCode();
	}

	@Override
	public String toString() {
		return getClass().getName() + ": " + getCacheOperationSource();
	}

	@Nullable
	protected abstract CacheOperationSource getCacheOperationSource();

}

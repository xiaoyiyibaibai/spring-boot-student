/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xiaodonghong.customcacheconfig;

import org.springframework.cache.Cache;

import java.lang.reflect.Method;
import java.util.Collection;

class CacheExpressionRootObject {

	private final Collection<? extends Cache> caches;

	private final Method method;

	private final Object[] args;

	private final Object target;

	private final Class<?> targetClass;


	public CacheExpressionRootObject(
			Collection<? extends Cache> caches, Method method, Object[] args, Object target, Class<?> targetClass) {

		this.method = method;
		this.target = target;
		this.targetClass = targetClass;
		this.args = args;
		this.caches = caches;
	}


	public Collection<? extends Cache> getCaches() {
		return this.caches;
	}

	public Method getMethod() {
		return this.method;
	}

	public String getMethodName() {
		return this.method.getName();
	}

	public Object[] getArgs() {
		return this.args;
	}

	public Object getTarget() {
		return this.target;
	}

	public Class<?> getTargetClass() {
		return this.targetClass;
	}

}

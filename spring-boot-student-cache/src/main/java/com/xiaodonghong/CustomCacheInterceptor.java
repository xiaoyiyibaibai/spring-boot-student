package com.xiaodonghong;

import com.xiaodonghong.aspectsupport.AbstractCustomCacheAspectSupport;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.cache.interceptor.*;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @ClassName CustomCacheInterceptor
 * @Description 自定义拦截器Advice
 * @Author renhao
 * @Date 2019/7/29 10:43
 **/
public class CustomCacheInterceptor extends AbstractCustomCacheAspectSupport implements MethodInterceptor, Serializable {

    private boolean initialized = false;
    @Override
    @Nullable
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();

        CacheOperationInvoker aopAllianceInvoker = () -> {
            try {
                return invocation.proceed();
            }
            catch (Throwable ex) {
                throw new CacheOperationInvoker.ThrowableWrapper(ex);
            }
        };

        try {
            return execute(aopAllianceInvoker, invocation.getThis(), method, invocation.getArguments());
        }
        catch (CacheOperationInvoker.ThrowableWrapper th) {
            throw th.getOriginal();
        }
    }

}

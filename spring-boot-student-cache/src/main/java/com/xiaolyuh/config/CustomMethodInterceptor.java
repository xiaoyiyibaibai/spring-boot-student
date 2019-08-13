package com.xiaolyuh.config;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @ClassName CustomMethodInterceptor
 * @Description TODO
 * @Author renhao
 * @Date 2019/8/9 16:35
 **/
@Component
public class CustomMethodInterceptor implements MethodInterceptor, Serializable {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        System.out.println("myClass="+CustomMethodInterceptor.class.getClass());
        return invocation.proceed();
    }
}

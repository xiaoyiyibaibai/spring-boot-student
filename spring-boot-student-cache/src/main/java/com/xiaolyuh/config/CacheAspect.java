package com.xiaolyuh.config;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * @ClassName CacheAspect
 * @Description TODO
 * @Author reo
 * @Date 2019/7/26 10:52
 **/
@Aspect
@Component
public class CacheAspect {

    @Pointcut("execution(* com.xiaolyuh.*.*(..))&&@annotation(org.springframework.cache.annotation.Cacheable)")
    public void pointCutDefForCacheable(){
    }

    @Before("pointCutDefForCacheable()" )
    public void doBeforeForCacheable(JoinPoint joinPoint){

        System.out.println("---------------------------------- doBeforeForCacheable start ------------------------------------ ");
        System.out.println("joinPoint.getArgs() = "+joinPoint.getArgs());
        System.out.println("joinPoint.getTarget().getClass().getName() = "+joinPoint.getTarget().getClass().getName());
        System.out.println("joinPoint.getThis().getClass().getName() = "+joinPoint.getThis().getClass().getName());
        System.out.println("joinPoint.getSignature().getName() = "+joinPoint.getSignature().getName());
        System.out.println("joinPoint.getArgs() = "+joinPoint.getArgs());
        System.out.println("joinPoint.getTarget().getClass().getName() = "+joinPoint.getTarget().getClass().getName());
        System.out.println("joinPoint.getThis().getClass().getName() = "+joinPoint.getThis().getClass().getName());
        System.out.println("---------------------------------- doBeforeForCacheable end ------------------------------------ ");
    }

    @After("pointCutDefForCacheable()" )
    public void doAfterForCacheable(JoinPoint joinPoint){

        System.out.println("---------------------------------- doAfterForCacheable start ------------------------------------ ");
        System.out.println("joinPoint.getArgs() = "+joinPoint.getArgs());
        System.out.println("joinPoint.getTarget().getClass().getName() = "+joinPoint.getTarget().getClass().getName());
        System.out.println("joinPoint.getThis().getClass().getName() = "+joinPoint.getThis().getClass().getName());
        System.out.println("joinPoint.getSignature().getName() = "+joinPoint.getSignature().getName());
        System.out.println("joinPoint.getArgs() = "+joinPoint.getArgs());
        System.out.println("joinPoint.getTarget().getClass().getName() = "+joinPoint.getTarget().getClass().getName());
        System.out.println("joinPoint.getThis().getClass().getName() = "+joinPoint.getThis().getClass().getName());
        System.out.println("---------------------------------- doAfterForCacheable end ------------------------------------ ");
    }


    @Pointcut("execution(* com.xiaolyuh.*.*(..))&&@annotation(org.springframework.cache.annotation.CachePut)")
    public void pointCutDefForCachePut(){

    }

    @Before("pointCutDefForCachePut()" )
    public void doBeforeForCachePut(JoinPoint joinPoint){

        System.out.println("---------------------------------- doBeforeForCachePut start ------------------------------------ ");
        System.out.println("joinPoint.getArgs() = "+joinPoint.getArgs());
        System.out.println("joinPoint.getTarget().getClass().getName() = "+joinPoint.getTarget().getClass().getName());
        System.out.println("joinPoint.getThis().getClass().getName() = "+joinPoint.getThis().getClass().getName());
        System.out.println("joinPoint.getSignature().getName() = "+joinPoint.getSignature().getName());
        System.out.println("joinPoint.getArgs() = "+joinPoint.getArgs());
        System.out.println("joinPoint.getTarget().getClass().getName() = "+joinPoint.getTarget().getClass().getName());
        System.out.println("joinPoint.getThis().getClass().getName() = "+joinPoint.getThis().getClass().getName());
        System.out.println("---------------------------------- doBeforeForCachePut end ------------------------------------ ");
    }

    @After("pointCutDefForCachePut()" )
    public void doAfterForCachePut(JoinPoint joinPoint){

        System.out.println("---------------------------------- doAfterForCachePut start ------------------------------------ ");
        System.out.println("joinPoint.getArgs() = "+joinPoint.getArgs());
        System.out.println("joinPoint.getTarget().getClass().getName() = "+joinPoint.getTarget().getClass().getName());
        System.out.println("joinPoint.getThis().getClass().getName() = "+joinPoint.getThis().getClass().getName());
        System.out.println("joinPoint.getSignature().getName() = "+joinPoint.getSignature().getName());
        System.out.println("joinPoint.getArgs() = "+joinPoint.getArgs());
        System.out.println("joinPoint.getTarget().getClass().getName() = "+joinPoint.getTarget().getClass().getName());
        System.out.println("joinPoint.getThis().getClass().getName() = "+joinPoint.getThis().getClass().getName());
        System.out.println("---------------------------------- doAfterForCachePut end ------------------------------------ ");
    }

}

package com.xiaodonghong.annotations;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

import java.lang.annotation.Documented;
        import java.lang.annotation.ElementType;
        import java.lang.annotation.Inherited;
        import java.lang.annotation.Retention;
        import java.lang.annotation.RetentionPolicy;
        import java.lang.annotation.Target;
        import org.springframework.core.annotation.AliasFor;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface CustomCacheable {
    @AliasFor("cacheNames")
    String[] value() default {};

    @AliasFor("value")
    String[] cacheNames() default {};

    String key() default "";

    String keyGenerator() default "";

    String cacheManager() default "";

    /**
     * @Author xiaodongohong
     * @Description 刷新時間
     *  -1 表示永遠不刷新
     * @Date 10:20 2019/8/13
     * @Param []
     * @return java.lang.String
     **/
    int refreshTimes() default -1;

    /**
     * @Author xiaodongohong
     * @Description 過期時間
     *    -1 表示永遠不過期
     * @Date 10:20 2019/8/13
     * @Param []
     * @return java.lang.String
     **/
    int ttl() default -1;

    String cacheResolver() default "";

    String condition() default "";

    String unless() default "";

    boolean sync() default false;
}

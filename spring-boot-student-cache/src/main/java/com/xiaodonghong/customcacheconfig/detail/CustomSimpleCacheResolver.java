package com.xiaodonghong.customcacheconfig.detail;

import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleCacheResolver;
import org.springframework.lang.Nullable;

/**
 * @ClassName CustomSimpleCacheResolver
 * @Description TODO
 * @Author renhao
 * @Date 2019/7/29 17:51
 **/
public class CustomSimpleCacheResolver extends SimpleCacheResolver {
     @Nullable
     public static SimpleCacheResolver of(@Nullable CacheManager cacheManager) {
        return (cacheManager != null ? new SimpleCacheResolver(cacheManager) : null);
    }
}

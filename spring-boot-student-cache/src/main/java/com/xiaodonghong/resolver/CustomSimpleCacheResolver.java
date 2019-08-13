package com.xiaodonghong.resolver;

import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.SimpleCacheResolver;
import org.springframework.lang.Nullable;

import java.util.Collection;

/**
 * @ClassName CustomSimpleCacheResolver
 * @Description TODO
 * @Author renhao
 * @Date 2019/8/13 17:34
 **/
public class CustomSimpleCacheResolver extends SimpleCacheResolver {
    @Nullable
   public static SimpleCacheResolver of(@Nullable CacheManager cacheManager) {
        return (cacheManager != null ? new SimpleCacheResolver(cacheManager) : null);
    }

    @Override
    protected Collection<String> getCacheNames(CacheOperationInvocationContext<?> context) {
        return context.getOperation().getCacheNames();
    }
}

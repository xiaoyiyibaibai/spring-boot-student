package com.xiaodonghong.parser;

import com.xiaodonghong.annotations.CustomCacheEvict;
import com.xiaodonghong.annotations.CustomCachePut;
import com.xiaodonghong.annotations.CustomCacheable;
import com.xiaodonghong.annotations.CustomCaching;
import com.xiaodonghong.operations.CustomCacheableOperation;
import org.springframework.cache.annotation.*;
import org.springframework.cache.interceptor.CacheEvictOperation;
import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.cache.interceptor.CachePutOperation;
import org.springframework.cache.interceptor.CacheableOperation;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @ClassName CustomSpringCacheAnnotationParser
 * @Description TODO 該類是解析CachePut Cacheable等註解的類
 * @Author renhao
 * @Date 2019/7/29 11:43
 **/
public class CustomSpringCacheAnnotationParser implements CacheAnnotationParser {
    private static final Set<Class<? extends Annotation>> CACHE_OPERATION_ANNOTATIONS = new LinkedHashSet<>( 8 );

    static {
        //TODO 设置拦截的类
        CACHE_OPERATION_ANNOTATIONS.add( CustomCacheable.class );
        CACHE_OPERATION_ANNOTATIONS.add( CustomCacheEvict.class );
        CACHE_OPERATION_ANNOTATIONS.add( CustomCachePut.class );
        CACHE_OPERATION_ANNOTATIONS.add( CustomCaching.class );
    }

    //TODO 父类核心方法
    @Override
    @Nullable
    public Collection<CacheOperation> parseCacheAnnotations(Class<?> type) {
        DefaultCacheConfig defaultCacheConfig = new DefaultCacheConfig( type );
        return parseCacheAnnotations(defaultCacheConfig,type);
    }
    //TODO 父类核心方法
    @Override
    @Nullable
    public Collection<CacheOperation> parseCacheAnnotations(Method method) {
        DefaultCacheConfig defaultCacheConfig = new DefaultCacheConfig( method.getDeclaringClass() );
        return parseCacheAnnotations(defaultCacheConfig,method);
    }


    @Nullable
    private Collection<CacheOperation> parseCacheAnnotations(DefaultCacheConfig cachingConfig, AnnotatedElement ae) {
        Collection<CacheOperation> ops = parseCacheAnnotations(cachingConfig, ae, false);
        if (ops != null&& ops.size()>1){
            Collection<CacheOperation> localOps = parseCacheAnnotations(cachingConfig, ae, true);
            if (localOps!=null){
                return localOps;
            }
        }
        return ops;
    }

    @Nullable
    private Collection<CacheOperation> parseCacheAnnotations(
            DefaultCacheConfig cachingConfig, AnnotatedElement ae, boolean localOnly) {
        Collection<? extends Annotation> anns = (localOnly ?
                AnnotatedElementUtils.getAllMergedAnnotations(ae, CACHE_OPERATION_ANNOTATIONS) :
                AnnotatedElementUtils.findAllMergedAnnotations(ae, CACHE_OPERATION_ANNOTATIONS));

        if (anns.isEmpty()){
            return null;
        }

        final Collection<CacheOperation> ops = new ArrayList<>( 1 );
        anns.stream().filter( ann -> ann instanceof CustomCacheable ).forEach(
                ann -> ops.add(parseCacheableAnnotation(ae , cachingConfig, (CustomCacheable)ann)));
        anns.stream().filter( ann -> ann instanceof CustomCacheEvict ).forEach(
                ann -> ops.add(parseEvictAnnotation(ae , cachingConfig, (CustomCacheEvict)ann)));
        anns.stream().filter( ann-> ann instanceof CustomCachePut ).forEach(
                ann-> ops.add(parsePutAnnotation( ae,cachingConfig,(CustomCachePut) ann ))
        );
        anns.stream().filter(ann -> ann instanceof CustomCaching ).forEach(
                ann -> parseCachingAnnotation(ae, cachingConfig, (CustomCaching) ann, ops));
        return ops;
    }

    private CacheableOperation parseCacheableAnnotation(
            AnnotatedElement ae, DefaultCacheConfig defaultConfig, CustomCacheable cacheable) {

        CacheableOperation.Builder builder = new CacheableOperation.Builder();

        builder.setName(ae.toString());
        builder.setCacheNames(cacheable.cacheNames());
        builder.setCondition(cacheable.condition());
        builder.setUnless(cacheable.unless());
        builder.setKey(cacheable.key());
        builder.setKeyGenerator(cacheable.keyGenerator());
        builder.setCacheManager(cacheable.cacheManager());
        builder.setCacheResolver(cacheable.cacheResolver());
        builder.setSync(cacheable.sync());
        defaultConfig.applyDefault(builder);
        CacheableOperation op = builder.build();
        validateCacheOperation(ae, op);

        return op;
    }

    private CustomCacheableOperation parseCacheableAnnotation2(
            AnnotatedElement ae, DefaultCacheConfig defaultConfig, CustomCacheable cacheable) {

        CustomCacheableOperation.Builder builder = new CustomCacheableOperation.Builder();

        builder.setName(ae.toString());
        builder.setCacheNames(cacheable.cacheNames());
        builder.setCondition(cacheable.condition());
        builder.setUnless(cacheable.unless());
        builder.setKey(cacheable.key());
        builder.setKeyGenerator(cacheable.keyGenerator());
        builder.setCacheManager(cacheable.cacheManager());
        builder.setCacheResolver(cacheable.cacheResolver());
        builder.setSync(cacheable.sync());
        builder.setRefreshTimes( cacheable.refreshTimes() );
        builder.setTtl( cacheable.ttl() );
        defaultConfig.applyDefault(builder);
        CustomCacheableOperation op = builder.build();
        validateCacheOperation(ae, op);

        return op;
    }
    private CacheEvictOperation parseEvictAnnotation(
            AnnotatedElement ae, DefaultCacheConfig defaultConfig, CustomCacheEvict cacheEvict) {
        CacheEvictOperation.Builder builder = new CacheEvictOperation.Builder();

        builder.setName( ae.toString() );
        builder.setCacheNames( cacheEvict.cacheNames() );
        builder.setCondition( cacheEvict.condition() );
        builder.setKey( cacheEvict.key() );
        builder.setKeyGenerator( cacheEvict.keyGenerator() );
        builder.setCacheManager( cacheEvict.cacheManager() );
        builder.setCacheResolver( cacheEvict.cacheResolver() );
        builder.setCacheWide( cacheEvict.allEntries() );
        builder.setBeforeInvocation( cacheEvict.beforeInvocation() );

        defaultConfig.applyDefault( builder );
        CacheEvictOperation op = builder.build();
        return op;
    }

    private CacheOperation parsePutAnnotation(AnnotatedElement ae, DefaultCacheConfig defaultCacheConfig, CustomCachePut cachePut) {
        CachePutOperation.Builder builder = new CachePutOperation.Builder();
        builder.setName( ae.toString() );
        builder.setCacheNames( cachePut.cacheNames() );
        builder.setCondition( cachePut.condition() );
        builder.setUnless( cachePut.unless() );
        builder.setKey( cachePut.key() );
        builder.setKeyGenerator(cachePut.keyGenerator());
        builder.setCacheManager(cachePut.cacheManager());
        builder.setCacheResolver(cachePut.cacheResolver());

        defaultCacheConfig.applyDefault(builder);
        CachePutOperation op = builder.build();
        validateCacheOperation(ae, op);

        return op;
    }

    private void parseCachingAnnotation(
            AnnotatedElement ae, DefaultCacheConfig defaultConfig, CustomCaching caching, Collection<CacheOperation> ops) {
        CustomCacheable [] cacheables = caching.cacheable();
        for (CustomCacheable  cacheable: cacheables){
            ops.add( parseCacheableAnnotation( ae, defaultConfig, cacheable) );
        }
        CustomCacheEvict [] cacheEvicts = caching.evict();
        for (CustomCacheEvict cacheEvict: cacheEvicts){
            ops.add( parseEvictAnnotation(ae, defaultConfig, cacheEvict));
        }
        CustomCachePut[] cachePuts = caching.put();
        for (CustomCachePut cachePut: cachePuts){
            ops.add(parsePutAnnotation(ae, defaultConfig, cachePut));
        }
    }

    private void validateCacheOperation(AnnotatedElement ae, CacheOperation operation) {
        if (StringUtils.hasText( operation.getKey() ) && StringUtils.hasText( operation.getKeyGenerator() )) {
            throw new IllegalStateException( "Invalid cache annotation configuration on '" +
                    ae.toString() + "'. Both 'key' and 'keyGenerator' attributes have been set. " +
                    "These attributes are mutually exclusive: either set the SpEL expression used to" +
                    "compute the key at runtime or set the name of the KeyGenerator bean to use." );
        }

        if (StringUtils.hasText( operation.getCacheManager() ) && StringUtils.hasText( operation.getCacheResolver() )) {
            throw new IllegalStateException( "Invalid cache annotation configuration on '" +
                    ae.toString() + "'. Both 'cacheManager' and 'cacheResolver' attributes have been set. " +
                    "These attributes are mutually exclusive: the cache manager is used to configure a" +
                    "default cache resolver if none is set. If a cache resolver is set, the cache manager" +
                    "won't be used." );
        }
    }

    @Override
    public boolean equals(Object other) {
        return (this == other || other instanceof CustomSpringCacheAnnotationParser);
    }

    @Override
    public int hashCode() {
        return CustomSpringCacheAnnotationParser.class.hashCode();
    }

    private static class DefaultCacheConfig {

        private final Class<?> target;

        @Nullable
        private String[] cacheNames;

        @Nullable
        private String keyGenerator;

        @Nullable
        private String cacheManager;

        @Nullable
        private String cacheResolver;

        private boolean initialized = false;

        public DefaultCacheConfig(Class<?> target) {
            this.target = target;
        }

        /**
         * Apply the defaults to the specified {@link CacheOperation.Builder}.
         *
         * @param builder the operation builder to update
         */
        public void applyDefault(CacheOperation.Builder builder) {
            if (!this.initialized) {
                CacheConfig annotation = AnnotatedElementUtils.findMergedAnnotation( this.target, CacheConfig.class );
                if (annotation != null) {
                    this.cacheNames = annotation.cacheNames();
                    this.keyGenerator = annotation.keyGenerator();
                    this.cacheManager = annotation.cacheManager();
                    this.cacheResolver = annotation.cacheResolver();
                }
                this.initialized = true;
            }

            if (builder.getCacheNames().isEmpty() && this.cacheNames != null) {
                builder.setCacheNames( this.cacheNames );
            }
            if (!StringUtils.hasText( builder.getKey() ) && !StringUtils.hasText( builder.getKeyGenerator() ) &&
                    StringUtils.hasText( this.keyGenerator )) {
                builder.setKeyGenerator( this.keyGenerator );
            }

            if (StringUtils.hasText( builder.getCacheManager() ) || StringUtils.hasText( builder.getCacheResolver() )) {
                // One of these is set so we should not inherit anything
            } else if (StringUtils.hasText( this.cacheResolver )) {
                builder.setCacheResolver( this.cacheResolver );
            } else if (StringUtils.hasText( this.cacheManager )) {
                builder.setCacheManager( this.cacheManager );
            }
        }
    }


}

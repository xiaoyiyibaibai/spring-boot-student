package com.xiaodonghong.customcacheconfig;

import org.springframework.cache.annotation.*;
import org.springframework.cache.interceptor.CacheEvictOperation;
import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.cache.interceptor.CachePutOperation;
import org.springframework.cache.interceptor.CacheableOperation;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedArrayType;
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
        CACHE_OPERATION_ANNOTATIONS.add( Cacheable.class );
        CACHE_OPERATION_ANNOTATIONS.add( CacheEvict.class );
        CACHE_OPERATION_ANNOTATIONS.add( CachePut.class );
        CACHE_OPERATION_ANNOTATIONS.add( Caching.class );
    }

    @Override
    @Nullable
    public Collection<CacheOperation> parseCacheAnnotations(Class<?> type) {
        DefaultCacheConfig defaultCacheConfig = new DefaultCacheConfig( type );
        return parseCacheAnnotations(defaultCacheConfig,type);
    }

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
        anns.stream().filter( ann -> ann instanceof Cacheable ).forEach(
                ann -> ops.add(parseCacheableAnnotation(ae , cachingConfig, (Cacheable)ann)));
        anns.stream().filter( ann -> ann instanceof CacheEvict ).forEach(
                ann -> ops.add(parseEvictAnnotation(ae , cachingConfig, (CacheEvict)ann)));
        anns.stream().filter( ann-> ann instanceof CachePut ).forEach(
                ann-> ops.add(parsePutAnnotation( ae,cachingConfig,(CachePut) ann ))
        );
        anns.stream().filter(ann -> ann instanceof Caching).forEach(
                ann -> parseCachingAnnotation(ae, cachingConfig, (Caching) ann, ops));
        return ops;
    }

    private CacheableOperation parseCacheableAnnotation(
            AnnotatedElement ae, DefaultCacheConfig defaultConfig, Cacheable cacheable) {

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

    private CacheEvictOperation parseEvictAnnotation(
            AnnotatedElement ae, DefaultCacheConfig defaultConfig, CacheEvict cacheEvict) {
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

    private CacheOperation parsePutAnnotation(AnnotatedElement ae, DefaultCacheConfig defaultCacheConfig, CachePut cachePut) {
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
            AnnotatedElement ae, DefaultCacheConfig defaultConfig, Caching caching, Collection<CacheOperation> ops) {
        Cacheable [] cacheables = caching.cacheable();
        for (Cacheable cacheable: cacheables){
            ops.add( parseCacheableAnnotation( ae, defaultConfig, cacheable) );
        }
        CacheEvict [] cacheEvicts = caching.evict();
        for (CacheEvict cacheEvict: cacheEvicts){
            ops.add( parseEvictAnnotation(ae, defaultConfig, cacheEvict));
        }
        CachePut[] cachePuts = caching.put();
        for (CachePut cachePut: cachePuts){
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

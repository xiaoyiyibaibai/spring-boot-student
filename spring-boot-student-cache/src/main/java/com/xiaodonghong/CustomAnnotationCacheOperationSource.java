package com.xiaodonghong;


import org.springframework.cache.annotation.CacheAnnotationParser;
import org.springframework.cache.interceptor.AbstractFallbackCacheOperationSource;
import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import com.xiaodonghong.parser.CustomSpringCacheAnnotationParser;


import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @ClassName CustomAnnotationCacheOperationSource
 * @Description TODO
 * @Author renhao
 * @Date 2019/7/29 11:16
 **/
public class CustomAnnotationCacheOperationSource extends AbstractFallbackCacheOperationSource implements Serializable {
    private final boolean publicMethodsOnly ;

    private final Set<CacheAnnotationParser> annotationParsers;

    public CustomAnnotationCacheOperationSource() {
        this(true);
    }
    public CustomAnnotationCacheOperationSource(boolean publicMethodsOnly) {
        this.publicMethodsOnly = publicMethodsOnly;
        //TODO  註解解析器，用於解析Cacheable  CachPut等信息
        this.annotationParsers = Collections.singleton(new CustomSpringCacheAnnotationParser());
    }


    public CustomAnnotationCacheOperationSource(CacheAnnotationParser annotationParser) {
        this.publicMethodsOnly = true;
        Assert.notNull(annotationParser, "CacheAnnotationParser must not be null");
        this.annotationParsers = Collections.singleton(annotationParser);
    }

    public CustomAnnotationCacheOperationSource(CacheAnnotationParser... annotationParsers) {
        this.publicMethodsOnly = true;
        Assert.notEmpty(annotationParsers, "At least one CacheAnnotationParser needs to be specified");
        this.annotationParsers = new LinkedHashSet<>( Arrays.asList(annotationParsers));
    }

    public CustomAnnotationCacheOperationSource(Set<CacheAnnotationParser> annotationParsers) {
        this.publicMethodsOnly = true;
        Assert.notEmpty(annotationParsers, "At least one CacheAnnotationParser needs to be specified");
        this.annotationParsers = annotationParsers;
    }

    @Override
    protected Collection<CacheOperation> findCacheOperations(Class<?> clazz) {
        return determineCacheOperations(parser -> parser.parseCacheAnnotations(clazz));
    }

    @Override
    protected Collection<CacheOperation> findCacheOperations(Method method) {
        return determineCacheOperations(parser -> parser.parseCacheAnnotations(method));
    }

    @Nullable
    protected Collection<CacheOperation> determineCacheOperations(CustomAnnotationCacheOperationSource.CacheOperationProvider provider) {
      Collection<CacheOperation> ops = null;
      for (CacheAnnotationParser annotationParser: this.annotationParsers){
          Collection<CacheOperation> annOps = provider.getCacheOperations( annotationParser );
          if (annOps != null) {
              if (ops == null) {
                  ops = annOps;
              }
              else {
                  Collection<CacheOperation> combined = new ArrayList<>(ops.size() + annOps.size());
                  combined.addAll(ops);
                  combined.addAll(annOps);
                  ops = combined;
              }
          }
      }
     return ops;
    }



    @Override
    protected boolean allowPublicMethodsOnly() {
        return this.publicMethodsOnly;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CustomAnnotationCacheOperationSource)) {
            return false;
        }
        CustomAnnotationCacheOperationSource otherCos = (CustomAnnotationCacheOperationSource) other;
        return (this.annotationParsers.equals(otherCos.annotationParsers) &&
                this.publicMethodsOnly == otherCos.publicMethodsOnly);
    }

    @Override
    public int hashCode() {
        return this.annotationParsers.hashCode();
    }

    @FunctionalInterface
    protected interface CacheOperationProvider {
        @Nullable
        Collection<CacheOperation> getCacheOperations(CacheAnnotationParser parser);
    }
}

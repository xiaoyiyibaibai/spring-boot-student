package com.xiaodonghong.aspectsupport;

import java.io.IOException;
import java.lang.reflect.Method;

import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaodonghong.resolver.CustomSimpleCacheResolver;
import com.xiaolyuh.entity.Person;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.*;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.lang.Nullable;
import org.springframework.util.*;
import org.springframework.util.function.SingletonSupplier;
import org.springframework.util.function.SupplierUtils;

public abstract class AbstractCustomCacheAspectSupport extends CacheAspectSupport {
    private boolean initialized = false;
    @Nullable
    private SingletonSupplier<CacheResolver> cacheResolver;
    @Nullable
    private BeanFactory beanFactory;
    private final CacheOperationExpressionEvaluator evaluator = new CacheOperationExpressionEvaluator();
    private final Map<CacheOperationCacheKey, CacheOperationMetadata> metadataCache = new ConcurrentHashMap<>(1024);
    private SingletonSupplier<KeyGenerator> keyGenerator = SingletonSupplier.of(SimpleKeyGenerator::new);
    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public void configure(
            @Nullable Supplier<CacheErrorHandler> errorHandler, @Nullable Supplier<KeyGenerator> keyGenerator,
            @Nullable Supplier<CacheResolver> cacheResolver, @Nullable Supplier<CacheManager> cacheManager) {

        this.errorHandler = new SingletonSupplier<>(errorHandler, SimpleCacheErrorHandler::new);
        this.keyGenerator = new SingletonSupplier<>(keyGenerator, SimpleKeyGenerator::new);
        this.cacheResolver = new SingletonSupplier<>(cacheResolver,
                () -> CustomSimpleCacheResolver.of( SupplierUtils.resolve(cacheManager)));
    }
    @Override
    public void setKeyGenerator(KeyGenerator keyGenerator) {
        this.keyGenerator = SingletonSupplier.of(keyGenerator);
    }

    @Override
    public KeyGenerator getKeyGenerator() {
        return this.keyGenerator.obtain();
    }

    @Nullable
    @Override
    public Object execute(CacheOperationInvoker invoker, Object target, Method method, Object[] args) {
        // Check whether aspect is enabled (to cope with cases where the AJ is pulled in automatically)
        if (this.initialized) {
            Class<?> targetClass = getTargetClass( target );
            CacheOperationSource cacheOperationSource = getCacheOperationSource();
            if (cacheOperationSource != null) {
                Collection<CacheOperation> operations = cacheOperationSource.getCacheOperations( method, targetClass );
                if (!CollectionUtils.isEmpty( operations )) {
                    Object result = execute( invoker, method,
                            new CacheOperationContexts( operations, method, args, target, targetClass ) );
               if (result!=null){
                   ObjectMapper objectMapper = new ObjectMapper();
                   Class returnType = method.getReturnType();
                   if (returnType.isAssignableFrom( Collection.class )) {

                   } else if (returnType.isAssignableFrom( HashMap.class )) {

                   } else {
                   }
                   try {
                       String json = objectMapper.writeValueAsString( result );
                       boolean flag = json.startsWith( "[" ) && json.endsWith( "]" );
                       if (flag) {
                           return objectMapper.readValue( json, new TypeReference<List<Person>>() {
                           } );
                       }
                       boolean flag2 = json.startsWith( "{" ) && json.endsWith( "}" );
                       if (flag2) {
                           return objectMapper.readValue( json, new TypeReference<Person>() {
                           } );
                       }
                   } catch (JsonProcessingException e) {
                       e.printStackTrace();
                   } catch (IOException e) {
                       e.printStackTrace();
                   } catch (Exception e) {
                       e.printStackTrace();
                   }
               }
               return result;
                }
            }
        }

        return invoker.invoke();
    }


    @Nullable
    private Object execute(final CacheOperationInvoker invoker, Method method, CacheOperationContexts contexts) {
        if (contexts.isSynchronized()) {
            CacheOperationContext context = contexts.get(CacheableOperation.class).iterator().next();
            if (isConditionPassing(context, CacheOperationExpressionEvaluator.NO_RESULT)) {
                Object key = generateKey(context, CacheOperationExpressionEvaluator.NO_RESULT);
                Cache cache = context.getCaches().iterator().next();
                try {
                    return wrapCacheValue(method, cache.get(key, () -> unwrapReturnValue(invokeOperation(invoker))));
                }
                catch (Cache.ValueRetrievalException ex) {
                    // The invoker wraps any Throwable in a ThrowableWrapper instance so we
                    // can just make sure that one bubbles up the stack.
                    throw (CacheOperationInvoker.ThrowableWrapper) ex.getCause();
                }
            }
            else {
                // No caching required, only call the underlying method
                return invokeOperation(invoker);
            }
        }
        // Process any early evictions
        processCacheEvicts(contexts.get(CacheEvictOperation.class), true,
                CacheOperationExpressionEvaluator.NO_RESULT);

        // Check if we have a cached item matching the conditions
        Cache.ValueWrapper cacheHit = findCachedItem(contexts.get(CacheableOperation.class));

        // Collect puts from any @Cacheable miss, if no cached item is found
        List<CachePutRequest> cachePutRequests = new LinkedList<>();
        if (cacheHit == null) {
            collectPutRequests(contexts.get(CacheableOperation.class),
                    CacheOperationExpressionEvaluator.NO_RESULT, cachePutRequests);
        }

        Object cacheValue;
        Object returnValue;

        if (cacheHit != null && !hasCachePut(contexts)) {
            // If there are no put requests, just use the cache hit
            cacheValue = cacheHit.get();
            returnValue = wrapCacheValue(method, cacheValue);
        }
        else {
            // Invoke the method if we don't have a cache hit
            returnValue = invokeOperation(invoker);
            cacheValue = unwrapReturnValue(returnValue);
        }

        // Collect any explicit @CachePuts
        collectPutRequests(contexts.get(CachePutOperation.class), cacheValue, cachePutRequests);

        // Process any collected put requests, either from @CachePut or a @Cacheable miss
        for (CachePutRequest cachePutRequest : cachePutRequests) {
            cachePutRequest.apply(cacheValue);
        }

        // Process any late evictions
        processCacheEvicts(contexts.get(CacheEvictOperation.class), false, cacheValue);

        return returnValue;
    }

    private boolean hasCachePut(CacheOperationContexts contexts) {
        // Evaluate the conditions *without* the result object because we don't have it yet...
        Collection<CacheOperationContext> cachePutContexts = contexts.get(CachePutOperation.class);
        Collection<CacheOperationContext> excluded = new ArrayList<>();
        for (CacheOperationContext context : cachePutContexts) {
            try {
                if (!context.isConditionPassing(CacheOperationExpressionEvaluator.RESULT_UNAVAILABLE)) {
                    excluded.add(context);
                }
            }
            catch (VariableNotAvailableException ex) {
                // Ignoring failure due to missing result, consider the cache put has to proceed
            }
        }
        // Check if all puts have been excluded by condition
        return (cachePutContexts.size() != excluded.size());
    }

    // TODO 查找缓存中是否存在
    @Nullable
    private Cache.ValueWrapper findCachedItem(Collection<CacheOperationContext> contexts) {
        Object result = CacheOperationExpressionEvaluator.NO_RESULT;
        for (CacheOperationContext context : contexts) {
            if (isConditionPassing(context, result)) {
                Object key = generateKey(context, result);
                Cache.ValueWrapper cached = findInCaches(context, key);
                if (cached != null) {
                    return cached;
                }
                else {
                    if (logger.isTraceEnabled()) {
                        logger.trace("No cache entry for key '" + key + "' in cache(s) " + context.getCacheNames());
                    }
                }
            }
        }
        return null;
    }


    private void collectPutRequests(Collection<CacheOperationContext> contexts,
                                    @Nullable Object result, Collection<CachePutRequest> putRequests) {

        for (CacheOperationContext context : contexts) {
            if (isConditionPassing(context, result)) {
                Object key = generateKey(context, result);
                putRequests.add(new CachePutRequest(context, key));
            }
        }
    }

    @Nullable
    private Cache.ValueWrapper findInCaches(CacheOperationContext context, Object key) {
        for (Cache cache : context.getCaches()) {
            Cache.ValueWrapper wrapper = doGet(cache, key);
            if (wrapper != null) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Cache entry for key '" + key + "' found in cache '" + cache.getName() + "'");
                }
                return wrapper;
            }
        }
        return null;
    }
    // TODO  根据Collection<CacheOperationContext>执行删除操作
    private void processCacheEvicts(
            Collection<CacheOperationContext> contexts, boolean beforeInvocation, @Nullable Object result) {

        for (CacheOperationContext context : contexts) {
            CacheEvictOperation operation = (CacheEvictOperation) context.metadata.operation;
            if (beforeInvocation == operation.isBeforeInvocation() && isConditionPassing(context, result)) {
                performCacheEvict(context, operation, result);
            }
        }
    }

    private void performCacheEvict(
            CacheOperationContext context, CacheEvictOperation operation, @Nullable Object result) {

        Object key = null;
        for (Cache cache : context.getCaches()) {
            if (operation.isCacheWide()) {
                logInvalidating(context, operation, null);
                doClear(cache);
            }
            else {
                if (key == null) {
                    key = generateKey(context, result);
                }
                logInvalidating(context, operation, key);
                doEvict(cache, key);
            }
        }
    }

    private void logInvalidating(CacheOperationContext context, CacheEvictOperation operation, @Nullable Object key) {
        if (logger.isTraceEnabled()) {
            logger.trace("Invalidating " + (key != null ? "cache key [" + key + "]" : "entire cache") +
                    " for operation " + operation + " on method " + context.metadata.method);
        }
    }
    private boolean isConditionPassing(CacheOperationContext context, @Nullable Object result) {
        boolean passing = context.isConditionPassing(result);
        if (!passing && logger.isTraceEnabled()) {
            logger.trace("Cache condition failed on method " + context.metadata.method +
                    " for operation " + context.metadata.operation);
        }
        return passing;
    }

    private Object generateKey(CacheOperationContext context, @Nullable Object result) {
        Object key = context.generateKey(result);
        if (key == null) {
            throw new IllegalArgumentException("Null key returned for cache operation (maybe you are " +
                    "using named params on classes without debug info?) " + context.metadata.operation);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Computed cache key '" + key + "' for operation " + context.metadata.operation);
        }
        return key;
    }

    @Nullable
    private Object wrapCacheValue(Method method, @Nullable Object cacheValue) {
        if (method.getReturnType() == Optional.class &&
                (cacheValue == null || cacheValue.getClass() != Optional.class)) {
            return Optional.ofNullable(cacheValue);
        }
        return cacheValue;
    }

    @Nullable
    private Object unwrapReturnValue(Object returnValue) {
        return ObjectUtils.unwrapOptional(returnValue);
    }


    @Override
    public void afterSingletonsInstantiated() {
        if (getCacheResolver() == null) {
            // Lazily initialize cache resolver via default cache manager...
            Assert.state(this.beanFactory != null, "CacheResolver or BeanFactory must be set on cache aspect");
            try {
                setCacheManager(this.beanFactory.getBean(CacheManager.class));
            }
            catch (NoUniqueBeanDefinitionException ex) {
                throw new IllegalStateException("No CacheResolver specified, and no unique bean of type " +
                        "CacheManager found. Mark one as primary or declare a specific CacheManager to use.");
            }
            catch (NoSuchBeanDefinitionException ex) {
                throw new IllegalStateException("No CacheResolver specified, and no bean of type CacheManager found. " +
                        "Register a CacheManager bean or remove the @EnableCaching annotation from your configuration.");
            }
        }
        this.initialized = true;
    }

    private Class<?> getTargetClass(Object target) {
        return AopProxyUtils.ultimateTargetClass( target );
    }

    protected CacheOperationContext getOperationContextCus(
            CacheOperation operation, Method method, Object[] args, Object target, Class<?> targetClass) {

        CacheOperationMetadata metadata = getCacheOperationMetadata2(operation, method, targetClass);
        return new CacheOperationContext(metadata, args, target);
    }

    protected  CacheOperationMetadata getCacheOperationMetadata2(
            CacheOperation operation, Method method, Class<?> targetClass) {

        CacheOperationCacheKey cacheKey = new CacheOperationCacheKey(operation, method, targetClass);
        CacheOperationMetadata metadata = this.metadataCache.get(cacheKey);
        if (metadata == null) {
            KeyGenerator operationKeyGenerator;
            if (StringUtils.hasText(operation.getKeyGenerator())) {
                operationKeyGenerator = getBean(operation.getKeyGenerator(), KeyGenerator.class);
            }
            else {
                operationKeyGenerator = getKeyGenerator();
            }
            CacheResolver operationCacheResolver;
            if (StringUtils.hasText(operation.getCacheResolver())) {
                operationCacheResolver = getBean(operation.getCacheResolver(), CacheResolver.class);
            }
            else if (StringUtils.hasText(operation.getCacheManager())) {
                CacheManager cacheManager = getBean(operation.getCacheManager(), CacheManager.class);
                operationCacheResolver = new SimpleCacheResolver(cacheManager);
            }
            else {
                operationCacheResolver = getCacheResolver();
                Assert.state(operationCacheResolver != null, "No CacheResolver/CacheManager set");
            }
            metadata = new CacheOperationMetadata(operation, method, targetClass,
                    operationKeyGenerator, operationCacheResolver);
            this.metadataCache.put(cacheKey, metadata);
        }
        return metadata;
    }

    // TODO 定义上下文类集合
    private class CacheOperationContexts {

        private final MultiValueMap<Class<? extends CacheOperation>, CacheOperationContext> contexts;

        private final boolean sync;

        public CacheOperationContexts(Collection<? extends CacheOperation> operations, Method method,
                                      Object[] args, Object target, Class<?> targetClass) {

            this.contexts = new LinkedMultiValueMap<>( operations.size() );
            for (CacheOperation op : operations) {
                this.contexts.add( op.getClass(), getOperationContextCus( op, method, args, target, targetClass ) );
            }
            this.sync = determineSyncFlag( method );
        }

        public Collection<CacheOperationContext> get(Class<? extends CacheOperation> operationClass) {
            Collection<CacheOperationContext> result = this.contexts.get( operationClass );
            return (result != null ? result : Collections.emptyList());
        }

        public boolean isSynchronized() {
            return this.sync;
        }

        private boolean determineSyncFlag(Method method) {
            List<CacheOperationContext> cacheOperationContexts = this.contexts.get( CacheableOperation.class );
            if (cacheOperationContexts == null) {  // no @Cacheable operation at all
                return false;
            }
            boolean syncEnabled = false;
            for (CacheOperationContext cacheOperationContext : cacheOperationContexts) {
                if (((CacheableOperation) cacheOperationContext.getOperation()).isSync()) {
                    syncEnabled = true;
                    break;
                }
            }
            if (syncEnabled) {
                if (this.contexts.size() > 1) {
                    throw new IllegalStateException(
                            "@Cacheable(sync=true) cannot be combined with other cache operations on '" + method + "'" );
                }
                if (cacheOperationContexts.size() > 1) {
                    throw new IllegalStateException(
                            "Only one @Cacheable(sync=true) entry is allowed on '" + method + "'" );
                }
                CacheOperationContext cacheOperationContext = cacheOperationContexts.iterator().next();
                CacheableOperation operation = (CacheableOperation) cacheOperationContext.getOperation();
                if (cacheOperationContext.getCaches().size() > 1) {
                    throw new IllegalStateException(
                            "@Cacheable(sync=true) only allows a single cache on '" + operation + "'" );
                }
                if (StringUtils.hasText( operation.getUnless() )) {
                    throw new IllegalStateException(
                            "@Cacheable(sync=true) does not support unless attribute on '" + operation + "'" );
                }
                return true;
            }
            return false;
        }
    }

    //TODO 定义CachePutRequest请求
    private class CachePutRequest {

        private final CacheOperationContext context;

        private final Object key;

        public CachePutRequest(CacheOperationContext context, Object key) {
            this.context = context;
            this.key = key;
        }

        public void apply(@Nullable Object result) {
            if (this.context.canPutToCache( result )) {
                for (Cache cache : this.context.getCaches()) {
                    doPut( cache, this.key, result );
                }
            }
        }
    }

    protected static class CacheOperationMetadata {

        private final CacheOperation operation;

        private final Method method;

        private final Class<?> targetClass;

        private final Method targetMethod;

        private final AnnotatedElementKey methodKey;

        private final KeyGenerator keyGenerator;

        private final CacheResolver cacheResolver;

        public CacheOperationMetadata(CacheOperation operation, Method method, Class<?> targetClass,
                                      KeyGenerator keyGenerator, CacheResolver cacheResolver) {

            this.operation = operation;
            this.method = BridgeMethodResolver.findBridgedMethod( method );
            this.targetClass = targetClass;
            this.targetMethod = (!Proxy.isProxyClass( targetClass ) ?
                    AopUtils.getMostSpecificMethod( method, targetClass ) : this.method);
            this.methodKey = new AnnotatedElementKey( this.targetMethod, targetClass );
            this.keyGenerator = keyGenerator;
            this.cacheResolver = cacheResolver;
        }
    }


    protected class CacheOperationContext implements CacheOperationInvocationContext<CacheOperation> {

        private final CacheOperationMetadata metadata;

        private final Object[] args;

        private final Object target;

        private final Collection<? extends Cache> caches;

        private final Collection<String> cacheNames;

        @Nullable
        private Boolean conditionPassing;

        public CacheOperationContext(CacheOperationMetadata metadata, Object[] args, Object target) {
            this.metadata = metadata;
            this.args = extractArgs( metadata.method, args );
            this.target = target;
            this.caches = AbstractCustomCacheAspectSupport.this.getCaches( this, metadata.cacheResolver );
            this.cacheNames = createCacheNames( this.caches );
        }

        @Override
        public CacheOperation getOperation() {
            return this.metadata.operation;
        }

        @Override
        public Object getTarget() {
            return this.target;
        }

        @Override
        public Method getMethod() {
            return this.metadata.method;
        }

        @Override
        public Object[] getArgs() {
            return this.args;
        }

        private Object[] extractArgs(Method method, Object[] args) {
            if (!method.isVarArgs()) {
                return args;
            }
            Object[] varArgs = ObjectUtils.toObjectArray( args[args.length - 1] );
            Object[] combinedArgs = new Object[args.length - 1 + varArgs.length];
            System.arraycopy( args, 0, combinedArgs, 0, args.length - 1 );
            System.arraycopy( varArgs, 0, combinedArgs, args.length - 1, varArgs.length );
            return combinedArgs;
        }

        protected boolean isConditionPassing(@Nullable Object result) {
            if (this.conditionPassing == null) {
                if (StringUtils.hasText( this.metadata.operation.getCondition() )) {
                    EvaluationContext evaluationContext = createEvaluationContext( result );
                    this.conditionPassing = evaluator.condition( this.metadata.operation.getCondition(),
                            this.metadata.methodKey, evaluationContext );
                } else {
                    this.conditionPassing = true;
                }
            }
            return this.conditionPassing;
        }

        protected boolean canPutToCache(@Nullable Object value) {
            String unless = "";
            if (this.metadata.operation instanceof CacheableOperation) {
                unless = ((CacheableOperation) this.metadata.operation).getUnless();
            } else if (this.metadata.operation instanceof CachePutOperation) {
                unless = ((CachePutOperation) this.metadata.operation).getUnless();
            }
            if (StringUtils.hasText( unless )) {
                EvaluationContext evaluationContext = createEvaluationContext( value );
                return !evaluator.unless( unless, this.metadata.methodKey, evaluationContext );
            }
            return true;
        }

        /**
         * Compute the key for the given caching operation.
         */
        @Nullable
        protected Object generateKey(@Nullable Object result) {
            if (StringUtils.hasText( this.metadata.operation.getKey() )) {
                EvaluationContext evaluationContext = createEvaluationContext( result );
                return evaluator.key( this.metadata.operation.getKey(), this.metadata.methodKey, evaluationContext );
            }
            return this.metadata.keyGenerator.generate( this.target, this.metadata.method, this.args );
        }

        private EvaluationContext createEvaluationContext(@Nullable Object result) {
            return evaluator.createEvaluationContext( this.caches, this.metadata.method, this.args,
                    this.target, this.metadata.targetClass, this.metadata.targetMethod, result, beanFactory );
        }

        protected Collection<? extends Cache> getCaches() {
            return this.caches;
        }

        protected Collection<String> getCacheNames() {
            return this.cacheNames;
        }

        private Collection<String> createCacheNames(Collection<? extends Cache> caches) {
            Collection<String> names = new ArrayList<>();
            for (Cache cache : caches) {
                names.add( cache.getName() );
            }
            return names;
        }
    }
    private static final class CacheOperationCacheKey implements Comparable<CacheOperationCacheKey> {

        private final CacheOperation cacheOperation;

        private final AnnotatedElementKey methodCacheKey;

        private CacheOperationCacheKey(CacheOperation cacheOperation, Method method, Class<?> targetClass) {
            this.cacheOperation = cacheOperation;
            this.methodCacheKey = new AnnotatedElementKey(method, targetClass);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof CacheOperationCacheKey)) {
                return false;
            }
           CacheOperationCacheKey otherKey = (CacheOperationCacheKey) other;
            return (this.cacheOperation.equals(otherKey.cacheOperation) &&
                    this.methodCacheKey.equals(otherKey.methodCacheKey));
        }

        @Override
        public int hashCode() {
            return (this.cacheOperation.hashCode() * 31 + this.methodCacheKey.hashCode());
        }

        @Override
        public String toString() {
            return this.cacheOperation + " on " + this.methodCacheKey;
        }

        @Override
        public int compareTo(CacheOperationCacheKey other) {
            int result = this.cacheOperation.getName().compareTo(other.cacheOperation.getName());
            if (result == 0) {
                result = this.methodCacheKey.compareTo(other.methodCacheKey);
            }
            return result;
        }
    }
}

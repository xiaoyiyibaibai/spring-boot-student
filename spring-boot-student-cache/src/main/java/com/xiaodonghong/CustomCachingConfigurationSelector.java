package com.xiaodonghong;


import com.xiaodonghong.annotations.CustomEnableCaching;
import org.springframework.cache.annotation.CachingConfigurationSelector;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.AdviceModeImportSelector;
import org.springframework.context.annotation.AutoProxyRegistrar;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName CustomCachingConfigurationSelector
 * @Description  selector信息
 * @Author renhao
 * @Date 2019/7/29 10:45
 **/
public class CustomCachingConfigurationSelector  extends AdviceModeImportSelector<CustomEnableCaching> {

   private static final String PROXY_JCACHE_CONFIGURATION_CLASS =
           "org.springframework.cache.jcache.config.ProxyJCacheConfiguration";

    private static final String CACHE_ASPECT_CONFIGURATION_CLASS_NAME =
            "org.springframework.cache.aspectj.AspectJCachingConfiguration";

    private static final String JCACHE_ASPECT_CONFIGURATION_CLASS_NAME =
            "org.springframework.cache.aspectj.AspectJJCacheConfiguration";

    private static final boolean jsr107Present;

    private static final boolean jcacheImplPresent;

    static {
        ClassLoader classLoader = CustomCachingConfigurationSelector.class.getClassLoader();
        jsr107Present = ClassUtils.isPresent( "javax.cache.Cache", classLoader );
        jcacheImplPresent = ClassUtils.isPresent( PROXY_JCACHE_CONFIGURATION_CLASS,classLoader );
    }

    @Override
    protected String[] selectImports(AdviceMode adviceMode) {
        switch (adviceMode){
            case PROXY:
                return getProxyImports();
            case ASPECTJ:
                return  getAspectJImports();
             default:
                 return null;
        }
    }

    private String [] getProxyImports(){
        List<String> result = new ArrayList<>( 3 );
        result.add( AutoProxyRegistrar.class.getName() );
        result.add( CustomProxyCachingConfiguration.class.getName() );
        if (jsr107Present&&jcacheImplPresent){
            result.add( PROXY_JCACHE_CONFIGURATION_CLASS );
        }
        return StringUtils.toStringArray( result );
    }

    private String [] getAspectJImports(){
        List<String> result = new ArrayList<>(2);
        result.add( CACHE_ASPECT_CONFIGURATION_CLASS_NAME );
        if (jsr107Present&&jcacheImplPresent){
            result.add( JCACHE_ASPECT_CONFIGURATION_CLASS_NAME );
        }
        return StringUtils.toStringArray(result);
    }

}

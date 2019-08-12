package com.xiaodonghong;

import com.xiaodonghong.CustomCacheInterceptor;
import com.xiaodonghong.customcacheconfig.CustomBeanFactoryCacheOperationSourceAdvisor;
import com.xiaodonghong.domain.CustomAnnotationCacheOperationSource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.cache.annotation.AbstractCachingConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.config.CacheManagementConfigUtils;
import org.springframework.cache.interceptor.BeanFactoryCacheOperationSourceAdvisor;
import org.springframework.cache.interceptor.CacheOperationSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.util.HashMap;

/**
 * @ClassName CustomProxyCachingConfiguration
 * @Description 配置文件注入CustomCacheInterceptor類
 * @Author renhao
 * @Date 2019/7/29 10:59
 **/
@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class CustomProxyCachingConfiguration extends AbstractCachingConfiguration {

    @Bean(name = CacheManagementConfigUtils.CACHE_ADVISOR_BEAN_NAME)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public CustomBeanFactoryCacheOperationSourceAdvisor cacheAdvisor() {
        // TODO 注入拦截
        CustomBeanFactoryCacheOperationSourceAdvisor advisor = new CustomBeanFactoryCacheOperationSourceAdvisor();
        //TODO 定义拦截注解
        advisor.setCacheOperationSource(cacheOperationSource());
        //TODO 定义了 拦截处理
        advisor.setAdvice(cacheInterceptor());
        if (this.enableCaching != null) {
            advisor.setOrder(this.enableCaching.<Integer>getNumber("order"));
        }


        return advisor;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public CacheOperationSource cacheOperationSource() {
        // TODO 定义拦截和处理的注解信息。其中有一个 CustomSpringCacheAnnotationParser 用于定义拦截的接口，和处理拦截
        return new CustomAnnotationCacheOperationSource();
    }

    /**
     * @Author xiaodongohong
     * @Description 自动注入拦截器
     * @Date 11:04 2019/7/29
     * @Param []
     * @return org.springframework.cache.interceptor.CacheInterceptor
     **/
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public CustomCacheInterceptor cacheInterceptor() {
        CustomCacheInterceptor interceptor = new CustomCacheInterceptor();
        //TODO 设置错误处理， key生成机制， cacheresolver 缓存解析， 和 CacheManager，信息
        // TODO 使用默认的errorHandler，SimpleKeyGenerator，
        interceptor.configure(this.errorHandler, this.keyGenerator, this.cacheResolver, this.cacheManager);
        // TODO 拦截处理中，需要的拦截注解一起 其拦截信息
        interceptor.setCacheOperationSource(cacheOperationSource());
        return interceptor;
    }

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        //TODO 定义了有这个注解才能生效缓存
        this.enableCaching = AnnotationAttributes.fromMap(
                importMetadata.getAnnotationAttributes( CustomEnableCaching.class.getName(), false));
        if (this.enableCaching == null) {
            throw new IllegalArgumentException(
                    "@EnableCaching is not present on importing class " + importMetadata.getClassName());
        }
    }
}
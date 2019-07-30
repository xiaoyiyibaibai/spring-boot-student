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
        advisor.setCacheOperationSource(cacheOperationSource());
        advisor.setAdvice(cacheInterceptor());
        if (this.enableCaching != null) {
            advisor.setOrder(this.enableCaching.<Integer>getNumber("order"));
        }


        return advisor;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public CacheOperationSource cacheOperationSource() {
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
        interceptor.configure(this.errorHandler, this.keyGenerator, this.cacheResolver, this.cacheManager);
        interceptor.setCacheOperationSource(cacheOperationSource());
        return interceptor;
    }

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.enableCaching = AnnotationAttributes.fromMap(
                importMetadata.getAnnotationAttributes( CustomEnableCaching.class.getName(), false));
        if (this.enableCaching == null) {
            throw new IllegalArgumentException(
                    "@EnableCaching is not present on importing class " + importMetadata.getClassName());
        }
    }
}
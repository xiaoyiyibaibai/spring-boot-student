package com.xiaodonghong.customcacheconfig;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;
import org.springframework.cache.interceptor.CacheOperationSource;
import org.springframework.lang.Nullable;

/**
 * @ClassName CustomBeanFactoryCacheOperationSourceAdvisor
 * @Description TODO
 * @Author renhao
 * @Date 2019/7/29 18:28
 **/
public class CustomBeanFactoryCacheOperationSourceAdvisor extends AbstractBeanFactoryPointcutAdvisor {
    @Nullable
    private CacheOperationSource cacheOperationSource;

    private final CacheOperationSourcePointcut pointcut = new CacheOperationSourcePointcut() {
        @Override
        @Nullable
        protected CacheOperationSource getCacheOperationSource() {
            return cacheOperationSource;
        }
    };

    public void setCacheOperationSource(CacheOperationSource cacheOperationSource) {
        this.cacheOperationSource = cacheOperationSource;
    }


    public void setClassFilter(ClassFilter classFilter) {
      this.pointcut.setClassFilter(classFilter);
    }

    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }
}

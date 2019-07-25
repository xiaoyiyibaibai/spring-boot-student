package com.xiaolyuh.config;

import com.xiaolyuh.component.TestService;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @ClassName StartApplicationReadyEventListener
 * @Description 必须添加@Component，否则不生效
 * @Author renhao
 * @Date 2019/7/24 16:15
 **/
@Component
public class StartApplicationReadyEventListener
        implements ApplicationListener<ApplicationReadyEvent>, ApplicationContextAware {
    private ApplicationContext applicationContext;
    private TestService testService;
    public StartApplicationReadyEventListener(TestService testService) {
        this.testService = testService;
    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        testService.showTest();



    }
}

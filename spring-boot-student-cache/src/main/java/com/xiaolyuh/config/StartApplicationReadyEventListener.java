package com.xiaolyuh.config;

import com.xiaolyuh.entity.Person;
import com.xiaolyuh.service.PersonService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

/**
 * @ClassName StartApplicationreadyEventListener
 * @Description TODO
 * @Author renhao
 * @Date 2019/7/25 15:57
 **/
@Component
public class StartApplicationReadyEventListener implements ApplicationListener<ApplicationReadyEvent>, ApplicationContextAware {
    private ApplicationContext applicationContext;
    private PersonService personService;
    private CacheManager cacheManager;
    private StringRedisTemplate stringRedisTemplate;
    private ConcurrentHashMap<String, Object> concurrentHashMap = new ConcurrentHashMap<>(  );
    @Autowired
    public StartApplicationReadyEventListener(PersonService personService,StringRedisTemplate stringRedisTemplate){
        this.personService = personService;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        //启动的时候读取缓存信息
        doSchedule();
    }
    private void doSchedule(){

        ScheduledExecutorService executor = Executors.newScheduledThreadPool( 5 );;
        executor.scheduleAtFixedRate(()->{
            // 清理缓存，并重新读取缓存
            cacheManager.getCache( "people" ).clear();
            cacheManager.getCache( "peoples" ).clear();
            try {
                TimeUnit.SECONDS.sleep( 300 );
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            List<Person> personList = personService .findAll();
            personList.stream().forEach( person -> {
                System.out.println("执行遍历project的每一个project进行处理！！");
                personService.findById( person.getId() );
            } );
        }, 0,40, TimeUnit.SECONDS );

     //   executor.execute(  );
    }
}

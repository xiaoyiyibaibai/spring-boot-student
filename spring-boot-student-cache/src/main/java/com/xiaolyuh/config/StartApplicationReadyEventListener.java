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

import java.nio.channels.Pipe;
import java.util.Date;
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
    private ConcurrentHashMap<String, Object> concurrentHashMap = new ConcurrentHashMap<>();

    @Autowired
    public StartApplicationReadyEventListener(PersonService personService, StringRedisTemplate stringRedisTemplate,CacheManager cacheManager) {
        this.personService = personService;
        this.stringRedisTemplate = stringRedisTemplate;
        this.cacheManager = cacheManager;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        //启动的时候读取缓存信息
        if(cacheManager!=null) {
            testStr();
        }
    }

//    private void doSchedule() {
//
//        ScheduledExecutorService executor = Executors.newScheduledThreadPool( 5 );
//        executor.scheduleAtFixedRate( () -> {
//            // 清理缓存，并重新读取缓存
//            cacheManager.getCache( "people" ).clear();
//            cacheManager.getCache( "peoples" ).clear();
//            try {
//                TimeUnit.SECONDS.sleep( 300 );
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            List<Person> personList = personService.findAll();
//            personList.stream().forEach( person -> {
//                System.out.println( "执行遍历project的每一个project进行处理！！" );
//                personService.findById( person.getId() );
//            } );
//        }, 0, 40, TimeUnit.SECONDS );
//
//        //   executor.execute(  );
//    }


    public void testStr() {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        // 参数：1、任务体
        //    2、首次执行的延时时间
        //      3、任务执行间隔
        // 4、间隔时间单
        service.scheduleAtFixedRate( new CacheTask(cacheManager,personService), 0, 60, TimeUnit.MINUTES );
    }

    class CacheTask implements Runnable {
        private Object object = new Object();
        private CacheManager cacheManager;
        private PersonService personService;

        public CacheTask() {
        }

        public CacheTask(CacheManager cacheManager, PersonService personService) {
            this.cacheManager = cacheManager;
            this.personService = personService;
        }

        @Override
        public void run() {
            synchronized (object) {
                System.out.println( "start 我是肖東紅的定時任務執行" + new Date() );
              //  啟動做緩存
              //  this.cacheManager.getCache( "people" ).clear();
              //  this.cacheManager.getCache( "peoples" ).clear();
                System.out.println( "end 我是肖東紅的定時任務執行" + new Date() );
            }

//            // 清理缓存，并重新读取缓存
//            cacheManager.getCache( "people" ).clear();
//            cacheManager.getCache( "peoples" ).clear();
//            try {
//                TimeUnit.SECONDS.sleep( 300 );
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            List<Person> personList = personService.findAll();
//            personList.stream().forEach( person -> {
//                System.out.println( "执行遍历project的每一个project进行处理！！" );
//                personService.findById( person.getId() );
//            } );
        }
    }
}

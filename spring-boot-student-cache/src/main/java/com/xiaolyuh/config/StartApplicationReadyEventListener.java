package com.xiaolyuh.config;

import com.xiaolyuh.entity.Person;
import com.xiaolyuh.service.PersonService;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

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
    public StartApplicationReadyEventListener(PersonService personService){
        this.personService = personService;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        List<Person> personList = personService .findAll();
        personList.stream().forEach( person -> {
            personService.findById( person.getId() );
        } );
    }
}

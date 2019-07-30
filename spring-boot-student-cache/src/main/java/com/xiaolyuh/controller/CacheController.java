package com.xiaolyuh.controller;

import com.xiaolyuh.entity.Person;
import com.xiaolyuh.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@RestController
public class CacheController {

    @Autowired
    PersonService personService;

    @RequestMapping("/put")
    public long put(@RequestBody Person person) {
        Person p = personService.save(person);
        return p.getId();
    }

    @RequestMapping("/find/{id}")
    public Person cacheable(@PathVariable Long id) {
        return personService.findById( id );
    }
    @RequestMapping("/find2/{id}")
    public Person cacheable2(@PathVariable Long id) {
        return personService.findById2( id );
    }

    @RequestMapping("/all")
    public List<Person> cacheable() {
        return personService.findAll();
    }

    @RequestMapping("/alls")
    public List<Person> cacheables() {
        List<Person> personList2 = new ArrayList<>(  );
        List<Person> personList = personService.findAll();
        for (Object temp:
             personList) {
            Long id;
            if (temp instanceof HashMap){
                HashMap hashMap = (HashMap)temp;
                id = Long.valueOf(  (Integer) hashMap.get( "id" ) );
            }else {
                id = ((Person)temp).getId();
            }

            Object person =  personService.findById( id);
            if (person instanceof Person) {
                personList2.add( (Person)person );
            }
        }
        return  personList2;
    }

    @RequestMapping("/evit")
    public String evit(Long id) {

        personService.remove(id);
        return "ok";
    }

}
package com.xiaolyuh.service.impl;

import com.xiaodonghong.annotations.CustomCacheEvict;
import com.xiaodonghong.annotations.CustomCachePut;
import com.xiaodonghong.annotations.CustomCacheable;
import com.xiaolyuh.entity.Person;
import com.xiaolyuh.repository.PersonRepository;
import com.xiaolyuh.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PersonServiceImpl implements PersonService {
    @Autowired
    PersonRepository personRepository;

    @Override
    @CustomCachePut(value = "custompeople", key = "#person.id")
    public Person save(Person person) {
        Person p = personRepository.save(person);
        System.out.println("为id、key为:" + p.getId() + "数据做了缓存");
        return p;
    }

    @Override
    @CustomCacheEvict(value = "custompeople")//2
    public void remove(Long id) {
        System.out.println("删除了id、key为" + id + "的数据缓存");
        //这里不做实际删除操作
    }

    @Override
    @CustomCacheable(value = "custompeople", key = "#person.id")//3
    public Person findOne(Person person) {
        Optional<Person>  p = personRepository.findById(person.getId());
        System.out.println("为id、key为:" + person.getId() + "数据做了缓存");
        return p.get();
    }

    @Override
    @CustomCacheable(value = "custompeople", key = "#id")//3
    public Person findById(Long id) {
        Optional<Person> p = personRepository.findById(id);
        System.out.println("为id、key为:" + id + "数据做了缓存");
        return p.get();
    }

    @Override
    @CustomCacheable(value = "custompeoples")//3
    public List<Person> findAll() {
       return personRepository.findAll();
    }

    @Override
    public Person findById2(Long id) {
        Optional<Person> p = personRepository.findById(id);
        System.out.println("为id、key为:" + id + "数据做了缓存");
        return p.get();
    }
}

package com.xiaolyuh.service;

import com.xiaolyuh.entity.Person;

import java.util.List;


public interface PersonService {
    Person save(Person person);

    void remove(Long id);

    Person findOne(Person person);
    public Person findById(Long id);
    public List<Person> findAll();
}

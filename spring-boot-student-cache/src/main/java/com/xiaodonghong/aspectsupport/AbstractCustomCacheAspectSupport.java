package com.xiaodonghong.aspectsupport;

import java.io.IOException;
import java.lang.reflect.Method;

import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaolyuh.entity.Person;
import org.springframework.cache.interceptor.CacheAspectSupport;
import org.springframework.cache.interceptor.CacheOperationInvoker;


public abstract class AbstractCustomCacheAspectSupport extends CacheAspectSupport {

   @Override
    public Object execute(CacheOperationInvoker invoker, Object target, Method method, Object[] args) {
       Object result =   super.execute( invoker,target,method,args );
       ObjectMapper objectMapper = new ObjectMapper();
       Class returnType = method.getReturnType();
       if (returnType.isAssignableFrom( Collection.class )) {

       } else if (returnType.isAssignableFrom( HashMap.class )) {

       } else {
       }
       try {
           String json = objectMapper.writeValueAsString( result );
           boolean flag = json.startsWith( "[" ) && json.endsWith( "]" );
           if (flag) {
               return objectMapper.readValue( json, new TypeReference<List<Person>>() {
               } );
           }
           boolean flag2 = json.startsWith( "{" ) && json.endsWith( "}" );
           if (flag2){
               return objectMapper.readValue( json, new TypeReference<Person>() {
               });
           }
       } catch (JsonProcessingException e) {
           e.printStackTrace();
       } catch (IOException e) {
           e.printStackTrace();
       } catch (Exception e) {
           e.printStackTrace();
       }
       return result;
    }

   
}

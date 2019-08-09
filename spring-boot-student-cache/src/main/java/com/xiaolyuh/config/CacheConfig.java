package com.xiaolyuh.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaolyuh.entity.Person;
import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.cache.RedisCacheConfiguration;
//import org.springframework.data.redis.cache.RedisCacheManager;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
//import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
//import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.io.IOException;

/**
 * @ClassName CacheConfig
 * @Description TODO
 * @Author renhao
 * @Date 2019/7/25 16:24
 **/
@Configuration
public class CacheConfig {


//    @Bean
//    public RedisCacheManager cacheManager(RedisConnectionFactory factory, ObjectMapper objectMapper) {
//        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer( Object.class );
//        jackson2JsonRedisSerializer.setObjectMapper( objectMapper );
//        SimpleModule simpleModule = new SimpleModule();
//        simpleModule.addDeserializer( Person.class, new PersonJsonDeserializeMapper( objectMapper ) );
//
//        objectMapper.registerModule( simpleModule );
//        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig().serializeValuesWith( RedisSerializationContext.SerializationPair.fromSerializer( jackson2JsonRedisSerializer ) );
//        return RedisCacheManager.builder( factory ).cacheDefaults( redisCacheConfiguration ).build();
//    }

    public static class PersonJsonDeserializeMapper extends JsonDeserializer<Person> {

        private final ObjectMapper objectMapper;

        public PersonJsonDeserializeMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public Person deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            TreeNode treeNode = p.readValueAsTree();
            return objectMapper.treeToValue( treeNode, Person.class );
        }
    }

}

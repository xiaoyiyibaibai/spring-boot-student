package com.xiaolyuh.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

/**
 * @ClassName CacheConfig
 * @Description TODO
 * @Author renhao
 * @Date 2019/7/25 16:24
 **/
@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory, ObjectMapper objectMapper) {
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer( Object.class );
        jackson2JsonRedisSerializer.setObjectMapper( objectMapper );
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig().serializeValuesWith( RedisSerializationContext.SerializationPair.fromSerializer( jackson2JsonRedisSerializer ) );
        return RedisCacheManager.builder( factory ).cacheDefaults( redisCacheConfiguration ).build();
    }

}

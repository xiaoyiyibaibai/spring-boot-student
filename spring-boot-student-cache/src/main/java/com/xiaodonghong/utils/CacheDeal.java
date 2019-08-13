package com.xiaodonghong.utils;

import org.springframework.data.redis.core.RedisTemplate;

/**
 * @ClassName CacheDeal
 * @Description TODO
 * @Author renhao
 * @Date 2019/8/13 11:51
 **/
public class CacheDeal {
    /**
     * 刷新缓存数据
     */
//    private void refreshCache(Object key, String cacheKeyStr) {
//        Long ttl = this.redisOperations.getExpire(cacheKeyStr);
//        if (null != ttl && ttl <= CustomizedRedisCache.this.preloadSecondTime) {
//            // 尽量少的去开启线程，因为线程池是有限的
//            ThreadTaskUtils.run(new Runnable() {
//                @Override
//                public void run() {
//                    // 加一个分布式锁，只放一个请求去刷新缓存
//                    RedisLock redisLock = new RedisLock((RedisTemplate) redisOperations, cacheKeyStr + "_lock");
//                    try {
//                        if (redisLock.lock()) {
//                            // 获取锁之后再判断一下过期时间，看是否需要加载数据
//                            Long ttl = CustomizedRedisCache.this.redisOperations.getExpire(cacheKeyStr);
//                            if (null != ttl && ttl <= CustomizedRedisCache.this.preloadSecondTime) {
//                                // 通过获取代理方法信息重新加载缓存数据
//                                CustomizedRedisCache.this.getCacheSupport().refreshCacheByKey(CustomizedRedisCache.super.getName(), cacheKeyStr);
//                            }
//                        }
//                    } catch (Exception e) {
//                        logger.info(e.getMessage(), e);
//                    } finally {
//                        redisLock.unlock();
//                    }
//                }
//            });
//        }
//    }

}

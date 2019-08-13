package com.xiaolyuh;

import com.xiaodonghong.annotations.CustomEnableCaching;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// 开启缓存，需要显示的指定
// @EnableCaching
@CustomEnableCaching
public class SpringBootStudentCacheApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootStudentCacheApplication.class, args);
    }
}

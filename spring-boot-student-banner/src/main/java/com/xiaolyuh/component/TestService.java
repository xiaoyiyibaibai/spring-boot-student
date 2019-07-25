package com.xiaolyuh.component;

import org.springframework.stereotype.Service;

/**
 * @ClassName TestService
 * @Description TODO
 * @Author renhao
 * @Date 2019/7/24 16:18
 **/
@Service
public class TestService {
    public void showTest(){
        System.out.println(this.getClass()+"show");
    }
}

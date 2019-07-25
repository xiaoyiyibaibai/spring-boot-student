package com.xiaolyuh.controller;

import com.xiaolyuh.component.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName TestController
 * @Description TODO
 * @Author renhao
 * @Date 2019/7/24 16:18
 **/
@RestController
public class TestController {
    @Autowired
    TestService testService;
    @RequestMapping(value = "/test/hello")
    public void show(){
        testService.showTest();
    }
}

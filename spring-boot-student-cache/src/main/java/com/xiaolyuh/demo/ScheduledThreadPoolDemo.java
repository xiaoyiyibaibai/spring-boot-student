package com.xiaolyuh.demo;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledThreadPoolDemo {

    public static void main(String[] args) {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);
        /**
         * 1,executorService.scheduleAtFixedRate:创建一个周期性任务，从上个任务开始，过period周期执行下一个（如果执行时间>period，则以执行时间为周期）
         * 2,executorService.scheduleWithFixedDelay：创建一个周期上午，从上个任务结束，过period周期执行下一个。
         */
        //如果前边任务没有完成则调度也不会启动
        executorService.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(1000);
                    System.out.println("当前时间：" + System.currentTimeMillis()/1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },0,2, TimeUnit.SECONDS);
    }
}
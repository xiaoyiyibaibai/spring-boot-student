package com.xiaolyuh.filewatch;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

/**
 * @ClassName TestWatchService
 * @Description TODO
 * @Author renhao
 * @Date 2019/7/30 9:27
 **/
public class TestWatchService {
    public static void main(String[] args) throws IOException {
        String file_path = "D:/yanshi";
        WatchService watchService = FileSystems.getDefault().newWatchService();
        Path p = Paths.get(file_path);
        p.register( watchService,StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_CREATE);

        Thread thread = new Thread( ()->{
            try {
                while(true){
                    WatchKey watchKey = watchService.take();
                    List<WatchEvent<?>> watchEvents = watchKey.pollEvents();
                    for(WatchEvent<?> event : watchEvents){
                        //TODO 根据事件类型采取不同的操作。。。。。。。
                        System.out.println("["+file_path+"/"+event.context()+"]文件发生了["+event.kind()+"]事件");
                    }
                    watchKey.reset();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } );
        thread.setDaemon( false );
        thread.start();
        Runtime.getRuntime().addShutdownHook(new Thread( ()->{
            try {
                watchService.close();
            } catch (Exception e) {
            }
        } )  );
    }
}

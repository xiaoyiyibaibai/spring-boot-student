package com.xiaolyuh.filewatch;

import java.nio.file.Path;

/**
 * @ClassName FileListener
 * @Description 文件解析監聽接口，監聽器根據文件操作類型調用該接口的業務實現類執行處理
 * @Author renhao
 * @Date 2019/7/30 9:02
 **/
public interface FileListener {
    /**
     * @Author xiaodongohong
     * @Description 在監視目錄中新增文件時的處理操作
     * @Date 9:05 2019/7/30
     * @Param [file]
     * @return void
     **/
    void onCreate(Path file);
    /**
     * @Author xiaodongohong
     * @Description 在監視目錄中修改文件時的處理操作
     * @Date 9:05 2019/7/30
     * @Param [path]
     * @return void
     **/
    void onModify(Path file);
    /**
     * @Author xiaodongohong
     * @Description 在監視目錄中刪除文件時的處理操作
     * @Date 9:06 2019/7/30
     * @Param [file]
     * @return void
     **/
    void onDelete(Path file);
}

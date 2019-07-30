package com.xiaolyuh.filewatch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @ClassName FileDemo
 * @Description TODO
 * @Author renhao
 * @Date 2019/7/30 9:08
 **/
public class FileDemo {
    public static void main(String[] args) throws IOException {
        Path path = Paths.get(".");
        //  Files.list() 遍历文件和目录
        Files.list( path ).forEach( System.out::println );
        // Files.newDirectoryStream() 遍历文件和目录
        Files.newDirectoryStream(path).forEach(System.out::println);
     // Files::isReularFile 找出目录中的文件
      Files.list( Paths.get( "." ) ).filter( Files::isRegularFile ).forEach( System.out::println );
   //file->file.isHidden() 找出隐藏文件
        final File[] files = new File(".").listFiles(file->file.isHidden());
        for(File file:files){
            System.out.println(file.getName());
        }
     //Files.newBufferedWriter 迅速创建一个BufferedWriter，可以使编码语法更简洁
        Path path2 = Paths.get( "D:\\test.txt" );
        try(BufferedWriter writer = Files.newBufferedWriter( path2 )) {
            writer.write( "hello world!" );
        }catch (IOException e){
            e.printStackTrace();
        }

        try {
            Files.write(Paths.get("D:\\test1.txt"),"Hello".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }



    }
}

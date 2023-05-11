package com.xuecheng.media;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * ClassName: BigFileTest
 * Package: com.xuecheng.media
 * Description:
 *
 * @Author: XKing
 * @Create: 2023/5/11 - 15:13
 * @Version: 1.0
 */
public class BigFileTest {
    @Test
    public void testChunk() throws IOException {
        File sourseFile = new File("D:\\xuecheng-media\\Day1-00.项目导学.mp4");
        String chunkFilePath="D:\\xuecheng-media\\chunk\\";
        int chunkSize=1024 * 1024 * 1;
        int chunkNum=(int)Math.ceil(sourseFile.length()*1.0/chunkSize);
        byte[] bytes=new byte[1024];
        RandomAccessFile raf_r = new RandomAccessFile(sourseFile, "r");

        for (int i = 0; i < chunkNum; i++) {
            //创建分块文件
            File chunkFile = new File(chunkFilePath + i);
            //分块文件写入流
            RandomAccessFile raf_rw = new RandomAccessFile(chunkFile, "rw");
            int len=-1;
            while((len=raf_r.read(bytes))!=-1){
                raf_rw.write(bytes,0,len);
                if(chunkFile.length()>=chunkSize){
                    break;
                }
            }
        }
    }


    @Test
    public void testMerge() throws IOException {
        File sourseFile = new File("D:\\xuecheng-media\\Day1-00.项目导学.mp4");
        File distFile = new File("D:\\xuecheng-media\\Day1-00.项目导学_1.mp4");
        String chunkFilePath="D:\\xuecheng-media\\chunk";
        File chunkFiles = new File(chunkFilePath);
        File[] files = chunkFiles.listFiles();
        List<File> fileList = Arrays.asList(files);
        Collections.sort(fileList,(File f1,File f2)->{
            return Integer.parseInt(f1.getName())-Integer.parseInt(f2.getName());
        });
        RandomAccessFile raf_rw = new RandomAccessFile(distFile, "rw");
        for(File chunkFile:files){
            RandomAccessFile raf_r = new RandomAccessFile(chunkFile, "r");
            byte[] bytes=new byte[1024];
            int len=-1;
            while((len=raf_r.read(bytes))!=-1){
                raf_rw.write(bytes,0,len);
            }
        }
        String sourseMd5 = DigestUtils.md5Hex(new FileInputStream(sourseFile));
        String distMd5 = DigestUtils.md5Hex(new FileInputStream(distFile));
        if(sourseMd5.equals(distMd5)){
            System.out.println("合并成功！");
        }


    }
}

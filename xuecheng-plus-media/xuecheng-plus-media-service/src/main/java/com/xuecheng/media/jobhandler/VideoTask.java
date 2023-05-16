package com.xuecheng.media.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: VideoTask
 * Package: com.xuecheng.media.jobhandler
 * Description:
 *
 * @Author: XKing
 * @Create: 2023/5/14 - 16:30
 * @Version: 1.0
 */
@Component
@Slf4j
public class VideoTask {
    @Autowired
    MediaFileProcessService mediaFileProcessService;

    @Autowired
    MediaProcessHistoryMapper mediaProcessHistoryMapper;

    @Autowired
    MediaFileService mediaFileService;

    //从nacos获取配置信息
    @Value("${videoprocess.ffmpegpath}")
    String ffmpegpath;

    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws InterruptedException {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        List<MediaProcess> mediaProcessList = null;
        int size = 0;
        try {
            //取出cpu核心数作为一次处理数据的条数
            int processors = Runtime.getRuntime().availableProcessors();
            //一次处理视频数量不要超过cpu核心数
            mediaProcessList = mediaFileProcessService.getMediaProcessList(shardIndex, shardTotal, processors);
            size = mediaProcessList.size();
            log.debug("取出待处理视频任务{}条", size);
            if (size == 0) {
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        //计数器
        CountDownLatch countDownLatch = new CountDownLatch(size);

        ExecutorService executorService = Executors.newFixedThreadPool(size);
        mediaProcessList.forEach(mediaProcess -> {
            try {
                executorService.execute(()->{
                    //任务id
                    Long taskId = mediaProcess.getId();
                    boolean b = mediaFileProcessService.startTask(taskId);
                    if(!b){
                        return;
                    }
                    log.debug("开始执行任务:{}", mediaProcess);

                    //从minio下载avi视频
                    String bucket = mediaProcess.getBucket();

                    String filePath = mediaProcess.getFilePath();

                    String fileId = mediaProcess.getFileId();

                    File orginFile = mediaFileService.downloadFileFromMinIO(bucket, filePath);
                    if(orginFile==null){
                        log.debug("下载待处理文件失败,originalFile:{}", bucket.concat(mediaProcess.getFilePath()));
                        mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", fileId, null, "下载待处理文件失败");
                        return;
                    }
                    //开始转码
                    File mp4File=null;
                    try {
                        mp4File = File.createTempFile("mp4", ".mp4");
                    } catch (IOException e) {
                        log.error("创建mp4临时文件失败");
                        mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", fileId, null, "创建mp4临时文件失败");
                        return;
                    }
                    //视频处理结果
                    String result = "";
                    try {
                        //开始处理视频
                        Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegpath, orginFile.getAbsolutePath(), mp4File.getName(), mp4File.getAbsolutePath());
                        //开始视频转换，成功将返回success
                        result = videoUtil.generateMp4();
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.error("处理视频文件:{},出错:{}", mediaProcess.getFilePath(), e.getMessage());
                    }
                    if (!result.equals("success")) {
                        //记录错误信息
                        log.error("处理视频失败,视频地址:{},错误信息:{}", bucket + filePath, result);
                        mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", fileId, null, result);
                        return;
                    }
                    //将mp4上传至minio
                    //mp4在minio的存储路径
                    String objectName = getFilePath(fileId, ".mp4");
                    //访问url
                    String url = "/" + bucket + "/" + objectName;
                    try {
                        mediaFileService.addMediaFilesToMinIO(mp4File.getAbsolutePath(), "video/mp4", bucket, objectName);
                        //将url存储至数据，并更新状态为成功，并将待处理视频记录删除存入历史
                        mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "2", fileId, url, null);
                    } catch (Exception e) {
                        log.error("上传视频失败或入库失败,视频地址:{},错误信息:{}", bucket + objectName, e.getMessage());
                        //最终还是失败了
                        mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", fileId, null, "处理后视频上传或入库失败");
                    }


                });
            }finally {
                countDownLatch.countDown();
            }

        });
        //等待,给一个充裕的超时时间,防止无限等待，到达超时时间还没有处理完成则结束任务
        countDownLatch.await(30, TimeUnit.MINUTES);
    }
    private String getFilePath(String fileMd5,String fileExt){
        return   fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
    }
}

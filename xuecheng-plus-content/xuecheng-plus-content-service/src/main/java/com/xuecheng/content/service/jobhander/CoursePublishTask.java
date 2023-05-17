package com.xuecheng.content.service.jobhander;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: CoursePublishTask
 * Package: com.xuecheng.content.service.jobhander
 * Description:
 *
 * @Author: XKing
 * @Create: 2023/5/17 - 14:30
 * @Version: 1.0
 */
@Component
@Slf4j
public class CoursePublishTask extends MessageProcessAbstract {
    @Autowired
    CoursePublishService coursePublishService;
    //任务调度入口
    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler(){
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        //调用父类的方法执行任务
        process(shardIndex,shardTotal,"course_publish",30,60);
    }




    /**
     * 执行课程发布的任务
     * @param mqMessage 执行任务内容
     * @return
     */
    @Override
    public boolean execute(MqMessage mqMessage)   {
        String businessKey1 = mqMessage.getBusinessKey1();
        int courseId = Integer.parseInt(businessKey1);
        //课程静态化上传MinIO
        generateCourseHtml(mqMessage,courseId);
        //TODO 向ElasticSearch中写索引数据
        saveCourseIndex(mqMessage,courseId);
        //TODO 向Redis写缓存
        saveCourseCache(mqMessage,courseId);
        //任务完成
        return true;
    }

    private void generateCourseHtml(MqMessage mqMessage,long courseId){
        log.debug("开始进行课程静态化,课程id:{}",courseId);
        //任务幂等性处理
        //消息id
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();

        //查询任务执行状态
        int stageOne = mqMessageService.getStageOne(taskId);
        if(stageOne>0){
            log.debug("课程静态化任务完成，无需处理。。。");
            return;
        }
        File file=coursePublishService.generateCourseHtml(courseId);
        if(file==null){
            throw new XueChengPlusException("生成的静态文件问空");
        }
        try {
            coursePublishService.uploadCourseHtml(courseId,file);
        } catch (IOException e) {
            throw new XueChengPlusException("上传静态文件异常");
        }

        mqMessageService.completedStageOne(taskId);

    }
    //将课程信息缓存至redis
    public void saveCourseCache(MqMessage mqMessage,long courseId){
        log.debug("将课程信息缓存至redis,课程id:{}",courseId);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }
    //保存课程索引信息
    public void saveCourseIndex(MqMessage mqMessage,long courseId){
        log.debug("保存课程索引信息,课程id:{}",courseId);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}

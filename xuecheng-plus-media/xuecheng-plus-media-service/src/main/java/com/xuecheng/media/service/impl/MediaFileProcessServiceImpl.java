package com.xuecheng.media.service.impl;

import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaFileProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ClassName: MediaFileProcessServiceImpl
 * Package: com.xuecheng.media.service.impl
 * Description:
 *
 * @Author: XKing
 * @Create: 2023/5/14 - 15:49
 * @Version: 1.0
 */
@Service
@Slf4j
public class MediaFileProcessServiceImpl implements MediaFileProcessService {
    @Autowired
    MediaProcessMapper mediaProcessMapper;

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MediaProcessHistoryMapper mediaProcessHistoryMapper;
    /**
     * 获得待处理任务的列表
     * @param shardIndex 分片序号
     * @param shardTotal 分片总数
     * @param count 获取记录数
     * @return
     */
    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        List<MediaProcess> mediaProcesses = mediaProcessMapper.selectListByShardIndex(shardTotal, shardIndex, count);
        return mediaProcesses;
    }

    /**
     * 使用数据库实现的分布式锁保证只能一个线程能抢到任务
     * @param id 任务id
     * @return
     */
    @Override
    public boolean startTask(long id) {
        int result = mediaProcessMapper.startTask(id);
        return result<=0?false:true;
    }

    /**
     * 视频转换完成后将信息保存到数据库
     * @param taskId  任务id
     * @param status 任务状态
     * @param fileId  文件id
     * @param url url
     * @param errorMsg 错误信息
     */
    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        //查询要更新的任务
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if(mediaProcess==null){
            return;
        }
        //如果执行任务失败
        if(status.equals("3")){
            //设置mediaProcess中的status，fail_count
            mediaProcess.setStatus("3");
            mediaProcess.setFailCount(mediaProcess.getFailCount()+1);
            mediaProcess.setErrormsg(errorMsg);
            mediaProcessMapper.updateById(mediaProcess);
            return;
        }
        //执行任务成功
        //视频转码成功，更新mediaFiles中的url
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        mediaFiles.setUrl(url);
        mediaFilesMapper.updateById(mediaFiles);
        //更新mediaProcess状态
        mediaProcess.setStatus("2");
        mediaProcess.setFinishDate(LocalDateTime.now());
        mediaProcess.setUrl(url);
        //将mediaProcess表中的信息存入到mediaProcessHistory
        MediaProcessHistory processHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess,processHistory);
        mediaProcessHistoryMapper.insert(processHistory);
        //mediaProcess表中删除该条信息
        mediaProcessMapper.deleteById(taskId);
    }


}

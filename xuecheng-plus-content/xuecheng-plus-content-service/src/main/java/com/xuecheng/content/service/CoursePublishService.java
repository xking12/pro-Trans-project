package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;

import java.io.File;
import java.io.IOException;

/**
 * ClassName: CoursePublishService
 * Package: com.xuecheng.content.service
 * Description:
 *
 * @Author: XKing
 * @Create: 2023/5/16 - 20:15
 * @Version: 1.0
 */
public interface CoursePublishService {
    /**
     * 获取课程预览信息
     * @param courseId
     * @return
     */
    public CoursePreviewDto getCoursePreviewInfo(Long courseId);

    /**
     * 提交审核
     * @param companyId
     * @param courseId
     */
    public void commitAudit(Long companyId,Long courseId);

    /**
     * 课程发布接口
     * @param companyId 机构id
     * @param courseId  课程id
     */
    public void publish(Long companyId,Long courseId);

    /**
     * @description 课程静态化
     * @param courseId  课程id
     * @return File 静态化文件
     * @author Mr.M
     */
    public File generateCourseHtml(Long courseId);
    /**
     * @description 上传课程静态化页面
     * @param file  静态化文件
     * @return void
     * @author Mr.M
     */
    public void  uploadCourseHtml(Long courseId,File file) throws IOException;
}

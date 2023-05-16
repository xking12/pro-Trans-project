package com.xuecheng.content.service;

import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

/**
 * ClassName: CourseTeacherService
 * Package: com.xuecheng.content.service
 * Description:
 *
 * @Author: XKing
 * @Create: 2023/5/16 - 10:04
 * @Version: 1.0
 */

public interface CourseTeacherService {
    /**
     * 教师信息分页查询
     * @param courseId
     * @return
     */
    public List<CourseTeacher> list(long courseId);

    public CourseTeacher addTeacher(CourseTeacher courseTeacher,Long companyId);
}

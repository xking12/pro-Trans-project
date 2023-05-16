package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ClassName: CourseTeacherServiceImpl
 * Package: com.xuecheng.content.service.impl
 * Description:
 *
 * @Author: XKing
 * @Create: 2023/5/16 - 10:06
 * @Version: 1.0
 */
@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {
    @Autowired
    CourseTeacherMapper courseTeacherMapper;

    @Autowired
    CourseBaseMapper courseBaseMapper;


    /**
     * 查询教师列表
     * @param courseId
     * @return
     */
    @Override
    public List<CourseTeacher> list(long courseId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId,courseId);
        return courseTeacherMapper.selectList(queryWrapper);
    }

    /**
     * 新增教师
     * @param courseTeacher
     * @param companyId
     * @return
     */
    @Override
    public CourseTeacher addTeacher(CourseTeacher courseTeacher, Long companyId) {
        //本机构只能新增本机构的教师
        Long courseId = courseTeacher.getCourseId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(!courseBase.getCompanyId().equals(companyId)){
            throw new XueChengPlusException("本机构只能新增本机构的教师");
        }
        CourseTeacher courseTeacherNew = new CourseTeacher();
        BeanUtils.copyProperties(courseTeacher,courseTeacherNew);

        courseTeacherNew.setCreateDate(LocalDateTime.now());

        courseTeacherMapper.insert(courseTeacherNew);
        return courseTeacherNew;
    }
}

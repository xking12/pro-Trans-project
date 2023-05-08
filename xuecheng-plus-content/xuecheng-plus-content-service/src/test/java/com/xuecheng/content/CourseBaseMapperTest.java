package com.xuecheng.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * ClassName: CourseBaseMapperTest
 * Package: com.xuecheng.content
 * Description:
 *
 * @Author: XKing
 * @Create: 2023/5/7 - 18:21
 * @Version: 1.0
 */
@SpringBootTest
public class CourseBaseMapperTest {
    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Test
    public void testCourseBase(){
        CourseBase courseBase = courseBaseMapper.selectById(18);
        Assertions.assertNotNull(courseBase);

        //1.前端给过来的三个查询参数
        QueryCourseParamsDto paramsDto = new QueryCourseParamsDto();
        paramsDto.setCourseName("java");
        paramsDto.setAuditStatus("202004");
        paramsDto.setPublishStatus("203001");
        //1.1创建QueryWrapper
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        //1.2根据java模糊匹配
        queryWrapper.like(StringUtils.isNotBlank(paramsDto.getCourseName()),CourseBase::getName,paramsDto.getCourseName());
        //1.3课程审核状态
        queryWrapper.eq(StringUtils.isNotBlank(paramsDto.getAuditStatus()),CourseBase::getAuditStatus,paramsDto.getAuditStatus());
        //1.4课程发布状态
        queryWrapper.eq(StringUtils.isNotBlank(paramsDto.getPublishStatus()),CourseBase::getStatus,paramsDto.getPublishStatus());

        //2.前端传过来的分页参数
        PageParams pageParams = new PageParams();
        pageParams.setPageNo(1L);
        pageParams.setPageSize(2L);
        Page<CourseBase> courseBasePage = new Page<>(pageParams.getPageNo(),pageParams.getPageSize());
        //2.1分页查询
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(courseBasePage, queryWrapper);
        //3.将查询的结果返回给前端
        //3.1返回查询的record
        List<CourseBase> records = pageResult.getRecords();
        //3.2返回查询的总记录数
        long total = pageResult.getTotal();
        //3.3返回
        PageResult<CourseBase> courseBasePageResult=new PageResult<CourseBase>(records,total,pageParams.getPageNo(),pageParams.getPageSize());
        System.out.println(courseBasePageResult);

    }
}

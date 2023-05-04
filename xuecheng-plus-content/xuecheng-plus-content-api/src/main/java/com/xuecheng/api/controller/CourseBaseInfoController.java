package com.xuecheng.api.controller;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: CourseBaseInfoController
 * Package: com.xuecheng.api.controller
 * Description:课程信息编辑接口
 *
 * @Author: XKing
 * @Create: 2023/5/4 - 15:01
 * @Version: 1.0
 */
@RestController
public class CourseBaseInfoController {

    @RequestMapping ("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto){

        return null;
    }
}

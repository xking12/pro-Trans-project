package com.xuecheng.content.api;

import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ClassName: AddTeacherController
 * Package: com.xuecheng.content.api
 * Description:
 *
 * @Author: XKing
 * @Create: 2023/5/16 - 9:51
 * @Version: 1.0
 */
@Api(value = "教师设置接口接口",tags = "教师设置接口接口")
@RestController
public class AddTeacherController {
    @Autowired
    CourseTeacherService courseTeacherService;

    @ApiOperation("教师分页查询接口")
    @GetMapping("courseTeacher/list/{courseId}")
    public List<CourseTeacher> list(@PathVariable long courseId){
       return courseTeacherService.list(courseId);
    }

    @ApiOperation("添加教师信息")
    @PostMapping("courseTeacher")
    public CourseTeacher addTeacher(@RequestBody CourseTeacher courseTeacher){
        Long companyId = 1232141425L;
        return courseTeacherService.addTeacher(courseTeacher,companyId);

    }


}

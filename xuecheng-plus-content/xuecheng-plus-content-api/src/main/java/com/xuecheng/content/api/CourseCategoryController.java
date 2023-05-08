package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description 课程分类相关接口
 * @date 2023/2/12 11:54
 */
@RestController
public class CourseCategoryController {

    @Autowired
    CourseCategoryService courseCategoryService;

    //写接口的步骤
    //1.确定请求方式，使用restful规范
    //2.根据业务需求，写好sql并确定DTO，前后端输入输出的参数规定好，可以使用swagger文档
    //3.编写service
    @GetMapping("/course-category/tree-nodes")
    public List<CourseCategoryTreeDto> queryTreeNodes() {
        return courseCategoryService.queryTreeNodes("1");
    }

}

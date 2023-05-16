package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ClassName: TeachplanController
 * Package: com.xuecheng.content.api
 * Description:
 *
 * @Author: XKing
 * @Create: 2023/5/9 - 15:31
 * @Version: 1.0
 */
@Api(value = "课程计划编辑接口",tags = "课程计划编辑接口")
@RestController
public class TeachplanController {

    @Autowired
    TeachplanService teachplanService;

    @GetMapping("/teachplan/{courseId}/tree-nodes")
    @ApiOperation("查询课程计划树形结构")
   public List<TeachplanDto> getTreeNodes(@PathVariable long courseId){
        List<TeachplanDto> tree=teachplanService.findTeachplanTree(courseId);
        return tree;
    }

    @ApiOperation("课程计划创建或者修改")
    @PostMapping("/teachplan")
    public void saveTeachplan( @RequestBody SaveTeachplanDto teachplan){

            teachplanService.saveTeachplan(teachplan);
    }

    @ApiOperation(value = "课程计划和媒资信息绑定")
    @PostMapping("/teachplan/association/media")
    public void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto){
        teachplanService.associationMedia(bindTeachplanMediaDto);
    }

    //TODO:@ApiOperation(value = "接触课程计划和媒资信息绑定")


    //TODO:@ApiOperation(value = "删除小结和删除本章")

}

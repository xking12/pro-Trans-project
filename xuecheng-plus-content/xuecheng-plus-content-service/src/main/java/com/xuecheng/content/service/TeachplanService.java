package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;

import java.util.List;

/**
 * ClassName: TeachplanService
 * Package: com.xuecheng.content.service
 * Description:
 *
 * @Author: XKing
 * @Create: 2023/5/9 - 16:13
 * @Version: 1.0
 */
public interface TeachplanService {
    /**
     * 查询课程计划
     * @param courseId
     * @return
     */
    public List<TeachplanDto> findTeachplanTree(long courseId);

    /**
     * 新增或者修改课程计划
     * @param teachplanDto
     */
    public void saveTeachplan(SaveTeachplanDto teachplanDto);

}

package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.service.TeachplanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ClassName: TeachplanServieImpl
 * Package: com.xuecheng.content.service.impl
 * Description:
 *
 * @Author: XKing
 * @Create: 2023/5/9 - 16:15
 * @Version: 1.0
 */
@Service
public class TeachplanServieImpl implements TeachplanService {
    @Autowired
    TeachplanMapper teachplanMapper;

    /**
     * 根据课程id树形查询课程计划
     * @param courseId
     * @return
     */
    @Override
    public List<TeachplanDto> findTeachplanTree(long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }
}

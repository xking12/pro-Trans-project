package com.xuecheng.content.service;

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
    public List<TeachplanDto> findTeachplanTree(long courseId);
}

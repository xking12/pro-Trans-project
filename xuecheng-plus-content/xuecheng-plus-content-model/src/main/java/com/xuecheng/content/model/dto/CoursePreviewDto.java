package com.xuecheng.content.model.dto;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * ClassName: CoursePreviewDto
 * Package: com.xuecheng.content.model.dto
 * Description:
 *
 * @Author: XKing
 * @Create: 2023/5/16 - 20:12
 * @Version: 1.0
 */
@Data
@ToString
public class CoursePreviewDto {
    //课程基本信息,课程营销信息
    CourseBaseInfoDto courseBase;

    //课程计划信息
    List<TeachplanDto> teachplans;

    //师资信息暂时不加...
}

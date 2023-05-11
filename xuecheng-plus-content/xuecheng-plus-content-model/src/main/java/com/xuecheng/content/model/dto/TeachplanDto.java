package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * ClassName: TeachplanDto
 * Package: com.xuecheng.content.model.dto
 * Description:
 *
 * @Author: XKing
 * @Create: 2023/5/9 - 15:36
 * @Version: 1.0
 */
@ToString
@Data
public class TeachplanDto extends Teachplan {
    //课程计划相关的媒资信息
    TeachplanMedia teachplanMedia;

    //子节点
    List<TeachplanDto> teachPlanTreeNodes;
}

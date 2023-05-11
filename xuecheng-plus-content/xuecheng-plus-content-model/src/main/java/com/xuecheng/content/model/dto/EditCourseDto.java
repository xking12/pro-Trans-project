package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * ClassName: EditCourseDto
 * Package: com.xuecheng.content.model.dto
 * Description:
 *
 * @Author: XKing
 * @Create: 2023/5/9 - 14:07
 * @Version: 1.0
 */
@Data
@ApiModel(value="EditCourseDto", description="修改课程基本信息")
public class EditCourseDto extends AddCourseDto{
    @ApiModelProperty(value = "课程id", required = true)
    private Long id;
}

package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * ClassName: BindTeachplanMediaDto
 * Package: com.xuecheng.content.model.dto
 * Description:
 *绑定媒资前端传过来的Json
 * @Author: XKing
 * @Create: 2023/5/16 - 14:38
 * @Version: 1.0
 */
@Data
@ToString
@ApiModel(value="BindTeachplanMediaDto", description="教学计划-媒资绑定提交数据")
public class BindTeachplanMediaDto {
    @ApiModelProperty(value = "媒资文件id", required = true)
    private String mediaId;

    @ApiModelProperty(value = "媒资文件名称", required = true)
    private String fileName;

    @ApiModelProperty(value = "课程计划标识", required = true)
    private Long teachplanId;
}

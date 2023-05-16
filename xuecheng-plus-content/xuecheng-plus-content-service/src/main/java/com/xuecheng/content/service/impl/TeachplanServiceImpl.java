package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
public class TeachplanServiceImpl implements TeachplanService {
    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;

    /**
     * 根据课程id树形查询课程计划
     * @param courseId
     * @return
     */
    @Override
    public List<TeachplanDto> findTeachplanTree(long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }

    /**
     * 新增或者修改课程计划
     * @param teachplanDto
     */
    @Override
    public void saveTeachplan(SaveTeachplanDto teachplanDto) {
        //获取课程计划id
        Long id = teachplanDto.getId();
        //不为null的话则修改课程计划
        if(id!=null){
            Teachplan teachplan = teachplanMapper.selectById(id);
            BeanUtils.copyProperties(teachplanDto,teachplan);
            teachplanMapper.updateById(teachplan);
        }else{
            //新增课程计划
            int count=getTeachplanCount(teachplanDto.getCourseId(),teachplanDto.getParentid());
            Teachplan teachplanNew = new Teachplan();
            BeanUtils.copyProperties(teachplanDto,teachplanNew);
            teachplanNew.setOrderby(count+1);
            teachplanMapper.insert(teachplanNew);
        }
    }

    /**
     * 绑定媒资
     * @param bindTeachplanMediaDto
     * @return
     */
    @Override
    public TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {
        //获取教学计划id
        Long teachplanId = bindTeachplanMediaDto.getTeachplanId();
        //根据id查询得到teachplan
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if(teachplan==null){
            throw new XueChengPlusException("教学计划不存在");
        }
        Integer grade = teachplan.getGrade();
        if(grade!=2){
            throw new XueChengPlusException("只允许第二级教学计划绑定媒资文件");
        }
        //如果课程已经绑定了媒资，则删除它
        Long courseId = teachplan.getCourseId();
        LambdaQueryWrapper<TeachplanMedia> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeachplanMedia::getCourseId,courseId);
        teachplanMediaMapper.delete(queryWrapper);
        //再添加教学计划与媒资的绑定关系
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        teachplanMedia.setCourseId(courseId);
        teachplanMedia.setTeachplanId(teachplanId);
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        teachplanMedia.setMediaId(bindTeachplanMediaDto.getMediaId());
        teachplanMedia.setCreateDate(LocalDateTime.now());
        teachplanMediaMapper.insert(teachplanMedia);
        return teachplanMedia;
    }

    private int getTeachplanCount(Long courseId, Long parentid) {
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId,courseId);
        queryWrapper.eq(Teachplan::getParentid,parentid);
        Integer count = teachplanMapper.selectCount(queryWrapper);
        return count;
    }
}

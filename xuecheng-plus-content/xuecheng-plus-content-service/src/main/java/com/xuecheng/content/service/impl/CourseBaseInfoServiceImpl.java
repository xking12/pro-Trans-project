package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ClassName: CourseBaseInfoServiceImpl
 * Package: com.xuecheng.content.service.impl
 * Description:
 *
 * @Author: XKing
 * @Create: 2023/5/7 - 20:09
 * @Version: 1.0
 */
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {
    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    /**
     * @param pageParams 分页查询参数
     * @param courseParamsDto 查询条件
     * @return PageResult<CourseBase>
     */
    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto courseParamsDto) {
        //1.1创建QueryWrapper
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        //1.2根据java模糊匹配
        queryWrapper.like(StringUtils.isNotBlank(courseParamsDto.getCourseName()),CourseBase::getName,courseParamsDto.getCourseName());
        //1.3课程审核状态
        queryWrapper.eq(StringUtils.isNotBlank(courseParamsDto.getAuditStatus()),CourseBase::getAuditStatus,courseParamsDto.getAuditStatus());
        //1.4课程发布状态
        queryWrapper.eq(StringUtils.isNotBlank(courseParamsDto.getPublishStatus()),CourseBase::getStatus,courseParamsDto.getPublishStatus());

        //2.前端传过来的分页参数
        Page<CourseBase> courseBasePage = new Page<>(pageParams.getPageNo(),pageParams.getPageSize());
        //2.1分页查询
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(courseBasePage, queryWrapper);
        //3.将查询的结果返回给前端
        //3.1返回查询的record
        List<CourseBase> records = pageResult.getRecords();
        //3.2返回查询的总记录数
        long total = pageResult.getTotal();
        //3.3返回
        PageResult<CourseBase> courseBasePageResult=new PageResult<CourseBase>(records,total,pageParams.getPageNo(),pageParams.getPageSize());
        return  courseBasePageResult;
    }

    /**
     * @param companyId 机构id
     * @param dto 课程信息
     * @return
     */
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {
        //合法性校验
        if (StringUtils.isBlank(dto.getName())) {
            throw new XueChengPlusException("课程名称为空");
        }

        if (StringUtils.isBlank(dto.getMt())) {
            throw new XueChengPlusException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getSt())) {
            throw new XueChengPlusException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getGrade())) {
            throw new XueChengPlusException("课程等级为空");
        }

        if (StringUtils.isBlank(dto.getTeachmode())) {
            throw new XueChengPlusException("教育模式为空");
        }

        if (StringUtils.isBlank(dto.getUsers())) {
            throw new XueChengPlusException("适应人群为空");
        }

        if (StringUtils.isBlank(dto.getCharge())) {
            throw new XueChengPlusException("收费规则为空");
        }
        CourseBase newCourseBase = new CourseBase();
        BeanUtils.copyProperties(dto,newCourseBase);
        //机构id
        newCourseBase.setCompanyId(companyId);
        //设置审核状态
        newCourseBase.setAuditStatus("202002");
        //设置发布状态
        newCourseBase.setStatus("203001");
        //添加时间
        newCourseBase.setCreateDate(LocalDateTime.now());

        int insert = courseBaseMapper.insert(newCourseBase);
        if(insert<=0){
            throw new XueChengPlusException("新增课程基本信息失败");
        }
        //课程营销信息入表
        CourseMarket newCourseMarket = new CourseMarket();
        Long courseId = newCourseBase.getId();
        BeanUtils.copyProperties(dto,newCourseMarket);
        newCourseMarket.setId(courseId);
        int i=saveCourseMarket(newCourseMarket);
        if(i<=0){
            throw new XueChengPlusException("新增课程营销信息失败");
        }
        //查询课程的基本信息，从Course_Base表和Course_market和Course_category表中查询组合
        CourseBaseInfoDto courseBaseInfoDto=getCourseBaseInfo(courseId);
        return courseBaseInfoDto;
    }

    /**
     * 修改课程信息
     * @param companyId
     * @param editCourseDto
     * @return
     */
    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto) {
        Long courseId = editCourseDto.getId();
        //校验，防止伪造的editCourseDto恶意修改课程信息
        if(courseId==null){
            throw new XueChengPlusException("课程不存在");
        }
        //根据课程id查询到课程基本信息。
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        //校验本机构只能修改本机构的课程
        if(!courseBase.getCompanyId().equals(companyId)){
            throw new XueChengPlusException("本机构只能修改本机构的课程信息");
        }
        BeanUtils.copyProperties(editCourseDto,courseBase);
        courseBase.setChangeDate(LocalDateTime.now());
        courseBaseMapper.updateById(courseBase);

        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(editCourseDto,courseMarket);
        saveCourseMarket(courseMarket);

        CourseBaseInfoDto courseBaseInfo = this.getCourseBaseInfo(courseId);
        return courseBaseInfo;
    }

    /**
     * 得到课程的基本信息包括CourseBase和CourseMarket
     * @param courseId
     * @return
     */
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase == null){
            return null;
        }
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        if(courseMarket != null){
            BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);
        }

        //查询分类名称
        CourseCategory courseCategoryBySt = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setStName(courseCategoryBySt.getName());
        CourseCategory courseCategoryByMt = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setMtName(courseCategoryByMt.getName());

        return courseBaseInfoDto;

    }

    /**
     * 保存营销信息
     * @param newCourseMarket
     * @return
     */
    private int saveCourseMarket(CourseMarket newCourseMarket) {
        String charge = newCourseMarket.getCharge();
        if(charge==null){
            throw new XueChengPlusException("未选择收费规则");
        }
        if(charge.equals("201001")){
            Float price = newCourseMarket.getPrice();
            if(price<=0||price==null){
                throw new XueChengPlusException("课程为收费价格不能为空且必须大于0");
            }
        }
        CourseMarket courseMarket = courseMarketMapper.selectById(newCourseMarket.getId());
        if(courseMarket==null){
            return courseMarketMapper.insert(newCourseMarket);
        }else {
            BeanUtils.copyProperties(newCourseMarket,courseMarket);
            courseMarket.setId(newCourseMarket.getId());
            return courseMarketMapper.updateById(courseMarket);
        }
    }
}

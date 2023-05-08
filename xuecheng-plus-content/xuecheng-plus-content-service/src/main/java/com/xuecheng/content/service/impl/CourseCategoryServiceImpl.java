package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {
    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {

        //1.利用递归查询树形表
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes(id);
        //2.将查询的结果转换成map，key=id，value=courseCategoryTreeDtos（过滤掉根节点）
        Map<String,CourseCategoryTreeDto> tempMap =courseCategoryTreeDtos.stream().
                filter(item->!id.equals(item.getId())).
                collect(Collectors.toMap(key->key.getId(), value->value,(key1, key2)->key2));
        //3.创建最终返回的dto
        List<CourseCategoryTreeDto> categoryTreeDtos = new ArrayList<>();
        //3.1收集最终想要的dto
        courseCategoryTreeDtos.stream().filter(item->!id.equals(item.getId())).forEach(item->{
            //3.2如果parentId==id就将其放进返回的dto(二级节点放入dto)
            if(item.getParentid().equals(id)){
                categoryTreeDtos.add(item);
            }
            //3.3找到当前节点的父节点
            CourseCategoryTreeDto categoryTreeDtoParent = tempMap.get(item.getParentid());
            //3.4如果不为空则代表当前节点为二级节点以后的节点
            if(categoryTreeDtoParent!=null){
                //3.5如果当前节点的父节点为空则创建子节点
                if(categoryTreeDtoParent.getChildrenTreeNodes()==null){
                    categoryTreeDtoParent.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                }
                //3.6将当前节点放进父节点的childrenTreeNodes属性中去
                categoryTreeDtoParent.getChildrenTreeNodes().add(item);
            }
        });

        return categoryTreeDtos;
    }
}
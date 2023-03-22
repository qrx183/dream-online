package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;
    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes(id);
        List<CourseCategoryTreeDto> res = new ArrayList<>();
        // map id->treeNode
        Map<String,CourseCategoryTreeDto> mapTreeNodes = courseCategoryTreeDtos.stream().
                filter(item->!id.equals(item.getId())).
                collect(Collectors.toMap(key->key.getId(), value->value,(key1, key2)->key2));

        courseCategoryTreeDtos.stream().filter(item->!id.equals(item.getId())).forEach(item ->{
            if (item.getParentid().equals(id)){
                res.add(item);
            }
            CourseCategoryTreeDto courseCategoryTreeDto = mapTreeNodes.get(item.getParentid());
            if (courseCategoryTreeDto != null) {
                if (courseCategoryTreeDto.getChildrenTreeNodes() == null) {
                    courseCategoryTreeDto.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                }
                courseCategoryTreeDto.getChildrenTreeNodes().add(item);
            }


        });

        return res;
    }
    @Test
    public void show() {
        System.out.println(queryTreeNodes("1"));
    }
}

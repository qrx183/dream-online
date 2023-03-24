package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import lombok.Data;

import java.util.List;

@Data
public class TeachPlanDto extends Teachplan {
    private List<TeachPlanDto> teachPlanTreeNodes;
    private TeachplanMedia teachplanMedia;
}
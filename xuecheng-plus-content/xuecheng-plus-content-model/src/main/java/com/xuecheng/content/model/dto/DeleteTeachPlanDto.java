package com.xuecheng.content.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class DeleteTeachPlanDto {
    private long id;
    private long parentid;
    private List<DeleteTeachPlanDto> deleteTeachPlanDtoList;
}

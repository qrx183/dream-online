package com.xuecheng.content.model.dto;

import lombok.Data;

@Data
public class DeleteCourseResponseDto {
    private String errCode;
    private String errMessage;

    public DeleteCourseResponseDto(String errCode, String errMessage) {
        this.errCode = errCode;
        this.errMessage = errMessage;
    }
}

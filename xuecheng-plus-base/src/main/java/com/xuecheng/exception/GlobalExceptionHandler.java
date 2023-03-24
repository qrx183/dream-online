package com.xuecheng.exception;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理的异常
     * @param e
     * @return
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(XcPlusException.class)
    public RestErrorResponse customException(XcPlusException e) {

        log.error("[系统异常] {}",e.getMessage(),e);
        return new RestErrorResponse(e.getMessage());
    }


    /**
     * 未处理的异常
     * @param e
     * @return
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public RestErrorResponse exception(Exception e) {
        log.error("[系统异常] {}",e.getMessage(),e);
        return new RestErrorResponse(CommonError.UNKNOWN_ERROR.getErrMessage());
    }

    /**
     * 未处理的异常
     * @param e
     * @return
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public RestErrorResponse methodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("[系统异常] {}",e.getMessage(),e);
        BindingResult bindingResult = e.getBindingResult();
        List<String> errors = new ArrayList<>();
        bindingResult.getFieldErrors().stream().forEach(item -> {
            errors.add(item.getDefaultMessage());
        });
        return new RestErrorResponse(StringUtils.join(errors,','));
    }

}

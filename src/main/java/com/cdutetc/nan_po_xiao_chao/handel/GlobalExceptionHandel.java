package com.cdutetc.nan_po_xiao_chao.handel;

import com.cdutetc.nan_po_xiao_chao.domain.Result;
import com.cdutetc.nan_po_xiao_chao.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandel {

    @ExceptionHandler({BusinessException.class})
    public Result businessExceptionHandel(BusinessException businessException){
        log.warn("业务异常 {}",businessException.getMessage());
        return Result.error(businessException.getCode(), businessException.getMessage());
    }

    // 第14课：超限发生在 multipart 解析阶段，进不了 Controller，只能在处理台接
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result maxUploadSizeHandel(MaxUploadSizeExceededException e){
        log.warn("上传文件超过大小限制: {}", e.getMessage());
        return Result.error(40406, "上传文件超过大小限制");
    }

    @ExceptionHandler({IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result illegalArgumentExceptionHandel(IllegalArgumentException e){
        log.warn("参数异常 {}",e.getMessage());
        return Result.error(400, "参数错误: " + e.getMessage());
    }

    @ExceptionHandler({Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result exceptionHandel(Exception e){
        log.error("未知异常", e);
        return Result.error(500, "服务器内部错误");
    }
}

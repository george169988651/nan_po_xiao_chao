package com.cdutetc.nan_po_xiao_chao.exception;

import lombok.Data;
import org.springframework.stereotype.Service;

@Data
public class BusinessException extends RuntimeException{

    private int code;

    private String message;

    public BusinessException(int code,String message) {
        super(message);
        this.code=code;
        this.message=message;
    }
}

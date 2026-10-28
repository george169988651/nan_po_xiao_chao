package com.cdutetc.nan_po_xiao_chao.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Result<T> {

    private int code;

    private String message;

    private T data;

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }


    public static <T> Result<T> success(T data){
        return new Result<>(200,"success",data);
    }

    public static <T> Result<T> error(String message){
        return new Result<>(404, message, null);
    }

    public static <T> Result<T> error(int code, String message){
        return new Result<>(code, message, null);
    }

}

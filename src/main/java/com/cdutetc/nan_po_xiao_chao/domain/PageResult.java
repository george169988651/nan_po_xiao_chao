package com.cdutetc.nan_po_xiao_chao.domain;

import lombok.Data;

/**
 * 分页统一返回结构。
 * 必须带 getter(@Data),否则 Jackson 序列化成 {} —— 字段全 private 且无 getter 时
 * Jackson 找不到任何可输出属性,前端只会收到空对象。
 */
@Data
public class PageResult<T> {

    private int code;

    private String message;

    private T data;

    private Long total;

    private PageResult(int code, String message, T data,Long total) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.total=total;
    }


    public static <T> PageResult<T> success(T data,Long total){
        return new PageResult<>(200,"success",data,total);
    }

    public static <T> PageResult<T> error(String message){
        return new PageResult<>(404,"error",null,null);
    }

}

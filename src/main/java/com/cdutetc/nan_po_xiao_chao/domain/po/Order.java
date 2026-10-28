package com.cdutetc.nan_po_xiao_chao.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单表实体类
 * autoResultMap = true 让查询结果也走 Jackson3TypeHandler,
 * 否则只有插入时序列化,select 回读的 order_list 是 null
 */
@Data
@TableName(value = "dish_order", autoResultMap = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户姓名
     */
    private String userName;

    /**
     * 对应菜单表的id,列类型是 varchar(JSON 文本),读写都由 TypeHandler 负责
     */
    @TableField(value = "order_list", typeHandler = Jackson3TypeHandler.class)
    private List<Long> orderList;

    /**
     * 其他消息
     */
    private String message;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

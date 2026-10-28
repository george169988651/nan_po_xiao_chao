package com.cdutetc.nan_po_xiao_chao.domain.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 小票明细行 VO（出参：后端 → 前端）。
 * 第10课新增：字段按前端需要从订单明细实体裁剪而来——
 * 实体上的 id、order 外键等账本细节，前端不需要就不给看。
 */
@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class OrderItemVO {

    /**
     * 菜单名
     */
    private String dishName;

    /**
     * 数量
     */
    private Integer count;

    /**
     * 单价
     */
    private BigDecimal unitPrice;

    /**
     * 小计 = 单价 × 数量
     */
    private BigDecimal subTotal;

    /**
     * 兼容旧调用方：历史字段名，值同 subTotal
     */
    private BigDecimal price;

    /**
     * 兼容旧调用方的三参工厂：price 按"小计"理解，同时补齐 unitPrice。
     */
    public static OrderItemVO of(String dishName, Integer count, BigDecimal price) {
        OrderItemVO vo = new OrderItemVO();
        vo.setDishName(dishName);
        vo.setCount(count);
        vo.setPrice(price);
        vo.setSubTotal(price);
        if (count != null && count > 0 && price != null) {
            vo.setUnitPrice(price.divide(BigDecimal.valueOf(count), 2, java.math.RoundingMode.HALF_UP));
        }
        return vo;
    }

}

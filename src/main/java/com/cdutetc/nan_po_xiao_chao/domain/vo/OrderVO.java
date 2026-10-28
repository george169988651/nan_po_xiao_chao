package com.cdutetc.nan_po_xiao_chao.domain.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单小票 VO（出参：后端 → 前端）。
 * 第10课新增：账本（entity.Order）不能直接发给前端——
 * 双向关联会被 JSON 无限递归、懒加载随时炸、实体字段（如以后的密码）全部裸奔；
 * VO 按前端需要裁剪字段，实体永不出 Service 之外。
 */
@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class OrderVO {

    private Long orderId;
    private Long userId;
    private String username;   // 下单人（第16课：登录后订单记名）
    private List<OrderItemVO> itemList;
    private BigDecimal totalMoney;
    private String msg;
    private BigDecimal totalPrice;
    private LocalDateTime createTime;

}

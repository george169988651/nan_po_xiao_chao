package com.cdutetc.nan_po_xiao_chao.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.cdutetc.nan_po_xiao_chao.domain.PageResult;
import com.cdutetc.nan_po_xiao_chao.domain.dto.PageDTO;
import com.cdutetc.nan_po_xiao_chao.domain.po.Order;
import com.cdutetc.nan_po_xiao_chao.domain.vo.OrderPageVO;
import com.cdutetc.nan_po_xiao_chao.domain.vo.OrderVO;

import java.util.List;

public interface OrderService extends IService<Order> {
    PageResult queryOrderPage(PageDTO pageDTO);

    void saveOrder(List<Order> orders);

    /** 第12课：分页翻账本，page 从 0 开始，按下单时间倒序 */
    OrderPageVO getOrderPage(int page, int size);

    /** 第12课：订单详情，查不到抛 40402 */
    OrderVO getOrder(Long id);

    /** 第12课：按菜名查"谁点过这道菜"，JPQL 联表的 MyBatis 等价实现 */
    List<OrderVO> searchByDish(String dishName);
}

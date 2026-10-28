package com.cdutetc.nan_po_xiao_chao.tool;

import cn.hutool.core.lang.Pair;
import com.cdutetc.nan_po_xiao_chao.context.UserContext;
import com.cdutetc.nan_po_xiao_chao.domain.Result;
import com.cdutetc.nan_po_xiao_chao.domain.dto.OrderDTO;
import com.cdutetc.nan_po_xiao_chao.domain.vo.OrderVO;
import com.cdutetc.nan_po_xiao_chao.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 第20课：MCP 下单工具。
 * MCP 调用不经过 HTTP 拦截器，没有登录态，这里用 "ai-guest" 系统身份登记，
 * 用完必须在 finally 里清理 ThreadLocal，否则线程池复用会串号。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderTool {

    private final MenuService menuService;

    @Tool(description = "为顾客下一笔订单：传入菜名和份数，返回订单号与金额")
    public String placeOrder(String dishName, int count) {
        UserContext.setCurrentUser(Pair.of("0", "ai-guest"));
        try {
            OrderDTO dto = new OrderDTO();
            OrderDTO.DishCount dish = new OrderDTO.DishCount();
            dish.setDishName(dishName);
            dish.setCount(count);
            dto.setDishList(List.of(dish));

            Result result = menuService.order(dto);
            OrderVO vo = (OrderVO) result.getData();
            return "下单成功：订单号 " + vo.getOrderId() + "，总额 " + vo.getTotalMoney() + " 元";
        } catch (Exception e) {
            log.error("MCP 下单失败: {}", e.getMessage());
            return "下单失败：" + e.getMessage();
        } finally {
            UserContext.removeCurrentUser();
        }
    }
}

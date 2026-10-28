package com.cdutetc.nan_po_xiao_chao.function;

import com.cdutetc.nan_po_xiao_chao.domain.po.Menu;
import com.cdutetc.nan_po_xiao_chao.domain.po.Order;
import com.cdutetc.nan_po_xiao_chao.exception.BusinessException;
import com.cdutetc.nan_po_xiao_chao.service.MenuService;
import com.cdutetc.nan_po_xiao_chao.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
@Slf4j
public class FunctionTool {

    private final OrderService orderService;

    private final MenuService menuService;


    @Bean("orderFunctionToolCallback")
    @Description("AI下单工具")
    public FunctionToolCallback orderFunctionToolCallback(){
        return FunctionToolCallback.builder("orderFunctionToolCallback", new BiFunction<Order, ToolContext, Order>() {
            @Override
            public Order apply(Order order, ToolContext toolContext) {
                Map<String, Object> context = toolContext.getContext();
                if(order==null){
                    log.info("请求为空");
                    throw new BusinessException(400, "请求为空");
                }
                try {
                    orderService.save(order);
                    return order;
                } catch (Exception e) {
                    log.error("下单失败", e);
                    throw new RuntimeException(e);
                }
            }
        })
                .description("AI根据用户的需要自主完成下单工具")
                .inputType(Order.class)
                .build();

    }

    @Bean("menuFunctionToolCallback")
    @Description("AI查询菜单工具")
    public FunctionToolCallback menuFunctionToolCallback(){
        return FunctionToolCallback.builder("menuFunctionToolCallback", new Supplier<List<Menu>>() {
                    @Override
                    public List<Menu> get() {
                        return menuService.list();
                    }
                })
                .description("AI查询菜工具")
                .build();
    }

}

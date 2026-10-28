package com.cdutetc.nan_po_xiao_chao.tool;

import com.cdutetc.nan_po_xiao_chao.domain.po.Order;
import com.cdutetc.nan_po_xiao_chao.service.MenuService;
import com.cdutetc.nan_po_xiao_chao.service.OrderService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.List;
import java.util.function.BiFunction;


@Configuration
@Slf4j
@RequiredArgsConstructor
public class BulkOrderTool {

    @Autowired
    @Lazy
    private MenuService menuService;

    @Autowired
    @Lazy
    private OrderService orderService;


    @Bean("queryOrderTool")
    public FunctionToolCallback queryOrder(){

        return FunctionToolCallback.builder("queryOrderTool", new BiFunction<Void, ToolContext, Object>() {
            @Override
            public Object apply(Void v, ToolContext toolContext) {
                log.info("ai查询菜单表工具完成");
                return menuService.queryMenu().getData();
            }
        }).inputType(Void.class).description("ai查询菜单表工具").build();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderRequest{
        @ToolParam(description = "生成的订单列表,用户姓名使用真实用户名",required = true)
        public List<Order> list;
    }

}
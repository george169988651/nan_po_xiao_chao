package com.cdutetc.nan_po_xiao_chao.tool;

import com.cdutetc.nan_po_xiao_chao.domain.po.Menu;
import com.cdutetc.nan_po_xiao_chao.domain.po.Order;
import com.cdutetc.nan_po_xiao_chao.service.MenuService;
import com.cdutetc.nan_po_xiao_chao.service.OrderService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiTool {

    private final MenuService menuService;
    private final OrderService orderService;

    @Tool(name = "getMenu",
            description = """
              查询餐厅的全部菜单。
              返回内容为每道菜的「ID、菜名、价格」，格式如：
              1. 宫保鸡丁 - ￥28.00
              2. 麻婆豆腐 - ￥18.00
              使用场景：
              - 用户询问有什么菜、菜单、价格时调用。
              - 用户要下单但你不确定菜品ID时，必须先调用此工具获取菜品ID。
              注意：
              - 返回的「序号」就是菜品ID，下单时必须使用这个ID，禁止编造。
              """)
    public String getMenu() {
        log.info(">>> getMenu 被调用");
        try {
            List<Menu> menus = (List<Menu>) menuService.queryMenu().getData();
            if (menus == null || menus.isEmpty()) {
                return "暂无菜单";
            }
            return menus.stream()
                    .map(m -> m.getId() + ". " + m.getName() + " - ￥" + m.getPrice())
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            log.error("查询菜单失败", e);
            return "查询菜单失败：" + e.getMessage();
        }
    }

    @Tool(name = "insertOrder",
            description = """
              帮助用户完成下单操作。
              
              调用前置条件：
              - 必须先用 getMenu 查询菜单，获取菜品ID。
              - 禁止编造菜品ID，禁止使用不存在的菜品。
              
              参数说明：
              - orderList：菜品ID列表，来自 getMenu 返回的序号。
                例如 getMenu 返回「3. 鱼香肉丝」，则 orderList 填 [3]。
                两道菜填 [3, 1]。
              
              不需要填写的字段：
              - 用户ID、用户名、下单时间，由系统自动填充，不要生成。
              
              使用场景：
              - 用户明确表示要点某道菜时调用。
              """)
    public String insertOrder(
            @ToolParam(description = "用户下单信息，只需填菜品ID列表", required = true)
            OrderInfo order
            , ToolContext toolContext
            ) {
        log.info(">>> insertOrder 被调用，参数：{}", order);
        if (order == null || order.getOrderList() == null || order.getOrderList().isEmpty()) {
            return "下单失败：没有选择菜品";
        }
        try {
            orderService.saveOrder(List.of(
                    Order.builder()
                            .userId(Long.valueOf(toolContext.getContext().get("userId").toString()))
                            .userName(toolContext.getContext().get("userName").toString())
                            .message(order.getMessage())
                            .createTime(LocalDateTime.now())
                            .updateTime(LocalDateTime.now())
                            .orderList(order.getOrderList())
                            .build()
            ));
            return "下单成功，共 " + order.getOrderList().size() + " 道菜";
        } catch (Exception e) {
            log.error("下单失败", e);
            return "下单失败：" + e.getMessage();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderInfo {
        @ToolParam(description = "菜品ID列表，来自 getMenu 返回的序号，如 [3] 或 [1,3,5]", required = true)
        private List<Long> orderList;
        @ToolParam(description = "口味备注", required = false)
        private String message;
    }
}
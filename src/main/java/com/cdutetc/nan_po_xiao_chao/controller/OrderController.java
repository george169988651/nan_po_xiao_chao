package com.cdutetc.nan_po_xiao_chao.controller;

import com.cdutetc.nan_po_xiao_chao.domain.PageResult;
import com.cdutetc.nan_po_xiao_chao.domain.Result;
import com.cdutetc.nan_po_xiao_chao.domain.dto.PageDTO;
import com.cdutetc.nan_po_xiao_chao.domain.vo.OrderPageVO;
import com.cdutetc.nan_po_xiao_chao.domain.vo.OrderVO;
import com.cdutetc.nan_po_xiao_chao.service.AiService;
import com.cdutetc.nan_po_xiao_chao.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "订单接口")
public class OrderController {

    private final OrderService orderService;

    private final AiService aiService;

    //分页查询订单
    @PostMapping("/page")
    @Operation(summary = "分页查询订单接口")
    public PageResult queryOrderPage(@Validated  @RequestBody PageDTO pageDTO){
        return orderService.queryOrderPage(pageDTO);
    }

    // ---- 第12课：翻账本 ----

    @GetMapping("/list")
    @Operation(summary = "分页查订单,page 从 0 开始,按下单时间倒序")
    public Result<OrderPageVO> listOrders(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "5") int size) {
        return Result.success(orderService.getOrderPage(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "订单详情")
    public Result<OrderVO> getOrder(@PathVariable Long id) {
        return Result.success(orderService.getOrder(id));
    }

    @GetMapping("/search")
    @Operation(summary = "按菜名查订单:谁点过这道菜")
    public Result<List<OrderVO>> searchByDish(@RequestParam String dish) {
        return Result.success(orderService.searchByDish(dish));
    }

    @PostMapping("/ai")
    @Operation(summary = "AI 自然语言下单:先说句话,解析后走人工下单同一套逻辑")
    public Result<OrderVO> orderByAi(@RequestBody Map<String, String> body) {
        return Result.success(aiService.orderByAi(body.get("message")));
    }

}

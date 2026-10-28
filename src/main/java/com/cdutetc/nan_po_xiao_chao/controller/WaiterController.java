package com.cdutetc.nan_po_xiao_chao.controller;

import com.cdutetc.nan_po_xiao_chao.domain.Result;
import com.cdutetc.nan_po_xiao_chao.domain.dto.OrderDTO;
import com.cdutetc.nan_po_xiao_chao.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/waiter")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "服务员")
public class WaiterController {

    private final MenuService menuService;

    //查询菜单
    @GetMapping("/menu")
    @Operation(summary = "查询菜单接口")
    public Result queryMenu(){
        return menuService.queryMenu();
    }

    //点单
    @PostMapping("/order")
    @Operation(summary = "下单接口")
    public Result order(@RequestBody OrderDTO orderDTO){
        return menuService.order(orderDTO);
    }

}

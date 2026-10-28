package com.cdutetc.nan_po_xiao_chao.controller;

import com.cdutetc.nan_po_xiao_chao.domain.Result;
import com.cdutetc.nan_po_xiao_chao.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "开店接口")
public class HelloController {

//    private final MenuService menuService;

    @GetMapping
    @Operation(summary = "验证接口：店开了没有")
    public Result hello(){
        log.info("验证接口接受到异常");
        return Result.success("欢迎来到南坡小炒");
    }
}

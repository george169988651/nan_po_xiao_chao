package com.cdutetc.nan_po_xiao_chao.controller;

import com.cdutetc.nan_po_xiao_chao.domain.Result;
import com.cdutetc.nan_po_xiao_chao.domain.dto.LoginDTO;
import com.cdutetc.nan_po_xiao_chao.domain.dto.RefreshDTO;
import com.cdutetc.nan_po_xiao_chao.domain.dto.RegisterDTO;
import com.cdutetc.nan_po_xiao_chao.domain.vo.LoginVO;
import com.cdutetc.nan_po_xiao_chao.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "用户认证接口")
public class LoginController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "注册接口")
    public Result register(@RequestBody RegisterDTO registerDTO) {
        userService.register(registerDTO);
        return Result.success(null);
    }

    @PostMapping("/login")
    @Operation(summary = "登录接口,返回双令牌")
    public Result login(@RequestBody LoginDTO loginDTO) {
        return Result.success(userService.login(loginDTO));
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新接口,用 refreshToken 换发新令牌")
    public Result refresh(@RequestBody RefreshDTO refreshDTO) {
        return Result.success(userService.refresh(refreshDTO));
    }
}
